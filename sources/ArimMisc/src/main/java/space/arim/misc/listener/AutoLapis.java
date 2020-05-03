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
package space.arim.misc.listener;

import org.bukkit.DyeColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class AutoLapis implements Listener {

	private final String world;
	
	public AutoLapis(String world) {
		this.world = world;
	}
	
	private ItemStack makeLapis() {
		Dye dye = new Dye();
		dye.setColor(DyeColor.BLUE);
		return dye.toItemStack(3);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onInventoryOpen(InventoryOpenEvent evt) {
		if (evt.getInventory() instanceof EnchantingInventory && evt.getPlayer().getWorld().getName().equalsIgnoreCase(world)) {
			evt.getInventory().setItem(1, makeLapis());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onInventoryClose(InventoryCloseEvent evt) {
		if (evt.getInventory() instanceof EnchantingInventory && evt.getPlayer().getWorld().getName().equalsIgnoreCase(world)) {
			evt.getInventory().setItem(1, null);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	private void onInventoryClick(InventoryClickEvent evt) {
		if (evt.getInventory() instanceof EnchantingInventory && evt.getWhoClicked().getWorld().getName().equalsIgnoreCase(world) && evt.getSlot() == 1) {
			evt.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onEnchant(EnchantItemEvent evt) {
		if (evt.getInventory() instanceof EnchantingInventory && evt.getEnchanter().getWorld().getName().equalsIgnoreCase(world)) {
			evt.getInventory().setItem(1, makeLapis());
		}
	}
	
}
