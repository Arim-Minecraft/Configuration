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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import space.arim.lib.util.NMS;

public class SilkTouchSpawners implements Listener {

	private final String world;
	
	public SilkTouchSpawners(String world) {
		this.world = world;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onBlockBreak(BlockBreakEvent evt) {

		Block block = evt.getBlock();
		if (block.getWorld().getName().equalsIgnoreCase(world) && block.getType() == Material.MOB_SPAWNER
				&& evt.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {

			((Item) block.getWorld().spawnEntity(evt.getBlock().getLocation(), EntityType.DROPPED_ITEM))
					.setItemStack(NMS.createSpawnerItem(((CreatureSpawner) block.getState()).getSpawnedType()));
		}
	}
	
}
