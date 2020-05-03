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
package space.arim.lib.api;

public enum Game {
	
	KitPvP("&4&lKit PvP"),
	TntWars("&e&lTnT Wars"),
	SkyFactions("&9&lSkyFactions"),
	Survival("&a&lAnarchy &r&2&lSurvival"),
	Combo("&5&lCombo"),
	Duel("&b&lDuel Battle"),
	Hub("&6&lArim"),
	Other("&6&lArim");
	
	private final String title;
	
	private Game(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static Game parseGame(String world) {
		if (world.equalsIgnoreCase("KitPvP") || world.equalsIgnoreCase("FFA") || world.equalsIgnoreCase("KitPvP3")) {
			return Game.KitPvP;
		} else if (world.equalsIgnoreCase("SkyFactions")) {
			return Game.SkyFactions;
		} else if (world.equalsIgnoreCase("Combo")) {
			return Game.Combo;
		} else if (world.equalsIgnoreCase("TntWar")) {
			return Game.TntWars;
		} else if (world.equalsIgnoreCase("Hub")) {
			return Game.Hub;
		} else if (world.equalsIgnoreCase("Survival") || world.equalsIgnoreCase("Survival_nether")
				|| world.equalsIgnoreCase("Survival_the_end")) {
			return Game.Survival;
		} else if (world.equalsIgnoreCase("Duel")) {
			return Game.Duel;
		}
		return Game.Other;
	}
	
	public static Game fromString(String input) {
		for (Game game : Game.values()) {
			if (game.toString().equalsIgnoreCase(input)) {
				return game;
			}
		}
		return null;
	}
}
