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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import space.arim.shaded.org.slf4j.Logger;

import space.arim.universal.registry.Registry;
import space.arim.universal.registry.RegistryPriority;
import space.arim.universal.util.AutoClosable;

import space.arim.api.platform.PlatformUUIDResolution;
import space.arim.api.uuid.UUIDResolution;
import space.arim.api.uuid.UUIDUtil;

public class AltCheck extends PlatformUUIDResolution implements AutoClosable {

	private final Logger logger;
	private final File folder;
	private final Function<String, UUID> platformNameToUUID;
	private final Function<UUID, String> platformUUIDToName;
	
	private final ConcurrentHashMap<UUID, CacheElement> cache = new ConcurrentHashMap<UUID, CacheElement>();
	
	private volatile HashSet<CompletableFuture<?>> futures;
	
	private static final int ASYNC_IO_PARALLELISM_THRESHOLD = 10;
	
	public AltCheck(Logger logger, File folder, Registry registry, Function<String, UUID> platformNameToUUID, Function<UUID, String> platformUUIDToName) {
		this.logger = logger;
		this.folder = folder;
		this.platformNameToUUID = platformNameToUUID;
		this.platformUUIDToName = platformUUIDToName;
		registry.register(UUIDResolution.class, RegistryPriority.LOWER, this, "Arim AltCheck");
	}
	
	public void update(UUID uuid, String name, String address) {
		cache.computeIfAbsent(uuid, (u) -> new CacheElement(u, name, address)).update(name, address);
	}
	
	public Set<String> getPlayerNamesForIp(String address) {
		HashSet<String> players = new HashSet<String>();
		for (CacheElement element : cache.values()) {
			if (element.hasIp(address)) {
				players.add(element.getName());
			}
		}
		return players;
	}
	
	public Set<String> getIps(UUID uuid) {
		CacheElement element = cache.get(uuid);
		if (element != null) {
			Set<String> ips = element.getIps();
			if (ips != null) {
				return new HashSet<String>(ips);
			}
		}
		return Collections.emptySet();
	}
	
	@Override
	protected UUID resolveFromCache(String name) {
		for (CacheElement element : cache.values()) {
			if (element.getName().equalsIgnoreCase(name)) {
				return element.getUniqueId();
			}
		}
		UUID uuid1 = platformNameToUUID.apply(name);
		if (uuid1 != null) {
			update(uuid1, name, null);
			return uuid1;
		}
		return null;
	}
	
	@Override
	protected String resolveFromCache(UUID uuid) {
		CacheElement element = cache.get(uuid);
		if (element != null) {
			return element.getName();
		}
		String name1 = platformUUIDToName.apply(uuid);
		if (name1 != null) {
			update(uuid, name1, null);
			return name1;
		}
		return null;
	}
	
	private Runnable getFileLoadAction(File loadFile) {
		return () -> {
			UUID uuid = UUIDUtil.expandAndParse(loadFile.getName());
			try (Scanner scanner = new Scanner(loadFile, "UTF-8")) {
				if (scanner.hasNext()) {
					cache.put(uuid, CacheElement.fromString(uuid, scanner.next()));
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		};
	}
	
	public void startLoad() {
		File[] loadFiles = folder.listFiles();
		if (loadFiles != null) {

			if (loadFiles.length >= ASYNC_IO_PARALLELISM_THRESHOLD) {
				futures = new HashSet<>();
			}
			for (File loadFile : loadFiles) {

				Runnable cmd = getFileLoadAction(loadFile);
				if (futures != null) {
					futures.add(CompletableFuture.runAsync(cmd));

				} else {
					cmd.run();
				}
			}
		}
	}

	public void finishLoad() {
		if (futures != null) {
			futures.forEach((f) -> f.join()); // await termination
			futures = null;
		}
	}

	@Override
	public void close() {
		if (folder.isDirectory() || folder.mkdirs()) {
			cache.forEach(ASYNC_IO_PARALLELISM_THRESHOLD, (uuid, element) -> {

				File saveFile = new File(folder, uuid.toString().replace("-", ""));
				if (saveFile.exists() && !saveFile.delete()) {
					logger.warn("Could not override data file " + saveFile.getPath());

				} else {
					try (OutputStream output = new FileOutputStream(saveFile); OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8")) {
						writer.append(element.toString());
					} catch (IOException ex) {
						logger.warn("Failed saving UUID/IP cache to " + saveFile.getPath() + "!", ex);
					}
				}
			});
		} else {
			logger.warn("Could not create altcheck cache directory " + folder.getPath());
		}
	}

	@Override
	public void update(UUID uuid, String name, boolean force) {
		update(uuid, name, null);
	}

}
