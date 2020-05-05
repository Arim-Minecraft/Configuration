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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import space.arim.shaded.com.github.benmanes.caffeine.cache.Cache;
import space.arim.shaded.com.github.benmanes.caffeine.cache.Caffeine;

import space.arim.universal.util.AutoClosable;

import space.arim.serverstarter.ServerStarter;

/**
 * Yes, we sometimes contradict the convention about underscores in method declarations. <br>
 * However, this is a good example where it actually makes the code cleaner, as you may see.
 * 
 * @author A248
 *
 */
public class OmegaDataLoader implements Listener, AutoClosable {

	private final Omega omega;
	
	final Cache<UUID, PartialPlayer> pending;
	
	OmegaDataLoader(final Omega omega) {
		this.omega = omega;
		pending = Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES)
				.<UUID, PartialPlayer>removalListener((uuid, partial, cause) -> partial.abort(omega)).build();
		ServerStarter.afterAllowed = this::onAPPLE_start;
	}
	
	private void onAPPLE_start(AsyncPlayerPreLoginEvent evt) {

		UUID uuid = evt.getUniqueId();
		String name = evt.getName();
		byte[] address = evt.getAddress().getAddress();

		PartialPlayer partial;

		OmegaPlayer existing = omega.getPlayer(uuid);
		if (existing == null) {
			partial = new PendingPlayer(uuid, name, address);

		} else {
			// the previous player data hasn't finished saving and unloading yet
			partial = new ExistingPlayer(existing, name, address);
		}
		pending.put(evt.getUniqueId(), partial);

		partial.begin(omega);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	private void onAPPLE_complete(AsyncPlayerPreLoginEvent evt) {

		UUID uuid = evt.getUniqueId();
		if (evt.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {

			pending.getIfPresent(uuid).joinLoading();
		} else { // discard
			pending.invalidate(uuid);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onLogin_start(PlayerLoginEvent evt) {

		PartialPlayer partial = pending.getIfPresent(evt.getPlayer().getUniqueId());
		assert partial != null;

	}

	@EventHandler(priority = EventPriority.MONITOR)
	private void onLogin_complete(PlayerLoginEvent evt) {

		UUID uuid = evt.getPlayer().getUniqueId();

		PartialPlayer partial = pending.getIfPresent(uuid);
		assert partial != null;

		if (evt.getResult() != PlayerLoginEvent.Result.ALLOWED) {
			// discard
			pending.invalidate(uuid);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerJoin(PlayerJoinEvent evt) {

		Player player = evt.getPlayer();
		UUID uuid = player.getUniqueId();

		omega.add(player, pending.getIfPresent(uuid)).onPlayerJoin(player, new TransientPlayer(omega, player));

		pending.invalidate(uuid);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onTeleportVanishPatch(PlayerTeleportEvent evt) {
		TransientPlayer player = omega.getTransientPlayer(evt.getPlayer());
		if (player.isVanish()) {
			player.hidePlayerFromUnvanished();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onWorldChange(PlayerChangedWorldEvent evt) {
		omega.getTransientPlayer(evt.getPlayer()).changeWorld();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	private void onPlayerQuit(PlayerQuitEvent evt) {

		OmegaPlayer player = omega.getPlayer(evt.getPlayer().getUniqueId());
		player.nullifyTransientInfo();

		// TODO Using priority MONITOR until we can transition away from Skript
		// at which point we'll move this to LOWEST to start saving ASAP
		player.save(omega);
	}
	
	@Override
	public void close() {
		pending.invalidateAll();
	}

}
