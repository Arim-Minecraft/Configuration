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
package space.arim.lib.kitpvp;

public enum Kit {
	Archer(KitSoundEnum.LEATHER),
	Astronaut(KitSoundEnum.GOLD),
	Dust(KitSoundEnum.SOFT),
	Giant((CustomSound) null),
	Gladiator(KitSoundEnum.CHAINMAIL),
	Gremlin((CustomSound) null),
	Hammer(KitSoundEnum.DIAMOND),
	Ninja(KitSoundEnum.LEATHER),
	Paladin(KitSoundEnum.GOLD),
	Pioneer(KitSoundEnum.LEATHER),
	Pyro(KitSoundEnum.CHAINMAIL),
	Sniper(KitSoundEnum.LEATHER),
	Snowman(KitSoundEnum.SOFT),
	Standard(KitSoundEnum.CHAINMAIL),
	Swordsman(KitSoundEnum.IRON),
	Tank(KitSoundEnum.DIAMOND),
	Werewolf((CustomSound) null),
	Wizard(KitSoundEnum.LEATHER);
	
	private final CustomSound sound;
	
	private Kit(CustomSound sound) {
		this.sound = sound;
	}
	
	private Kit(KitSoundEnum kitSound) {
		this(kitSound.toSound());
	}
	
	public CustomSound getSound() {
		return sound;
	}
	
	public static Kit fromString(String input) {
		for (Kit kit : Kit.values()) {
			if (kit.toString().equalsIgnoreCase(input)) {
				return kit;
			}
		}
		return null;
	}
	
	public enum ArmorType {
		DIAMOND(KitSoundEnum.DIAMOND),
		IRON(KitSoundEnum.IRON),
		CHAINMAIL(KitSoundEnum.CHAINMAIL),
		GOLD(KitSoundEnum.GOLD),
		LEATHER(KitSoundEnum.LEATHER),
		NONE(null);
		
		private final KitSoundEnum sound;
		
		private ArmorType(KitSoundEnum sound) {
			this.sound = sound;
		}
		
		public CustomSound toSound() {
			return sound.toSound();
		}
		
	}
}
