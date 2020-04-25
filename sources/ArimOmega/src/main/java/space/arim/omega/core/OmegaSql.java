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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import space.arim.api.sql.PooledLoggingSql;

public class OmegaSql extends PooledLoggingSql {

	private final Logger logger;
	private final HikariDataSource dataSource;
	
	private final Executor asyncExecutor;
	
	OmegaSql(Logger logger, String host, int port, String database, String url, String username, String password, int connections) {
		this.logger = logger;
		HikariConfig config = new HikariConfig();
		config.setMinimumIdle(connections);
		config.setMaximumPoolSize(connections);
		config.addDataSourceProperty("autoReconnect", "true");
		config.addDataSourceProperty("characterEncoding","utf8");
		config.addDataSourceProperty("useUnicode","true");
		config.addDataSourceProperty("serverTimezone", "UTC");
		config.setJdbcUrl(url.replace("%HOST%", host).replace("%PORT%", Integer.toString(port)).replace("%DATABASE%", database));
		config.setUsername(username);
		config.setPassword(password);
		config.setConnectionTimeout(25000L);
		asyncExecutor = Executors.newFixedThreadPool(connections);
		dataSource = new HikariDataSource(config);
	}
	
	/**
	 * Creates the stats table if it does not exist.
	 * 
	 * @return a future indicating the progress
	 */
	CompletableFuture<?> makeStatsTableIfNotExist() {
		return executeAsync(() -> {
			try {
				executionQuery("CREATE TABLE IF NOT EXISTS `omega_stats` ("
						+ "`uuid` VARCHAR(32) PRIMARY KEY,"
						+ "`balance` BIGINT NOT NULL,"
						+ "`kitpvp_kills` INT NOT NULL,"
						+ "`kitpvp_deaths` INT NOT NULL,"
						+ "`combo_kills` INT NOT NULL,"
						+ "`combo_deaths` INT NOT NULL,"
						+ "`monthly_reward` INT NOT NULL)");
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}
	
	/**
	 * Creates the preferences table if it does not exist. <br>
	 * <br>
	 * The friended_ignored column has a max size of dependent on the implementation of its serialised form.
	 * See {@link MutablePrefs#getFriended_ignored()} and {@link MutablePrefs#mapToString(java.util.Map)}. <br>
	 * Thus, the max size of the column is 50 * (32 + 1 + 1) + 50 * (32 + 1 + 1) + 99, or 3499.
	 * 
	 * @return a future indicating the progress
	 */
	CompletableFuture<?> makePrefsTableIfNotExist() {
		return executeAsync(() -> {
			try {
				executionQuery("CREATE TABLE IF NOT EXISTS `omega_prefs` ("
						+ "`uuid` VARCHAR(32) PRIMARY KEY,"
						+ "`toggle_prefs` TINYINT NOT NULL,"
						+ "`chat_colour` CHAR(2) NOT NULL,"
						+ "`name_colour` CHAR(2) NOT NULL,"
						+ "`friended_ignored` VARCHAR(3499) NOT NULL)");
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}
	
	CompletableFuture<?> executeAsync(Runnable cmd) {
		return CompletableFuture.runAsync(cmd, asyncExecutor);
	}
	
	<T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, asyncExecutor);
	}
	
	@Override
	protected HikariDataSource getDataSource() {
		return dataSource;
	}

	@Override
	protected void log(String message) {
		logger.info(message);
	}
	
}
