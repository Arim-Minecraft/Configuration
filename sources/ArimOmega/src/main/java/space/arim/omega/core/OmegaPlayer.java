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

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import space.arim.api.util.sql.CloseMe;

import space.arim.uuidvault.api.UUIDUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains all player information, including statistics and preferences. <br>
 * Also used for {@link Rank}s, and internally to help implement {@link TransientPlayer}s. <br>
 * <br>
 * <b>Statistics</b> <br>
 * Available statistics are level, balance, kitpvp kills, kitpvp deaths, combo kills, combo deaths, and monthly reward. <br>
 * <br>
 * The balance is stored as an AtomicLong. <br>
 * The other statistics are all integers stored in an AtomicIntegerArray, the indexes of which are as follows: <br>
 * 0 {@literal -} level <br>
 * 1 {@literal -} kitpvp kills <br>
 * 2 {@literal -} kitpvp deaths <br>
 * 3 {@literal -} combo kills <br>
 * 4 {@literal -} combo deaths <br>
 * 5 {@literal -} monthly reward <br>
 * <br>
 * <b>Preferences</b> <br>
 * Available preferences are chat colour, name colour, and eight on/off preferences,
 * the indexes of which are as follows: <br>
 * 0 {@literal -} AutoTree, true indicates on <br>
 * 1 {@literal -} AutoItem, true indicates on <br>
 * 2 {@literal -} PMs, true indicates PMs are allowed <br>
 * 3 {@literal -} Sounds, true indicates custom sounds permitted <br>
 * 4 {@literal -} Bypass PMs, true indicates ability to bypass others' PMs setting <br>
 * 5 {@literal -} Sidebar, false disables the scoreboard <br>
 * 6 {@literal -} Kit descriptions, false reduces kit description verbosity <br>
 * 7 {@literal -} World chat, false only shows chat from the same world as the player <br>
 * <br>
 * Chat colour and name colour are stored as volatile strings. A user really can't cause a concurrency error here. <br>
 * The on/off preferences would be stored as a boolean array, but for efficiency purposes, this array
 * is combined into a single byte, which again for efficiency purposes is an int.
 * 
 * @author A248
 *
 */
public class OmegaPlayer {

	@Getter
	private final UUID uuid;
	@Getter
	private volatile String name;

	// Transient
	
	@Getter
	private transient volatile Rank rank;
	
	private transient volatile TransientPlayer transientInfo;
	
	// From SQL backend
	
	// Stats
	
	@Getter(AccessLevel.PACKAGE)
	private final AtomicLong balance;
	@Getter(AccessLevel.PACKAGE)
	private final AtomicIntegerArray integer_stats;
	
	private static final int MAX_RAW_XP = 10672500;
	
	// Prefs
	
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
	private volatile char chatcolour;
	@Getter
	@Setter
	private volatile char namecolour;
	
	// Prevent saving twice
	
	private final AtomicBoolean isCurrentlySaving = new AtomicBoolean(false);
	
	/**
	 * Minutes within a month, equal to 1440 (seconds in a day) times 30 (days in a month). <br>
	 * Sort of like a unix timestamp but in minutes, not seconds, to save space. <br>
	 * <br>
	 * This is used with the monthly reward of the player, which is also in minutes.
	 * 
	 */
	private static final int MINUTES_IN_MONTH = 1440 * 30;
	
	private static final Logger logger = LoggerFactory.getLogger(OmegaPlayer.class);
	
	OmegaPlayer(UUID uuid, String name, Rank rank, PlayerNumbers numbers) {
		this.uuid = uuid;
		this.name = name;
		this.rank = rank;
		balance = new AtomicLong(numbers.getBalance());
		integer_stats = new AtomicIntegerArray(numbers.getInteger_stats());
		toggle_prefs = new AtomicInteger(numbers.getToggle_prefs());
		chatcolour = numbers.getChatcolour();
		namecolour = numbers.getNamecolour();
	}
	
	/*
	 * 
	 * Basic information modification
	 * 
	 */
	
	/**
	 * Sets the name of the OmegaPlayer
	 * 
	 * @param name the name
	 */
	void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the rank of the OmegaPlayer
	 * 
	 * @param rank the rank
	 */
	void setRank(Rank rank) {
		this.rank = rank;
	}
	
	/*
	 * 
	 * Public library accessors
	 * 
	 */
	
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
		} while (!PlayerNumbers.compareAndSetArray(integer_stats, 0, expect, update));
	}
	
	/**
	 * Gets the current balance of the player
	 * 
	 * @return the current balance
	 */
	public long getCurrentBalance() {
		return balance.get();
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
	 * Increments the player's kills in Kit PvP
	 * 
	 * @return the updated kills
	 */
	public int incrementKitPvP_Kills() {
		return PlayerNumbers.incrementAndGetArray(integer_stats, 1);
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
	 * Increments the player's deaths in Kit PvP
	 * 
	 * @return the updated deaths
	 */
	public int incrementKitPvP_Deaths() {
		return PlayerNumbers.incrementAndGetArray(integer_stats, 2);
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
	 * Increments the player's kills in Combo
	 * 
	 * @return the updated kills
	 */
	public int incrementCombo_Kills() {
		return PlayerNumbers.incrementAndGetArray(integer_stats, 3);
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
	 * Increments the player's deaths in Combo
	 * 
	 * @return the updated deaths
	 */
	public int incrementCombo_Deaths() {
		return PlayerNumbers.incrementAndGetArray(integer_stats, 4);
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
	 * @return true if the reward was activated, false if the last reward was less than a month ago
	 */
	public boolean activateMonthlyReward() {
		int existing;
		int now;
		do {
			existing = integer_stats.get(5);
			now = Omega.currentTimeMinutes();
			if (now - existing < MINUTES_IN_MONTH) {
				return false;
			}
		} while (!PlayerNumbers.compareAndSetArray(integer_stats, 5, existing, now));
		return true;
	}
	
	/**
	 * Gets a current preference according to its index. <br>
	 * See the class javadoc for indexes.
	 * 
	 * @param index the index
	 * @return the current preference
	 */
	public boolean getPreference(int index) {
		assert 0 <= index && index <= 7;

		int pref = 1 << index;
		int current = toggle_prefs.get();
		return (current & pref) != 0;
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

		int pref = 1 << index;
		int existing;
		int update;
		do {
			existing = toggle_prefs.get();
			update = existing ^ pref;
		} while (!PlayerNumbers.compareAndSetInteger(toggle_prefs, existing, update));
		return (update & pref) != 0;
	}
	
	/*
	 * 
	 * Transient-related internals
	 * 
	 */
	
	/**
	 * Detaches transient player info. <br>
	 * This is called in PlayerQuitEvent.
	 * 
	 */
	void nullifyTransientInfo() {
		transientInfo = null;
	}
	
	/**
	 * Gets transient player info. <br>
	 * Transient info is not necessarily thread safe.
	 * 
	 * @return transient player info
	 */
	TransientPlayer getTransientInfo() {
		return transientInfo;
	}
	
	/**
	 * Called in the PlayerJoinEvent.
	 * 
	 * @param player the bukkit player
	 * @param transientInfo the transient player info
	 */
	void onPlayerJoin(Player player, TransientPlayer transientInfo) {
		this.transientInfo = transientInfo;
		applyDisplayNames(player);
		transientInfo.hideVanishedFromPlayer();
	}
	
	/**
	 * Applies the rank display/tag and name/chat colour prefs of this OmegaPlayer to the player.
	 * 
	 * @param player the player, should be the same actual player as this one
	 */
	void applyDisplayNames(Player player) {
		assert player.getUniqueId().equals(uuid);

		player.setDisplayName(ChatColor.translateAlternateColorCodes('&', rank.getDisplay() + " &" + chatcolour + player.getName()));
		player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', rank.getTag() + " &" + namecolour + player.getName()));
	}
	
	/* 
	 * 
	 * Saving
	 * 
	 */
	
	/**
	 * Saves and unloads the OmegaPlayer
	 * 
	 * @param omega the omega manager
	 * @return a completable future representing the saving and unloading
	 */
	CompletableFuture<?> save(Omega omega) {
		OmegaSql sql = omega.sql;
		
		if (!isCurrentlySaving.compareAndSet(false, true)) {
			return CompletableFuture.completedFuture(null);
		}
		return sql.executeAsync(() -> {
			long balance = this.balance.get();
			int level = integer_stats.get(0);
			int kitpvp_kills = integer_stats.get(1);
			int kitpvp_deaths = integer_stats.get(2);
			int combo_kills = integer_stats.get(3);
			int combo_deaths = integer_stats.get(4);
			int monthly_reward = integer_stats.get(5);
			int toggle_prefs = this.toggle_prefs.get();
			char chatcolour = this.chatcolour;
			char namecolour = this.namecolour;
			try (CloseMe cm = sql.execute("INSERT INTO `omega_numbers` "
					+ "(`uuid`, "
					+ "`balance`, `level`, `kitpvp_kills`, `kitpvp_deaths`, `combo_kills`, `combo_deaths`, `monthly_reward`, "
					+ "`toggle_prefs`, `chat_colour`, `name_colour`) "
					+ "VALUES (?, "
					+ "?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?) "
					+ "ON DUPLICATE KEY UPDATE "
					+ "`balance` = ?, `level` = ?, `kitpvp_kills` = ?, `kitpvp_deaths` = ?, `combo_kills` = ?, `combo_deaths` = ?, `monthly_reward` = ?, "
					+ "`toggle_prefs` = ?, `chat_colour` = ?, `name_colour` = ?", UUIDUtil.byteArrayFromUUID(uuid),
					balance, level, kitpvp_kills, kitpvp_deaths, combo_kills, combo_deaths, monthly_reward, toggle_prefs, chatcolour, namecolour,
					balance, level, kitpvp_kills, kitpvp_deaths, combo_kills, combo_deaths, monthly_reward, toggle_prefs, chatcolour, namecolour)) {
				
			} catch (SQLException ex) {
				logger.error("Error while saving {} / {}", uuid, name, ex);
			}
		}).thenRun(() -> {
			if (transientInfo == null) {
				omega.remove(uuid);
			}
			isCurrentlySaving.set(false);
		});
	}

	void removeIfOfflineUnlessSaving(Omega omega) {
		if (!isCurrentlySaving.get() && transientInfo == null) {
			omega.remove(uuid);
		}
	}	

}
