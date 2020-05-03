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
package space.arim.lib.skript.expr;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprNewLocation extends SimpleExpression<Location> {
	private Expression<Number> x;
	private Expression<Number> y;
	private Expression<Number> z;
	private Expression<String> world;
	private Expression<Number> yaw;
	private Expression<Number> pitch;
	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
	@Override
	public boolean isSingle() {
		return true;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int arg1, Kleenean arg2, ParseResult arg3) {
		x = (Expression<Number>) exprs[0];
		y = (Expression<Number>) exprs[1];
		z = (Expression<Number>) exprs[2];
		world = (Expression<String>) exprs[3];
		yaw = (Expression<Number>) exprs[4];
		pitch = (Expression<Number>) exprs[5];
		return true;
	}
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "arimlib new location from " + x.toString(event, debug) + ", " + y.toString(event, debug) + ", " + z.toString(event, debug) + " in world " + world.toString(event, debug) + " with yaw " + yaw.toString(event, debug) + ", pitch " + pitch.toString(event, debug) + ".";
	}
	@Override
	@Nullable
	protected Location[] get(Event evt) {
		if (Bukkit.getWorld(world.getSingle(evt)) != null) {
			return new Location[] {new Location(Bukkit.getWorld(world.getSingle(evt)), x.getSingle(evt).doubleValue(), y.getSingle(evt).doubleValue(), z.getSingle(evt).doubleValue(), yaw.getSingle(evt).floatValue(), pitch.getSingle(evt).floatValue())};
		}
		return null;
	}
	
}
