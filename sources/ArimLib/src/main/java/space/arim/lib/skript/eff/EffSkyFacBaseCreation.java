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
package space.arim.lib.skript.eff;

import org.eclipse.jdt.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import space.arim.lib.ArimLibPlugin;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffSkyFacBaseCreation extends Effect {
	
	private Expression<Location> location;
	private Expression<Player> player;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		location = (Expression<Location>) exprs[0];
		player = (Expression<Player>) exprs[1];
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk create skyfaction base at " + location.toString(e, debug) + " for " + player.toString(e, debug);
	}
	
	@Override
	public void execute(Event e) {
		/*
		com.sk89q.worldedit.world.World world = new BukkitWorld(w);
		EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
		File file = new File(ArimLibPlugin.inst().center().getDataFolder(), "skyfacbase.schematic");
		try (FileInputStream fis = new FileInputStream(file)) {
			Operations.complete(
					new ClipboardHolder(ClipboardFormat.findByFile(file).getReader(fis).read(world.getWorldData()),
							world.getWorldData()).createPaste(session, world.getWorldData()).ignoreAirBlocks(true)
									.build());
		} catch (IOException | WorldEditException ex) {
			ex.printStackTrace();
			return false;
		}
		*/
		Location loc = location.getSingle(e);
		Player p = player.getSingle(e);
		Bukkit.getServer().getScheduler().runTaskAsynchronously(ArimLibPlugin.inst().center(),
				() -> (new SkyFacBaseCreator(loc)).create(p));
	}

}
