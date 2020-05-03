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

import java.util.Objects;

class PacketSound {

	static final PacketSound EMPTY = new PacketSound(null, 0F, 0F);
	
	final String sound;
	final float volume;
	final float pitch;
	
	PacketSound(String sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(pitch);
		result = prime * result + ((sound == null) ? 0 : sound.hashCode());
		result = prime * result + Float.floatToIntBits(volume);
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object instanceof PacketSound) {
			PacketSound other = (PacketSound) object;
			return Objects.equals(sound, other.sound) && Float.floatToIntBits(pitch) == Float.floatToIntBits(other.pitch)
					&& Float.floatToIntBits(volume) == Float.floatToIntBits(other.volume);
		}
		return false;
	}
	
}
