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

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprDamageDelay extends SimpleExpression<Number> {

	private Expression<Entity> entity;
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entity = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk max damage delay for " + entity.toString(e, debug);
	}

	@Override
	@Nullable
	protected Number[] get(Event e) {
		Entity entity = this.entity.getSingle(e);
		return (entity instanceof LivingEntity) ? new Number[] {((LivingEntity) entity).getMaximumNoDamageTicks()} : null;
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return (mode == ChangeMode.SET) ? new Class<?>[] {Number.class} : null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		Entity entity = this.entity.getSingle(e);
		if (entity instanceof LivingEntity && mode == ChangeMode.SET) {
			((LivingEntity) entity).setMaximumNoDamageTicks(((Number) delta[0]).intValue());
		}
	}

}
