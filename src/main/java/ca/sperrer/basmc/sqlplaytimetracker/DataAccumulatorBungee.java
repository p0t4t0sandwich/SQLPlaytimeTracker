package ca.sperrer.basmc.sqlplaytimetracker;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

class DataAccumulatorBungee extends DataAccumulator<ProxyServer,ProxiedPlayer> {
    public static void update_playtime() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            System.out.println("Saving data for: " + player.getName() + " (" + player.getUniqueId() + ")");
            String server_name = player.getServer().getInfo().getName();
            try {
                String SQL_QUERY = "UPDATE playtime SET " + server_name + " = " + server_name + " + 1 WHERE player_id=(SELECT player_id FROM player_data WHERE player_uuid='" + player.getUniqueId() + "');";
                Connection con = DataAccumulator.getConnection();
                PreparedStatement pst = con.prepareStatement(SQL_QUERY);
                pst.executeUpdate();
                con.close();
            } catch (SQLException e) {
                try {
                    String SQL_QUERY = "ALTER TABLE playtime ADD " + server_name + "  INT DEFAULT 0;";
                    Connection con = DataAccumulator.getConnection();
                    PreparedStatement pst = con.prepareStatement(SQL_QUERY);
                    pst.executeUpdate();
                    con.close();
                } catch (SQLException f) {
                    //throw new RuntimeException(f);
                    System.out.println(f);
                }
                //throw new RuntimeException(e);
                System.out.println(e);
            }
        }
    }
    @Override
    void player_login_data(ProxiedPlayer player) {
        long unixTime = System.currentTimeMillis() / 1000L;
        String player_uuid = String.valueOf(player.getUniqueId());
        try {
            Connection con = DataAccumulator.getConnection();

            String SQL_QUERY = "INSERT INTO `player_data` (`player_uuid`, `player_name`, first_login, last_online, last_streak, streak) SELECT ?, ?, " + unixTime + ", " + unixTime + ", " + unixTime + ", 1 FROM DUAL WHERE NOT EXISTS (SELECT * FROM `player_data` WHERE `player_uuid`=? LIMIT 1)";
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            pst.setString(1, player_uuid);
            pst.setString(2, player.getName());
            pst.setString(3, player_uuid);
            pst.executeUpdate();

            SQL_QUERY = "INSERT INTO `playtime` (player_id) SELECT (SELECT player_id FROM `player_data` WHERE `player_uuid`=?) FROM DUAL WHERE NOT EXISTS (SELECT * FROM `playtime` WHERE `player_id`=(SELECT player_id FROM `player_data` WHERE `player_uuid`=?) LIMIT 1)";
            pst = con.prepareStatement(SQL_QUERY);
            pst.setString(1, player_uuid);
            pst.setString(2, player_uuid);
            pst.executeUpdate();


            SQL_QUERY = "INSERT INTO `currency` (player_id, tokens, exploit_tokens, donator_tokens, channel_point_tokens) SELECT (SELECT player_id FROM `player_data` WHERE `player_uuid`=?), 0, 0, 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT * FROM `currency` WHERE `player_id`=(SELECT player_id FROM `player_data` WHERE `player_uuid`=?) LIMIT 1)";
            pst = con.prepareStatement(SQL_QUERY);
            pst.setString(1, player_uuid);
            pst.setString(2, player_uuid);
            pst.executeUpdate();

            SQL_QUERY = "SELECT * FROM player_data WHERE player_uuid = '" + player_uuid + "';";
            pst = con.prepareStatement(SQL_QUERY);
            ResultSet rs = pst.executeQuery(SQL_QUERY);
            rs.next();

            long last_streak = rs.getLong("last_streak");
            long timeval = unixTime - last_streak - (unixTime % 86400);

            if (timeval > 86400) {
                //Reset Streak
                SQL_QUERY = "UPDATE player_data SET streak = 1 WHERE player_uuid = ?;";
                pst = con.prepareStatement(SQL_QUERY);
                pst.setString(1, player_uuid);
                pst.executeUpdate();

                SQL_QUERY = "UPDATE currency SET tokens = tokens + 1 WHERE `player_id`=(SELECT player_id FROM `player_data` WHERE `player_uuid`=?);";
                pst = con.prepareStatement(SQL_QUERY);
                pst.setString(1, player_uuid);
                pst.executeUpdate();

                SQL_QUERY = "UPDATE player_data SET last_streak = " + unixTime + " WHERE player_uuid = ?;";
                pst = con.prepareStatement(SQL_QUERY);
                pst.setString(1, player_uuid);
                pst.executeUpdate();
            } else if (timeval > 0 && timeval < 86400) {
                //ADD 1 to streak and give rewards
                SQL_QUERY = "UPDATE player_data SET streak = streak + 1 WHERE player_uuid = ?;";
                pst = con.prepareStatement(SQL_QUERY);
                pst.setString(1, player_uuid);
                pst.executeUpdate();

                SQL_QUERY = "UPDATE currency SET tokens = tokens + 2 WHERE `player_id`=(SELECT player_id FROM `player_data` WHERE `player_uuid`=?);";
                pst = con.prepareStatement(SQL_QUERY);
                pst.setString(1, player_uuid);
                pst.executeUpdate();

                SQL_QUERY = "UPDATE player_data SET last_streak = " + unixTime + " WHERE player_uuid = ?;";
                pst = con.prepareStatement(SQL_QUERY);
                pst.setString(1, player_uuid);
                pst.executeUpdate();

                //Streak Message
                SQL_QUERY = "SELECT streak FROM player_data WHERE player_uuid = '" + player_uuid + "';";
                pst = con.prepareStatement(SQL_QUERY);
                rs = pst.executeQuery(SQL_QUERY);
                rs.next();
                int streak = rs.getInt("streak");
                player.sendMessage(new ComponentBuilder("Your daily streak is now " + streak + ", Keep up the good work!").color(ChatColor.GREEN).create());
            }
            //Update Last Login
            SQL_QUERY = "UPDATE player_data SET last_online = " + unixTime + " WHERE player_uuid = ?;";
            pst = con.prepareStatement(SQL_QUERY);
            pst.setString(1, player_uuid);
            pst.executeUpdate();

            con.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    @Override
    void player_logout_data(ProxiedPlayer player) {
        long unixTime = System.currentTimeMillis() / 1000L;
        try {
            Connection con = DataAccumulator.getConnection();

            //Update Last Login
            String SQL_QUERY = "UPDATE player_data SET last_online = " + unixTime + " WHERE player_uuid = ?;";
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            pst.setString(1, String.valueOf(player.getUniqueId()));
            pst.executeUpdate();

            con.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
