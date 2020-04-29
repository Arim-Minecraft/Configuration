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
package space.arim.omega.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import lombok.Getter;
import lombok.Setter;

public class MutablePrefs {

	/**
	 * There are 8 on/off preferences. <br>
	 * <br>
	 * We would use separate booleans to represent these, but for performance purposes,
	 * we use a AtomicInteger which is essentially an "AtomicByte". <br>
	 * The byte's bits correspond to a boolean array of length 8. <br>
	 * <br>
	 * Since this is really an array of booleans, we refer to the index of the preference. <br>
	 * "Indexes" are described in {@link #togglePreference(int)} <br>
	 * <br>
	 * MySQL doesn't have a boolean data type, so using an integer also saves disk space.
	 * 
	 */
	final AtomicInteger toggle_prefs;
	
	@Getter
	@Setter
	private volatile String chatcolour;
	@Getter
	@Setter
	private volatile String namecolour;
	
	@Getter
	private final FriendedIgnored friended_ignored;
	
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
	private static final byte DEFAULT_TOGGLE_PREFS = (byte) byteFromBooleanArray(new boolean[] {false, false, true, true, false, true, true, true});
	
	MutablePrefs(byte toggle_prefs, String chat_colour, String namecolour, FriendedIgnored friended_ignored) {
		this.toggle_prefs = new AtomicInteger(toggle_prefs);
		this.chatcolour = chat_colour;
		this.namecolour = namecolour;
		this.friended_ignored = friended_ignored;
	}
	
	/**
	 * The default preferences <br>
	 * <br>
	 * Default toggle preferences: {@link #DEFAULT_TOGGLE_PREFS} <br>
	 * Default chat colour: {@literal &}f <br>
	 * Default name colour: {@literal &}b <br>
	 * Ignored / friended: none
	 * 
	 */
	// Values here MUST equal those in #isCurrentlyDefault
	static MutablePrefs makeDefaultValues() {
		return new MutablePrefs(DEFAULT_TOGGLE_PREFS, "&f", "&b", new FriendedIgnored());
	}
	
	/**
	 * Whether the player's prefs are currently equal to the default values
	 * 
	 * @return true if equal, false otherwise
	 */
	boolean isCurrentlyDefault() {
		return toggle_prefs.get() == DEFAULT_TOGGLE_PREFS &&
				chatcolour.equals("&f") &&
				namecolour.equals("&b") &&
				friended_ignored.isEmpty();
	}
	
	/**
	 * Gets a current preference according to its index. <br>
	 * See {@link #togglePreference(int)} for indexes.
	 * 
	 * @param index the index
	 * @return the current preference
	 */
	public boolean getPreference(int index) {
		return booleanArrayFromByte(toggle_prefs.get())[index];
	}
	
	/**
	 * Toggles an on/off preference according to its index and returns the updated result. <br>
	 * <br>
	 * 0 {@literal -} AutoTree, true indicates on <br>
	 * 1 {@literal -} AutoItem, true indicates on <br>
	 * 2 {@literal -} PMs, true indicates PMs are allowed <br>
	 * 3 {@literal -} Sounds, true indicates custom sounds permitted <br>
	 * 4 {@literal -} Bypass PMs, true indicates ability to bypass others' PMs setting <br>
	 * 5 {@literal -} Sidebar, false disables the scoreboard <br>
	 * 6 {@literal -} Kit descriptions, false reduces kit description verbosity <br>
	 * 7 {@literal -} World chat, false only shows chat from the same world as the player <br>
	 *  <br>
	 *  <b>Note that bypassing PMs is an excalibur+ rank feature, so remember to check permissions.</b>
	 * 
	 * @param index the index of the preferences
	 * @return the updated state
	 */
	public boolean togglePreference(int index) {
		assert 0 <= index && index <= 7;
		
		// using bytes creates a lot of unnecessary casting
		int existing;
		int update;
		boolean result;
		do {
			existing = toggle_prefs.get();
			boolean[] change = booleanArrayFromByte(existing);
			result = change[index] = !change[index];
			update = byteFromBooleanArray(change);
		} while (!compareAndSet(toggle_prefs, existing, update));
		return result;
	}
	
	/*
	 * Faster version of AtomicInteger#compareAndSet
	 * See https://dzone.com/articles/wanna-get-faster-wait-bit
	 */
	
	private static boolean compareAndSet(AtomicInteger atomInt, int expect, int update) {
		if (!atomInt.compareAndSet(expect, update)) {
			LockSupport.parkNanos(1L);
			return false;
		}
		return true;
	}
	
	/**
	 * Converts the bits of a byte to a boolean array. <br>
	 * E.g., 11110101 => true, true, true, true, false, true, false true
	 * 
	 * @param source the source byte, really an int to avoid casting
	 * @return the boolean array
	 */
	private static boolean[] booleanArrayFromByte(int source) {
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
	 * determines a bit of the array. <br>
	 * <br>
	 * This is the inverse operation of {@link #booleanArrayFromByte(int)}. <br>
	 * Note that the source array must have length 8.
	 * 
	 * @param source the boolean array
	 * @return an int rather than a byte to avoid casting
	 */
	private static int byteFromBooleanArray(boolean[] source) {
		assert source.length == 8;

		return ((source[7] ? 1<<7 : 0) + (source[6] ? 1<<6 : 0) + (source[5] ? 1<<5 : 0) + 
                (source[4] ? 1<<4 : 0) + (source[3] ? 1<<3 : 0) + (source[2] ? 1<<2 : 0) + 
                (source[1] ? 1<<1 : 0) + (source[0] ? 1 : 0));
	}
	
}
