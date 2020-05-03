package space.arim.misc.listener;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import space.arim.lib.api.ArimLib;
import space.arim.lib.api.Game;
import space.arim.misc.Misc;

import be.isach.ultracosmetics.UltraCosmetics;
import me.libraryaddict.disguise.DisguiseAPI;

public class General implements Listener {
	private Misc misc;
	private ArimLib api;
	
	public General(Misc misc, ArimLib api) {
		this.misc = misc;
		this.api = api;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	private void netherPortalWGFix(BlockPhysicsEvent evt) {
		if (evt.isCancelled() && evt.getBlock().getType().equals(Material.PORTAL)
				&& evt.getBlock().getWorld().getName().contains("Survival")) {
			evt.setCancelled(false);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	private void doubleJumpComplete(PlayerToggleFlightEvent evt) {
		Player player = evt.getPlayer();
		if (player.getWorld().getName().equals("Hub") && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
			evt.setCancelled(true);
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setVelocity(player.getLocation().getDirection().multiply(3));
			player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void doubleJumpStart(PlayerMoveEvent evt) { // hestitate to use but our code is fast
		Player player = evt.getPlayer();
		if (player.getWorld().getName().equals("Hub") && !player.isFlying() && player.isOnGround()) {
			player.setAllowFlight(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onProjectileHit(ProjectileHitEvent evt) {
		Projectile projectile = evt.getEntity();
		if (projectile.getType().equals(EntityType.ARROW) && projectile.getFireTicks() > 0) {
			Location loc = projectile.getLocation();
			Game game = api.parseGame(loc.getWorld());
			if (game == Game.SkyFactions) {
				double x = loc.getX();
				double z = loc.getZ();
				if ((x > 32 || x < -32) && (z > 32 || z < -32)) {
					Material type = loc.getBlock().getType();
					if (type == null || type.equals(Material.AIR)) {
						loc.getBlock().setType(Material.FIRE);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	private void onBlockBreak(ItemSpawnEvent evt) {
		Player player = api.skript().getHelper().getAutoPickupItems().remove(evt.getLocation().getBlock().getLocation());
		if (player != null) {
			ItemStack itemToAdd = evt.getEntity().getItemStack();
			Map<Integer, ItemStack> leftovers = player.getInventory().addItem(itemToAdd);
			evt.setCancelled(true);
			if (!leftovers.isEmpty()) {
				player.getWorld().dropItemNaturally(evt.getLocation(), leftovers.get(0));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	private void cacheImmediately(AsyncPlayerPreLoginEvent evt) {
		misc.getAltCheck().update(evt.getUniqueId(), evt.getName(), evt.getAddress().getHostAddress());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onJoin(PlayerJoinEvent evt) {
		api.broadcast().addPlayer(evt.getPlayer());
		api.scoreboard().addBoard(evt.getPlayer());
		api.tablists().addTablist(evt.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onWorldChange(PlayerChangedWorldEvent evt) {
		api.scoreboard().changeWorld(evt.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onQuit(PlayerQuitEvent evt) {
		api.broadcast().removePlayer(evt.getPlayer());
		api.scoreboard().removeBoard(evt.getPlayer());
		api.tablists().removeTablist(evt.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void removeArrowsOnDeath(PlayerRespawnEvent e) {
		if (e.getPlayer().getWorld().getName().equalsIgnoreCase("KitPvP")
				|| e.getPlayer().getWorld().getName().equalsIgnoreCase("FFA")
				|| e.getPlayer().getWorld().getName().equalsIgnoreCase("TntWar")) {
			Bukkit.getScheduler().runTaskLater(api.center(), () -> api.setStuckArrows(e.getPlayer(), (byte) 0), 1L);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void removeArrowsAlways(EntityDamageByEntityEvent e) {
		if (e.getEntity().getWorld().getName().equalsIgnoreCase("Duel")) {
			if (e.getDamager() instanceof Arrow && e.getEntity() instanceof Player) {
				Player player = (Player) e.getEntity();
				Bukkit.getScheduler().runTaskLater(api.center(),
						() -> api.setStuckArrows(player, (byte) 0), 1L);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void setBed(PlayerBedEnterEvent e) {
		if (e.getPlayer().getWorld().getName().equalsIgnoreCase("Survival")) {
			e.getPlayer().setBedSpawnLocation(e.getBed().getLocation(), true);
			api.skript().setVar("save_bedspawn::" + e.getPlayer().getUniqueId(), e.getBed().getLocation());
		} else if (api.skript().isVar("save_bedspawn::" + e.getPlayer().getUniqueId(), Location.class)) {
			Location bedloc = api.skript().getVar("save_bedspawn::" + e.getPlayer().getUniqueId());
			Bukkit.getScheduler().runTaskLater(api.center(), () -> e.getPlayer().setBedSpawnLocation(bedloc, true), 1L);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onSpectatorTeleport(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.SPECTATE) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void clearMorphs(PlayerTeleportEvent e) {
		Plugin uc = api.center().getServer().getPluginManager().getPlugin("UltraCosmetics");
		if (uc instanceof UltraCosmetics) {
			((UltraCosmetics) uc).getPlayerManager().getUltraPlayer(e.getPlayer()).removeMorph();
			DisguiseAPI.undisguiseToAll(e.getPlayer());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void playerIllegalClientDetection(PlayerKickEvent e) {
		if (e.getReason().equals("Illegal characters in chat")) {
			e.setCancelled(true);
			e.getPlayer().getServer().dispatchCommand(e.getPlayer().getServer().getConsoleSender(),
					"aakick " + e.getPlayer().getName() + " invalchat");
		} else if (e.getReason().equals("Reason: Auto-banned for attempting to use WorldDownloader.")
				|| e.getReason().equals("&c&lReason: &7Auto-banned for attempting to use WorldDownloader.")
				|| e.getReason().equals(ChatColor.translateAlternateColorCodes('&',
						"&c&lReason: &7Auto-banned for attempting to use WorldDownloader."))) {
			e.setCancelled(true);
			e.getPlayer().getServer().dispatchCommand(e.getPlayer().getServer().getConsoleSender(),
					"aakick " + e.getPlayer().getName() + " wdl");
		} else if (e.getReason().equals("disconnect.spam")) {
			e.setCancelled(true);
			e.getPlayer().getServer().dispatchCommand(e.getPlayer().getServer().getConsoleSender(),
					"aakick " + e.getPlayer().getName() + " spamdisconnect");
		}
	}
	
	/*
	 * @EventHandler(priority=EventPriority.LOWEST) private void
	 * BetterHoppers(InventoryMoveItemEvent e) { ItemStack[] array =
	 * e.getDestination().getContents(); for (int n = 0; n < array.length; n++) { if
	 * (array[n] == null) { return; } else if ((e.getItem().isSimilar(array[n])) &&
	 * ((array[n].getAmount() + e.getItem().getAmount()) <
	 * (e.getItem().getMaxStackSize() + 1))) { return; } } e.setCancelled(true); }
	 */
	/*
	 * @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) private
	 * void AutoFixItems(PlayerItemDamageEvent e) { if
	 * ((e.getPlayer().getWorld().getName().equalsIgnoreCase("KitPvP") ||
	 * e.getPlayer().getWorld().getName().equalsIgnoreCase("FFA") ||
	 * e.getPlayer().getWorld().getName().equalsIgnoreCase("Duel")) &&
	 * (!e.getItem().getItemMeta().spigot().isUnbreakable())) {
	 * e.getItem().getItemMeta().spigot().setUnbreakable(true); } }
	 */
	/*
	 * @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) private
	 * void AutoReturnArrows(EntityDamageByEntityEvent e) { if
	 * (e.getEntity().getWorld().getName().equalsIgnoreCase("KitPvP") ||
	 * e.getEntity().getWorld().getName().equalsIgnoreCase("FFA")) { if
	 * (e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {
	 * ProjectileSource shooter = ((Arrow) e.getDamager()).getShooter(); if (shooter
	 * instanceof Player) { ((Player) shooter).getInventory().addItem(new
	 * ItemStack(Material.ARROW)); } } } }
	 */
}
