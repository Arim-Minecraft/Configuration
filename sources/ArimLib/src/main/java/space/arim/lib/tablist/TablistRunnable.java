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
package space.arim.lib.tablist;

import java.util.concurrent.TimeUnit;

public class TablistRunnable implements Runnable {
	
	private final TablistManager manager;
	
	TablistRunnable(TablistManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				updateTabLists();
				TimeUnit.MILLISECONDS.sleep(3950L);
			} catch (InterruptedException ex) {}
		}		
	}
	
	private void updateTabLists() {
		String header = getHeader();
		String footer = getFooter();
		manager.lib.forEach((uuid, tablist) -> {
			tablist.setHeader(header);
			tablist.setFooter(footer);
			tablist.update();
		});
	}
	
	private String getHeader() {
		return "&r" + '\n' +
				"&b&m-----&3&m-----&8&m-----&8[ &6&lArim &8]&8&m-----&3&m-----&b&m-----" + '\n' +
				"&r" + '\n' +
				"&7Website: &3&lwww.arim.space" + '\n' +
				"&7Discord: &3&l8pRkq8u" + '\n' +
				"&r" + '\n' +
				"&9Online: &6" + manager.getOnlineThreadSafe() + "&r&7/100" + '\n' +
				"&r";
	}
	
	private String getFooter() {
		return "&r" + '\n' +
				"&b&m-----&3&m-----&8&m-----&8[ &6&lArim &8]&8&m-----&3&m-----&b&m-----";
	}
	
}
