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

class ExistingPlayer extends PartialPlayer {

	private final OmegaPlayer player;
	
	ExistingPlayer(OmegaPlayer player) {
		this.player = player;
	}
	
	@Override
	void begin(Omega manager) {
		
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
	void abort(Omega manager) {
		player.removeIfOfflineUnlessSaving(manager);
	}

}
