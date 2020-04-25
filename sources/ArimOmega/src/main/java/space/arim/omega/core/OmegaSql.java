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

import org.slf4j.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import space.arim.api.sql.ExecutableQuery;
import space.arim.api.sql.PooledLoggingSql;

import lombok.Getter;

public class OmegaSql extends PooledLoggingSql {

	private final Logger logger;
	private final HikariDataSource dataSource;
	
	@Getter
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
	
	CompletableFuture<?> makeStatsTableIfNotExist() {
		return connectAsync(() -> {
			try {
				// TODO define tables
				executionQueries(new ExecutableQuery("CREATE TABLE IF NOT EXISTS `kitpvp_stats`"));
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}
	
	CompletableFuture<?> makePrefsTableIfNotExist() {
		return connectAsync(() -> {
			
		});
	}
	
	CompletableFuture<?> connectAsync(Runnable cmd) {
		return CompletableFuture.runAsync(cmd, asyncExecutor);
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
