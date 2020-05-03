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
package space.arim.misc;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import space.arim.lib.api.Game;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class ArimPapiExpansion extends PlaceholderExpansion {
	
	@Override
	public boolean canRegister() {
		return true;
	}
	
	@Override
	public String getAuthor() {
		return "A248";
	}
	
	@Override
	public String getIdentifier() {
		return "arimserver";
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
		switch (identifier) {
		case "game":
			return (offlinePlayer.isOnline())
					? Game.parseGame(offlinePlayer.getPlayer().getWorld().getName()).getTitle()
					: null;
		case "online":
			return Integer.toString(Bukkit.getServer().getOnlinePlayers().size());
		default:
			return null;
		}
	}
	
}
