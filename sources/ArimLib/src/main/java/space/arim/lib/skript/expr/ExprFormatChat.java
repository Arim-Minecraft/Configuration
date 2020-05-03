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

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprFormatChat extends SimpleExpression<String> {
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET){
			return new Class<?>[]{String.class};
		}
		return null;
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(AsyncPlayerChatEvent.class)){
			Skript.error("The arimsk expression 'chat format' may only be used in chat events");
			return false;
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event arg0, boolean arg1) {
		return "arimsk chat format";
	}

	//@SuppressWarnings("null")
	@Override
	@Nullable
	protected String[] get(Event e) {
		return new String[]{convertToFriendly(((AsyncPlayerChatEvent) e).getFormat())};
	}
	
	//delta[0] has to be a String unless Skript has horribly gone wrong
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null){
			return;
		}
		String format = null;
		if (mode == Changer.ChangeMode.SET){
			String newFormat = (String) delta[0];
			if (newFormat == null){
				return;
			}
			format = convertToNormal(newFormat);
		}else if (mode == Changer.ChangeMode.RESET){
			format = "<%s> %s";
		}
		if (format == null){
			return;
		}
		((AsyncPlayerChatEvent) e).setFormat(format);
	}
	
	//@SuppressWarnings({"null"}) //First parameter is marked as @NonNull and String#replaceAll won't return null
	private static String convertToNormal(String format){
		return format.replaceAll("%", "%%")
				.replaceAll("(?i)\\[(player|sender)]", "%1\\$s")
				.replaceAll("(?i)\\[(message|msg)]", "%2\\$s");
	}
	
	//@SuppressWarnings({"null"}) //First parameter is marked as @NonNull and String#replaceAll won't return null
	private static String convertToFriendly(String format){
		format = format.replaceAll("%%", "%")
			.replaceAll("%1\\$s", "[player]")
			.replaceAll("%2\\$s", "[message]");
		//Format uses %s instead of %1$s and %2$s
		if (format.contains("%s")){
			if (StringUtils.countMatches(format, "%s") >= 2){
				// Format uses two %s, the order is player, message
				format = format.replaceFirst("%s", "[player]");
				format = format.replaceFirst("%s", "[message]");
			} else {
				// Format mixes %<number>$s and %s
				format = format.replaceFirst("%s", (format.contains("[player]") || format.contains("%1$s") ? "[message]" : "[player]"));
			}
		}
		return format;
	}

}
