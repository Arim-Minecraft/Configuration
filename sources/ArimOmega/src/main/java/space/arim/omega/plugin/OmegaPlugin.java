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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.api.util.log.LoggerConverter;

import space.arim.omega.core.AltcheckEntry;
import space.arim.omega.core.BaltopEntry;
import space.arim.omega.core.Omega;
import space.arim.omega.core.OmegaSwiftConomy;
import space.arim.omega.util.BytesUtil;
import space.arim.swiftconomy.core.AbstractSwiftConomy;

public class OmegaPlugin extends JavaPlugin {

	private Omega omega;
	
	@Override
	public void onEnable() {
		omega = new Omega(getDataFolder(), LoggerConverter.get().convert(getLogger()));
		omega.registerWith(this);
		omega.startLoad();
		getServer().getScheduler().runTaskLater(this, omega::finishLoad, 1L);
	}
	
	@Override
	public void onDisable() {
		omega.close();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("pay")) {
			if (args.length >= 2) {
				boolean isPlayer = sender instanceof Player;
				long transaction;
				AbstractSwiftConomy economy;
				try {
					double inputAmount = Double.parseDouble(args[1]);
					if (inputAmount <= 0D && !isPlayer) {
						sendMessage(sender, "&cYou cannot pay negative or zero amounts.");
						return true;
					}
					economy = omega.getEconomy();
					transaction = economy.getArithmetic().fromDouble(inputAmount);
				} catch (NumberFormatException ex) {
					sendMessage(sender, "&cInvalid number: &e" + args[1]);
					return true;
				}
				Player target = null;
				for (Player other : getServer().getOnlinePlayers()) {
					if (other.getName().equalsIgnoreCase(args[0])) {
						target = other;
						break;
					}
				}
				if (target == null) {
					sendMessage(sender, "&6Arim>> &cPlayer &e" + args[0] + "&c is offline.");
					return true;
				}
				if (isPlayer) {
					Boolean result = economy.pay(((Player) sender).getUniqueId(), target.getUniqueId(), transaction);
					if (result == null) {
						sendMessage(sender, "&7You do not have enough money to do this.");
					} else if (result) {
						sendMessage(sender, "&7Transferred &a$" + economy.displayBalance(transaction) + "&7 to " + target.getName());
					} else {
						sendMessage(sender, "&cInvalid state : Concurrency error – no balance stored for online player " + args[0]);
					}
				} else {
					Boolean result = economy.deposit(target.getUniqueId(), transaction);
					if (result == null) {
						sendMessage(sender, "&7Cannot force &e" + args[0] + "&7 into debt.");
					} else if (result) {
						sendMessage(sender, "&7Gave &a$" + economy.displayBalance(transaction) + "&7 to " + target.getName());
					} else {
						sendMessage(sender, "&cInvalid state : Concurrency error – no balance stored for online player " + args[0]);
					}
				}
			} else {
				sendMessage(sender, "&cUsage: /pay &e<player> <amount>&c.");
			}
			return true;
		} else if (command.getName().equals("bal")) {
			OmegaSwiftConomy economy = omega.getEconomy();
			if (args.length >= 1) {
				economy.findOfflineBalance(args[0]).thenAccept((baltopEntry) -> {
					if (baltopEntry == null) {
						sendMessage(sender, "&6Arim>> &cPlayer &e" + args[0] + "&c has never been online.");
					} else {
						sendMessage(sender, "&7Balance for &e" + baltopEntry.getName() + "&7 is &a$" + economy.displayBalance(baltopEntry.getBalance()));
					}
				});
			} else if (sender instanceof Player) {
				sendMessage(sender, "&7Your balance is &a$" + economy.displayBalance(omega.getPlayer((Player) sender).getStats().getCurrentBalance()));
			} else {
				sendMessage(sender, "&cSpecify a player.");
			}
			return true;
		} else if (command.getName().equals("omegabaltop")) {
			omega.getEconomy().getTopBalances().thenAccept((entries) -> {
				int position = 0;
				for (BaltopEntry entry : entries) {
					sendMessage(sender, "&3" + ++position + "&7. " + entry.getName() + " &a$" + omega.getEconomy().displayBalance(entry.getBalance()));
				}
			});
			return true;
		} else if (command.getName().equals("altcheck")) {
			if (sender.hasPermission("arim.helper")) {
				if (args.length >= 1) {
					omega.findPlayerInfoWithIPs(args[0]).thenAccept((info) -> {
						if (info == null) {
							sendMessage(sender, "&6Arim>> &cPlayer &e" + args[0] + "&c not found.");
							return;
						}
						omega.conductAltcheck(info).thenAcceptAsync((map) -> {
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
	
	private void sendMessage(CommandSender sender, String message) {
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
