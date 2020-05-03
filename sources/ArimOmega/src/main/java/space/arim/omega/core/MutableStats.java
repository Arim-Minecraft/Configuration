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

import lombok.AccessLevel;
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
	@Getter(AccessLevel.PACKAGE)
	private final AtomicIntegerArray integer_stats;
	
	private static final int MAX_RAW_XP = 10672500;
	
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
	
	/**
	 * Gets the raw experience of the player. <br>
	 * The player's level is calculated as a function of the raw XP. <br>
	 * <br>
	 * The maximum XP a player can receive is 10672500. This is because
	 * the max level (see {@link #getLevel()} is 1000.
	 * 
	 * @return the raw xp
	 */
	public int getRawXP() {
		return integer_stats.get(0);
	}
	
	/**
	 * Calculates the player's level based on their experience. <br>
	 * Players gain experience by completing all sorts of activities. <br>
	 * <br>
	 * Every player has a level from 0 to 1000. The level is calculated
	 * based on <code>3.468221632739*(RAW_XP^(7/20))</code>
	 * 
	 * @return the level
	 */
	public int getLevel() {
		return (int) (3.468221632739 * Math.pow(getRawXP(), ((double) 7) / 20));
	}
	
	/**
	 * Increments the experience of the player. <br>
	 * <br>
	 * For reference, kit pvp kills grant 80 XP.
	 * 
	 * @param addition the amount of XP to grant
	 */
	public void incrementXP(int addition) {
		int expect;
		int update;
		do {
			expect = integer_stats.get(0);
			update = expect + addition;
			if (update > MAX_RAW_XP) {
				update = MAX_RAW_XP;
			}
		} while (!compareAndSetArray(integer_stats, 0, expect, update));
	}
	
	/**
	 * Gets the player's kills in Kit PvP
	 * 
	 * @return the kills in kitpvp
	 */
	public int getKitPvP_Kills() {
		return integer_stats.get(1);
	}
	
	/**
	 * Gets the player's deaths in Kit PvP
	 * 
	 * @return the deaths in kitpvp
	 */
	public int getKitPvP_Deaths() {
		return integer_stats.get(2);
	}
	
	/**
	 * Gets the player's kills in Combo
	 * 
	 * @return the kills in combo
	 */
	public int getCombo_Kills() {
		return integer_stats.get(3);
	}
	
	/**
	 * Gets the player's deaths in Combo
	 * 
	 * @return the deaths in combo
	 */
	public int getCombo_Deaths() {
		return integer_stats.get(4);
	}
	
	/**
	 * Calculates the player's KDR based on kills and deaths. <br>
	 * This may be used to calculate KDR for either Kit PvP or Combo. <br>
	 * <br>
	 * The kills and deaths parameters are provided as reminders that
	 * updates may happen concurrently, thus it it is necessary
	 * to get the kills and deaths before getting the KDR.
	 * 
	 * @param kills the kills
	 * @param deaths the deaths
	 * @return the calculated kdr
	 */
	public double calculateKdr(int kills, int deaths) {
		return (deaths == 0) ? kills : ((double) kills)/deaths;
	}
	
	/**
	 * Activates the monthly reward for the player. <br>
	 * Remember to check player permissions first, only ranked players
	 * have access to monthly rewards. <br>
	 * <br>
	 * Returns <code>false</code> if the player's last reward was less than a month ago. <br>
	 * Else, the value of the player's last reward is automatically set to the current time. <br>
	 * <br>
	 * <i>The caller is trusted with providing physical rewards if this returns true.</i>
	 * 
	 * @param player the player
	 * @return true if the reward was activated, false if the last reward was less than a month ago
	 */
	public boolean activateMonthlyReward() {
		int existing;
		int now;
		do {
			existing = integer_stats.get(5);
			now = Omega.currentTimeMinutes();
			if (now - existing < Omega.MINUTES_IN_MONTH) {
				return false;
			}
		} while (!compareAndSetArray(integer_stats, 5, existing, now));
		return true;
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
