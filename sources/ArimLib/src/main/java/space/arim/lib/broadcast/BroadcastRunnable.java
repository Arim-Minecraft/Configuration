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
package space.arim.lib.broadcast;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;

import me.tigerhix.lib.bossbar.Bossbar;

public class BroadcastRunnable implements Runnable {

	private int broadcastTimer = 0;
	private int barIndex = 0;
	private final ArrayList<Integer> broadcasts = new ArrayList<Integer>();

	private final BroadcastManager manager;
	private final Player target;
	
	BroadcastRunnable(BroadcastManager manager, Player target) {
		this.manager = manager;
		this.target = target;
	}
	
	private void removeBar(Player player) {
		manager.barlib.clearBossbar(player);
	}

	private void setBar(Player player, String message) {
		Bossbar bossbar = manager.barlib.getBossbar(player);
		bossbar.setMessage(message);
		bossbar.setPercentage(1F);
	}

	@Override
	public void run() {
		String worldName = target.getWorld().getName();
		if (worldName.equalsIgnoreCase(manager.world)) {
			barIndex++;
			if (barIndex >= manager.getMutableBars().size()) {
				barIndex = 0;
			}
			setBar(target, manager.getMutableBars().get(barIndex));
		} else {
			removeBar(target);
		}
		broadcastTimer++;
		if (broadcastTimer == 24) {
			/*
			 * 1 = rules 2 = discord 3 = vote 4 = donate 5 = help 6 = duel 7 = buy 8 =
			 * logout_safely 9 = skyfactions_claim 10 = skyfactions_pipes 11 =
			 * survival_noclaims
			 */
			broadcasts.clear();
			broadcasts.add(1);
			broadcasts.add(2);
			broadcasts.add(3);
			broadcasts.add(4);
			broadcasts.add(5);
			if (worldName.equalsIgnoreCase("SkyFactions")) {
				broadcasts.add(7);
				broadcasts.add(8);
				broadcasts.add(9);
				broadcasts.add(10);
			} else if (worldName.equalsIgnoreCase("Survival")
					|| worldName.equalsIgnoreCase("Survival_nether")
					|| worldName.equalsIgnoreCase("Survival_the_end")) {
				broadcasts.add(11);
			} else if (worldName.equalsIgnoreCase("KitPvP")
					|| worldName.equalsIgnoreCase("FFA")
					|| worldName.equalsIgnoreCase("KitPvP3")) {
				broadcasts.add(7);
			} else if (!worldName.equalsIgnoreCase("Duel")
					&& !worldName.equalsIgnoreCase("Combo")) {
				broadcasts.add(6);
				broadcasts.add(7);
			}
			manager.api.center().getServer().dispatchCommand(manager.api.center().getServer().getConsoleSender(),
					"infocast " + target.getName() + " "
							+ broadcasts.get(ThreadLocalRandom.current().nextInt(broadcasts.size())));
			broadcastTimer = 0;
		}
	}
	
}
