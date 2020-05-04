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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import space.arim.omega.util.BytesUtil;

import lombok.RequiredArgsConstructor;

/**
 * A player whose data is being loaded from the SQL backend.
 * 
 * @author A248
 *
 */
@RequiredArgsConstructor
class PendingPlayer extends PartialPlayer {

	private final UUID uuid;
	private final String name;
	private final byte[] address;
	
	private volatile CompletableFuture<Byte[][]> futureIps;
	private volatile CompletableFuture<MutableStats> futureStats;
	private volatile CompletableFuture<MutablePrefs> futurePrefs;
	private volatile Rank rank;
	
	@Override
	void begin(Omega omega) {
		OmegaSql sql = omega.sql;
		futureIps = sql.selectAsync(() -> {
			ArrayList<Byte[]> ips = new ArrayList<>();
			try (ResultSet rs = sql.selectionQuery("SELECT `ips` FROM `omega_identify` WHERE `uuid` = ?", uuid.toString().replace("-", ""))) {
				if (rs.next()) {
					for (String previous : rs.getString("ips").split(",")) {
						byte[] previousIp = Base64.getDecoder().decode(previous);
						if (!Arrays.equals(address, previousIp)) {
							ips.add(BytesUtil.boxAll(previousIp));
						}
					}
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}

			ips.add(BytesUtil.boxAll(address));
			// We only store up to 20 IPs per player
			if (ips.size() > OmegaPlayer.MAX_STORED_IPS) {
				ips.remove(0); // remove oldest address
			}
			return ips.toArray(new Byte[][] {});
		});
		futureStats = sql.selectAsync(() -> {
			try (ResultSet rs = sql.selectionQuery("SELECT * FROM `omega_stats` WHERE `uuid` = ?", uuid.toString().replace("-", ""))) {
				if (rs.next()) {
					return new MutableStats(rs.getInt("level"), rs.getLong("balance"), rs.getInt("kitpvp_kills"),
							rs.getInt("kitpvp_deaths"), rs.getInt("combo_kills"), rs.getInt("combo_deaths"),
							rs.getInt("monthly_reward"));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return MutableStats.makeDefaultValues();
		});
		futurePrefs = sql.selectAsync(() -> {
			try (ResultSet rs = sql.selectionQuery("SELECT * FROM `omega_prefs` WHERE `uuid` = ?", uuid.toString().replace("-", ""))) {
				if (rs.next()) {
					return new MutablePrefs(rs.getByte("toggle_prefs"), rs.getString("chat_colour"), rs.getString("name_colour"),
							MutablePrefs.friendedIgnoredFromString(rs.getString("friended_ignored")));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return MutablePrefs.makeDefaultValues();
		});
	}
	
	@Override
	void joinLoading() {
		futureIps.join();
		futureStats.join();
		futurePrefs.join();
	}
	
	@Override
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	@Override
	OmegaPlayer finish() {
		return new OmegaPlayer(uuid, name, BytesUtil.unboxAll2D(futureIps.join()), rank, futureStats.join(), futurePrefs.join());
	}
	
	@Override
	void abort(Omega omega) {
		futureIps.cancel(false);
		futureStats.cancel(false);
		futurePrefs.cancel(false);
		futureIps = null;
		futureStats = null;
		futurePrefs = null;
	}
	
}
