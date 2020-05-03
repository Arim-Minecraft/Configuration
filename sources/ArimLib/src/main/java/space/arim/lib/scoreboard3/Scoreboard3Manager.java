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
package space.arim.lib.scoreboard3;

import org.bukkit.entity.Player;

import space.arim.lib.api.ArimLib;

public abstract class Scoreboard3Manager {
	
	final ArimLib api;
	
	Scoreboard3Manager(ArimLib api) {
		this.api = api;
	}
	
	public abstract void addBoard(Player player);
	
	public abstract void changeWorld(Player player);
	
	public abstract void removeBoard(Player player);
	
	boolean enabled(Player player) {
		String name = player.getName();
		if (api.skript().isVar("prefs::" + name + "::sidebar", Boolean.class)) {
			return api.skript().getVar("prefs::" + name + "::sidebar");
		}
		return true;
	}
	
	private String skriptVar(String variable) {
		Object skriptVar = api.skript().getVar(variable);
		if (skriptVar instanceof String) {
			return (String) skriptVar;
		}
		return "None";
	}

	private String coordVar(String variable) {
		Object coord = api.skript().getVar(variable);
		if (coord instanceof Number) {
			return Double.toString(Math.floor(((Number) coord).doubleValue()));
		}
		return "0";
	}

	private String statVar(String variable) {
		Object stat = api.skript().getVar(variable);
		if (stat instanceof Number) {
			return api.decimalFormatThreadSafe(((Number) stat).doubleValue());
		}
		return "0";
	}

	String getBarVar(Player target, String variable) {
		// debug("Getting sidebar variable " + variable + " for target " +
		// target.getName() + ".");
		if (variable.equalsIgnoreCase("clan")) {
			return api.getFaction(target.getUniqueId());
		} else if (variable.equalsIgnoreCase("balance")) {
			return api.getBalanceFormatted(target);
		} else if (variable.equalsIgnoreCase("kit")) {
			return skriptVar("cooldown::" + target.getName() + "::kit");
		} else if (variable.equalsIgnoreCase("rank")) {
			return skriptVar("ranks::" + target.getName() + "::rank");
		} else if (variable.equalsIgnoreCase("online")) {
			return Integer.toString(api.center().getServer().getOnlinePlayers().size());
		} else if (variable.equalsIgnoreCase("kitpvp_kills")) {
			return statVar("stats::" + target.getName() + "::kills");
		} else if (variable.equalsIgnoreCase("kitpvp_deaths")) {
			return statVar("stats::" + target.getName() + "::deaths");
		} else if (variable.equalsIgnoreCase("kitpvp_kdr")) {
			return statVar("stats::" + target.getName() + "::kdr");
		} else if (variable.equalsIgnoreCase("combo_kills")) {
			return statVar("combo::stats::" + target.getName() + "::kills");
		} else if (variable.equalsIgnoreCase("combo_deaths")) {
			return statVar("combo::stats::" + target.getName() + "::deaths");
		} else if (variable.equalsIgnoreCase("combo_kdr")) {
			return statVar("combo::stats::" + target.getName() + "::kdr");
		} else if (variable.equalsIgnoreCase("duel_opponent")) {
			return skriptVar("opponent::" + target);
		} else if (variable.equalsIgnoreCase("duel_arena")) {
			return skriptVar("arena::" + target);
		} else if (variable.equalsIgnoreCase("surv_x")) {
			return coordVar("surv::" + target.getName() + "::home::x");
		} else if (variable.equalsIgnoreCase("surv_y")) {
			return coordVar("surv::" + target.getName() + "::home::y");
		} else if (variable.equalsIgnoreCase("surv_z")) {
			return coordVar("surv::" + target.getName() + "::home::z");
		}
		return "None";
	}

	public void close() {
		
	}
	
}
