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
package space.arim.lib.skript.eff;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffCustomSound extends Effect {

	private Expression<String> sound;
	private Expression<Number> volume;
	private Expression<Number> pitch;
	private Expression<Location> location;
	private Expression<Player> targets;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int arg1, Kleenean arg2, ParseResult arg3) {
		sound = (Expression<String>) exprs[0];
		targets = (Expression<Player>) exprs[1];
		location = (Expression<Location>) exprs[2];
		volume = (Expression<Number>) exprs[3];
		pitch = (Expression<Number>) exprs[4];
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "arimsk play custom sound " + sound.toString(event, debug) + " for " + targets.toString(event, debug)
				+ " at " + location.toString(event, debug) + " with volume " + volume.toString(event, debug) + " pitch "
				+ pitch.toString(event, debug) + ".";
	}

	@Override
	protected void execute(Event evt) {
		for (Player target : targets.getArray(evt)) {
			target.playSound(location.getSingle(evt), Sound.valueOf(sound.getSingle(evt)), volume.getSingle(evt).floatValue(),
					pitch.getSingle(evt).floatValue());
		}
	}

}
