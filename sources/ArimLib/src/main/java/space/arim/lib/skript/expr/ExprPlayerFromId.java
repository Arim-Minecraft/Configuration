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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprPlayerFromId extends SimpleExpression<Player> {
	private Expression<String> target;
	@Override
	public Class<? extends Player> getReturnType() {
		return Player.class;
	}
	@Override
	public boolean isSingle() {
		return true;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int arg1, Kleenean arg2, ParseResult arg3) {
		target = (Expression<String>) exprs[0];
		return true;
	}
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "arimsk player from  id " + target.toString(event, debug) + ".";
	}
	@Override
	@Nullable
	protected Player[] get(Event evt) {
		try {
			Player p = Bukkit.getPlayer(UUID.fromString(target.getSingle(evt)));
			if (p != null) {
				if (p.isOnline()) {
					return new Player[] {p};
				}
				return null;
			}
		} catch (IllegalArgumentException ex) {return null;}
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getUniqueId().toString().equalsIgnoreCase(target.getSingle(evt))) {
				return new Player[] {player};
			}
		}/**/
		return null;
	}
}
