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

import space.arim.api.uuid.UUIDUtil;

import space.arim.swiftconomy.core.AbstractSwiftConomy;

public class OmegaSwiftConomy extends AbstractSwiftConomy {

	/**
	 * The default / starting balance
	 * 
	 */
	static final long STARTING_BALANCE = 3000_0000L;
	
	private static final int BALTOP_SIZE = 5;
	
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
		return (player != null) ? player.getStats().getBalance() : null;
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
			return CompletableFuture.completedFuture(new BaltopEntry(player.getUuid(), player.getName(), player.getStats().getBalance().get()));
		}
		OmegaSql sql = omega.sql;
		return omega.findPlayerInfo(name).thenCompose((info) -> (info == null) ? null : sql.selectAsync(() -> {
			UUID uuid = info.getUuid();
			try (ResultSet rs = sql.selectionQuery("SELECT `balance` FROM `omega_stats` WHERE `uuid` = UNHEX(?)", uuid.toString().replace("-", ""))) {

				return new BaltopEntry(uuid, info.getName(), (rs.next()) ? rs.getLong("balance") : STARTING_BALANCE);

			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return null;
		}));
	}
	
	public CompletableFuture<List<BaltopEntry>> getTopBalances() {
		OmegaSql sql = omega.sql;
		return sql.selectAsync(() -> {
			List<BaltopEntry> entries = new ArrayList<>();

			omega.forEach((uuid, player) ->
				potentiallyAddToList(entries, uuid, player.getName(), player.getStats().getBalance().get()));

			try (ResultSet rs = sql.selectionQuery("SELECT `HEX(uuid),name,balance` FROM `omega_stats` ORDER BY `balance` DESC LIMIT " + BALTOP_SIZE)) {
				while (rs.next()) {

					UUID uuid = UUIDUtil.expandAndParse(rs.getString("HEX(uuid)"));

					// check if we already accounted for this player earlier
					boolean found = false;
					for (BaltopEntry entry : entries) {
						if (entry.getUuid().equals(uuid)) {
							found = true;
							break;
						}
					}
					if (!found) {
						potentiallyAddToList(entries, uuid, rs.getString("name"), rs.getLong("balance"));
					}
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return entries;
		});
	}

}
