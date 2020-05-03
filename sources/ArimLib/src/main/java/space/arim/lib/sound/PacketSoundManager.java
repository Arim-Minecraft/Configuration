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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import space.arim.lib.api.ArimLib;

import com.comphenix.protocol.ProtocolLibrary;

/**
 * A manager designed to cancel the MC sound called {@link SOUND_TO_CANCEL}
 * 
 * @author A248
 *
 */
public class PacketSoundManager {

	final ConcurrentHashMap<UUID, PacketSound> replacements = new ConcurrentHashMap<>();
	private final SoundPacketListener listener;
	
	/**
	 * The objective is to change the sound ("game.player.hurt")
	 * to a custom sound of our own designation. <br>
	 * <br>
	 * After extensive testing it was determined that this sound is
	 * actually NOT sent on 1.8 when it is the client who is damaged. <br>
	 * Namely, the client is responsible for playing its own damage sounds,
	 * but the server is responsible for playing the damage sounds of other
	 * players when those other players are damaged. <br>
	 * <br>
	 * However, "game.player.hurt" is NEVER sent from the server.
	 * It is simply played on the client when the player is damaged. <br>
	 * <br>
	 * Therefore, the only way to change the default hit sound is to use
	 * a custom resource pack.
	 * 
	 */
	public static final String SOUND_TO_CANCEL = "game.player.hurt";
	
	/**
	 * A manager designed to cancel the MC sound starting with {@link SOUND_TO_CANCEL}
	 * 
	 * @param api ArimLib
	 */
	public PacketSoundManager(ArimLib api) {
		listener = new SoundPacketListener(this, api);
		ProtocolLibrary.getProtocolManager().addPacketListener(listener);
	}
	
	/**
	 * See {@link #PacketSoundManager(ArimLib)} and {@link SOUND_TO_CANCEL}.
	 * 
	 * @param player the player for whom to cancel the sounding
	 */
	public void cancelDefaultHitSound(Player player) {
		replacements.put(player.getUniqueId(), PacketSound.EMPTY);
	}
	
	/**
	 * See {@link #PacketSoundManager(ArimLib)} and {@link SOUND_TO_CANCEL}.
	 * 
	 * @param player the player for whom to replace the sounding
	 * @param sound the sound to use as a replacement
	 * @param volume the volume to use for the replacement
	 * @param pitch the pitch to use for the replacement
	 */
	public void replaceDefaultHitSound(Player player, String sound, float volume, float pitch) {
		replacements.put(player.getUniqueId(), new PacketSound(sound, volume, 63F * pitch));
	}
	
}
