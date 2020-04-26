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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import space.arim.serverstarter.ServerStarter;

/**
 * Yes, we sometimes contradict the convention about underscores in method declarations. <br>
 * However, this is a good example where it actually makes the code cleaner, as you may see.
 * 
 * @author A248
 *
 */
public class OmegaDataLoader implements Listener{

	private final Omega omega;
	
	final Cache<UUID, PartialPlayer> pending;
	
	OmegaDataLoader(final Omega omega) {
		this.omega = omega;
		ServerStarter.afterAllowed = this::onAPPLE_start;
		pending = Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES)
				.<UUID, PartialPlayer>removalListener((uuid, partial, cause) -> partial.abort(omega)).build();
	}
	
	private void onAPPLE_start(AsyncPlayerPreLoginEvent evt) {

		PartialPlayer partial;

		OmegaPlayer existing = omega.getPlayer(evt.getUniqueId());
		if (existing == null) {
			partial = new PendingPlayer(evt.getUniqueId());

		} else {
			// the previous player data hasn't finished saving and unloading yet
			partial = new ExistingPlayer(existing);
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

		omega.add(player, pending.getIfPresent(uuid)).applyDisplayNames(player);
		
		pending.invalidate(uuid);
	}

	// TODO Using priority MONITOR until we can transition away from Skript
	// at which point we'll use LOWEST to start saving ASAP
	@EventHandler(priority = EventPriority.MONITOR)
	private void onPlayerQuit(PlayerQuitEvent evt) {

		omega.getPlayer(evt.getPlayer().getUniqueId()).save(omega);
	}

}
