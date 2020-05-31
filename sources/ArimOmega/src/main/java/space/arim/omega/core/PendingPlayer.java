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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A player whose data is being loaded from the SQL backend.
 * 
 * @author A248
 *
 */
class PendingPlayer extends PartialPlayer {

	private static final Logger logger = LoggerFactory.getLogger(PendingPlayer.class);
	
	private final UUID uuid;

	private volatile CompletableFuture<PlayerNumbers> futureNumbers;
	private volatile Rank rank;
	
	PendingPlayer(UUID uuid, String name, byte[] address) {
		super(uuid, name, address);
		this.uuid = uuid;
	}
	
	@Override
	void begin(Omega omega) {
		super.begin(omega);

		OmegaSql sql = omega.sql;
		futureNumbers = sql.selectAsync(() -> {
			try (ResultSet rs = sql.select("SELECT * FROM `omega_numbers` WHERE `uuid` = ?", rawUUID)) {
				if (rs.next()) {
					return new PlayerNumbers(
							rs.getLong("balance"),
							new int[] {rs.getInt("level"), rs.getInt("kitpvp_kills"), rs.getInt("kitpvp_deaths"),
									rs.getInt("combo_kills"), rs.getInt("combo_deaths"), rs.getInt("monthly_reward")},
							rs.getByte("toggle_prefs"), (char) rs.getShort("chat_colour"), (char) rs.getShort("name_colour"));
				}
				logger.debug("No player stats/prefs found for {}, using default values", uuid);
			} catch (SQLException ex) {
				logger.error("Error selecting player statistics and preferences for {}", uuid, ex);
			}
			return PlayerNumbers.makeDefaultValues();
		});
	}
	
	@Override
	void joinLoading() {
		futureNumbers.join();
	}
	
	@Override
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	@Override
	OmegaPlayer finish() {
		return new OmegaPlayer(uuid, name, rank, futureNumbers.join());
	}
	
	@Override
	void abort(Omega omega) {
		futureNumbers.cancel(false);
		futureNumbers = null;
	}
	
}
