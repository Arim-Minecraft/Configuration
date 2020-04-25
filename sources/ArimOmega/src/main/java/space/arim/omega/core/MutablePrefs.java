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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.Setter;

public class MutablePrefs {

	@Getter
	private final AtomicBoolean tree;
	@Getter
	private final AtomicBoolean item;
	@Getter
	private final AtomicBoolean msg;
	@Getter
	@Setter
	private volatile String colour;
	@Getter
	@Setter
	private volatile String namecolour;
	@Getter
	private final AtomicBoolean sound;
	@Getter
	private final AtomicBoolean bypasspm;
	@Getter
	private final AtomicBoolean sidebar;
	@Getter
	@Setter
	private volatile KitDesc kitdesc;
	@Getter
	private final AtomicBoolean worldchat;
	@Getter
	private final List<UUID> friends;
	@Getter
	private final List<UUID> ignored;
	
	MutablePrefs(boolean tree, boolean item, boolean msg, String colour, String namecolour, boolean sound,
			boolean bypasspm, boolean sidebar, int kitdesc, boolean worldchat, List<UUID> friends, List<UUID> ignored) {
		this.tree = new AtomicBoolean(tree);
		this.item = new AtomicBoolean(item);
		this.msg = new AtomicBoolean(msg);
		this.colour = colour;
		this.namecolour = namecolour;
		this.sound = new AtomicBoolean(sound);
		this.bypasspm = new AtomicBoolean(bypasspm);
		this.sidebar = new AtomicBoolean(sidebar);
		this.kitdesc = KitDesc.values()[kitdesc];
		this.worldchat = new AtomicBoolean(worldchat);
		this.friends = friends;
		this.ignored = ignored;
	}
	
	public enum KitDesc {
		
		ON,
		PARTIAL,
		OFF;
		
	}
	
}
