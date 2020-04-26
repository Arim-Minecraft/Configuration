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

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import space.arim.swiftconomy.core.AbstractSwiftConomy;

public class OmegaSwiftConomy extends AbstractSwiftConomy {

	private final Omega omega;
	
	protected OmegaSwiftConomy(Omega omega) {
		super(4, 2); // accuracy of 4, display decimals of 2
		this.omega = omega;
	}

	@Override
	public Collection<UUID> getAllUUIDs() {
		return new HashSet<>(omega.allUUIDs());
	}

	@Override
	protected AtomicLong getRawBalance(UUID uuid) {
		OmegaPlayer player = omega.getPlayer(uuid);
		return (player != null) ? player.getStats().getBalance() : null;
	}

}
