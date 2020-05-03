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
package space.arim.lib.util;

public class Pair<U, V> {
	
	private final U item1;
	private final V item2;
	
	public Pair(U item1, V item2) {
		this.item1 = item1;
		this.item2 = item2;
	}
	
	public U item1() {
		return item1;
	}
	
	public V item2() {
		return item2;
	}
	
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + ((item1 == null) ? 0 : item1.hashCode());
		result = 31 * result + ((item2 == null) ? 0 : item2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object anotherPair) {
		if (anotherPair instanceof Pair) {
			Pair<?, ?> otherPair = (Pair<?, ?>) anotherPair;
			return (this == anotherPair) || (Misc.equal(item1(), otherPair.item1())) && (Misc.equal(item2(), otherPair.item2())); 
		}
		return false;
	}
}
