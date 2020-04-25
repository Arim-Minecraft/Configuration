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
import lombok.Getter;
import lombok.Setter;

public class SqlPlayerStats {

	@Getter @Setter private volatile long balance;
	@Getter @Setter private volatile int kitpvp_kills;
	@Getter @Setter private volatile int kitpvp_deaths;
	@Getter @Setter private volatile int combo_kills;
	@Getter @Setter private volatile int combo_deaths;
	
	@Getter @Setter private volatile MutablePrefs prefs;
	@Getter @Setter private volatile List<UUID> friends;
	@Getter @Setter private volatile List<UUID> ignored;
	@Getter @Setter private volatile long monthly_reward;
	
}
