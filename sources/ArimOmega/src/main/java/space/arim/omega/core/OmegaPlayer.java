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
import java.util.Base64;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Player;

import lombok.Getter;

public class OmegaPlayer {

	@Getter
	private final UUID uuid;
	@Getter
	private volatile String name;
	@Getter
	private volatile byte[][] ips;

	// Transient
	
	@Getter
	private transient volatile Rank rank;
	
	private transient volatile TransientPlayer transientInfo;
	
	// From SQL backend
	
	// Stats table
	
	@Getter
	private final MutableStats stats;
	
	// Prefs table
	
	@Getter
	private final MutablePrefs prefs;
	
	// Prevent saving twice
	
	private final AtomicBoolean isCurrentlySaving = new AtomicBoolean(false);
	
	/**
	 * The maximum amount of IP addresses stored
	 * 
	 */
	static final int MAX_STORED_IPS = 20;
	
	OmegaPlayer(UUID uuid, String name, byte[][] ips, Rank rank, MutableStats stats, MutablePrefs prefs) {
		this.uuid = uuid;
		this.name = name;
		this.ips = ips;
		this.rank = rank;
		this.stats = stats;
		this.prefs = prefs;
	}
	
	/**
	 * Sets the name of the OmegaPlayer
	 * 
	 * @param name the name
	 */
	void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the ips of the OmegaPlayer
	 * 
	 * @param ips the ips
	 */
	void setIps(byte[][] ips) {
		this.ips = ips;
	}
	
	/**
	 * Sets the rank of the OmegaPlayer
	 * 
	 * @param rank the rank
	 */
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	void nullifyTransientInfo() {
		transientInfo = null;
	}
	
	/**
	 * Gets transient player info. <br>
	 * Transient info is not necessarily thread safe.
	 * 
	 * @return transient player info
	 */
	TransientPlayer getTransientInfo() {
		return transientInfo;
	}
	
	/**
	 * Called in the PlayerJoinEvent.
	 * 
	 * @param player the bukkit player
	 * @param transientInfo the transient player info
	 */
	void onPlayerJoin(Player player, TransientPlayer transientInfo) {
		this.transientInfo = transientInfo;
		applyDisplayNames(player);
		transientInfo.hideVanishedFromPlayer();
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
	
	static byte[][] decodeIps(String rawIps) {
		String[] split = rawIps.split(",");
		byte[][] result = new byte[split.length][];
		for (int n = 0; n < split.length; n++) {
			result[n] = Base64.getDecoder().decode(split[n]);
		}
		return result;
	}
	
	/**
	 * Saves and unloads the OmegaPlayer
	 * 
	 * @param omega the omega manager
	 * @return a completable future representing the saving and unloading
	 */
	CompletableFuture<?> save(Omega omega) {
		HashSet<CompletableFuture<?>> futures = new HashSet<>();
		OmegaSql sql = omega.sql;

		if (!isCurrentlySaving.compareAndSet(false, true)) {
			return CompletableFuture.completedFuture(null);
		}
		String name = this.name;
		futures.add(sql.executeAsync(() -> {
			StringBuilder builder = new StringBuilder();
			for (byte[] ip : ips) {
				builder.append(',').append(Base64.getEncoder().encodeToString(ip));
			}
			String iplist = builder.substring(1);
			int updated = Omega.currentTimeMinutes();
			try {
				sql.executionQuery("INSERT INTO `omega_identify` "
						+ "(`uuid`, `name`, `ips`, `updated`) "
						+ "VALUES (?, ?, ?, ?) "
						+ "ON DUPLICATE KEY UPDATE "
						+ "`name` = ?, `ips` = ?, `updated` = ?",
						uuid.toString().replace("-", ""), name, iplist, updated,
						name, iplist, updated);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}));
		if (!stats.isCurrentlyDefault()) {
			futures.add(sql.executeAsync(() -> {
				long balance = stats.getBalance().get();
				int kitpvp_kills = stats.getKitpvp_kills().get();
				int kitpvp_deaths = stats.getKitpvp_deaths().get();
				int combo_kills = stats.getCombo_kills().get();
				int combo_deaths = stats.getCombo_deaths().get();
				long monthly_reward = stats.getMonthly_reward().get();
				try {
					sql.executionQuery("INSERT INTO `omega_stats` "
							+ "(`uuid`, `name`, `balance`, `kitpvp_kills`, `kitpvp_deaths`, `combo_kills`, `combo_deaths`, `monthly_reward`) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?) "
							+ "ON DUPLICATE KEY UPDATE "
							+ "`name` = ?, `balance` = ?, `kitpvp_kills` = ?, `kitpvp_deaths` = ?, `combo_kills` = ?, `combo_deaths` = ?, `monthly_reward` = ?",
							uuid.toString().replace("-", ""), name, balance, kitpvp_kills, kitpvp_deaths, combo_kills, combo_deaths, monthly_reward,
							name, balance, kitpvp_kills, kitpvp_deaths, combo_kills, combo_deaths, monthly_reward);
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}));
		}
		if (!prefs.isCurrentlyDefault()) {
			futures.add(sql.executeAsync(() -> {
				try {
					byte toggle_prefs = (byte) prefs.toggle_prefs.get();
					String chat_colour = prefs.getChatcolour();
					String name_colour = prefs.getNamecolour();
					String friended_ignored = prefs.getFriended_ignored().toString();
					sql.executionQuery("INSERT INTO `omega_prefs` "
							+ "(`uuid`, `toggle_prefs`, `chat_colour`, `name_colour`, `friended_ignored`) "
							+ "VALUES (?, ?, ?, ?, ?) "
							+ "ON DUPLICATE KEY UPDATE "
							+ "`toggle_prefs` = ?, `chat_colour` = ?, `name_colour` = ?, `friended_ignored` = ?",
							uuid.toString().replace("-", ""), toggle_prefs, chat_colour, name_colour, friended_ignored,
							toggle_prefs, chat_colour, name_colour, friended_ignored);
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}));
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[] {})).thenRunAsync(() -> {
			isCurrentlySaving.set(false);
			if (!omega.isOnline(uuid)) {
				omega.remove(uuid);
			}
		});
	}

	void removeIfOfflineUnlessSaving(Omega omega) {
		if (!isCurrentlySaving.get() && !omega.isOnline(uuid)) {
			omega.remove(uuid);
		}
	}	

}
