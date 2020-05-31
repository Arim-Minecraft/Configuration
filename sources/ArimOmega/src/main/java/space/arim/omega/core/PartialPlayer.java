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

import space.arim.api.util.sql.CloseMe;

import space.arim.uuidvault.api.UUIDUtil;

abstract class PartialPlayer {

	final byte[] rawUUID;
	final String name;
	private final byte[] address;
	
	PartialPlayer(UUID uuid, String name, byte[] address) {
		rawUUID = UUIDUtil.byteArrayFromUUID(uuid);
		this.name = name;
		this.address = address;
	}
	
	void begin(Omega omega) {
		OmegaSql sql = omega.sql;
		sql.executeAsync(() -> {
			long currentTime = System.currentTimeMillis();
			try (CloseMe cm = sql.execute(
					"INSERT INTO `omega_identify` "
					+ "(`uuid`, `name`, `address`, `updated`) "
					+ "VALUES (?, ?, ?, ?) "
					+ "ON DUPLICATE KEY UPDATE "
					+ "`updated` = ?", rawUUID, name, address, currentTime, currentTime)) {

			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}
	
	abstract void joinLoading();
	
	abstract void setRank(Rank rank);
	
	abstract OmegaPlayer finish();
	
	abstract void abort(Omega omega);
	
}
