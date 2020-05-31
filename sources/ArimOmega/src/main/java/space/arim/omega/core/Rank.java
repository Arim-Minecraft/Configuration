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

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Rank {

	@Getter
	private final String id;
	@Getter
	private final String display;
	@Getter
	private final String tag;
	@Getter
	private final String json;
	
	public static final Rank NONE = new Rank("default", "", "", "");
	
	public String getPermission() {
		return "arim.rank." + id;
	}
	
	@Override
	public int hashCode() {
		return 31 + id.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		return (this == object || (object instanceof Rank) && id.equals(((Rank) object).id));
	}
	
}
