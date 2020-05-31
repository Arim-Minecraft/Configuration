/* 
 * ArimLib
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ArimLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ArimLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ArimLib. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.omega.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.api.concurrent.AsyncStartingModule;
import space.arim.api.config.SimpleConfig;

import space.arim.omega.util.BytesUtil;

import space.arim.uuidvault.api.UUIDUtil;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;

public class Omega implements AsyncStartingModule {

	private final File dataFolder;
	private final ConcurrentHashMap<UUID, OmegaPlayer> players = new ConcurrentHashMap<>();
	
	final Logger logger;
	final OmegaSql sql;
	final OmegaDataLoader loader;
	@Getter
	private final OmegaSwiftConomy economy;
	
	private List<Rank> ranks;
	
	private CompletableFuture<?> future;
	
	/**
	 * Milliseconds within a minute, used to divide into System.currentTimeMillis
	 * to get the current unix time in minutes.
	 * 
	 */
	private static final int MILLIS_IN_MINUTE = 60000;
	
	public Omega(File dataFolder, Logger logger) {
		this.dataFolder = dataFolder;
		this.logger = logger;

		SimpleConfig sqlCfg = new SimpleConfig(dataFolder, "sql.yml", "version") {};
		sqlCfg.reload();
		sql = new OmegaSql(sqlCfg.getString("host"), sqlCfg.getInt("port"), sqlCfg.getString("database"),
				sqlCfg.getString("url"), sqlCfg.getString("username"), sqlCfg.getString("password"),
				sqlCfg.getInt("connections"));

		loader = new OmegaDataLoader(this);

		economy = new OmegaSwiftConomy(this);
	}

	public void registerWith(JavaPlugin plugin) {
		Server server = plugin.getServer();
		server.getPluginManager().registerEvents(loader, plugin);
		server.getServicesManager().register(Economy.class, economy, plugin, ServicePriority.High);
	}

	@Override
	public void startLoad() {
		future = CompletableFuture.allOf(sql.makeTablesIfNotExist(), CompletableFuture.runAsync(() -> {
			List<Rank> ranks = new ArrayList<>();
			try (Scanner scanner = new Scanner(new File(dataFolder, "ranks.txt"), "UTF-8")) {
				ArrayList<String> lines = new ArrayList<>(4);
				while (scanner.hasNextLine()) {
					lines.add(scanner.nextLine());
					if (lines.size() == 4) {
						ranks.add(new Rank(lines.get(0).toLowerCase(), lines.get(1), lines.get(2), lines.get(3)));
						lines.clear();
					}
				}
			} catch (FileNotFoundException ex) {
				logger.warn("No ranks.txt found", ex);
			}
			this.ranks = ranks;
		}));
	}
	
	@Override
	public void finishLoad() {
		future.join();
		future = null;
	}
	
	/**
	 * Run something for each loaded player
	 * 
	 * @param action what to do
	 */
	void forEach(BiConsumer<UUID, OmegaPlayer> action) {
		players.forEach(action);
	}
	
	/**
	 * Run something for each transient player info. <br>
	 * Will not do anything for OmegaPlayers which are saving
	 * but not online, because such do not have transient info.
	 * 
	 * @param action what to do
	 */
	void forEachTransient(Consumer<TransientPlayer> action) {
		players.forEach((uuid, player) -> {
			TransientPlayer transientPlayer = player.getTransientInfo();
			if (transientPlayer != null) {
				action.accept(transientPlayer);
			}
		});
	}
	
	/**
	 * Gets a player by UUID. <code>null</code> if not online or loaded.
	 * 
	 * @param uuid the player uuid
	 * @return the omega player or <code>null</code> if not loaded
	 */
	OmegaPlayer getPlayer(UUID uuid) {
		return players.get(uuid);
	}
	
	/**
	 * Gets an online OmegaPlyer
	 * 
	 * @param player the player
	 * @return the omega player
	 */
	public OmegaPlayer getPlayer(Player player) {
		return getPlayer(player.getUniqueId());
	}
	
	/**
	 * Gets a transient player by UUID. <code>null</code> if not online. <br>
	 * <b>Transient player info is not thread safe</b>
	 * 
	 * @param uuid the player uuid
	 * @return the transient player or <code>null</code> if offline
	 */
	TransientPlayer getTransientPlayer(UUID uuid) {
		OmegaPlayer player = getPlayer(uuid);
		return (player != null) ? player.getTransientInfo() : null;
	}
	
	/**
	 * Gets transient player information. <br>
	 * <b>Most transient player info is NOT thread safe</b>
	 * 
	 * @param player the player
	 * @return the transient player
	 */
	public TransientPlayer getTransientPlayer(Player player) {
		return getTransientPlayer(player.getUniqueId());
	}
	
	/**
	 * Checks whether a player with the given uuid is online safely,
	 * in a thread safe manner.
	 * 
	 * @param uuid the player uuid
	 * @return true if the player with the uuid is online, false otherwise
	 */
	public boolean isOnline(UUID uuid) {
		return getTransientPlayer(uuid) != null;
	}
	
	/**
	 * Reloads the player's rank based on permissions. <br>
	 * <b>Must be called SYNC </b> <br>
	 * Will also update any displays in chat or tablist.
	 * 
	 * @param player the player
	 */
	public void refreshRank(Player player) {
		OmegaPlayer p = getPlayer(player);
		p.setRank(findRank(player));
		p.applyDisplayNames(player);
	}
	
	/**
	 * Finishes the partial player, adds the omega player to the loaded players map,
	 * and returns the completed OmegaPlayer
	 * 
	 * @param player the player
	 * @param partial the partial player
	 * @return the omega player
	 */
	OmegaPlayer add(Player player, PartialPlayer partial) {
		partial.setRank(findRank(player));
		OmegaPlayer p = partial.finish();
		players.put(player.getUniqueId(), p);
		return p;
	}
	
	/**
	 * Gets the current unix time in minutes. <br>
	 * Times in seconds cannot be stored beyond 2038 using an int
	 * https://en.wikipedia.org/wiki/Year_2038_problem. <br>
	 * However, we can use an int for efficiency purposes when we only need minutes precision.
	 * 
	 * @return the time in minutes
	 */
	static int currentTimeMinutes() {
		return (int) System.currentTimeMillis() / MILLIS_IN_MINUTE;
	}
	
	/**
	 * Gets an online player by name
	 * 
	 * @param name the player name
	 * @return the player or <code>null</code> if not found
	 */
	public OmegaPlayer getPlayerByName(String name) {
		for (OmegaPlayer player : players.values()) {
			if (player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}
	
	/**
	 * Finds a player by name, returns UUID and name. <br>
	 * The input name is case insensitive, while the output name is correctly capitalised.
	 * 
	 * @param name the player name
	 * @return a completable future of player info whose result is <code>null</code> if not found
	 */
	public CompletableFuture<PlayerInfo> findPlayerInfo(String name) {
		OmegaPlayer player = getPlayerByName(name);
		if (player != null) {
			return CompletableFuture.completedFuture(new PlayerInfo(player.getUuid(), player.getName()));
		}
		// MySQL is not case sensitive https://stackoverflow.com/a/61484046/6548501
		return sql.selectAsync(() -> {
			try (ResultSet rs = sql.select("SELECT `uuid`, `name` FROM `omega_identify` WHERE `name` = ? ORDER BY `updated` DESC LIMIT 1", name)) {
				
				if (rs.next()) {
					return new PlayerInfo(UUIDUtil.uuidFromByteArray(rs.getBytes("uuid")), rs.getString("name"));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return null;
		});
	}
	
	/**
	 * Finds a player by name, returns UUID, name, and IP addresses. <br>
	 * The input name is case insensitive, while the output name is correctly capitalised.
	 * 
	 * @param name the player name
	 * @return a completable future of player info whose result is <code>null</code> if not found
	 */
	public CompletableFuture<PlayerInfoWithAddresses> findPlayerInfoWithIPs(String name) {
		return sql.selectAsync(() -> {
			/*
			 * SQL Objective
			 * Select all IP addresses of the UUID corresponding to the most recent occurence of the name.
			 */
			try (ResultSet rs = sql.select("SELECT DISTINCT `uuid`, `address` "
					+ "FROM `omega_identify` "
					+ "WHERE `uuid` IN "
					+ "(SELECT `uuid` FROM `omega_identify` WHERE `name` = ? ORDER BY `updated` DESC LIMIT 1)")) {

				List<Byte[]> ips = null;
				while (rs.next()) {
					if (ips == null) {
						ips = new ArrayList<>();
					}
					ips.add(BytesUtil.boxAll(rs.getBytes("address")));
				}
				if (ips == null) {
					// Empty result set
					return null;
				}
				return new PlayerInfoWithAddresses(UUIDUtil.uuidFromByteArray(rs.getBytes("uuid")),
						rs.getString("name"), BytesUtil.unboxAll2D(ips.toArray(new Byte[][] {})));

			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return null;
		});
	}
	
	private CompletableFuture<Map<Byte[], Set<PlayerInfo>>> conductAltcheck(UUID uuid) {
		return sql.selectAsync(() -> {
			Map<Byte[], Set<PlayerInfo>> result = new HashMap<>();
			byte[] rawUUID = UUIDUtil.byteArrayFromUUID(uuid);
			/*
			 * SQL Objective
			 * Find other player UUIDs, with latest names, whose addresses match that of the target UUID
			 */
			try (ResultSet rs = sql.select("SELECT `identify`.`uuid` AS `final_uuid`, `identify`.`name` AS `final_name`, `identify`.`address` as `final_address` "
					+ "FROM `omega_identify` `identify` "
					+ "LEFT JOIN `omega_identify` `identifyAlso` "
					+ "ON (`identify`.`uuid` = `identifyAlso`.`uuid` AND `identify`.`address` = `identifyAlso`.`address` AND `identify`.`updated` < `identifyAlso`.`updated`) "
					+ "WHERE `identifyAlso`.`uuid` IS NULL AND `identify`.`uuid` != ? "
					+ "AND `identify`.`address` IN (SELECT `address` FROM `omega_identify` WHERE `uuid` = ?)", rawUUID, rawUUID)) {

				while (rs.next()) {
					result.computeIfAbsent(BytesUtil.boxAll(rs.getBytes("final_address`")), (k) -> new HashSet<>())
							.add(new PlayerInfo(UUIDUtil.uuidFromByteArray(rs.getBytes("final_uuid")), rs.getString("final_name")));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return result;
		});
	}
	
	/**
	 * Conducts an alt check for a player name. To find a target,
	 * The most recent UUID which had the name is used. <br>
	 * Then, an alt check is conducted to find the other players
	 * whose addresses match that of the target. <br>
	 * <br>
	 * The result is a map of the target's addresses to sets of PlayerInfo.
	 * There is no corresponding set if no players matched the address. <br>
	 * <br>
	 * If the altcheck yields no results, either because the player with the name
	 * was not found, or because no other addresses
	 * 
	 * @param name the player name
	 * @return a completable future whose result is a map which is empty if the altcheck yielded no results
	 */
	public CompletableFuture<Map<Byte[], Set<PlayerInfo>>> conductAltcheck(String name) {
		return findPlayerInfo(name).thenCompose((info) -> (info == null) ? CompletableFuture.completedFuture(null) : conductAltcheck(info));
		// TODO
		// We can't use this until ArimAPI adds support for composite results
		/*
		sql.selectAsync(() -> {
			Map<Byte[], Set<PlayerInfo>> result = new HashMap<>();
			/*
			 * SQL Objective
			 * Find other player names whose addresses match the UUID corresponding to the name,
			 * all using the most up-to-date name records.
			 */
			/*try (ResultSet rs = sql.select("CREATE VIEW `tempViewUuid` AS "
					+ "SELECT `uuid` FROM `omega_identify` WHERE `name` = ? ORDER BY `updated` DESC LIMIT 1; "
					
					+ "CREATE VIEW `tempViewAddresses` "
					+ "AS SELECT `address` FROM `omega_identify` WHERE `uuid` IN (SELECT `uuid` FROM `tempViewUuid`); "
					
					+ "SELECT `identify`.`uuid` AS `final_uuid`, `identify`.`name` AS `final_name`, `identify`.`address` AS `final_address` "
					+ "FROM `omega_identify` `identify` "
					+ "LEFT JOIN `omega_identify` `identifyAlso` "
					+ "ON (`identify`.`uuid` = `identifyAlso`.`uuid` AND `identify`.`address` = `identifyAlso`.`address` AND `identify`.`updated` < `identifyAlso`.`updated`) "
					+ "WHERE `identifyAlso`.`uuid` IS NULL AND `identify`.`uuid` NOT IN (SELECT `uuid` FROM `tempViewUuid`) "
					+ "AND `identify`.`address` IN (SELECT `address` FROM `tempViewAddresses`); "
					
					+ "DROP VIEW `tempViewAddresses`; "
					+ "DROP VIEW `tempViewUuid`", name)) {
				
				while (rs.next()) {
					result.computeIfAbsent(BytesUtil.boxAll(rs.getBytes("final_address`")), (k) -> new HashSet<>())
							.add(new PlayerInfo(UUIDUtil.uuidFromByteArray(rs.getBytes("final_uuid")), rs.getString("final_name")));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return result;
		});
		*/
		/*
		 * "This version of MySQL doesn't yet support 'LIMIT & IN/ALL/ANY/SOME subquery'"
		 * So, our subquery can't work because it requires LIMIT
		 * 
		 */
		/*
		OmegaPlayer player = getPlayerByName(name);
		if (player != null) {
			return conductAltcheck(player.getUuid());
		}
		return sql.selectAsync(() -> {
			Map<Byte[], Set<PlayerInfo>> result = new HashMap<>();
			try (ResultSet rs = sql.select("SELECT * FROM `omega_identify` `identify` "
					+ "LEFT JOIN `omega_identify` `identifyAlso` "
					+ "ON (`identify`.`uuid` = `identifyAlso`.`uuid` AND `identify`.`address` = `identifyAlso`.`address` AND `identify`.`updated` < `identifyAlso`.`updated`) "
					+ "WHERE `identifyAlso`.`uuid` IS NULL AND `identify`.`address` IN "
					+ "(SELECT DISTINCT `address` FROM `omega_identify` WHERE `uuid` IN (SELECT `uuid` FROM `omega_identify` WHERE `name` = ? ORDER BY `updated` DESC LIMIT 1))")) {
				
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return result;
		});
		*/
	}
	
	/**
	 * Conducts an altcheck based on the target with the UUID and name. <br>
	 * The altcheck will find other players whose addresses match that of the target. <br>
	 * <br>
	 * The result is a map of the target's addresses to sets of PlayerInfo.
	 * There is no corresponding set if no other players matched the address.
	 * 
	 * @param info the player info of the target
	 * @return a completable future whose result is a map which is never null
	 */
	public CompletableFuture<Map<Byte[], Set<PlayerInfo>>> conductAltcheck(PlayerInfo info) {
		return conductAltcheck(info.getUuid());
	}
	
	/**
	 * Forcibly removes the player information without saving it
	 * 
	 * @param uuid the player uuid
	 */
	void remove(UUID uuid) {
		players.remove(uuid);
	}

	private Rank findRank(Player player) {
		for (Rank rank : ranks) {
			if (player.hasPermission(rank.getPermission())) {
				return rank;
			}
		}
		return Rank.NONE;
	}
	
	@Override
	public void close() {
		loader.close();
		sql.close();
	}
	
}
