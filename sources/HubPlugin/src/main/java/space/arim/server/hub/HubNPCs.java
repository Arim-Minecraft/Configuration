/* 
 * HubPlugin
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * HubPlugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * HubPlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with HubPlugin. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.server.hub;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import space.arim.api.concurrent.AsyncStartingModule;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.NPCLibOptions;
import net.jitse.npclib.NPCLibOptions.MovementHandling;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.skin.Skin;

public class HubNPCs implements AsyncStartingModule, Listener {

	private final NPCLib lib;
	
	private final HashMap<UUID, String> cmds = new HashMap<>();
	
	public HubNPCs(HubPlugin hub) {
		lib = new NPCLib(hub, new NPCLibOptions().setMovementHandling(MovementHandling.repeatingTask(4L)));
	}
	
	@Override
	public void startLoad() {
		
	}

	@Override
	public void finishLoad() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		//Executor executor = (cmd) -> Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, cmd);
		World hubWorld = Bukkit.getWorld("Hub");
		
		// Ecotastic, Kit PvP, 2070113374, -3.5 128 11.5 Hub 225 5
		NPC kitpvp = lib.createNPC(singleton("&4&lKit PvP"));
		fetchSkinAndApply(kitpvp, 2070113374, executor);
		kitpvp.setLocation(new Location(hubWorld, -3.5, 128, 11.5, 225, 5));
		kitpvp.create();
		cmds.put(kitpvp.getUniqueId(), "kitpvp");
		
		// Ecogestic, Minigames, 1019560908, 8.5 128 11.5 Hub 135 5
		NPC minigames = lib.createNPC(singleton("&a&lMinigames"));
		fetchSkinAndApply(minigames, 1019560908, executor);
		minigames.setLocation(new Location(hubWorld, 8.5, 128, 11.5, 135, 5));
		minigames.create();
		cmds.put(minigames.getUniqueId(), "minigames");
		
		// Aerodactyl_, SkyFactions, 100680423, -3.5 128 -0.5 Hub 315 5
		NPC skyfactions = lib.createNPC(singleton("&9&lSkyFactions"));
		fetchSkinAndApply(skyfactions, 100680423, executor);
		skyfactions.setLocation(new Location(hubWorld, -3.5, 128, -0.5, 315, 5));
		skyfactions.create();
		cmds.put(skyfactions.getUniqueId(), "skyfactions");
		
		// jecode, Survival, 79576070, 8.5 128 -0.5 Hub 45 5
		NPC survival = lib.createNPC(singleton("&a&lAnarchy &r&2&lSurvival"));
		fetchSkinAndApply(survival, 79576070, executor);
		survival.setLocation(new Location(hubWorld, 8.5, 128, -0.5, 45, 5));
		survival.create();
		cmds.put(survival.getUniqueId(), "survival");
		
		executor.shutdown();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	private void onNPCInteract(NPCInteractEvent evt) {
		Bukkit.dispatchCommand(evt.getWhoClicked(), cmds.get(evt.getNPC().getUniqueId()));
	}
	
	private List<String> singleton(String element) {
		return Collections.singletonList(ChatColor.translateAlternateColorCodes('&', element));
	}
	
	private CompletableFuture<?> fetchSkinAndApply(NPC npc, int id, Executor executor) {
		return fetchSkin(id, executor).thenAccept((skin) -> npc.setSkin(skin));
	}
	
	private CompletableFuture<Skin> fetchSkin(int id, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {

			Scanner scanner = null;
			HttpURLConnection httpURLConnection = null;
			StringBuilder builder = null;
			try {

				httpURLConnection = (HttpURLConnection) (new URL("https://api.mineskin.org/get/id/" + id)).openConnection();
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setDoInput(true);
				httpURLConnection.connect();

				scanner = new Scanner(httpURLConnection.getInputStream());
				builder = new StringBuilder();
				while (scanner.hasNextLine()) {
					builder.append(scanner.nextLine());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				return null;

			} finally {
				if (scanner != null) {
					scanner.close();
				}
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
			}
			JsonObject textures = ((JsonObject) (new JsonParser()).parse(builder.toString())).get("data")
					.getAsJsonObject().get("texture").getAsJsonObject();
			return new Skin(textures.get("value").getAsString(), textures.get("signature").getAsString());
		}, executor);
	}
	
}
