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

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;

public class OmegaPlayer {

	// Transient
	
	@Getter
	private final transient UUID uuid;
	@Getter
	private transient volatile Rank rank;
	
	// From SQL backend
	
	// Stats table
	
	@Getter
	private final MutableStats stats;
	
	// Prefs table
	
	@Getter
	private final MutablePrefs prefs;
	
	// Prevent saving twice
	
	private final AtomicBoolean isCurrentlySaving = new AtomicBoolean(false);
	
	OmegaPlayer(UUID uuid, Rank rank, MutableStats stats, MutablePrefs prefs) {
		this.uuid = uuid;
		this.rank = rank;
		this.stats = stats;
		this.prefs = prefs;
	}
	
	/**
	 * Sets the rank of the OmegaPlayer
	 * 
	 * @param rank the rank
	 */
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	/**
	 * Applies the rank display/tag and name/chat colour prefs of this OmegaPlayer to the player.
	 * 
	 * @param player the player, should be the same actual player as this one
	 */
	void applyDisplayNames(Player player) {
		assert player.getUniqueId().equals(uuid);

		player.setDisplayName(rank.getDisplay() + " " + prefs.getChatcolour() + player.getName());
		player.setPlayerListName(rank.getTag() + " " + prefs.getNamecolour() + player.getName());
	}
	
	/**
	 * Saves and unloads the OmegaPlayer
	 * 
	 * @param manager the omega manager
	 * @return a completable future representing the saving and unloading
	 */
	CompletableFuture<?> save(Omega manager) {
		CompletableFuture<?>[] futures = new CompletableFuture<?>[2];
		OmegaSql sql = manager.sql;

		if (!isCurrentlySaving.compareAndSet(false, true)) {
			return CompletableFuture.completedFuture(null);
		}

		// TODO still working on this
		futures[0] = sql.executeAsync(() -> {
			try {
				long balance = stats.getBalance().get();
				int kitpvp_kills = stats.getKitpvp_kills().get();
				int kitpvp_deaths = stats.getKitpvp_deaths().get();
				int combo_kills = stats.getCombo_kills().get();
				int combo_deaths = stats.getCombo_deaths().get();
				long monthly_reward = stats.getMonthly_reward().get();
				sql.executionQuery("INSERT INTO `omega_stats` "
						+ "(`uuid`, `balance`, `kitpvp_kills`, `kitpvp_deaths`, `combo_kills`, `combo_deaths`, `monthly_reward`) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?) "
						+ "ON DUPLICATE KEY UPDATE "
						+ "`balance` = ?, `kitpvp_kills` = ?, `kitpvp_deaths` = ?, `combo_kills` = ?, `combo_deaths` = ?, `monthly_reward` = ?",
						uuid.toString().replace("-", ""), balance, kitpvp_kills, kitpvp_deaths, combo_kills, combo_deaths, monthly_reward,
						balance, kitpvp_kills, kitpvp_deaths, combo_kills, combo_deaths, monthly_reward);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
		futures[1] = sql.executeAsync(() -> {
			try {
				byte toggle_prefs = (byte) prefs.toggle_prefs.get();
				String chat_colour = prefs.getChatcolour();
				String name_colour = prefs.getNamecolour();
				String friended_ignored = MutablePrefs.mapToString(prefs.getFriended_ignored());
				sql.executionQuery("INSERT INTO `omega_prefs` "
						+ "(`uuid`, `toggle_prefs`, `chat_colour`, `name_colour`, `friended_ignored`) "
						+ "VALUES (?, ?, ?, ?, ?) "
						+ "ON DUPLICATE KEY UPDATE "
						+ "`toggle_prefs` = ?, `chat_colour` = ?, `name_colour` = ?, `friended_ignored` = ?",
						uuid, toggle_prefs, chat_colour, name_colour, friended_ignored,
						toggle_prefs, chat_colour, name_colour, friended_ignored);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});

		return CompletableFuture.allOf(futures).thenRunAsync(() -> {
			isCurrentlySaving.set(false);
			if (!isOnlineThreadSafe(manager)) {
				manager.remove(uuid);
			}
		});
	}

	public boolean isOnlineThreadSafe(Omega omega) {
		return (Bukkit.isPrimaryThread()) ? Bukkit.getPlayer(uuid) != null
				: omega.supplySynced(() -> Bukkit.getPlayer(uuid) != null).join();
	}

	void removeIfOfflineUnlessSaving(Omega manager) {
		if (!isCurrentlySaving.get() && !isOnlineThreadSafe(manager) && !isCurrentlySaving.get()) {
			manager.remove(uuid);
		}
	}

}
