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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import space.arim.api.uuid.UUIDUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Retains personal preferences for players. <br>
 * <br>
 * Available preferences are chat colour, name colour, a list of players friended, a list of players ignored,
 * and eight on/off preferences, the indexes of which are as follows: <br>
 * 0 {@literal -} AutoTree, true indicates on <br>
 * 1 {@literal -} AutoItem, true indicates on <br>
 * 2 {@literal -} PMs, true indicates PMs are allowed <br>
 * 3 {@literal -} Sounds, true indicates custom sounds permitted <br>
 * 4 {@literal -} Bypass PMs, true indicates ability to bypass others' PMs setting <br>
 * 5 {@literal -} Sidebar, false disables the scoreboard <br>
 * 6 {@literal -} Kit descriptions, false reduces kit description verbosity <br>
 * 7 {@literal -} World chat, false only shows chat from the same world as the player <br>
 * <br>
 * Chat colour and name colour are stored as volatile strings. A user can't cause a concurrency error here. <br>
 * The lists of friended and ignored players are internally combined into a map where a <code>true</code> value indicates
 * a friend and a <code>false</code> value an ignored player. <br>
 * The on/off preferences would be stored as a boolean array, but for efficiency purposes, this array
 * is combined into a single byte, which again for efficiency purposes is an int.
 * 
 * @author A248
 *
 */
public class MutablePrefs {

	/**
	 * There are 8 on/off preferences. <br>
	 * <br>
	 * We would use a boolean array to represent these, but for performance purposes,
	 * we use essentially an "AtomicByte", really an AtomicInteger. <br>
	 * The byte's bits correspond to a boolean array of length 8. <br>
	 * <br>
	 * Since this is really an array of booleans, we refer to the index of the preference. <br>
	 * "Indexes" are described in the class javadoc. <br>
	 * <br>
	 * MySQL doesn't have a boolean data type, so using an integer also saves disk space.
	 * 
	 */
	@Getter(AccessLevel.PACKAGE)
	private final AtomicInteger toggle_prefs;
	
	@Getter
	@Setter
	private volatile String chatcolour;
	@Getter
	@Setter
	private volatile String namecolour;
	
	/**
	 * A value of true indicates a friend, false an ignored user. <br>
	 * Players not in the map are neither friended nor ignored. <br>
	 * <br>
	 * Limits the amount of friends to 50 and the amount of ignored also to 50.
	 * 
	 */
	private final AtomicReference<Map<UUID, Boolean>> friended_ignored;
	
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
	
	MutablePrefs(byte toggle_prefs, String chat_colour, String namecolour, Map<UUID, Boolean> friended_ignored) {
		this.toggle_prefs = new AtomicInteger(toggle_prefs);
		this.chatcolour = chat_colour;
		this.namecolour = namecolour;
		this.friended_ignored = new AtomicReference<>(friended_ignored);
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
		return new MutablePrefs(DEFAULT_TOGGLE_PREFS, "&f", "&b", null);
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
				friended_ignored.get() == null;
	}
	
	/**
	 * Gets a current preference according to its index. <br>
	 * See the class javadoc for indexes.
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
	 * See the class javadoc for indexes <br>
	 * <br>
	 * <b>Note that bypassing PMs is an excalibur+ rank feature, so remember to check permissions.</b>
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
		} while (!compareAndSetInteger(toggle_prefs, existing, update));
		return result;
	}
	
	/**
	 * Ignores the player by the given UUID. <br>
	 * Returns a result indicating if the ignore was successful (true),
	 * the target is already ignored (false), or the max amount of ignored
	 * players has been reached (null)
	 * 
	 * @param other the uuid of the player to ignore
	 * @return a tristate indicating success (true), already ignored (false), or limit reached (null)
	 */
	public Boolean ignore(UUID other) {
		return mutateFriendedIgnoredAtomically((map) -> {

			if (map == null) {
				map = new HashMap<>();

			// Check size
			} else if (map.size() == 100) {
				return null;
			} else if (map.size() >= 50) {
				int ignored = 0;
				for (Map.Entry<UUID, Boolean> entry : map.entrySet()) {
					if (!entry.getValue()) {
						ignored++;
					}
				}
				if (ignored == 50) {
					return null;
				}
			}

			Boolean previous = map.put(other, false);
			return previous == null || previous;
		});
	}
	
	/**
	 * Unignores the player by the given UUID.
	 * 
	 * @param other the uuid of the player to unignore
	 * @return true if successful, false if the player was not ignored
	 */
	public boolean unignore(UUID other) {
		return mutateFriendedIgnoredAtomically((map) -> map != null && map.remove(other, false));
	}
	
	/**
	 * Friends the player by the given UUID. <br>
	 * Returns a result indicating if the friending was successful (true),
	 * the target is already friended (false), or the max amount of friended
	 * players has been reached (null)
	 * 
	 * @param other the uuid of the player to friend
	 * @return a tristate indicating success (true), already friended (false), or limit reached (null)
	 */
	public Boolean friend(UUID other) {
		return mutateFriendedIgnoredAtomically((map) -> {

			if (map == null) {
				map = new HashMap<>();

			// Check size
			} else if (map.size() == 100) {
				return null;
			} else if (map.size() >= 50) {
				int friended = 0;
				for (Map.Entry<UUID, Boolean> entry : map.entrySet()) {
					if (entry.getValue()) {
						friended++;
					}
				}
				if (friended == 50) {
					return null;
				}
			}

			Boolean previous = map.put(other, true);
			return previous == null || !previous;
		});
	}
	
	/**
	 * Unfriends the player by the given UUID.
	 * 
	 * @param other the uuid of the player to unfriend
	 * @return true if successful, false if the player was not friended
	 */
	public boolean unfriend(UUID other) {
		return mutateFriendedIgnoredAtomically((map) ->  map != null && map.remove(other, true));
	}
	
	private Boolean mutateFriendedIgnoredAtomically(Function<Map<UUID, Boolean>, Boolean> computation) {
		Map<UUID, Boolean> existing;
		Map<UUID, Boolean> update;
		Boolean result;
		do {
			existing = friended_ignored.get();
			update = (existing != null) ? new HashMap<>(existing) : null;
			result = computation.apply(update);
		} while (!compareAndSetReference(friended_ignored, existing, update));
		return result;
	}
	
	/**
	 * Serialises the friended and ignored players
	 * 
	 * @return a compressed map string
	 */
	String friendedIgnoredToString() {
		StringBuilder builder = new StringBuilder();
		friended_ignored.get().forEach((uuid, value) -> {
			builder.append(',').append(uuid.toString().replace("-", "")).append(':').append((value) ? '1' : '0');
		});
		return (builder.length() == 0) ? "<empty>" : builder.substring(1);
	}
	
	/**
	 * Gets friended and ignored players from a compressed map string
	 * 
	 * @param str the string
	 * @return friended and ignored players
	 */
	static Map<UUID, Boolean> friendedIgnoredFromString(String str) {
		HashMap<UUID, Boolean> result = null;
		if (!str.equals("<empty>")) {
			result = new HashMap<>();
			for (String data : str.split(",")) {
				String[] subData = data.split(":");
				result.put(UUIDUtil.expandAndParse(subData[0]), subData[1].equals("1"));
			}
		}
		return result;
	}
	
	/*
	 * Faster version of AtomicInteger#compareAndSet
	 * See https://dzone.com/articles/wanna-get-faster-wait-bit
	 */
	
	private static boolean compareAndSetInteger(AtomicInteger atomInt, int expect, int update) {
		if (!atomInt.compareAndSet(expect, update)) {
			LockSupport.parkNanos(1L);
			return false;
		}
		return true;
	}
	
	/*
	 * Same, but for AtomicReference
	 */
	
	private static <T> boolean compareAndSetReference(AtomicReference<T> reference, T expect, T update) {
		if (!reference.compareAndSet(expect, update)) {
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
