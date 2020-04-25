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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.Getter;

public class PendingPlayer extends PartialPlayer {

	@Getter
	private final transient UUID uuid;
	
	private volatile CompletableFuture<SqlPlayerStats> futureStats;
	private volatile Rank rank;
	
	public PendingPlayer(UUID uuid) {
		this.uuid = uuid;
	}
	
	@Override
	void begin(Omega manager) {
		//CompletableFuture<?>[] futures = new CompletableFuture<?>[4];
		
		// TODO still working on this
		futureStats = null;
	}
	
	@Override
	void joinStats() {
		futureStats.join();
	}
	
	@Override
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	@Override
	OmegaPlayer finish() {
		return new OmegaPlayer(uuid, rank, futureStats.join());
	}
	
	@Override
	void abort(Omega manager) {
		futureStats = null;
	}
	
}
