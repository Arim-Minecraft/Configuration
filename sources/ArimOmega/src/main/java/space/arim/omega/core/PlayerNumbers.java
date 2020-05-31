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
	private static final int DEFAULT_TOGGLE_PREFS = byteFromBooleanArray(new boolean[] {false, false, true, true, false, true, true, true});
	
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
	
	/**
	 * Converts the bits of a byte to a boolean array. <br>
	 * E.g., 11110101 => true, true, true, true, false, true, false true
	 * 
	 * @param source the source byte, really an int to avoid casting
	 * @return the boolean array, always of length 8
	 */
	static boolean[] booleanArrayFromByte(int source) {
		boolean[] result = new boolean[8];
	    result[0] = ((source & 0x01) != 0);
	    result[1] = ((source & 0x02) != 0);
	    result[2] = ((source & 0x04) != 0);
	    result[3] = ((source & 0x08) != 0);
	    result[4] = ((source & 0x10) != 0);
	    result[5] = ((source & 0x20) != 0);
	    result[6] = ((source & 0x40) != 0);
	    result[7] = ((source & 0x80) != 0);
	    return result;
	}
	
	/**
	 * Converts a boolean array to a byte, where each boolean
	 * determines a bit of the byte. <br>
	 * <br>
	 * This is the inverse operation of {@link #booleanArrayFromByte(int)}. <br>
	 * Note that the source array must have length 8.
	 * 
	 * @param source the boolean array, must be length 8
	 * @return an int rather than a byte to avoid casting
	 */
	static int byteFromBooleanArray(boolean[] source) {
		assert source.length == 8;

		return ((source[7] ? 1<<7 : 0) + (source[6] ? 1<<6 : 0) + (source[5] ? 1<<5 : 0) + 
                (source[4] ? 1<<4 : 0) + (source[3] ? 1<<3 : 0) + (source[2] ? 1<<2 : 0) + 
                (source[1] ? 1<<1 : 0) + (source[0] ? 1 : 0));
	}
	
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
