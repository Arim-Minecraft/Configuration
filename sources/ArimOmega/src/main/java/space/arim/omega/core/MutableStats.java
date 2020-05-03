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

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import lombok.Getter;

/**
 * Holds serveral numeric statistics for a player: <br>
 * level, balance, kitpvp kills, kitpvp deaths, combo kills, combo deaths, and monthly reward. <br>
 * <br>
 * The balance is stored as an AtomicLong. <br>
 * The other statistics are all integers stored in an AtomicIntegerArray, the indexes of which are as follows: <br>
 * 0 {@literal -} level
 * 1 {@literal -} kitpvp kills
 * 2 {@literal -} kitpvp deaths
 * 3 {@literal -} combo kills
 * 4 {@literal -} combo deaths
 * 5 {@literal -} monthly reward
 * 
 * @author A248
 *
 */
public class MutableStats {

	@Getter
	private final AtomicLong balance;
	@Getter
	private final AtomicIntegerArray integer_stats;
	
	MutableStats(int level, long balance, int kitpvp_kills, int kitpvp_deaths, int combo_kills, int combo_deaths, int monthly_reward) {
		this.balance = new AtomicLong(balance);
		this.integer_stats = new AtomicIntegerArray(new int[] {level, kitpvp_kills, kitpvp_deaths, combo_kills, combo_deaths, monthly_reward});
	}
	
	/**
	 * Default statistics, starting balance of $3000, monthly reward immediately available. <br>
	 * Note that the balance is 3000 * 10^4 because of how SwiftConomy balances work internally.
	 * 
	 */
	// Values here MUST equal those in #isCurrentlyDefault
	static MutableStats makeDefaultValues() {
		return new MutableStats(0, OmegaSwiftConomy.STARTING_BALANCE, 0, 0, 0, 0, 0);
	}
	
	/**
	 * Whether the player's stats are currently equal to the default values
	 * 
	 * @return true if equal, false otherwise
	 */
	boolean isCurrentlyDefault() {
		return integer_stats.get(0) == 0 &&
				balance.get() == OmegaSwiftConomy.STARTING_BALANCE &&
				integer_stats.get(1) == 0 &&
				integer_stats.get(2) == 0 &&
				integer_stats.get(3) == 0 &&
				integer_stats.get(4) == 0 &&
				integer_stats.get(5) == 0;
	}
	
	/*
	 * Faster version of AtomicIntegerArray#compareAndSet
	 * See https://dzone.com/articles/wanna-get-faster-wait-bit
	 */
	
	static boolean compareAndSetArray(AtomicIntegerArray atomIntArray, int i, int expect, int update) {
		if (!atomIntArray.compareAndSet(i, expect, update)) {
			LockSupport.parkNanos(1L);
			return false;
		}
		return true;
	}
	
}
