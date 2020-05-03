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
package space.arim.lib.status;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.ChatColor;

import space.arim.lib.api.ArimLib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

public class StatusManager {

	private static final UUID EMPTY_UUID = UUID.fromString("0-0-0-0-0");
	
	private final PingPacketListener listener;
	
	volatile String motd;
	volatile Collection<WrappedGameProfile> hover;
	
	public StatusManager(ArimLib api) {
		listener = new PingPacketListener(this, api);
		setMotd("              &d&kabc&r &6&lArim.Space&r &d&kabc&r\n        &3&lServer starting, please wait...");
		setHover("&c&lServer is still starting...");
		ProtocolLibrary.getProtocolManager().addPacketListener(listener);
	}
	
	public void setMotd(String motd) {
		this.motd = ChatColor.translateAlternateColorCodes('&', motd);
	}
	
	public void setHover(String hover) {
		WrappedGameProfile profile = new WrappedGameProfile(EMPTY_UUID, ChatColor.translateAlternateColorCodes('&', hover));
		this.hover = Collections.singleton(profile);
	}
	
}
