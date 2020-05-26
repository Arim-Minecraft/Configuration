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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import space.arim.swiftconomy.api.SwiftConomy;

import space.arim.omega.core.BaltopEntry;
import space.arim.omega.core.OmegaSwiftConomy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EconomyCommands implements CommandExecutor {

	private final OmegaPlugin plugin;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("pay")) {
			if (args.length >= 2) {
				boolean isPlayer = sender instanceof Player;
				long transaction;
				SwiftConomy economy;
				try {
					double inputAmount = Double.parseDouble(args[1]);
					if (inputAmount <= 0D && isPlayer) {
						plugin.sendMessage(sender, "&cYou cannot pay negative or zero amounts.");
						return true;
					}
					economy = plugin.omega.getEconomy();
					transaction = economy.getArithmetic().fromDouble(inputAmount);
				} catch (NumberFormatException ex) {
					plugin.sendMessage(sender, "&cInvalid number: &e" + args[1]);
					return true;
				}
				Player target = plugin.getServer().getPlayer(args[0]);
				if (target == null) {
					plugin.sendMessage(sender, "&6Arim>> &cPlayer &e" + args[0] + "&c is offline.");
					return true;
				}
				if (isPlayer) {
					Boolean result = economy.pay(((Player) sender).getUniqueId(), target.getUniqueId(), transaction);
					if (result == null) {
						plugin.sendMessage(sender, "&7You do not have enough money to do this.");
					} else if (result) {
						plugin.sendMessage(sender, "&7Transferred &a" + economy.displayBalance(transaction) + "&7 to " + target.getName());
					} else {
						plugin.sendMessage(sender, "&cInvalid state : Concurrency error – no balance stored for online player " + args[0]);
					}
				} else {
					Boolean result = economy.deposit(target.getUniqueId(), transaction);
					if (result == null) {
						plugin.sendMessage(sender, "&7Cannot force &e" + args[0] + "&7 into debt.");
					} else if (result) {
						plugin.sendMessage(sender, "&7Gave &a" + economy.displayBalance(transaction) + "&7 to " + target.getName());
					} else {
						plugin.sendMessage(sender, "&cInvalid state : Concurrency error – no balance stored for online player " + args[0]);
					}
				}
			} else {
				plugin.sendMessage(sender, "&cUsage: /pay &e<player> <amount>&c.");
			}
			return true;
		} else if (command.getName().equals("bal")) {
			OmegaSwiftConomy economy = plugin.omega.getEconomy();
			if (args.length >= 1) {
				economy.findOfflineBalance(args[0]).thenAccept((baltopEntry) -> {
					if (baltopEntry == null) {
						plugin.sendMessage(sender, "&6Arim>> &cPlayer &e" + args[0] + "&c has never been online.");
					} else {
						plugin.sendMessage(sender, "&7Balance for &e" + baltopEntry.getName() + "&7 is &a" + economy.displayBalance(baltopEntry.getBalance()));
					}
				});
			} else if (sender instanceof Player) {
				plugin.sendMessage(sender, "&7Your balance is &a" + economy.displayBalance(plugin.omega.getPlayer((Player) sender).getStats().getCurrentBalance()));
			} else {
				plugin.sendMessage(sender, "&cSpecify a player.");
			}
			return true;
		} else if (command.getName().equals("baltop")) {
			plugin.omega.getEconomy().getTopBalances().thenAccept((entries) -> {
				int position = 0;
				for (BaltopEntry entry : entries) {
					plugin.sendMessage(sender, "&3" + ++position + "&7. " + entry.getName()
						+ " &a" + plugin.omega.getEconomy().displayBalance(entry.getBalance()));
				}
			});
			return true;
		}
		return false;
	}
	
}
