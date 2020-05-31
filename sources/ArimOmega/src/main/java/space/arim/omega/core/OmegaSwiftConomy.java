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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.arim.swiftconomy.core.AbstractSwiftConomy;

import space.arim.uuidvault.api.UUIDUtil;

public class OmegaSwiftConomy extends AbstractSwiftConomy {

	/**
	 * The default / starting balance
	 * 
	 */
	static final long STARTING_BALANCE = 3000_0000L;
	
	private static final int BALTOP_SIZE = 5;
	
	private static final Logger logger = LoggerFactory.getLogger(OmegaSwiftConomy.class);
	
	private final Omega omega;
	
	protected OmegaSwiftConomy(Omega omega) {
		super(4, 2); // accuracy of 4, display decimals of 2
		this.omega = omega;
	}

	@Override
	public Collection<UUID> getAllUUIDs() {
		Set<UUID> result = new HashSet<>();
		omega.forEach((uuid, player) -> result.add(uuid));
		return result;
	}

	@Override
	protected AtomicLong getRawBalance(UUID uuid) {
		OmegaPlayer player = omega.getPlayer(uuid);
		return (player != null) ? player.getBalance() : null;
	}
	
	private static void potentiallyAddToList(List<BaltopEntry> entries, UUID uuid, String name, long balance) {
		if (entries.size() < BALTOP_SIZE || balance > entries.get(BALTOP_SIZE - 1).getBalance()) {
			BaltopEntry entry = new BaltopEntry(uuid, name, balance);
			int search = Collections.binarySearch(entries, entry);
			entries.add(-(search + 1), entry);
		}
	}
	
	/**
	 * Finds the balance of a player by name. <br>
	 * If the player is offline, this will lookup the UUID and balance. <br>
	 * <br>
	 * The completable future's result includes the player UUID, the correctly capitalised
	 * player name, and the balance of the player.
	 * 
	 * @param name the player name
	 * @return a completable future whose result is <code>null</code> if not found
	 */
	public CompletableFuture<BaltopEntry> findOfflineBalance(String name) {
		OmegaPlayer player = omega.getPlayerByName(name);
		if (player != null) {
			return CompletableFuture.completedFuture(new BaltopEntry(player.getUuid(), player.getName(), player.getBalance().get()));
		}
		OmegaSql sql = omega.sql;
		return sql.selectAsync(() -> {
			/*
			 * SQL Objective
			 * Select balance, uuid, and latest name of offline player using the given name and the most up-to-date
			 * UUID matching the given name.
			 */
			try (ResultSet rs = sql.select("SELECT `identify`.`uuid` AS `final_uuid`, `identify`.`name` AS `final_name`, `numbers`.`balance` "
					+ "FROM `omega_identify` `identify` "
					+ "INNER JOIN `omega_numbers` `numbers` "
					+ "ON (`identify`.`uuid` = `numbers`.`uuid`) "
					+ "ORDER BY `identify`.`updated` DESC LIMIT 1")) {
				if (rs.next()) {
					// 1 = uuid, 2 = name, 3 = balance
					return new BaltopEntry(UUIDUtil.uuidFromByteArray(rs.getBytes("final_uuid")), rs.getString("final_name"), rs.getLong("balance"));
				}
			} catch (SQLException ex) {
				logger.error("Error finding offline balance for {}", name, ex);
			}
			return null;
		});
	}
	
	public CompletableFuture<List<BaltopEntry>> getTopBalances() {
		OmegaSql sql = omega.sql;
		return sql.selectAsync(() -> {
			List<BaltopEntry> entries = new ArrayList<>();

			omega.forEach((uuid, player) ->
				potentiallyAddToList(entries, uuid, player.getName(), player.getBalance().get()));

			/*
			 * SQL Objective
			 * Select top 5 balances, corresponding UUIDs, and corresponding most up-to-date name
			 */
			try (ResultSet rs = sql.select("SELECT `identify`.`uuid` AS `final_uuid`, `identify`.`name` AS `final_name`, `numbers`.`balance` "
					+ "FROM `omega_identify` `identify` "
					+ "LEFT JOIN `omega_identify` `identifyAlso` "
					+ "ON (`identify`.`uuid` = `identifyAlso`.`uuid` AND `identify`.`updated` < `identifyAlso`.`updated`) "
					+ "INNER JOIN `omega_numbers` `numbers` "
					+ "ON (`identify`.`uuid` = `numbers`.`uuid`) "
					+ "WHERE `identifyAlso`.`uuid` IS NULL "
					+ "ORDER BY `numbers`.`balance` DESC LIMIT " + BALTOP_SIZE)) {

				while (rs.next()) {

					UUID uuid = UUIDUtil.uuidFromByteArray(rs.getBytes("final_uuid"));

					// check if we already accounted for this player earlier
					boolean found = false;
					for (BaltopEntry entry : entries) {
						if (entry.getUuid().equals(uuid)) {
							found = true;
							break;
						}
					}
					if (!found) {
						potentiallyAddToList(entries, uuid, rs.getString("final_name"), rs.getLong("balance"));
					}
				}
			} catch (SQLException ex) {
				logger.error("Error determing baltop", ex);
			}
			return entries;
		});
	}

}
