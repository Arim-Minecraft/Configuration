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
package space.arim.lib.scoreboard3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.tigerhix.lib.scoreboard.ScoreboardLib;
import me.tigerhix.lib.scoreboard.type.Scoreboard;
import me.tigerhix.lib.scoreboard.type.ScoreboardHandler;

import space.arim.lib.api.ArimLib;
import space.arim.lib.api.Game;

/**
 * Scoreboard manager using a global handler, uses TigerHix's ScoreboardLib. <br>
 * <br>
 * Tested and made for 1.8.8, but uses Bukkit API so should work later. <br>
 * Supports 48 chars per line. Definitely not thread safe.
 * 
 * @author A248
 *
 */
public class Scoreboard2Manager extends Scoreboard3Manager implements AutoCloseable {
	
	private final Map<UUID, Scoreboard> boards = new HashMap<UUID, Scoreboard>();
	
	private final ScoreboardHandler handler;
	
	public Scoreboard2Manager(ArimLib api) {
		super(api);
		ScoreboardLib.setPluginInstance(api.center());
		handler = new Handler(this);
	}
	
	@Override
	public void addBoard(Player player) {
		Scoreboard board = ScoreboardLib.createScoreboard(player).setHandler(handler).setUpdateInterval(25L);
		boards.put(player.getUniqueId(), board);
		board.activate();
	}
	
	@Override
	public void changeWorld(Player player) {
		
	}
	
	@Override
	public void removeBoard(Player player) {
		Scoreboard board = boards.remove(player.getUniqueId());
		if (board != null) {
			board.deactivate();
		}
	}
	
	public Map<Integer, String> getLines(Game game, UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		HashMap<Integer, String> lines = new HashMap<Integer, String>();
		// debug("Manager#getLines> Fetching lines for " + game.toString() + "...");
		String clan = getBarVar(player, "clan");
		if (game == Game.KitPvP) {
			// nl
			lines.put(14, "&5&lMoney");
			lines.put(13, "&6" + getBarVar(player, "balance"));
			// nl
			lines.put(11, "&3&lKit");
			lines.put(10, "&l" + getBarVar(player, "kit"));
			// nl
			lines.put(8, "&e&lKills&r&7: " + getBarVar(player, "kitpvp_kills"));
			lines.put(7, "&4&lDeaths&r&7: " + getBarVar(player, "kitpvp_deaths"));
			lines.put(6, "&3&lKDR&r&7: " + getBarVar(player, "kitpvp_kdr"));
			// nl
			lines.put(4, "&c&lRank");
			lines.put(3, getBarVar(player, "rank"));
			// nl
			lines.put(1, "&9Online: &6" + getBarVar(player, "online") + "&r&7/100");
		} else if (game == Game.SkyFactions) {
			// nl
			lines.put(14, "&5&lMoney");
			lines.put(13, "&6" + getBarVar(player, "balance"));
			// nl
			lines.put(11, "&a&lFaction");
			lines.put(10, "&7" + clan);
			// nl
			if (clan.equalsIgnoreCase("None")) {
				lines.put(8, "&7Type &e/create &7to make a faction.");
			} else {
				lines.put(8, "&7Type &e/fac &7for all commands.");
			}
			// nl
			lines.put(6, "&c&lRank");
			lines.put(5, getBarVar(player, "rank"));
			// nl
			lines.put(3, "&9Online");
			lines.put(2, "&6" + getBarVar(player, "online") + "&r&7/100");
			// nl
		} else if (game == Game.Combo) {
			// nl
			lines.put(14, "&5&lMoney");
			lines.put(13, "&6" + getBarVar(player, "balance"));
			// nl
			lines.put(11, "&e&lKills&r&7: " + getBarVar(player, "combo_kills"));
			lines.put(10, "&4&lDeaths&r&7: " + getBarVar(player, "combo_deaths"));
			lines.put(9, "&3&lKDR&r&7: " + getBarVar(player, "combo_kdr"));
			// nl
			lines.put(7, "&c&lRank");
			lines.put(6, getBarVar(player, "rank"));
			// nl
			lines.put(4, "&9Online: &6" + getBarVar(player, "online") + "&r&7/100");
			// nl
			lines.put(2, "&aIP &r:: &a&lArim.Space");
			// nl
			/*
			 * } else if (game == Game.TntWars) { //nl //nl lines.put(13, "&5&lMoney");
			 * lines.put(12, "&6" + getBarVar(player, "balance")); //nl //nl lines.put(9,
			 * "&c&lRank"); lines.put(8, getBarVar(player, "rank")); //nl //nl lines.put(5,
			 * "&9Online"); lines.put(4, "&6" + getBarVar(player, "online") + "&r&7/100");
			 * //nl lines.put(2, "&aIP &r:: &a&lArim.Space"); //n1
			 */
		} else if (game == Game.Hub || game == Game.TntWars || game == Game.Other) {
			// nl
			lines.put(14, "&5&lMoney");
			lines.put(13, "&6" + getBarVar(player, "balance"));
			// nl
			// nl
			lines.put(10, "&c&lRank");
			lines.put(9, getBarVar(player, "rank"));
			// nl
			// nl
			lines.put(6, "&9Online");
			lines.put(5, "&6" + getBarVar(player, "online") + "&r&7/100");
			// nl
			// nl
			lines.put(2, "&aIP &r:: &a&lArim.Space");
			// n1
		} else if (game == Game.Survival) {
			// nl
			lines.put(14, "&5&lMoney");
			lines.put(13, "&6" + getBarVar(player, "balance"));
			// nl
			lines.put(11, "&b&lHome");
			lines.put(10, "&7" + getBarVar(player, "surv_x") + ", " + getBarVar(player, "surv_y") + ", "
					+ getBarVar(player, "surv_z") + ".");
			// nl
			lines.put(8, "&c&lRank");
			lines.put(7, getBarVar(player, "rank"));
			// nl
			lines.put(5, "&9Online");
			lines.put(4, "&6" + getBarVar(player, "online") + "&r&7/100");
			// nl
			lines.put(2, "&aIP &r:: &a&lArim.Space");
			// n1
		} else if (game == Game.Duel) {
			// nl
			lines.put(14, "&5&lMoney");
			lines.put(13, "&6" + getBarVar(player, "balance"));
			// nl
			lines.put(11, "&4&lOpponent");
			lines.put(10, "&7" + getBarVar(player, "duel_opponent"));
			// nl
			lines.put(8, "&3&lArena");
			lines.put(7, "&7" + getBarVar(player, "duel_arena"));
			// nl
			lines.put(5, "&c&lRank");
			lines.put(4, getBarVar(player, "rank"));
			// nl
			lines.put(2, "&9Online: &6" + getBarVar(player, "online") + "&r&7/100");
			// n1
			/*
			 * } else if (game == Game.Other) { //nl //nl lines.put(13, "&5&lMoney");
			 * lines.put(12, "&6" + getBarVar(player, "balance")); //nl //nl lines.put(9,
			 * "&c&lRank"); lines.put(8, getBarVar(player, "rank")); //nl //nl lines.put(5,
			 * "&9Online"); lines.put(4, "&6" + getBarVar(player, "online") + "&r&7/100");
			 * //nl lines.put(2, "&aIP &r:: &a&lArim.Space"); //n1
			 */
		}
		/*
		 * String list = null; for (HashMap.Entry<Integer, String> line :
		 * lines.entrySet()) { String val = line.getKey() + ":" + line.getValue(); if
		 * (list == null) { list = val; } else { list = list + " " + val; } }
		 * debug("Manager#getLines> Fetched lines successfully: " + list + ".");/
		 **/
		return lines;
	}
	
	@Override
	public void close() {
		boards.forEach((uuid, board) -> {
			board.deactivate();
		});
		boards.clear();
	}

}
