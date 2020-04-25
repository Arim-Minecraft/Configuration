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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Bukkit;

import space.arim.api.sql.ExecutableQuery;

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
	private final AtomicLong balance;
	@Getter
	private final AtomicInteger kitpvp_kills;
	@Getter
	private final AtomicInteger kitpvp_deaths;
	@Getter
	private final AtomicInteger combo_kills;
	@Getter
	private final AtomicInteger combo_deaths;
	@Getter
	private final AtomicLong monthly_reward;
	
	// Prefs table
	
	@Getter
	private final MutablePrefs prefs;
	
	private final AtomicBoolean isCurrentlySaving = new AtomicBoolean(false);
	
	OmegaPlayer(UUID uuid, Rank rank, SqlPlayerStats stats) {
		this.uuid = uuid;
		this.rank = rank;
		this.balance = new AtomicLong(stats.getBalance());
		this.kitpvp_kills = new AtomicInteger(stats.getKitpvp_kills());
		this.kitpvp_deaths = new AtomicInteger(stats.getKitpvp_deaths());
		this.combo_kills = new AtomicInteger(stats.getCombo_kills());
		this.combo_deaths = new AtomicInteger(stats.getCombo_deaths());
		this.prefs = stats.getPrefs();
		this.monthly_reward = new AtomicLong(stats.getMonthly_reward());
	}
	
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	/**
	 * Saves and unloads the OmegaPlayer
	 * 
	 * @param manager the omega manager
	 * @return a completable future representing the saving and unloading
	 */
	CompletableFuture<?> save(Omega manager) {
		CompletableFuture<?>[] futures = new CompletableFuture<?>[4];
		OmegaSql sql = manager.sql;

		if (!isCurrentlySaving.compareAndSet(false, true)) {
			return CompletableFuture.completedFuture(null);
		}

		// TODO still working on this
		futures[0] = sql.connectAsync(() -> {
			try {
				sql.executionQueries(new ExecutableQuery(" ", kitpvp_kills.get(), kitpvp_deaths.get()));
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
