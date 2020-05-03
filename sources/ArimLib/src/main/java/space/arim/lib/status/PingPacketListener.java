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

import space.arim.lib.api.ArimLib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;

class PingPacketListener extends PacketAdapter {
	
	private final StatusManager manager;
	
	PingPacketListener(StatusManager manager, ArimLib api) {
		super(PacketAdapter.params(api.center(), PacketType.Status.Server.SERVER_INFO).listenerPriority(ListenerPriority.LOW).optionAsync());
		this.manager = manager;
    }
	
	@Override
	public void onPacketSending(PacketEvent event) {
		final WrappedServerPing ping = event.getPacket().getServerPings().read(0);
        ping.setMotD(manager.motd);
        ping.setPlayers(manager.hover);
	}
	
}
