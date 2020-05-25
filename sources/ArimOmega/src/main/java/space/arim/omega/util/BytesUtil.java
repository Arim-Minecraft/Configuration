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
package space.arim.omega.util;

/**
 * Utility class for boxing and unboxing byte arrays.
 * 
 * @author A248
 *
 */
public class BytesUtil {

	private BytesUtil() {}
	
	/**
	 * Boxes all bytes in the array
	 * 
	 * @param source the primitive array
	 * @return a boxed array
	 */
	public static Byte[] boxAll(byte[] source) {
		Byte[] result = new Byte[source.length];
		for (int n = 0; n < source.length; n++) {
			result[n] = source[n];
		}
		return result;
	}
	
	/**
	 * Boxes all bytes in the 2D array
	 * 
	 * @param source the 2D primive array
	 * @return a 2D boxed array
	 */
	public static Byte[][] boxAll2D(byte[][] source) {
		Byte[][] result = new Byte[source.length][];
		for (int n = 0; n < source.length; n++) {
			result[n] = boxAll(source[n]);
		}
		return result;
	}
	
	/**
	 * Unboxes all bytes in the array
	 * 
	 * @param source the boxed array
	 * @return a primitive array
	 */
	public static byte[] unboxAll(Byte[] source) {
		byte[] result = new byte[source.length];
		for (int n = 0; n < source.length; n++) {
			result[n] = source[n];
		}
		return result;
	}
	
	/**
	 * Unboxes all bytes in the 2D array
	 * 
	 * @param source the 2D boxed array
	 * @return a 2D primitive array
	 */
	public static byte[][] unboxAll2D(Byte[][] source) {
		byte[][] result = new byte[source.length][];
		for (int n = 0; n < source.length; n++) {
			result[n] = unboxAll(source[n]);
		}
		return result;
	}
	
}
