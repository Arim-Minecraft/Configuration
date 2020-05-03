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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import space.arim.lib.ArimLibPlugin;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprPlayerVanishability extends SimpleExpression<Boolean> {

	private Expression<Player> player;
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		player = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk vanishability " + player.toString(e, debug);
	}

	@Override
	@Nullable
	protected Boolean[] get(Event e) {
		return new Boolean[] {ArimLibPlugin.inst().vanisher().isVanish(player.getSingle(e))};
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return (mode == ChangeMode.SET) ? new Class<?>[] {Boolean.class} : null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			if ((boolean) delta[0]) {
				ArimLibPlugin.inst().vanisher().vanish(player.getSingle(e));
			} else {
				ArimLibPlugin.inst().vanisher().unvanish(player.getSingle(e));
			}
		}
	}

}
