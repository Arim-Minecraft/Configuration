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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import space.arim.shaded.com.zaxxer.hikari.HikariConfig;
import space.arim.shaded.com.zaxxer.hikari.HikariDataSource;

import space.arim.universal.util.AutoClosable;

import space.arim.api.sql.AbstractSql;
import space.arim.api.sql.ExecutableQuery;

public class OmegaSql extends AbstractSql implements AutoClosable {

	private final HikariDataSource dataSource;
	
	private final ExecutorService asyncExecutor;
	
	OmegaSql(String host, int port, String database, String url, String username, String password, int connections) {
		HikariConfig config = new HikariConfig();
		config.setMinimumIdle(connections);
		config.setMaximumPoolSize(connections);
		//config.addDataSourceProperty("autoReconnect", "true");
		//config.addDataSourceProperty("characterEncoding","utf8mb4");
		//config.addDataSourceProperty("useUnicode","true");
		config.addDataSourceProperty("serverTimezone", "UTC");
		config.setJdbcUrl(url.replace("%HOST%", host).replace("%PORT%", Integer.toString(port)).replace("%DATABASE%", database));
		config.setUsername(username);
		config.setPassword(password);
		config.setConnectionTimeout(25_000L); // 25 seconds
		config.setMaxLifetime(1500_000L); // 1500 seconds = 25 minutes
		asyncExecutor = Executors.newFixedThreadPool(connections);
		dataSource = new HikariDataSource(config);
	}
	
	/**
	 * Creates all tables if they do not exist <br>
	 * <br>
	 * For the alts table, the ips column has a max size dependent on the implementation of its serialised form.
	 * IPs are stored as base64 strings and separated by commas. The most number of IPs stored for a single player is capped at 20.
	 * Ipv6 address have length 24. Therefore, the max size of the column is 20 * 24 + 19 if all 20 addresses are ipv6. <br>
	 * <br>
	 * For the prefs table, the friended_ignored column has a max size dependent on the implementation of its serialised form.
	 * See {@link MutablePrefs#getFriended_ignored()} and {@link MutablePrefs#mapToString(java.util.Map)}. <br>
	 * Thus, the max size of the column is 50 * (32 + 1 + 1) + 50 * (32 + 1 + 1) + 99, or 3499.
	 * 
	 * @return a future indicating the progress
	 */
	CompletableFuture<?> makeTablesIfNotExist() {
		return executeAsync(() -> {
			try {
				executionQueries(
						new ExecutableQuery("CREATE TABLE IF NOT EXISTS `omega_identify`("
						+ "`uuid` BINARY(16) PRIMARY KEY,"
						+ "`name` VARCHAR(16) NOT NULL,"
						+ "`ips` VARCHAR(499) NOT NULL,"
						+ "`updated` INT NOT NULL)"),
						new ExecutableQuery("CREATE TABLE IF NOT EXISTS `omega_stats` ("
						+ "`uuid` BINARY(16) PRIMARY KEY,"
						+ "`name` VARCHAR(16) NOT NULL,"
						+ "`level` INT NOT NULL,"
						+ "`balance` BIGINT NOT NULL,"
						+ "`kitpvp_kills` INT NOT NULL,"
						+ "`kitpvp_deaths` INT NOT NULL,"
						+ "`combo_kills` INT NOT NULL,"
						+ "`combo_deaths` INT NOT NULL,"
						+ "`monthly_reward` INT NOT NULL)"),
						new ExecutableQuery("CREATE TABLE IF NOT EXISTS `omega_prefs` ("
						+ "`uuid` BINARY(16) PRIMARY KEY,"
						+ "`toggle_prefs` TINYINT NOT NULL,"
						+ "`chat_and_name_colour` INT NOT NULL,"
						+ "`friended_ignored` VARCHAR(3499) NOT NULL)"));
			} catch (SQLException ex) {
				ex.printStackTrace();
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
		asyncExecutor.shutdown();
		try {
			asyncExecutor.awaitTermination(15L, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		dataSource.close();
	}

	@Override
	protected Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
}
