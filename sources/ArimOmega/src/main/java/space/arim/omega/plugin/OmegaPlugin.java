/* 
 * ArimOmega
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ArimOmega is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ArimOmega is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ArimOmega. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.omega.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import space.arim.api.util.log.LoggerConverter;

import space.arim.omega.core.Omega;

public class OmegaPlugin extends JavaPlugin {

	private static volatile Omega omega;
	
	@Override
	public void onEnable() {
		omega = new Omega(this, LoggerConverter.get().convert(getLogger()));
		omega.startLoad();
		getServer().getScheduler().runTaskLater(this, omega::finishLoad, 1L);
	}
	
	@Override
	public void onDisable() {
		omega.close();
	}
	
	public static Omega get() {
		return omega;
	}
	
}
