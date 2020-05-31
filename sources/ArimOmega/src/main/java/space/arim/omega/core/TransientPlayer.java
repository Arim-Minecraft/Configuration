/* 
 * ArimOmega
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ArimOmega is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ArimOmega is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ArimOmega. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.omega.core;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import lombok.Getter;

/**
 * Transient player information, a lot of which relates to world state. <br>
 * It should be assumed that methods are not thread safe unless explicitly specified.
 * 
 * @author A248
 *
 */
public class TransientPlayer {

	private final Omega omega;

	private final Player player;
	private final String name;
	
	@Getter
	private volatile String world;
	
	private volatile GameMode vanishGm;
	
	TransientPlayer(Omega omega, Player player) {
		this.omega = omega;
		this.player = player;
		this.name = player.getName();
	}
	
	/**
	 * Gets the player's name safely. <br>
	 * <b>This is thread safe</b>.
	 * 
	 * @return the name of the player
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Checks whether the player is vanished. <br>
	 * <b>This is thread safe</b>.
	 * 
	 * @return whether the player is vanished
	 */
	public boolean isVanish() {
		return vanishGm != null;
	}
	
	/**
	 * Hides vanished players from this one
	 * 
	 */
	void hideVanishedFromPlayer() {
		assert !isVanish();

		omega.forEachTransient((other) -> {
			if (other.isVanish()) {
				player.hidePlayer(other.player);
			}
		});
	}
	
	/**
	 * Hides this player from unvanished players
	 * 
	 */
	void hidePlayerFromUnvanished() {
		assert isVanish();

		omega.forEachTransient((other) -> {
			if (!other.isVanish()) {
				other.player.hidePlayer(player);
			}
		});
	}
	
	/**
	 * Updates the player's world
	 * 
	 */
	void changeWorld() {
		this.world = player.getWorld().getName();
	}
	
	/**
	 * Vanish the player
	 * 
	 */
	public void vanish() {
		if (vanishGm == null) {
			vanishGm = player.getGameMode();
			player.setGameMode(GameMode.SPECTATOR);

			omega.forEachTransient((other) -> {
				if (other.isVanish()) {
					player.showPlayer(other.player); // show vanished players to this player
				} else {
					other.player.hidePlayer(player); // hide this player from unvanished players
				}
			});
		}
	}
	
	/**
	 * Unvanish the player
	 * 
	 */
	public void unvanish() {
		if (vanishGm != null) {
			player.setGameMode(vanishGm);
			vanishGm = null;

			omega.forEachTransient((other) -> {
				if (other.isVanish()) {
					player.hidePlayer(other.player); // hide vanished players from this player
				} else {
					other.player.showPlayer(player); // show this player to unvanished players
				}
			});
		}
	}
	
}
