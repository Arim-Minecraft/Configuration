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

import java.util.Arrays;

/**
 * Utility class for boxing and unboxing bytes.
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
	
	/**
	 * Checks whether a 2D byte array contains a 1D array
	 * 
	 * @param targetArray the array in which to check
	 * @param element the element to check for
	 * @return true if the array contains the element, false otherwise
	 */
	public static boolean arrayContains2D(byte[][] targetArray, byte[] element) {
		for (byte[] existing : targetArray) {
			if (Arrays.equals(element, existing)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes the first element in the array, shifts all other elements,
	 * and adds a null element to the end. <br>
	 * The resulting array has thus the same size as the source.
	 * 
	 * @param source the source array
	 * @param elementToAdd the element to add
	 * @return the resulting array with elements shifted
	 */
	public static byte[][] popFirstThenPadOne2D(byte[][] source) {
		byte[][] updated = new byte[source.length][];
		for (int n = 1; n < source.length; n ++) {
			updated[n - 1] = source[n];
		}
		return updated;
	}
	
}
