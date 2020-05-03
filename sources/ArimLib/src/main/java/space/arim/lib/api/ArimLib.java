/* 
 * ArimLib
 * Copyright © 2019 Anand Beh <https://www.arim.space>
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
package space.arim.lib.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.universal.util.AutoClosable;

import space.arim.api.util.log.LoggerConverter;

import space.arim.lib.broadcast.BroadcastManager;
import space.arim.lib.kitpvp.Kit;
import space.arim.lib.kitpvp.PowerupManager;
import space.arim.lib.scoreboard3.Scoreboard2Manager;
import space.arim.lib.scoreboard3.Scoreboard3Manager;
import space.arim.lib.skript.SkriptManager;
import space.arim.lib.sound.PacketSoundManager;
import space.arim.lib.status.StatusManager;
import space.arim.lib.tablist.TablistManager;
import space.arim.lib.util.NMS;
import space.arim.lib.vanish.VanishManager;

import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;

public class ArimLib implements AutoClosable {
	
	private PrintStream writer = null;
	private final JavaPlugin center;
	private final BroadcastManager broadcast;
	private final Scoreboard3Manager scoreboard;
	private final Economy economy;

	private final Logger logger;
	private final ThreadLocal<SimpleDateFormat> dateformatter;
	private final ThreadLocal<DecimalFormat> decimalformatter;

	private final Random rand;
	
	private final SkriptManager skript;
	private final PowerupManager powerups;
	private final TablistManager tablists;
	private final StatusManager statuses;
	private final VanishManager vanisher;
	private final PacketSoundManager packetSounds;
	
	public ArimLib(JavaPlugin center, String datafolder, Economy economy) {
		this.center = center;
		logger = LoggerConverter.get().convert(center.getLogger());
		loadWriter(datafolder + File.separator + "info.log");
		logger.info("Loading Skript expressions...");
		skript = new SkriptManager(this);
		skript.load();

		broadcast = new BroadcastManager(this, "Hub");
		broadcast.startLoad();
		boolean holo = false;
		try {
			Class.forName("com.gmail.filoghost.holographicdisplays.api.Hologram");
			holo = true;
		} catch (ClassNotFoundException ignored) {}
		this.powerups = holo ? new PowerupManager(this) : null;

		this.scoreboard = new Scoreboard2Manager(this);

		this.tablists = new TablistManager(this);
		this.statuses = new StatusManager(this);
		this.vanisher = new VanishManager(this);
		this.packetSounds = new PacketSoundManager(this);
		this.dateformatter = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"));
		this.decimalformatter = ThreadLocal.withInitial(() -> {
			DecimalFormat result = new DecimalFormat("#.##");
			result.setRoundingMode(RoundingMode.FLOOR);
			return result;
		});
		rand = new Random();
		this.economy = economy;
	}
	
	public void finishLoad() {
		broadcast.finishLoad();
	}
	
	public Logger logger() {
		return logger;
	}
	
	private void loadWriter(String source) {
		center.getLogger().info("Loading logger system...");
		File file = new File(source);
		try {
			if (file.exists() || file.createNewFile()) {
				writer = new PrintStream(file);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
	}

	public BroadcastManager broadcast() {
		return broadcast;
	}

	public Scoreboard3Manager scoreboard() {
		return scoreboard;
	}

	public SkriptManager skript() {
		return skript;
	}

	public PowerupManager powerups() {
		return powerups;
	}
	
	public TablistManager tablists() {
		return tablists;
	}
	
	public StatusManager statuses() {
		return statuses;
	}

	public VanishManager vanisher() {
		return vanisher;
	}
	
	public PacketSoundManager packetSounds() {
		return packetSounds;
	}

	public int randomInt(int upperBound) {
		return 1 + rand.nextInt(upperBound);
	}
	
	public int randomInt(int lowerBound, int upperBound) {
		return lowerBound + randomInt(upperBound - lowerBound);
	}
	
	public boolean parseBoolean(String input) {
		if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("on")
				|| input.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	public Location centerCoords(Location input) {
		input.setX(Math.floor(input.getX()) + 0.5);
		input.setY(Math.floor(input.getY()));
		input.setZ(Math.floor(input.getZ()) + 0.5);
		return input;
	}

	public Location resetYawPitch(Location input) {
		input.setYaw(0);
		input.setPitch(0);
		return input;
	}

	public String decimalFormatThreadSafe(Double input) {
		return decimalformatter.get().format(input);
	}

	public Game parseGame(World world) {
		return Game.parseGame(world.getName());
	}
	
	/**
	 * Adds placeholders to a message
	 * 
	 * @param player the player
	 * @param message the message
	 * @return the same message with placeholders replaced
	 */
	public String setPlaceholders(Player player, String message) {
		return PlaceholderAPI.setPlaceholders(player, message);
	}

	public void setStuckArrows(Player target, Byte amount) {
		NMS.setStuckArrows(target, amount);
	}

	public void sendActionBar(Player target, String message) {
		NMS.sendActionBar(target, message);
	}

	public boolean hasKit(Player target) {
		return (skript().isVar("cooldown::" + target.getName() + "::kit", String.class))
				&& (Kit.fromString(skript().getVar("cooldown::" + target.getName() + "::kit")) != null);
	}

	public Kit getKit(Player target) {
		return Kit.fromString(skript().getVar("cooldown::" + target.getName() + "::kit"));
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
	public double getBalance(OfflinePlayer target) {
		return (economy != null) ? economy.getBalance(target) : 0;
	}
	
	public String getBalanceFormatted(OfflinePlayer target) {
		return (economy != null) ? economy.format(economy.getBalance(target)) : "$0.00";
	}
	
	public String getFaction(UUID uniqueId) {
		return PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(uniqueId), "%arimskript_faction%");
	}
	
	public void serverLog(String message) {
		center.getLogger().info(message);
	}
	
	public void log(String message) {
		writer.append("[" + dateformatter.get().format(new Date()) + "] " + message + "\n");
	}
	
	public boolean checkFile(File file) {
		try {
			if (file.exists() && file.canRead() && file.canWrite()) {
				return true;
			} else if (file.exists()) {
				file.delete();
			}
			if (!file.getParentFile().mkdirs()) {
				return false;
			}
			if (!file.createNewFile()) {
				return false;
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	public JavaPlugin center() {
		return center;
	}
	
	public Server server() {
		return center.getServer();
	}
	
	@Override
	public void close() {
		writer.close();
		broadcast.close();
		scoreboard.close();
		skript.close();
		writer = null;
	}
}
