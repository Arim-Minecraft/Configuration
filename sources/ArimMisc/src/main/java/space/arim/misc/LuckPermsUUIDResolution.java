/* 
 * ArimMisc
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ArimMisc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ArimMisc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ArimMisc. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.misc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import space.arim.api.uuid.UUIDResolution;

import net.luckperms.api.LuckPerms;

class LuckPermsUUIDResolution implements UUIDResolution {

	private final LuckPerms lp;
	
	LuckPermsUUIDResolution(LuckPerms lp) {
		this.lp = lp;
	}
	
	@Override
	public CompletableFuture<UUID> resolve(String name) {
		return lp.getUserManager().lookupUniqueId(name);
	}
	
	@Override
	public CompletableFuture<String> resolve(UUID uuid) {
		return lp.getUserManager().lookupUsername(uuid);
	}
	
	@Override
	public void update(UUID uuid, String name, boolean force) {
		lp.getUserManager().savePlayerData(uuid, name);
	}

}
