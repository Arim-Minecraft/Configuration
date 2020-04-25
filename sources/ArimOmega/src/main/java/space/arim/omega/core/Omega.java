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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.slf4j.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.api.concurrent.AsyncStartingModule;
import space.arim.api.config.SimpleConfig;

public class Omega implements AsyncStartingModule {

	private final JavaPlugin plugin;
	private final ConcurrentHashMap<UUID, OmegaPlayer> players = new ConcurrentHashMap<>();
	
	final Logger logger;
	final OmegaSql sql;
	final OmegaDataLoader loader;
	
	private Map<Integer, Rank> ranks;
	
	private CompletableFuture<?> future;
	
	private final Executor syncExecutor;
	
	public Omega(JavaPlugin plugin, Logger logger) {
		this.plugin = plugin;
		this.logger = logger;
		
		loader = new OmegaDataLoader(this);
		Bukkit.getServer().getPluginManager().registerEvents(loader, plugin);
		
		syncExecutor = (cmd) -> Bukkit.getServer().getScheduler().runTask(plugin, cmd);
		
		SimpleConfig sqlCfg = new SimpleConfig(plugin.getDataFolder(), "sql.yml", "version") {};
		sqlCfg.reload();
		sql = new OmegaSql(logger, sqlCfg.getString("host"), sqlCfg.getInt("port"), sqlCfg.getString("database"),
				sqlCfg.getString("url"), sqlCfg.getString("username"), sqlCfg.getString("password"),
				sqlCfg.getInt("connections"));
		sqlCfg.close();
	}
	
	@Override
	public void startLoad() {
		future = CompletableFuture.allOf(sql.makeTablesIfNotExist(), CompletableFuture.runAsync(() -> {
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
	 * Gets an online OmegaPlyer
	 * 
	 * @param uuid the player uuid
	 * @return the omega player
	 */
	public OmegaPlayer getPlayer(UUID uuid) {
		return players.get(uuid);
	}
	
	void add(UUID uuid, PartialPlayer player) {
		players.put(uuid, player.finish());
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
	
	Rank findRank(Player player) {
		for (int n = 0; n < ranks.size(); n++) {
			Rank rank = ranks.get(n);
			if (player.hasPermission(rank.getPermission())) {
				return rank;
			}
		}
		return Rank.NONE;
	}
	
}
