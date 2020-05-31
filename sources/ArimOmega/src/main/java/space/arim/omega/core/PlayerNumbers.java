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
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.LockSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerNumbers {

	private final long balance;
	private final int[] integer_stats;
	/*
	 * Really a byte
	 */
	private final int toggle_prefs;
	private final char chatcolour;
	private final char namecolour;
	
	private static final long DEFAULT_BALANCE = OmegaSwiftConomy.STARTING_BALANCE;
	
	private static final int[] DEFAULT_INTEGER_STATS = new int[] {0, 0, 0, 0, 0, 0};
	
	/**
	 * Default toggle preferences: <br>
	 * autotree = false <br>
	 * autoitem = false <br>
	 * pms = true <br>
	 * sounds = true <br>
	 * bypasspms = false <br>
	 * sidebar = true <br>
	 * kit descriptions = true <br>
	 * world chat = true
	 * 
	 */
	private static final int DEFAULT_TOGGLE_PREFS = 0b00110111;
	
	/**
	 * Creates the default values for player info
	 * 
	 * @return the default values
	 */
	static PlayerNumbers makeDefaultValues() {
		return new PlayerNumbers(DEFAULT_BALANCE, DEFAULT_INTEGER_STATS, DEFAULT_TOGGLE_PREFS, 'f', 'b');
	}
	
	/*static boolean isDefaultValues(AtomicLong balance, AtomicIntegerArray integer_stats, AtomicInteger toggle_prefs, char chatcolour, char namecolour) {
		return isDefaultValues(balance.get(), new int[] {integer_stats.get(0), integer_stats.get(1), integer_stats.get(2), integer_stats.get(3), integer_stats.get(4), integer_stats.get(5)},
				toggle_prefs.get(), chatcolour, namecolour);
	}
	
	static boolean isDefaultValues(long balance, int[] integer_stats, int toggle_prefs, char chatcolour, char namecolour) {
		return balance == DEFAULT_BALANCE && Arrays.equals(integer_stats, DEFAULT_INTEGER_STATS)
				&& toggle_prefs == DEFAULT_TOGGLE_PREFS && chatcolour == 'f' && namecolour == 'b';
	}*/
	
	/*
	 * Faster version of AtomicInteger#compareAndSet
	 * See https://dzone.com/articles/wanna-get-faster-wait-bit
	 */
	
	static boolean compareAndSetInteger(AtomicInteger atomInt, int expect, int update) {
		if (!atomInt.compareAndSet(expect, update)) {
			LockSupport.parkNanos(1L);
			return false;
		}
		return true;
	}
	
	/*
	 * Same, but for AtomicIntegerArray
	 * 
	 */
	
	static boolean compareAndSetArray(AtomicIntegerArray atomIntArray, int i, int expect, int update) {
		if (!atomIntArray.compareAndSet(i, expect, update)) {
			LockSupport.parkNanos(1L);
			return false;
		}
		return true;
	}
	
	static int incrementAndGetArray(AtomicIntegerArray atomIntArray, int i) {
		int expect;
		do {
			expect = atomIntArray.get(i);
		} while (!compareAndSetArray(atomIntArray, i, expect, expect + 1));
		return expect + 1;
	}
	
}
