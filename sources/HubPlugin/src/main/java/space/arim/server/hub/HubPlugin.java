/* 
 * HubPlugin
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * HubPlugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * HubPlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with HubPlugin. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.server.hub;

import org.bukkit.plugin.java.JavaPlugin;

public class HubPlugin extends JavaPlugin {
	
	private HubNPCs npcs;
	
	@Override
	public void onEnable() {
		npcs = new HubNPCs(this);
		npcs.startLoad();
		getServer().getPluginManager().registerEvents(npcs, this);
		getServer().getScheduler().runTaskLater(this, npcs::finishLoad, 4L);
	}
	
	@Override
	public void onDisable() {
		npcs.close();
	}
	
}
