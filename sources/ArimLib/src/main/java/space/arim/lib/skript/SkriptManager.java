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
package space.arim.lib.skript;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import space.arim.universal.util.AutoClosable;

import space.arim.lib.ArimLibPlugin;
import space.arim.lib.api.ArimLib;
import space.arim.lib.skript.cond.CondFilterMsg;
import space.arim.lib.skript.cond.CondGroundState;
import space.arim.lib.skript.cond.CondWhetherIdOnline;
import space.arim.lib.skript.cond.CondWhetherPlayerOnline;
import space.arim.lib.skript.eff.EffApplySounding;
import space.arim.lib.skript.eff.EffChangeGlobalStatusHover;
import space.arim.lib.skript.eff.EffChangeGlobalStatusMotd;
import space.arim.lib.skript.eff.EffCollectRevenueAt;
import space.arim.lib.skript.eff.EffCustomSound;
import space.arim.lib.skript.eff.EffEliminateSounding;
import space.arim.lib.skript.eff.EffFixHeldSlot;
import space.arim.lib.skript.eff.EffForceRespawning;
import space.arim.lib.skript.eff.EffMarkAutoPickup;
import space.arim.lib.skript.eff.EffOpenEntireServer;
import space.arim.lib.skript.eff.EffSkyFacBaseCreation;
import space.arim.lib.skript.eff.EffWipePotionEffects;
import space.arim.lib.skript.expr.ExprChunkLoaded;
import space.arim.lib.skript.expr.ExprConnectIp;
import space.arim.lib.skript.expr.ExprFindTopLocationAt;
import space.arim.lib.skript.expr.ExprFormatChat;
import space.arim.lib.skript.expr.ExprGenerateRandomCode;
import space.arim.lib.skript.expr.ExprItemPickedUp;
import space.arim.lib.skript.expr.ExprNewLocation;
import space.arim.lib.skript.expr.ExprPlayerFromId;
import space.arim.lib.skript.expr.ExprPlayerFromName;
import space.arim.lib.skript.expr.ExprPlayerVanishability;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.variables.Variables;
import lombok.Getter;

public class SkriptManager implements AutoClosable {
	
	private final ArimLib api;
	@Getter
	private final SkriptHelper helper;
	
	private static final List<String> dottedList;
	
	static {
		dottedList = new ArrayList<String>();
		dottedList.add("...");
	}
	
	public SkriptManager(ArimLib api) {
		this.api = api;
		helper = new SkriptHelper();
	}
	
	public static void deprecatedSyntaxWarning(String syntax) {
		ArimLibPlugin.inst().center().getLogger().warning("Deprecated syntax usage, please remove: " + syntax);
	}
	
	public void load() {
		
		if (!Bukkit.getPluginManager().isPluginEnabled("Skript") || !Skript.isAcceptRegistrations()) {
			throw new IllegalStateException("Skript not accepting registrations");
		}
		Skript.registerAddon(api.center());

		Skript.registerCondition(CondWhetherIdOnline.class, "[(arimsk|arimlib)] whether id %string% online");
		Skript.registerCondition(CondWhetherPlayerOnline.class, "[(arimsk|arimlib)] whether name %string% online");
		Skript.registerCondition(CondGroundState.class, "[(arimsk|arimlib)] whether %entity% on ground");
		Skript.registerCondition(CondFilterMsg.class, "[(arimsk|arimlib)] filter %string% by %strings%");

		Skript.registerEffect(EffApplySounding.class,
				"[(arimsk|arimlib)] apply sounding for %player% as %string%, %number%, %number%");
		Skript.registerEffect(EffChangeGlobalStatusHover.class,
				"[(arimsk|arimlib)] change global status hover to %string%");
		Skript.registerEffect(EffChangeGlobalStatusMotd.class,
				"[(arimsk|arimlib)] change global status motd to %string%");
		Skript.registerEffect(EffCollectRevenueAt.class, "[(arimsk|arimlib)] collect revenue at %location% for %player%");
		Skript.registerEffect(EffCustomSound.class,
				"[arimlib] play [custom] sound %string% for %players% at %location% with volume %number% pitch %number%");
		Skript.registerEffect(EffEliminateSounding.class, "[(arimsk|arimlib)] eliminate sounding for %player%");
		Skript.registerEffect(EffFixHeldSlot.class, "[(arimsk|arimlib)] fix held slot for %player%");
		Skript.registerEffect(EffForceRespawning.class, "[(arimsk|arimlib)] force respawning for %player%");
		Skript.registerEffect(EffMarkAutoPickup.class, "[(arimsk|arimlib)] mark auto pickup");
		Skript.registerEffect(EffOpenEntireServer.class, "[(arimsk|arimlib)] open entire server");
		Skript.registerEffect(EffSkyFacBaseCreation.class,
				"[(arimsk|arimlib)] create skyfaction base at %location% for %player%");
		Skript.registerEffect(EffWipePotionEffects.class, "[(arimsk|arimlib)]wipe potions from %player%");

		Skript.registerExpression(ExprChunkLoaded.class, Boolean.class, ExpressionType.PROPERTY,
				"[(arimsk|arimlib)] loaded status of %location%");
		Skript.registerExpression(ExprConnectIp.class, String.class, ExpressionType.SIMPLE,
				"[(arimsk|arimlib)] connect[(ion|ed)] ip [address]");
		Skript.registerExpression(ExprFindTopLocationAt.class, Location.class, ExpressionType.PROPERTY,
				"[(arimsk|arimlib)] find top location at %location%");
		Skript.registerExpression(ExprFormatChat.class, String.class, ExpressionType.SIMPLE,
				"[(arimsk|arimlib)] [the] chat format[ting]");
		Skript.registerExpression(ExprGenerateRandomCode.class, String.class, ExpressionType.SIMPLE,
				"[(arimsk|arimlib)] generate random code of length %number%");
		Skript.registerExpression(ExprItemPickedUp.class, Item.class, ExpressionType.SIMPLE,
				"[(arimsk|arimlib)] item-picked-up");
		Skript.registerExpression(ExprNewLocation.class, Location.class, ExpressionType.COMBINED,
				"[(arimsk|arimlib)] new location from %number%, %number%, %number% in [world] %string% with [yaw] %number%, [pitch] %number%");
		Skript.registerExpression(ExprPlayerFromId.class, Player.class, ExpressionType.SIMPLE,
				"[(arimsk|arimlib)] player from id %string%");
		Skript.registerExpression(ExprPlayerFromName.class, Player.class, ExpressionType.SIMPLE,
				"[(arimsk|arimlib)] player from name %string%");
		Skript.registerExpression(ExprPlayerVanishability.class, Boolean.class, ExpressionType.PROPERTY,
				"[(arimsk|arimlib)] vanishability %player%");
	}
	
	/**
	 * Checks if the variable exists and is an instance of the desired type. <br>
	 * We take care to lowercase the variable name so you don't have to.
	 * 
	 * @param variable the variable, case insensitive
	 * @param type the type
	 * @return true if an instance of the type and nonnull, false otherwise
	 */
	public boolean isVar(String variable, Class<?> type) {
		return type.isInstance(Variables.getVariable(variable.toLowerCase(Locale.ENGLISH), null, false));
	}
	
	/**
	 * Gets a variable as the desired type. <br>
	 * We take care to lowercase the variable name so you don't have to.
	 * 
	 * @param <T> the type
	 * @param variable the variable, case insensitive
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getVar(String variable) {
		return (T) Variables.getVariable(variable.toLowerCase(Locale.ENGLISH), null, false);
	}
	
	/**
	 * Sets the variable to the specified value <br>
	 * We take care to lowercase the variable name so you don't have to. <br>
	 * <br>
	 * If it's a list variable (::*), the value MUST be null. To set a node
	 * inside a list variable, instead use <code>setVar("list::1", value);</code>
	 * 
	 * @param variable the variable, case insensitive
	 * @param value the value
	 */
	public void setVar(String variable, Object value) {
		Variables.setVariable(variable.toLowerCase(Locale.ENGLISH), value, null, false);
	}
	
	/**
	 * Deletes a variable
	 * 
	 * @param variable the variable, case insensitive
	 */
	public void deleteVar(String variable) {
		setVar(variable, null);
	}
	
	/**
	 * Sets a list variable. If the provided list is null,
	 * a list containing only a single element, "...", is set.
	 * 
	 * @param variable with or without "::*", doesn't matter
	 * @param list the list to set
	 */
	void setListOrDot(String variable, List<?> list) {
		setList((variable.endsWith("::*") ? variable.substring(0, variable.length() - 3) : variable), (list == null) ? dottedList : list);
	}
	
	private void setList(String variable, List<?> list) {
		setVar(variable + "::*", null);
		for (int n = 0; n < list.size(); n++) {
			setVar(variable + "::" + n, list.get(n));
		}
	}
	
	@Override
	public void close() {
		
	}

}
