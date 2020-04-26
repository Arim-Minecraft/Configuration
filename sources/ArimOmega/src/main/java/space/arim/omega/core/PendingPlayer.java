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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class PendingPlayer extends PartialPlayer {

	private final UUID uuid;
	
	private volatile CompletableFuture<MutableStats> futureStats;
	private volatile CompletableFuture<MutablePrefs> futurePrefs;
	private volatile Rank rank;
	
	PendingPlayer(UUID uuid) {
		this.uuid = uuid;
	}
	
	@Override
	void begin(Omega omega) {
		OmegaSql sql = omega.sql;
		futureStats = sql.supplyAsync(() -> {
			try (ResultSet rs = sql.selectionQuery("SELECT * FROM `omega_stats` WHERE `uuid` = ?", uuid)) {
				if (!rs.next()) {
					// default statistics, starting balance of $3000, monthly reward immediately available
					return new MutableStats(3000L, 0, 0, 0, 0,
							(int) (System.currentTimeMillis() / Omega.MILLIS_IN_MINUTE - Omega.MINUTES_IN_MONTH));
				}
				return new MutableStats(rs.getLong("balance"), rs.getInt("kitpvp_kills"), rs.getInt("kitpvp_deaths"),
						rs.getInt("combo_kills"), rs.getInt("combo_deaths"), rs.getInt("monthly_reward"));
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return null;
		});
		futurePrefs = sql.supplyAsync(() -> {
			try (ResultSet rs = sql.selectionQuery("SELECT * FROM `omega_prefs` WHERE `uuid` = ?", uuid)) {
				if (!rs.next()) {
					return new MutablePrefs(MutablePrefs.DEFAULT_TOGGLE_PREFS, "&f", "&b", MutablePrefs.stringToMap("<empty>"));
				}
				return new MutablePrefs(rs.getByte("toggle_prefs"), rs.getString("chat_colour"), rs.getString("name_colour"),
						MutablePrefs.stringToMap(rs.getString("friended_ignored")));
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return null;
		});
	}
	
	@Override
	void joinLoading() {
		futureStats.join();
		futurePrefs.join();
	}
	
	@Override
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	@Override
	OmegaPlayer finish() {
		return new OmegaPlayer(uuid, rank, futureStats.join(), futurePrefs.join());
	}
	
	@Override
	void abort(Omega omega) {
		futureStats = null;
		futurePrefs = null;
	}
	
}
