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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.api.util.log.LoggerConverter;

import space.arim.omega.core.AltcheckEntry;
import space.arim.omega.core.Omega;
import space.arim.omega.util.BytesUtil;

public class OmegaPlugin extends JavaPlugin {

	Omega omega;
	
	@Override
	public void onEnable() {
		omega = new Omega(getDataFolder(), LoggerConverter.get().convert(getLogger()));
		omega.registerWith(this);
		omega.startLoad();
		getServer().getScheduler().runTaskLater(this, omega::finishLoad, 1L);
		EconomyCommands ecoCmds = new EconomyCommands(this);
		getServer().getPluginCommand("pay").setExecutor(ecoCmds);
		getServer().getPluginCommand("bal").setExecutor(ecoCmds);
		getServer().getPluginCommand("baltop").setExecutor(ecoCmds);
	}
	
	@Override
	public void onDisable() {
		omega.close();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("altcheck")) {
			if (sender.hasPermission("arim.helper")) {
				if (args.length >= 1) {
					omega.findPlayerInfoWithIPs(args[0]).thenCompose((info) -> {
						return (info == null) ? CompletableFuture.completedFuture(null) : omega.conductAltcheck(info);

					}).thenAccept((map) -> {
						if (map == null) {
							sendMessage(sender, "&6Arim>> &cPlayer &e" + args[0] + "&c not found.");
							return;
						}
						boolean found = false;
						for (Map.Entry<Byte[], Set<AltcheckEntry>> entry : map.entrySet()) {
							Set<AltcheckEntry> matches = entry.getValue();
							if (matches == null) {
								continue;
							}
							try {
								String address = InetAddress.getByAddress(BytesUtil.unboxAll(entry.getKey())).getHostAddress();
								StringBuilder builder = new StringBuilder();
								for (AltcheckEntry match : matches) {
									builder.append(',').append(match.getName());
								}
								sendMessage(sender, "&7IP: &e" + address + "&7. Players: " + builder.substring(0) + ".");
								found = true;
							} catch (UnknownHostException ex) {
								ex.printStackTrace();
								sendMessage(sender, "&6Arim>> &cInternal error, check server console.");
							}
						}
						if (!found) {
							sendMessage(sender, "&6Arim>> &cNo other players have matching IP address to &e" + args[0] + "&c.");
						}
					});
				} else {
					sendMessage(sender, "&6Arim>> &cUsage: /altcheck &e<player>&c.");
				}
			} else {
				sendMessage(sender, "&6Arim>> &cSorry, you cannot use this.");
			}
			return true;
		}
		return false;
	}
	
	void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	/**
	 * Gets the instance
	 * 
	 * @return the instance
	 */
	public static Omega get() {
		return JavaPlugin.getPlugin(OmegaPlugin.class).omega;
	}
	
}
