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
package space.arim.lib.skript.cond;

import org.eclipse.jdt.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class CondFilterMsg extends Condition {

	private Expression<String> msg;
	private Expression<String> filters;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		msg = (Expression<String>) exprs[0];
		filters = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk filter " + msg.toString(e, debug) + " by " + filters.toString(e, debug);
	}
	
	private static int editDistance(String s1, String s2) {
		
		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0) {
					costs[j] = j;
				} else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						}
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0) {
				costs[s2.length()] = lastValue;
			}
		}
		return costs[s2.length()];
	}
	
	/**
	 * should be applied for nonempty, lowercase strings
	 * 
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return the similarity between them
	 */
	private static double similarity(String s1, String s2) {
		String longer = s1;
		String shorter = s2;
		if (s1.length() < s2.length()) { // longer should always have greater length
			longer = s2;
			shorter = s1;
		}
		int longerLength = longer.length();
		return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

	}
	
	@Override
	public boolean check(Event e) {
		String msg = this.msg.getSingle(e).toLowerCase();
		for (String filter : filters.getArray(e)) {
			filter = filter.toLowerCase();
			if (msg.equals(filter)) {
				return true;
			} else if (msg.length() > filter.length()) {
				for (String word : msg.split(" ")) {
					if (msg.equals(word)) {
						return true;
					}
					double percentToMatch = (70D + 60D/(word.length()+5D))/100D;
					if (similarity(word.toLowerCase(), filter.toLowerCase()) >= percentToMatch) {
						return true;
					}
				}
				String msgNoSpaces = msg.replace(" ", "");
				if (msgNoSpaces.equals(filter)) {
					return true;
				} else if (msgNoSpaces.length() > filter.length()) {
					for (int n = filter.length(); n < msgNoSpaces.length(); n++) {
						String subWord = msgNoSpaces.substring(n - filter.length(), n);
						if (subWord.equals(filter)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
