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
package space.arim.lib.sound;

import space.arim.lib.api.ArimLib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

class SoundPacketListener extends PacketAdapter {
	
	private final PacketSoundManager manager;
	
	SoundPacketListener(PacketSoundManager manager, ArimLib api) {
		super(PacketAdapter.params(api.center(), PacketType.Play.Server.NAMED_SOUND_EFFECT).listenerPriority(ListenerPriority.LOW).optionAsync());
		this.manager = manager;
    }
	
	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		String sound = packet.getStrings().read(0);
		
		if (sound.startsWith(PacketSoundManager.SOUND_TO_CANCEL)) {

			PacketSound cs = manager.replacements.remove(event.getPlayer().getUniqueId());
			if (cs != null) {
				if (cs == PacketSound.EMPTY) {
					event.setCancelled(true);
				} else {
					packet.getStrings().write(0, cs.sound);
					packet.getFloat().write(0, cs.volume);
					packet.getFloat().write(1, cs.pitch);
				}
			}
		}
	}
	
}
