package space.arim.misc.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import space.arim.lib.api.ArimLib;

public class Respawner implements Listener {

	private ArimLib api;
	private HashMap<UUID, Location> respawnable = new HashMap<>();

	public Respawner(ArimLib api) {
		this.api = api;
	}

	private Location skriptLoc(String variable) {
		return (api.skript().isVar(variable, Location.class)) ? api.skript().getVar(variable) : null;
	}
	
	private Location getHomeLoc(Player player) {
		Object x = api.skript().getVar("surv::" + player.getName() + "::home::x");
		Object y = api.skript().getVar("surv::" + player.getName() + "::home::y");
		Object z = api.skript().getVar("surv::" + player.getName() + "::home::z");
		Object world = api.skript().getVar("surv::" + player.getName() + "::home::world");
		return (x instanceof Number && y instanceof Number && z instanceof Number && world instanceof String)
				? new Location(Bukkit.getWorld((String) world), ((Number) x).doubleValue(), ((Number) y).doubleValue(),
						((Number) z).doubleValue())
				: null;
	}
	
	private Location getRespawn(Player target) {
		switch (target.getWorld().getName()) {
		case "KitPvP":
			return skriptLoc("spawn.KitPvP");
		case "FFA":
			return skriptLoc("spawn.FFA");
		case "KitPvP3":
			return skriptLoc("spawn.KitPvP3");
		case "Factions":
			return skriptLoc("spawn.Fac");
		case "Combo":
			return skriptLoc("spawn.Combo");
		case "TntWar":
			return skriptLoc("spawn.TntWar");
		case "Survival":
			Location homeLoc = getHomeLoc(target);
			return (homeLoc != null) ? homeLoc
					: target.getBedSpawnLocation() != null ? target.getBedSpawnLocation() : skriptLoc("spawn.Survival");
		default:
			return skriptLoc("spawn.Hub");
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerDeath(PlayerDeathEvent e) {
		respawnable.put(e.getEntity().getUniqueId(), getRespawn(e.getEntity()));
		String world = e.getEntity().getWorld().getName();
		if (world.equalsIgnoreCase("KitPvP") || world.equalsIgnoreCase("FFA") || world.equalsIgnoreCase("KitPvP3")
				|| world.equalsIgnoreCase("Duel") || world.equalsIgnoreCase("TntWar")
				|| world.equalsIgnoreCase("Combo")) {
			e.getDrops().clear();
			Bukkit.getScheduler().runTaskLater(api.center(), () -> e.getEntity().spigot().respawn(), 1L);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerRespawn(PlayerRespawnEvent e) {
		Location location = respawnable.remove(e.getPlayer().getUniqueId());
		if (location != null) {
			e.setRespawnLocation(location);
		}
	}
}
