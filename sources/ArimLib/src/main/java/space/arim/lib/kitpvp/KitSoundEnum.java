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

import org.bukkit.Sound;

public enum KitSoundEnum {
	SOFT(new CustomSound(Sound.WOOD_CLICK, 1F, 0.8F)),
	LEATHER(new CustomSound(Sound.SHOOT_ARROW, 1F, 2F)),
	GOLD(new CustomSound(Sound.ITEM_BREAK, 1F, 1.8F)),
	CHAINMAIL(new CustomSound(Sound.ITEM_BREAK, 1F, 1.4F)),
	IRON(new CustomSound(Sound.BLAZE_HIT, 1F, 0.7F)),
	DIAMOND(new CustomSound(Sound.BLAZE_HIT, 1F, 0.9F));
	
	private final CustomSound sound;
	
	private KitSoundEnum(CustomSound sound) {
		this.sound = sound;
	}
	
	public CustomSound toSound() {
		return sound;
	}
}
