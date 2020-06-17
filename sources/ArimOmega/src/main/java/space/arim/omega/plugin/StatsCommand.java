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

import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import space.arim.omega.core.OmegaPlayer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StatsCommand implements CommandExecutor, Listener {

	private final OmegaPlugin plugin;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length >= 1) {
				OmegaPlayer subject = plugin.omega.getPlayerByName(args[0]);
				if (subject == null) {
					plugin.sendMessage(sender, "&6Arim>> &cPlayer &e" + args[0] + "&c is offline.");
					return true;
				}
				if (args.length >= 2) {
					switch (args[1].toLowerCase()) {
					case "kitpvp":
						showKitPvPStats(player, subject);
						break;
					case "combo":
						showComboStats(player, subject);
						break;
					case "duel":
						showDuelStats(player, subject);
						break;
					default:
						showMainStats(player, subject);
						break;
					}
				} else {
					showMainStats(player, subject);
				}
			} else {
				showMainStats(player, plugin.omega.getPlayer(player));
			}
		} else {
			plugin.sendMessage(sender, "&cPlayers only.");
		}
		return true;
	}
	
	private void showGui(Player player, int rows, String title, SettablePane pane) {
		Gui gui = new Gui(plugin, rows, ChatColor.translateAlternateColorCodes('&', title));
		gui.addPane(pane);
		gui.setOnGlobalClick((evt) -> evt.setCancelled(true));
		gui.show(player);
	}
	
	private void showMainStats(Player player, final OmegaPlayer subject) {
		SettablePane pane = new SettablePane(9, 6);
		showGui(player, 6, "&6&l" + subject.getName() + "'s &aStats", pane);
	}
	
	private void showKitPvPStats(Player player, final OmegaPlayer subject) {
		SettablePane pane = new SettablePane(9, 6);
		showGui(player, 6, "&6&l" + subject.getName() + "'s &aKitPvP Stats", pane);
	}
	
	private void showComboStats(Player player, final OmegaPlayer subject) {
		SettablePane pane = new SettablePane(9, 6);
		showGui(player, 6, "&6&l" + subject.getName() + "'s &aCombo Stats", pane);
	}
	
	private void showDuelStats(Player player, final OmegaPlayer subject) {
		SettablePane pane = new SettablePane(9, 6);
		showGui(player, 6, "&6&l" + subject.getName() + "'s &aDuel Stats", pane);
	}
	
	private GuiItem makeGuiItem(Material mat, Consumer<Player> onClick) {
		return new GuiItem(new ItemStack(mat), (onClick == null) ? null : (evt) -> onClick.accept((Player) evt.getWhoClicked()));
	}

}
