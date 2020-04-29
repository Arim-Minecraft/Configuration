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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import space.arim.api.uuid.UUIDUtil;

/**
 * Data describing friended and ignored players. <br>
 * <br>
 * Limits the amount of friends to 50
 * and the amount of ignored also to 50.
 * 
 * @author A248
 *
 */
public class FriendedIgnored {

	/**
	 * A value of true indicates a friend, false an ignored user. <br>
	 * Players not in the map are neither friended nor ignored.
	 * 
	 */
	private final AtomicReference<Map<UUID, Boolean>> ref;
	
	private FriendedIgnored(HashMap<UUID, Boolean> map) {
		ref = new AtomicReference<>(map);
	}
	
	FriendedIgnored() {
		this(null);
	}
	
	/**
	 * Converts the data to a string. This is considered its serialised form.
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		ref.get().forEach((uuid, value) -> {
			builder.append(',').append(uuid.toString().replace("-", "")).append(':').append((value) ? '1' : '0');
		});
		return (builder.length() == 0) ? "<empty>" : builder.substring(1);
	}
	
	/**
	 * Recreates an instance from a string (deserialises). <br>
	 * The string must be from {@link #toString()}
	 * 
	 * @param str the string
	 * @return an instance
	 */
	static FriendedIgnored fromString(String str) {
		HashMap<UUID, Boolean> result = null;
		if (!str.equals("<empty>")) {
			result = new HashMap<>();
			for (String data : str.split(",")) {
				String[] subData = data.split(":");
				result.put(UUIDUtil.expandAndParse(subData[0]), subData[1].equals("1"));
			}
		}
		return new FriendedIgnored(result);
	}
	
	/**
	 * Checks whether no players are friended or ignored.
	 * 
	 * @return true if no players are friended or ignored, false otherwise
	 */
	public boolean isEmpty() {
		return ref.get() == null;
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
		return mutateAtomically((map) -> {
			// Check size
			if (map.size() == 100) {
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
		return mutateAtomically((map) -> map.remove(other, false));
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
		return mutateAtomically((map) -> {
			// Check size
			if (map.size() == 100) {
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
		return mutateAtomically((map) ->  map.remove(other, true));
	}
	
	private Boolean mutateAtomically(Function<Map<UUID, Boolean>, Boolean> computation) {
		Map<UUID, Boolean> existing;
		Map<UUID, Boolean> update;
		Boolean result;
		do {
			existing = ref.get();
			update = (existing != null) ? new HashMap<>(existing) : new HashMap<>();
			result = computation.apply(update);
			if (update.isEmpty()) {
				update = null;
			}
		} while (!compareAndSet(ref, existing, update));
		return result;
	}
	
	/*
	 * Faster version of AtomicReference#compareAndSet
	 * See https://dzone.com/articles/wanna-get-faster-wait-bit
	 */
	
	private static <T> boolean compareAndSet(AtomicReference<T> reference, T expect, T update) {
		if (!reference.compareAndSet(expect, update)) {
			LockSupport.parkNanos(1L);
			return false;
		}
		return true;
	}
	
}
