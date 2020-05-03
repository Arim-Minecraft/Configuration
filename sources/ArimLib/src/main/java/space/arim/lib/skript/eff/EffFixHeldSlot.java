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

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.eclipse.jdt.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import space.arim.lib.ArimLibPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffFixHeldSlot extends Effect {

	private Expression<Player> player;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		player = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "arimsk fix held slot for " + player.toString(e, debug);
	}

	@Override
	protected void execute(Event e) {
		Player player = this.player.getSingle(e);
		int protocolSlot = 36 + player.getInventory().getHeldItemSlot();
		ItemStack item = player.getItemInHand();

		PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_SLOT);
		container.getModifier().writeDefaults();
		container.getIntegers().write(0, 0);
		container.getIntegers().write(1, protocolSlot);
		container.getItemModifier().write(0, item);
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, container);
		} catch (InvocationTargetException ex) {
			ArimLibPlugin.inst().center().getLogger().log(Level.WARNING, "Could not fix held item slot for " + player.getName(), ex);
		}
	}
	
	
	
}
