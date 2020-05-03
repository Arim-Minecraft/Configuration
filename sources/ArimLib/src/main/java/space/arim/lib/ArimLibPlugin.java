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
package space.arim.lib;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import space.arim.lib.api.ArimLib;

public class ArimLibPlugin extends JavaPlugin {

	private static final String[] dependencies = { "ProtocolLib", "Skript", "HolographicDisplays", "Vault" };
	private ArimLib api;
	
	private void requireDependency(String dependency) {
		getLogger().severe("*** EXTREME ERROR ***");
		getLogger().severe("ArimLib shutting down! Reason: Dependency " + dependency + " was not found.");
		getServer().getPluginManager().disablePlugin(this);
	}
	
	private Economy findVaultEconomyHook() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(Economy.class);
		return (economyProvider != null) ? economyProvider.getProvider() : null;
	}
	
	@Override
	public void onEnable() {
		for (String dependency : dependencies) {
			if (!getServer().getPluginManager().isPluginEnabled(dependency)) {
				requireDependency(dependency);
				return;
			}
		}
		Economy econ = findVaultEconomyHook();
		if (econ != null) {
			getLogger().info("Hooked into Vault!");
		} else {
			getLogger().warning("Could not hook into Vault!");
		}
		if (api == null) {
			getLogger().info("Initializing API...");
			api = new ArimLib(this, this.getDataFolder().getPath(), econ);
		} else {
			getLogger().severe("The API was already initialized. Is this a reload?");
		}
		getLogger().info("Finished loading!");
		getServer().getScheduler().runTaskLater(this, api::finishLoad, 1L);
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabling ArimLib! Closing the API...");
		api.close();
		api = null;
		getServer().getScheduler().cancelTasks(this);
	}

	public static ArimLib inst() {
		return JavaPlugin.getPlugin(ArimLibPlugin.class).api;
	}
}
