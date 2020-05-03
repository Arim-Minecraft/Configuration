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
package space.arim.lib.skript.eff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import space.arim.lib.ArimLibPlugin;

class SkyFacBaseCreator {

	private static final int bridgeYLevel = 94;
	
	private final Location destination;
	private final int centerX;
	private final int centerZ;
	private final World world;
	private final HashMap<Location, Material> totalBlockEdits;
	private final ArrayList<HashMap<Location, Material>> subMaps;
	private volatile int maxIndex;
	private final int yLevel;
	private Location chestLoc;
	private final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
	
	SkyFacBaseCreator(Location location) {
		destination = location;
		centerX = location.getBlockX();
		centerZ = location.getBlockZ();
		world = location.getWorld();
		totalBlockEdits = new HashMap<>();
		subMaps = new ArrayList<>();
		yLevel = bridgeYLevel + ThreadLocalRandom.current().nextInt(21) - 5;
	}
	
	private Material getRandomHazardousMaterial() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int result = random.nextInt(10);
		if (result > 2) {
			return Material.GRASS;
		}
		return (random.nextBoolean()) ? Material.SAND : Material.GRAVEL;
	}
	
	private void calculateBridges() {
		double x = 15.5;
		double z = 15.5;
		double startX = x;
		double startZ = z;
		
		do {
			totalBlockEdits.put(new Location(world, x + centerX, bridgeYLevel, z + centerZ), getRandomHazardousMaterial());
			if (x == 15.5) {
				if (z > -15.5) {
					z--;
				} else {
					x--;
				}
			} else if (x == -15.5) {
				if (z < 15.5) {
					z++;
				} else {
					x++;
				}
			} else if (z == 15.5) {
				x++;
			} else if (z == -15.5) {
				x--;
			}
		} while (x != startX && z != startZ);
	}
	
	private Location setBlockXYZ(double offsetX, int offsetY, double offsetZ, Material material) {
		Location loc = new Location(world, centerX + offsetX, yLevel + offsetY, centerZ + offsetZ);
		totalBlockEdits.put(loc, material);
		return loc;
	}
	
	private Location setBlock(double offsetX, double offsetZ, Material material) {
		return setBlockXYZ(offsetX, 0, offsetZ, material);
	}
	
	private Location setGrass0(double offsetX, double offsetZ) {
		return setBlockXYZ(offsetX, 0, offsetZ, Material.GRASS);
	}
	
	private Location setGrass1(double offsetX, double offsetZ) {
		return setBlockXYZ(offsetX, -1, offsetZ, Material.DIRT);
	}
	
	private Location setGrass2(double offsetX, double offsetZ) {
		return setBlockXYZ(offsetX, -2, offsetZ, Material.DIRT);
	}
	
	private Location setGrass3(double offsetX, double offsetZ) {
		return setBlockXYZ(offsetX, -3, offsetZ, Material.DIRT);
	}
	
	private Location setBlockUp(double offsetX, double offsetZ, Material material) {
		return setBlockXYZ(offsetX, 1, offsetZ, material);
	}
	
	private void createIsland() {
		// main bedrock
		setBlock(0.5, 0.5, Material.BEDROCK);
		setBlock(-0.5, 0.5, Material.BEDROCK);
		setBlock(0.5, -0.5, Material.BEDROCK);
		setBlock(-0.5, -0.5, Material.BEDROCK);
		
		// top grass layer
		setGrass0(-2.5, 2.5);setGrass0(-1.5, 2.5);setGrass0(-0.5, 2.5);setGrass0(0.5, 2.5);setGrass0(1.5, 2.5);setGrass0(2.5, 2.5);
		setGrass0(-2.5, 1.5);setGrass0(-1.5, 1.5);setGrass0(-0.5, 1.5);setGrass0(0.5, 1.5);setGrass0(1.5, 1.5);setGrass0(2.5, 1.5);
		setGrass0(-2.5, 0.5);setGrass0(-1.5, 0.5);										   setGrass0(1.5, 0.5);setGrass0(2.5, 0.5);
		setGrass0(-2.5, -0.5);setGrass0(-1.5, -0.5);									   setGrass0(1.5, -0.5);setGrass0(2.5, -0.5);
		setGrass0(-2.5, -1.5);setGrass0(-1.5, -1.5);setGrass0(-0.5, -1.5);setGrass0(0.5, -1.5);setGrass0(1.5, -1.5);setGrass0(2.5, -1.5);
		setGrass0(-2.5, -2.5);setGrass0(-1.5, -2.5);setGrass0(-0.5, -2.5);setGrass0(0.5, -2.5);setGrass0(1.5, -2.5);setGrass0(2.5, -2.5);
		
		// grass layer down 1
							 setGrass1(-1.5, 2.5);setGrass1(-0.5, 2.5);setGrass1(0.5, 2.5);setGrass1(1.5, 2.5);
		setGrass1(-2.5, 1.5);setGrass1(-1.5, 1.5);setGrass1(-0.5, 1.5);setGrass1(0.5, 1.5);setGrass1(1.5, 1.5);setGrass1(2.5, 1.5);
		setGrass1(-2.5, 0.5);setGrass1(-1.5, 0.5);										   setGrass1(1.5, 0.5);setGrass1(2.5, 0.5);
		setGrass1(-2.5, -0.5);setGrass1(-1.5, -0.5);									   setGrass1(1.5, -0.5);setGrass1(2.5, -0.5);
		setGrass1(-2.5, -1.5);setGrass1(-1.5, -1.5);setGrass1(-0.5, -1.5);setGrass1(0.5, -1.5);setGrass1(1.5, -1.5);setGrass1(2.5, -1.5);
							setGrass1(-1.5, -2.5);setGrass1(-0.5, -2.5);setGrass1(0.5, -2.5);setGrass1(1.5, -2.5);
		
		// grass layer down 2
							 					  setGrass2(-0.5, 2.5);setGrass2(0.5, 2.5);
							 setGrass2(-1.5, 1.5);setGrass2(-0.5, 1.5);setGrass2(0.5, 1.5);setGrass2(1.5, 1.5);
		setGrass2(-2.5, 0.5);setGrass2(-1.5, 0.5);										   setGrass2(1.5, 0.5);setGrass2(2.5, 0.5);
		setGrass2(-2.5, -0.5);setGrass2(-1.5, -0.5);									   setGrass2(1.5, -0.5);setGrass2(2.5, -0.5);
							  setGrass2(-1.5, -1.5);setGrass2(-0.5, -1.5);setGrass2(0.5, -1.5);setGrass2(1.5, -1.5);
													setGrass2(-0.5, -2.5);setGrass2(0.5, -2.5);
													
		
		// filling in some space
		setGrass1(0.5, 0.5);
		setGrass1(-0.5, 0.5);
		setGrass1(-0.5, -0.5);
		setGrass1(0.5, -0.5);
		setGrass2(0.5, 0.5);
		setGrass2(-0.5, 0.5);
		setGrass2(-0.5, -0.5);
		setGrass2(0.5, -0.5);
		
		// grass layer down 3
		setGrass3(0.5, 0.5);
		setGrass3(-0.5, 0.5);
		setGrass3(-0.5, -0.5);
		setGrass3(0.5, -0.5);

		setBlockUp(1.5, -1.5, Material.SAPLING);
		setBlockUp(-1.5, 1.5, Material.TORCH);
		setBlockUp(-1.5, -1.5, Material.WORKBENCH);

		chestLoc = setBlockUp(1.5, 1.5, Material.CHEST);
		// chest contents
		items.add(new ItemStack(Material.WATER_BUCKET));
		items.add(new ItemStack(Material.LAVA_BUCKET));
		items.add(new ItemStack(Material.BREAD, 12));
		items.add(new ItemStack(Material.STONE_PICKAXE));
		items.add(new ItemStack(Material.STONE_SWORD));
		items.add(new ItemStack(Material.BOW));
		items.add(new ItemStack(Material.ARROW, 16));
		items.add(new ItemStack(Material.BRICK, 3));
		items.add(new ItemStack(Material.WOOD, 7));
		items.add(new ItemStack(Material.LEATHER_CHESTPLATE));
		items.add(new ItemStack(Material.LEATHER_BOOTS));
		items.add(new ItemStack(Material.INK_SACK, 2));
		items.add(new ItemStack(Material.BONE, 5));
	}
	
	private void placeBlocksFromSynced(int index) {
		Map<Location, Material> blocks = subMaps.get(index);
		blocks.forEach((loc, mat) -> loc.getBlock().setType(mat, false));
	}
	
	private void afterCompletionSynced(Player player) {
		Chest chest = (Chest) chestLoc.getBlock().getState();
		chest.getInventory().addItem(items.toArray(new ItemStack[] {}));
		player.teleport(destination, TeleportCause.PLUGIN);
	}
	
	private void doPlacementSynced(Player player, int index) {
		placeBlocksFromSynced(index);
		int andThen = (index + 1);
		if (andThen == maxIndex) {
			afterCompletionSynced(player);
		} else {
			schedulePlacement(player, andThen);
		}
	}
	
	private void schedulePlacement(Player player, final int index) {
		Bukkit.getServer().getScheduler().runTaskLater(ArimLibPlugin.inst().center(), () -> doPlacementSynced(player, index), 2L);
	}
	
	void create(Player player) {
		calculateBridges();
		createIsland();
		
		// split up the map into many smaller submaps
		// objective: 10 blocks per tick every 2 ticks
		int n = 0;
		HashMap<Location, Material> current = new HashMap<>();
		for (Map.Entry<Location, Material> entry : totalBlockEdits.entrySet()) {

			if (++n == 10) { // reset the counter at 10 blocks
				subMaps.add(current);
				current = new HashMap<>();
				n = 0;
			}
			current.put(entry.getKey(), entry.getValue());
		}
		maxIndex = subMaps.size();
		
		schedulePlacement(player, 0);
	}
	
}
