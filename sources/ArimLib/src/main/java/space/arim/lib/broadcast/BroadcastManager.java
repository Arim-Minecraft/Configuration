/* 
 * ArimLib
 * Copyright © 2019 Anand Beh <https://www.arim.space>
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
package space.arim.lib.broadcast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import space.arim.api.concurrent.AsyncStartingModule;

import space.arim.lib.api.ArimLib;

import me.tigerhix.lib.bossbar.BossbarLib;

public class BroadcastManager implements AsyncStartingModule {
	final BossbarLib barlib;
	private final List<String> bossbars = new CopyOnWriteArrayList<>();
	final String world;
	final ArimLib api;
	private final HashMap<UUID, BukkitTask> broadcasting = new HashMap<UUID, BukkitTask>();
	
	private CompletableFuture<?> future;

	public BroadcastManager(ArimLib api, String world) {
		barlib = BossbarLib.createFor(api.center(), 49L);
		this.api = api;
		this.world = world;
	}
	
	@Override
	public void startLoad() {
		future = CompletableFuture.runAsync(() -> {
			try (Scanner scanner = new Scanner(new File(api.center().getDataFolder(), "bossbars.txt"), "UTF-8")) {
				while (scanner.hasNextLine()) {
					bossbars.add(ChatColor.translateAlternateColorCodes('&', scanner.nextLine()));
				}
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
		});
	}
	
	@Override
	public void finishLoad() {
		future.join();
	}

	public List<String> getMutableBars() {
		return this.bossbars;
	}

	public void removePlayer(Player target) {
		BukkitTask task = broadcasting.remove(target.getUniqueId());
		if (task != null) {
			task.cancel();
		}
	}
	
	public void addPlayer(final Player target) {
		broadcasting.put(target.getUniqueId(), api.center().getServer().getScheduler().runTaskTimer(api.center(),
				new BroadcastRunnable(this, target), 1L, 100L));
	}
}
