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
package space.arim.lib.skript.eff;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import com.pablo67340.guishop.api.GuiShopAPI;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffCollectRevenueAt extends Effect {

	private Expression<Location> location;
	private Expression<Player> player;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		location = (Expression<Location>) exprs[0];
		player = (Expression<Player>) exprs[1];
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk collect revenue at " + location.toString(e, debug) + " for " + player.toString(e, debug);
	}
	
	@Override
	protected void execute(Event e) {
		BlockState state = location.getSingle(e).getBlock().getState();
		if (state instanceof Container) {
			Container container = (Container) state;

			ArrayList<ItemStack> items = new ArrayList<ItemStack>();
			for (ItemStack item : container.getInventory().getContents()) {
				if (GuiShopAPI.canBeSold(item)) {
					items.add(item);
					container.getInventory().remove(item);
				}
			}
			ItemStack[] itemArray = items.toArray(new ItemStack[] {});
			if (itemArray.length == 0) {
				return;
			}

			GuiShopAPI.sellItems(player.getSingle(e), itemArray);
			
		}
		
	}

}
