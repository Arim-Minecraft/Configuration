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

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.arim.shaded.com.zaxxer.hikari.HikariConfig;

import space.arim.universal.util.AutoClosable;

import space.arim.api.util.sql.CloseMe;
import space.arim.api.util.sql.HikariPoolSqlBackend;
import space.arim.api.util.sql.SqlQuery;

public class OmegaSql extends HikariPoolSqlBackend implements AutoClosable {
	
	private final ExecutorService asyncExecutor;
	
	private static final Logger logger = LoggerFactory.getLogger(OmegaSql.class);
	
	OmegaSql(String host, int port, String database, String url, String username, String password, int connections) {
		super(buildHikariConfig(host, port, database, url, username, password, connections));
		asyncExecutor = Executors.newFixedThreadPool(connections);
	}
	
	private static HikariConfig buildHikariConfig(String host, int port, String database, String url, String username,
			String password, int connections) {

		HikariConfig config = new HikariConfig();
		config.setPoolName("Omega Connection Pool");
		config.setMinimumIdle(connections);
		config.setMaximumPoolSize(connections);
		//config.addDataSourceProperty("autoReconnect", "true");
		//config.addDataSourceProperty("characterEncoding","utf8mb4");
		//config.addDataSourceProperty("useUnicode","true");
		config.addDataSourceProperty("serverTimezone", "UTC");
		config.addDataSourceProperty("allowMultiQueries", "true");
		config.setAutoCommit(false);
		config.setJdbcUrl(url.replace("%HOST%", host).replace("%PORT%", Integer.toString(port)).replace("%DATABASE%", database));
		config.setUsername(username);
		config.setPassword(password);
		config.setConnectionTimeout(25_000L); // 25 seconds
		config.setMaxLifetime(1500_000L); // 1500 seconds = 25 minutes
		return config;
	}
	
	/**
	 * Creates all tables if they do not exist <br>
	 * <br>
	 * UUIDs are stored as <code>BINARY(16)</code> reflecting the fact that they are 16 bytes / 128 bits.
	 * To convert a UUID object to the appropriate byte array, and back, take advantage of UUIDVault's UUIDUtil. <br>
	 * IP addresses are stored as <code>VARBINARY(16)</code>. IPv4 addresses only use 4 bytes, but IPv6 ones use 16.
	 * InetAddress has built-in methods to convert addresses to and from byte arrays.
	 * <br>
	 * <br>
	 * <b>Tables</b> <br>
	 * <br>
	 * The player identifiers table is <i>omega_identify</i>. It contains distinct combinations
	 * of UUIDs, names, and IP addresses, the column names being <code>`uuid`, `name`, `address`</code>.
	 * Names are VARCHAR(16) reflecting the fact that MC accounts may only have 16 characters at most.
	 * Each row also has a column, <code>`updated`</code>, a unix timestamp, in miliseconds, of the last time
	 * the record was updated or created; this is a BIGINT. <br>
	 * <br>
	 * The stats/prefs table, a.k.a. the numbers table, is <i>omega_numbers</i>. It is essentially
	 * a key-value mapping of UUIDs to basic player statistics and information.
	 * 
	 * @return a future indicating the progress
	 */
	CompletableFuture<?> makeTablesIfNotExist() {
		return executeAsync(() -> {
			try (CloseMe cm = execute(
					SqlQuery.of("CREATE TABLE IF NOT EXISTS `omega_identify`("
							+ "`uuid` BINARY(16) NOT NULL, "
							+ "`name` VARCHAR(16) NOT NULL, "
							+ "`address` VARBINARY(16) NOT NULL, "
							+ "`updated` BIGINT NOT NULL, "
							+ "PRIMARY KEY (`uuid`, `name`, `address`))",
					SqlQuery.of("CREATE TABLE IF NOT EXISTS `omega_numbers` ("
							+ "`uuid` BINARY(16) PRIMARY KEY, "
							// Statistics
							+ "`balance` BIGINT NOT NULL, "
							+ "`level` INT NOT NULL, "
							+ "`kitpvp_kills` INT NOT NULL, "
							+ "`kitpvp_deaths` INT NOT NULL, "
							+ "`combo_kills` INT NOT NULL, "
							+ "`combo_deaths` INT NOT NULL, "
							+ "`monthly_reward` INT NOT NULL, "
							// Preferences
							+ "`toggle_prefs` TINYINT NOT NULL, "
							+ "`chat_colour` SMALLINT NOT NULL, "
							+ "`name_colour` SMALLINT NOT NULL)")))) {

			} catch (SQLException ex) {
				logger.error("Error creating tables", ex);
			}
		});
	}

	CompletableFuture<?> executeAsync(Runnable cmd) {
		return CompletableFuture.runAsync(cmd, asyncExecutor);
	}

	<T> CompletableFuture<T> selectAsync(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, asyncExecutor);
	}
	
	@Override
	public void close() {
		logger.debug("Beginning shutdown");
		asyncExecutor.shutdown();
		try {
			super.close();
			asyncExecutor.awaitTermination(15L, TimeUnit.SECONDS);
			logger.info("Shutdown completed without errors");
		} catch (SQLException | InterruptedException ex) {
			logger.error("Error during shutdown", ex);
		}
	}
	
}
