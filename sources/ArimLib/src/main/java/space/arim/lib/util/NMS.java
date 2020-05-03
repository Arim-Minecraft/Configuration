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
package space.arim.lib.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public final class NMS {

	static final String NMS_VERSION;
    static final String OBC_PACKAGE = "org.bukkit.craftbukkit.";
    static final String NMS_PACKAGE = "net.minecraft.server.";
	
	static {
		String packageName = Bukkit.getServer().getClass().getPackage().getName(); // e.g. "org.bukkit.craftbukkit.v1_8_R3"
		NMS_VERSION = packageName.substring(OBC_PACKAGE.length());
	}
	
    public static Class<?> nmsClass(String className) throws ClassNotFoundException {
        return Class.forName(NMS_PACKAGE  + NMS_VERSION + '.' + className);
    }
    
    public static Class<?> nmsClassNullable(String className) {
    	try {
    		return nmsClass(className);
    	} catch (ClassNotFoundException ex) {
    		return null;
    	}
    }
    
    public static Class<?> obcClass(String className) throws ClassNotFoundException {
    	return Class.forName(OBC_PACKAGE + NMS_VERSION + '.' + className);
    }
    
    public static Class<?> obcClassNullable(String className) {
    	try {
    		return obcClass(className);
    	} catch (ClassNotFoundException ex) {
    		return null;
    	}
    }
	
	public static void setStuckArrows(Player target, Byte amount) {
		((CraftPlayer) target).getHandle().getDataWatcher().watch(9, amount);
	}
	
	public static void sendActionBar(Player target, String message) {
        ((CraftPlayer) target).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(
        		ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', message.replace("\"", "")) + "\"}"), (byte) 2));
	}

    public static ItemStack createSpawnerItem(EntityType entity) {
    	 net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(new ItemStack(Material.MOB_SPAWNER));
         NBTTagCompound cmp = nmsItem.getTag();
         
         if (cmp == null) {
        	 cmp = new NBTTagCompound();
        	 nmsItem.setTag(cmp);
         }
         cmp.setString("GUIShopSpawner", entity.name());
         return CraftItemStack.asCraftMirror(nmsItem);
    }

    public static EntityType getSpawnerType(ItemStack item) {

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound cmp = nmsItem.getTag();

		return (cmp != null && cmp.hasKey("GUIShopSpawner")) ? EntityType.valueOf(cmp.getString("GUIShopSpawner"))
				: null;
    }
	
}
