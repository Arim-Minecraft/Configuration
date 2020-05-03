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

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerPickupItemEvent;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprItemPickedUp extends SimpleExpression<Item> {

	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Item> getReturnType() {
		return Item.class;
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(PlayerPickupItemEvent.class)) {
			Skript.error("The arimsk expression 'item-picked-up' may only be used in item drop events");
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk item-picked-up";
	}
	
	@Override
	@Nullable
	protected Item[] get(Event evt) {
		return new Item[] {((PlayerPickupItemEvent) evt).getItem()};
	}
	
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.RESET || mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE_ALL) ? new Class<?>[] {Boolean.class} : null;
	}
	
	@Override
	public void change(Event evt, Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE_ALL) {
			((PlayerPickupItemEvent) evt).getItem().remove();
		}
	}

}
