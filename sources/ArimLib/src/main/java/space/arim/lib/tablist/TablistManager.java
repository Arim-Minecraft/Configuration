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
package space.arim.lib.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import space.arim.lib.api.ArimLib;

import pl.kacperduras.protocoltab.manager.TabManager;

public class TablistManager {

	private volatile int online;
	
	final TabManager lib;
	
	public TablistManager(ArimLib api) {
		api.server().getScheduler().runTaskTimer(api.center(), () -> {
			setOnlineThreadSafe(Bukkit.getOnlinePlayers().size());
		}, 79L, 79L);
		lib = new TabManager(api.center());
		api.server().getScheduler().runTaskAsynchronously(api.center(), new TablistRunnable(this));
	}
	
	private void addPlayerToMap(@SuppressWarnings("unused") Player player) {
		
	}
	
	public void addTablist(Player player) {
		addPlayerToMap(player);
		lib.get(player);
	}
	
	private void setOnlineThreadSafe(int online) {
		this.online = online;
	}
	
	public int getOnlineThreadSafe() {
		return online;
	}
	
	public void removeTablist(Player player) {
		lib.remove(player);
	}
	
}
