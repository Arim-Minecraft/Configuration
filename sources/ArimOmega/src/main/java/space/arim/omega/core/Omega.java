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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.slf4j.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.api.concurrent.AsyncStartingModule;
import space.arim.api.config.SimpleConfig;

import net.milkbowl.vault.economy.Economy;

public class Omega implements AsyncStartingModule {

	private final JavaPlugin plugin;
	private final ConcurrentHashMap<UUID, OmegaPlayer> players = new ConcurrentHashMap<>();
	
	final Logger logger;
	final OmegaSql sql;
	final OmegaDataLoader loader;
	
	private Map<Integer, Rank> ranks;
	
	private CompletableFuture<?> future;
	
	private final Executor syncExecutor;
	
	/**
	 * Minutes within a month, equal to 1440 (seconds in a day) times 30 (days in a month). <br>
	 * Sort of like a unix timestamp but in minutes, not seconds, to save space. <br>
	 * <br>
	 * This is used with the monthly reward of the player, which is also in minutes.
	 * 
	 */
	static final int MINUTES_IN_MONTH = 1440 * 30;
	
	/**
	 * Milliseconds within a minute, used to divide into System.currentTimeMillis
	 * to get the current unix time in minutes.
	 * 
	 */
	static final int MILLIS_IN_MINUTE = 60000;
	
	public Omega(JavaPlugin plugin, Logger logger) {
		this.plugin = plugin;
		this.logger = logger;

		syncExecutor = (cmd) -> Bukkit.getServer().getScheduler().runTask(plugin, cmd);

		SimpleConfig sqlCfg = new SimpleConfig(plugin.getDataFolder(), "sql.yml", "version") {};
		sqlCfg.reload();
		sql = new OmegaSql(logger, sqlCfg.getString("host"), sqlCfg.getInt("port"), sqlCfg.getString("database"),
				sqlCfg.getString("url"), sqlCfg.getString("username"), sqlCfg.getString("password"),
				sqlCfg.getInt("connections"));
		sqlCfg.close();

		loader = new OmegaDataLoader(this);
		Bukkit.getServer().getPluginManager().registerEvents(loader, plugin);

		Bukkit.getServicesManager().register(Economy.class, new OmegaSwiftConomy(this), plugin, ServicePriority.High);
	}
	
	@Override
	public void startLoad() {
		future = CompletableFuture.allOf(sql.makeStatsTableIfNotExist(), sql.makePrefsTableIfNotExist(), CompletableFuture.runAsync(() -> {
			HashMap<Integer, Rank> ranks = new HashMap<>();
			try (Scanner scanner = new Scanner(new File(plugin.getDataFolder(), "ranks.txt"), "UTF-8")) {
				ArrayList<String> lines = new ArrayList<>(4);
				while (scanner.hasNextLine()) {
					lines.add(scanner.nextLine());
					if (lines.size() == 4) {
						String id = lines.get(0).toLowerCase();
						if (ranks.put(ranks.size(), new Rank(id, lines.get(1), lines.get(2), lines.get(3))) != null) {
							logger.warn("Duplicate rank " + id);
						}
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
	 * Gets all UUIDs for which there are omega players
	 * 
	 * @return a backed key set of uuids corresponding to loaded omega players
	 */
	Set<UUID> allUUIDs() {
		return players.keySet();
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
	 * Activates the monthly reward for the player. <br>
	 * Remember to check player permissions first, only ranked players
	 * have access to monthly rewards. <br>
	 * <br>
	 * This will return <code>false</code> the player's last reward
	 * is not more than a month ago or there was a concurrency error. <br>
	 * If <code>true</code> is returned, the value of the player's last reward
	 * is automatically set to the current time.
	 * 
	 * @param omega the omega manager
	 * @return true if the reward was activated and reset, false otherwise
	 */
	public boolean activateMonthlyRank(Player player) {
		MutableStats stats = getPlayer(player).getStats();
		int existing = stats.getMonthly_reward().get();
		int time = (int) (System.currentTimeMillis() / MILLIS_IN_MINUTE);
		return (time - existing > MINUTES_IN_MONTH) && stats.getMonthly_reward().compareAndSet(existing, time);
	}
	
	/**
	 * Reloads the player's rank based on permissions. <br>
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
	 * Forcibly removes the player information without saving it
	 * 
	 * @param uuid the player uuid
	 */
	void remove(UUID uuid) {
		players.remove(uuid);
	}
	
	<T> CompletableFuture<T> supplySynced(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, syncExecutor);
	}
	
	private Rank findRank(Player player) {
		for (int n = 0; n < ranks.size(); n++) {
			Rank rank = ranks.get(n);
			if (player.hasPermission(rank.getPermission())) {
				return rank;
			}
		}
		return Rank.NONE;
	}
	
}
