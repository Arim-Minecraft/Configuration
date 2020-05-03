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
package space.arim.lib.vanish;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import space.arim.lib.api.ArimLib;

public class VanishManager {

	private final ArimLib api;
	private final HashMap<UUID, GameMode> vanished = new HashMap<>();
	
	public VanishManager(ArimLib api) {
		this.api = api;
	}
	
	/**
	 * Ensures that the target player cannot see vanished players.
	 * 
	 * @param player the target player
	 */
	private void ensurePlayerCannotSeeVanished(Player player) {
		vanished.forEach((vanishUUID, gm) -> {
			Player vanish = Bukkit.getPlayer(vanishUUID);
			if (vanish != null) {
				player.hidePlayer(vanish);
			}
		});
	}
	
	/**
	 * Ensures that other, unvanished players cannot see the target player.
	 * 
	 * @param player the target player
	 */
	private void ensureUnvanishedCannotSeePlayer(Player player) {
		api.server().getOnlinePlayers().forEach((other) -> {
			if (!isVanish(other)) {
				other.hidePlayer(player);
			}
		});
	}
	
	public void onJoin(Player player) {
		ensurePlayerCannotSeeVanished(player);
	}
	
	public void vanish(Player player) {
		if (vanished.putIfAbsent(player.getUniqueId(), player.getGameMode()) == null) {
			ensureUnvanishedCannotSeePlayer(player);
			player.setGameMode(GameMode.SPECTATOR);
		}
	}
	
	public void unvanish(Player player) {
		GameMode previous = vanished.remove(player.getUniqueId());
		if (previous != null) {

			api.server().getOnlinePlayers().forEach((other) -> other.showPlayer(player));
			player.setGameMode(previous);
			ensurePlayerCannotSeeVanished(player);
		}
	}
	
	public boolean isVanish(Player player) {
		return vanished.containsKey(player.getUniqueId());
	}
	
	public void onTeleport(Player player) {
		if (isVanish(player)) {
			ensureUnvanishedCannotSeePlayer(player);
		}
	}
	
	public void onGameModeChange(Player player) {
		if (vanished.remove(player.getUniqueId()) != null) {
			api.server().getOnlinePlayers().forEach((other) -> other.showPlayer(player));
			ensurePlayerCannotSeeVanished(player);
		}
	}
	
	public void onQuit(Player player) {
		vanished.remove(player.getUniqueId());
	}
	
}
