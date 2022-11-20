package ca.sperrer.basmc.sqlplaytimetracker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

class DataAccumulator<S,P> {
    private static Map<String, String> sql_config;
    static {
        try {
            sql_config = ConfigHandler.get_config("plugins/SQLPlaytimeTracker.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        config.setJdbcUrl("jdbc:mysql://" + sql_config.get("host") + "/" + sql_config.get("database"));
        config.setUsername(sql_config.get("username"));
        config.setPassword(sql_config.get("password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    //void update_playtime(S server) {}
    void player_login_data(P player) {}
    void player_logout_data(P player) {}
}
