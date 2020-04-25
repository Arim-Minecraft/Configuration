/* 
 * ServerStarter
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ServerStarter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ServerStarter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ServerStarter. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.serverstarter;

import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerStarter extends JavaPlugin implements Listener {

	public static volatile boolean open = false;
	public static volatile String message = ChatColor.translateAlternateColorCodes('&', "&3&lServer starting, please wait...");
	public static volatile Consumer<AsyncPlayerPreLoginEvent> afterAllowed;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onConnect(AsyncPlayerPreLoginEvent evt) {
		if (!open) {
			getLogger().info("Blocked connection from " + evt.getName() + " (" + evt.getAddress().getHostAddress() + ") since the server is still starting.");
			evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
		} else if (afterAllowed != null) {
			afterAllowed.accept(evt);
		}
	}
	
}
