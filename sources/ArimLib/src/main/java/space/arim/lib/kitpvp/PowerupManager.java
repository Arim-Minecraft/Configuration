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

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import space.arim.lib.api.ArimLib;

public class PowerupManager {
	
	private final HashSet<Powerup> powerups = new HashSet<Powerup>();
	private final ArimLib api;
	
	public PowerupManager(ArimLib api) {
		this.api = api;
	}
	
	public void add(String name, Location location, Material item, PotionEffect[] potions, CustomSound...sounds) {
		Hologram holo = HologramsAPI.createHologram(api.center(), api.resetYawPitch(location));
		holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', name));
		Powerup powerup = new Powerup(holo, potions, sounds);
		holo.appendItemLine(new ItemStack(item)).setPickupHandler(powerup);
		powerups.add(powerup);
	}
	
	public void add(String name, Location location, Material item, PotionEffect potion, CustomSound...sounds) {
		add(name, location, item, new PotionEffect[] {potion}, sounds);
	}
	
	public void removeAll() {
		powerups.forEach(Powerup::close);
		powerups.clear();
	}

}
