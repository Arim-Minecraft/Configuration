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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import space.arim.omega.util.BytesUtil;

import lombok.AllArgsConstructor;

/**
 * A player whose data did not finish saving before he/she logged back in. 
 * 
 * @author A248
 *
 */
@AllArgsConstructor
class ExistingPlayer extends PartialPlayer {

	private final OmegaPlayer player;
	private final String name;
	private final byte[] address;
	
	@Override
	void begin(Omega omega) {
		// update player name, this is the only time OmegaPlayer#setName is called
		player.setName(name);

		/*
		 * Here we update the player's IPs based on the latest IP
		 */
		byte[][] existingIps = player.getIps();
		// Fast escape if the latest IP is already the one at the end of the list
		if (Arrays.equals(address, existingIps[existingIps.length - 1])) {
			return;
		}
		/*
		 * Follow the same procedure described in PendingPlayer#begin
		 */
		List<Byte[]> ips = new ArrayList<>();
		for (byte[] previousIp : existingIps) {
			if (Arrays.equals(address, previousIp)) {
				continue;
			}
			ips.add(BytesUtil.boxAll(previousIp));
		}

		ips.add(BytesUtil.boxAll(address));
		while (ips.size() > OmegaPlayer.MAX_STORED_IPS) {
			ips.remove(0);
		}
		// It's fine that we don't have mutex here.
		// Even if the player somehow logs in super-quickly twice,
		// it's no big deal to store only 1 of those IPs
		player.setIps(BytesUtil.unboxAll2D(ips.toArray(new Byte[][] {})));
	}
	
	@Override
	void joinLoading() {
		
	}
	
	@Override
	void setRank(Rank rank) {
		player.setRank(rank);
	}
	
	@Override
	OmegaPlayer finish() {
		return player;
	}
	
	@Override
	void abort(Omega omega) {
		player.removeIfOfflineUnlessSaving(omega);
	}

}
