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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import me.tigerhix.lib.scoreboard.common.EntryBuilder;
import me.tigerhix.lib.scoreboard.type.Entry;
import me.tigerhix.lib.scoreboard.type.ScoreboardHandler;

public class Handler implements ScoreboardHandler {
	
	private final Scoreboard2Manager manager;
	
	Handler(Scoreboard2Manager manager) {
		this.manager = manager;
	}
	
	@Override
	public List<Entry> getEntries(Player player) {
		if (!manager.enabled(player)) {
			return Collections.emptyList();
		}
		EntryBuilder builder = (new EntryBuilder()).blank();
		Map<Integer, String> lines = manager.getLines(manager.api.parseGame(player.getWorld()), player.getUniqueId());
		for (int n = 14; n > 0; n--) {
			if (lines.containsKey(n)) {
				builder.next(lines.get(n));
			} else {
				builder.blank();
			}
		}
		return builder.build();
	}
	
	@Override
	public String getTitle(Player player) {
		return manager.api.parseGame(player.getWorld()).getTitle();
	}
	
}
