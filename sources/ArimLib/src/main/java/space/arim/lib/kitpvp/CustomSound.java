/* 
 * ArimLib
 * Copyright © 2019 Anand Beh <https://www.arim.space>
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
package space.arim.lib.kitpvp;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CustomSound {
	private final Sound sound;
	private final float volume;
	private final float pitch;

	public CustomSound(Sound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	public void play(Player target, Location location) {
		target.playSound(location, this.sound, this.volume, this.pitch);
	}
	
	public Sound getSound() {
		return this.sound;
	}
	
	public float getVolume() {
		return this.volume;
	}
	
	public float getPitch() {
		return this.pitch;
	}
	
}
