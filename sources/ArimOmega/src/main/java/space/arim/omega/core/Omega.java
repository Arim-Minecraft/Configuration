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
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.api.concurrent.AsyncStartingModule;
import space.arim.api.config.SimpleConfig;
import space.arim.api.uuid.UUIDUtil;

import space.arim.omega.util.BytesUtil;

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
	 * Minutes within a month, equal to 1440 (seconds in a day) times 30 (days in a month). <br>
	 * Sort of like a unix timestamp but in minutes, not seconds, to save space. <br>
	 * <br>
	 * This is used with the monthly reward of the player, which is also in minutes.
	 * 
	 */
	private static final int MINUTES_IN_MONTH = 1440 * 30;
	
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
		sql = new OmegaSql(logger, sqlCfg.getString("host"), sqlCfg.getInt("port"), sqlCfg.getString("database"),
				sqlCfg.getString("url"), sqlCfg.getString("username"), sqlCfg.getString("password"),
				sqlCfg.getInt("connections"));
		sqlCfg.close();

		loader = new OmegaDataLoader(this);

		economy = new OmegaSwiftConomy(this);
	}

	public void registerWith(JavaPlugin plugin) {
		org.bukkit.Server server = plugin.getServer();
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
	 * Run something for each transient player info
	 * 
	 * @param action what to do
	 */
	void forEachTransient(Consumer<TransientPlayer> action) {
		players.forEach((uuid, player) -> action.accept(player.getTransientInfo()));
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
	 * Activates the monthly reward for the player. <br>
	 * Remember to check player permissions first, only ranked players
	 * have access to monthly rewards. <br>
	 * <br>
	 * Returns <code>false</code> if the player's last reward was less than a month ago. <br>
	 * Else, the value of the player's last reward is automatically set to the current time. <br>
	 * <br>
	 * <i>The caller is trusted with providing the reward if this returns true.</i>
	 * 
	 * @param player the player
	 * @return true if the reward was activated, false if the last reward was less than a month ago
	 */
	public boolean activateMonthlyReward(Player player) {
		AtomicIntegerArray integer_stats = getPlayer(player).getStats().getInteger_stats();
		int existing;
		int now;
		do {
			existing = integer_stats.get(5);
			now = currentTimeMinutes();
			if (now - existing < MINUTES_IN_MONTH) {
				return false;
			}
		} while (!MutableStats.compareAndSetArray(integer_stats, 5, existing, now));
		return true;
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
	
	private static void potentiallyAddToSetArray(byte[][] checkFor, Set<AltcheckEntry>[] preresult, UUID uuid, String name, byte[][] addresses) {
		for (int m = 0; m < addresses.length; m++) {
			AltcheckEntry cachedEntry = null;
			for (int n = 0; n < checkFor.length; n++) {
				byte[] address = addresses[n];
				if (Arrays.equals(checkFor[n], address)) {
					if (cachedEntry == null) {
						cachedEntry = new AltcheckEntry(uuid, name, address);
					}
					if (preresult[n] == null) {
						preresult[n] = new HashSet<>();
					}
					preresult[n].add(cachedEntry);
				}
			}
		}
	}
	
	/**
	 * Gets an online player by name
	 * 
	 * @param name the player name
	 * @return the player or <code>null</code> if not found
	 */
	OmegaPlayer getPlayerByName(String name) {
		for (OmegaPlayer player : players.values()) {
			if (player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}
	
	/**
	 * Finds a player by name, returns UUID and name. <br>
	 * The input name is case insensitive, while the output name is correctly capitalised. <br>
	 * <br>
	 * Note that the result of the completable future will have null IPs, use
	 * {@link #findPlayerInfoWithIPs(String)} if IPs are desired.
	 * 
	 * @param name the player name
	 * @return a completable future whose result is <code>null</code> if not found
	 */
	public CompletableFuture<IdentifyingPlayerInfo> findPlayerInfo(String name) {
		OmegaPlayer player = getPlayerByName(name);
		if (player != null) {
			return CompletableFuture.completedFuture(new IdentifyingPlayerInfo(player.getUuid(), player.getName(), null));
		}
		// MySQL is not case sensitive https://stackoverflow.com/a/61484046/6548501
		return sql.selectAsync(() -> {
			try (ResultSet rs = sql.selectionQuery("SELECT `uuid,name` FROM `omega_identify` WHERE `name` = ? ORDER BY `updated` DESC LIMIT 1", name)) {
				if (rs.next()) {

					return new IdentifyingPlayerInfo(UUIDUtil.expandAndParse(rs.getString("uuid")),
							rs.getString("name"), null);
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
	 * @return a completable future whose result is <code>null</code> if not found
	 */
	public CompletableFuture<IdentifyingPlayerInfo> findPlayerInfoWithIPs(String name) {
		OmegaPlayer player = getPlayerByName(name);
		if (player != null) {
			return CompletableFuture.completedFuture(new IdentifyingPlayerInfo(player.getUuid(), player.getName(), player.getIps()));
		}
		return sql.selectAsync(() -> {
			try (ResultSet rs = sql.selectionQuery("SELECT `uuid,name,ips` FROM `omega_identify` WHERE `name` = ? ORDER BY `updated` DESC LIMIT 1", name)) {
				if (rs.next()) {

					return new IdentifyingPlayerInfo(UUIDUtil.expandAndParse(rs.getString("uuid")),
							rs.getString("name"), OmegaPlayer.decodeIps(rs.getString("ips")));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return null;
		});
	}
	
	/**
	 * Conducts an alt check for a player. <br>
	 * The result is a map of the addresses to sets of AltcheckEntry.
	 * The corresponding set is null if no players matched the address.
	 * 
	 * @param playerInfo identifying player info including IP addresses
	 * @return a completable future whose result is never null
	 */
	public CompletableFuture<Map<Byte[], Set<AltcheckEntry>>> conductAltcheck(IdentifyingPlayerInfo playerInfo) {
		return sql.selectAsync(() -> {
			UUID uuid = playerInfo.getUuid();
			byte[][] checkFor = playerInfo.getAddresses();

			// the index of the preresult array corresponds to that of the checkFor array
			@SuppressWarnings("unchecked")
			Set<AltcheckEntry>[] preresult = (Set<AltcheckEntry>[]) new Set<?>[checkFor.length];

			players.forEach((uuidOther, player) -> {
				if (!uuid.equals(uuidOther)) {
					potentiallyAddToSetArray(checkFor, preresult, uuidOther, player.getName(), player.getIps());
				}
			});

			StringBuilder builder = new StringBuilder();
			for (byte[] ip : checkFor) {
				String encodedIp = Base64.getEncoder().encodeToString(ip);
				assert encodedIp.indexOf('%') == -1;

				builder.append(" OR `ips` LIKE BINARY '%").append(encodedIp).append("%'");
			}
			String ipScanPredicate = builder.substring(" OR ".length());
			try (ResultSet rs = sql.selectionQuery("SELECT * FROM `omega_alts` WHERE `uuid` != ? AND (" + ipScanPredicate + ")",
					uuid.toString().replace("-", ""))) {
				while (rs.next()) {

					potentiallyAddToSetArray(checkFor, preresult, UUIDUtil.expandAndParse(rs.getString("uuid")),
							rs.getString("name"), OmegaPlayer.decodeIps(rs.getString("ips")));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			// build the result
			Map<Byte[], Set<AltcheckEntry>> result = new HashMap<>();
			for (int n = 0; n < checkFor.length; n++) {
				result.put(BytesUtil.boxAll(checkFor[n]), preresult[n]);
			}
			return result;
		});
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
