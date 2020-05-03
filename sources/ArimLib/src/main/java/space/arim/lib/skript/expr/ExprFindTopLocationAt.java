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
package space.arim.lib.skript.expr;

import org.eclipse.jdt.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprFindTopLocationAt extends SimpleExpression<Location> {

	private Expression<Location> location;
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		location = (Expression<Location>) exprs[0];
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk find top location at " + location.toString(e, debug);
	}

	@Override
	@Nullable
	protected Location[] get(Event e) {
		Location current = this.location.getSingle(e);

		Block[] blocks = new Block[4];
		blocks[0] = current.getBlock();
		blocks[1] = findBlockAbove(current, 1);
		blocks[2] = findBlockAbove(current, 2);
		blocks[3] = findBlockAbove(current, 3);

		for (int searches = 0; searches < 10; searches++) {
			if (isSolidBlock(blocks[0]) && blocks[1].getType() == Material.AIR && blocks[2].getType() == Material.AIR
					&& blocks[3].getType() == Material.AIR && blocks[1].getLightFromSky() > 10) {
				return new Location[] {current};
			}
			blocks[0] = blocks[1];
			blocks[1] = blocks[2];
			blocks[2] = blocks[3];
			blocks[3] = findBlockAbove(current, 3);
			current.setY(current.getY() + 1);
		}
		return null;
	}
	
	private Block findBlockAbove(Location loc, int amount) {
		Location result = loc.clone();
		result.setY(result.getY() + amount);
		return result.getBlock();
	}
	
	private boolean isSolidBlock(Block block) {
		Material type = block.getType();
		return type != Material.AIR && type != Material.WATER && type != Material.LAVA && type != Material.SAND
				&& type != Material.GRAVEL;
	}

}
