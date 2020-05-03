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

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.handler.PickupHandler;

public class Powerup implements PickupHandler {
	
	private final Hologram hologram;
	private final PotionEffect[] potions;
	private final CustomSound[] sounds;
	
	public Powerup(Hologram hologram, PotionEffect[] potions, CustomSound...sounds) {
		this.hologram = hologram;
		this.potions = potions;
		this.sounds = sounds;
	}
	
	@Override
	public void onPickup(Player target) {
		if (!hologram.isDeleted()) {
			for (CustomSound sound : sounds) {
				sound.play(target, target.getLocation());
			}
			for (PotionEffect potion : potions) {
				target.addPotionEffect(potion);
			}
			hologram.delete();
		}
	}
	
	void close() {
		if (!hologram.isDeleted()) {
			hologram.delete();
		}
	}

}
