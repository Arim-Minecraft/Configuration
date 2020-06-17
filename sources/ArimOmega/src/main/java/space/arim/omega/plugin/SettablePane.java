/* 
 * ArimOmega
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ArimOmega is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ArimOmega is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ArimOmega. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.omega.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.Pane;

public class SettablePane extends Pane {

	private final List<GuiItem> items;
	
	protected SettablePane(int length, int height) {
		super(length, height);
		items = Arrays.asList(new GuiItem[length*height]);
	}

	/**
	 * Sets an item at a specific x, y relative to this pane's positioning.
	 * 
	 * @param x the relative x coordinate
	 * @param y the relative y coordinate
	 * @param item the gui item to set
	 */
	public void setItem(int x, int y, GuiItem item) {
		items.set(y*height + x, item);
	}
	
	@Override
	public void display(@NotNull Gui gui, @NotNull Inventory inventory, @NotNull PlayerInventory playerInventory,
			int paneOffsetX, int paneOffsetY, int maxLength, int maxHeight) {
		for (int index = 0; index < items.size(); index++) {
			GuiItem item = items.get(index);
			if (item == null || !item.isVisible()) {
				continue;
			}
			int x = paneOffsetX + index % Math.min(length, maxLength);
			int y = paneOffsetY + index / Math.min(length, maxLength);
			if (x > 9 || y > inventory.getSize() / 9) {
				  continue;
			}
			inventory.setItem(y*height + x, item.getItem());
		}
	}
	
	@Override
	public boolean click(@NotNull Gui gui, @NotNull InventoryClickEvent event, int paneOffsetX, int paneOffsetY,
			int maxLength, int maxHeight) {
		return items.get(y*height + x) != null;
	}

	@Override
	public @NotNull Collection<GuiItem> getItems() {
		return items;
	}

	@Override
	public @NotNull Collection<Pane> getPanes() {
		return new HashSet<>();
	}

	@Override
	public void clear() {
		for (int n = 0; n < items.size(); n++) {
			items.set(n, null);
		}
	}
	
	@Override
	public void setHeight(int height) {
		throw new UnsupportedOperationException("SettablePane has immutable dimensions");
	}
	
	@Override
	public void setLength(int length) {
		throw new UnsupportedOperationException("SettablePane has immutable dimensions");
	}
	
}
