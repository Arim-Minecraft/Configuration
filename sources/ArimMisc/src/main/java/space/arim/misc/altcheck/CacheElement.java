/* 
 * ArimMisc
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * ArimMisc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ArimMisc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ArimMisc. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.misc.altcheck;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import space.arim.api.uuid.UUIDUtil;

import space.arim.lib.ArimLibPlugin;

class CacheElement {
	
	private final UUID uuid;
	private volatile String name;
	private volatile Set<String> ips;
	
	CacheElement(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		ips = (ip == null || ip.isEmpty()) ? null : new HashSet<String>(Arrays.asList(ip.split(",")));
	}
	
	void update(String name, String ip) {
		this.name = name;
		if (ip != null && !ip.isEmpty()) {
			synchronized (this) {
				if (ips == null) {
					ips = new HashSet<String>();
				}
				ips.add(ip);
			}
		}
	}
	
	boolean hasIp(String address) {
		return ips != null && ips.contains(address);
	}
	
	UUID getUniqueId() {
		return uuid;
	}
	
	String getName() {
		return name;
	}
	
	Set<String> getIps() {
		return ips;
	}
	
	@Override
	public String toString() {
		String iplist;
		if (ips == null || ips.isEmpty()) {
			iplist = "";
		} else {
			StringBuilder builder = new StringBuilder();
			ips.forEach((ip) -> builder.append(',').append(ip));
			iplist = builder.toString();
		}
		ArimLibPlugin.inst().center().getLogger().info("IPs from ArimMisc are " + iplist);
		return name + "|" + iplist;
	}
	
	static CacheElement fromStringWithUUID(String input) {
		String[] data = input.split("\\|");
		return new CacheElement(UUIDUtil.expandAndParse(data[0]), data[1], (data.length > 2) ? data[1] : null);
	}
	
	static CacheElement fromString(UUID uuid, String input) {
		String[] data = input.split("\\|");
		return new CacheElement(uuid, data[0], (data.length > 1) ? data[1] : null);
	}
	
}
