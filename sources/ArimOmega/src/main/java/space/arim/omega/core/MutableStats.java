/* 
 * ArimOmega
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ArimOmega is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ArimOmega is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ArimOmega. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.omega.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;

public class MutableStats {

	@Getter
	private final AtomicLong balance;
	@Getter
	private final AtomicInteger kitpvp_kills;
	@Getter
	private final AtomicInteger kitpvp_deaths;
	@Getter
	private final AtomicInteger combo_kills;
	@Getter
	private final AtomicInteger combo_deaths;
	@Getter
	private final AtomicInteger monthly_reward;
	
	MutableStats(long balance, int kitpvp_kills, int kitpvp_deaths, int combo_kills, int combo_deaths, int monthly_reward) {
		this.balance = new AtomicLong(balance);
		this.kitpvp_kills = new AtomicInteger(kitpvp_kills);
		this.kitpvp_deaths = new AtomicInteger(kitpvp_deaths);
		this.combo_kills = new AtomicInteger(combo_kills);
		this.combo_deaths = new AtomicInteger(combo_deaths);
		this.monthly_reward = new AtomicInteger(monthly_reward);
	}
	
	/**
	 * Default statistics, starting balance of $3000, monthly reward immediately available. <br>
	 * Note that the balance is 3000 * 10^4 because of how SwiftConomy balances work internally.
	 * 
	 */
	// Values here MUST equal those in #isCurrentlyDefault
	static MutableStats makeDefaultValues() {
		return new MutableStats(3000_0000L, 0, 0, 0, 0, 0);
	}
	
	/**
	 * Whether the player's stats are currently equal to the default values
	 * 
	 * @return true if equal, false otherwise
	 */
	boolean isCurrentlyDefault() {
		return balance.get() == 3000_0000L &&
				kitpvp_kills.get() == 0 &&
				kitpvp_deaths.get() == 0 &&
				combo_kills.get() == 0 &&
				combo_deaths.get() == 0 &&
				monthly_reward.get() == 0;
	}
	
}
