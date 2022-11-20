package ca.sperrer.basmc.sqlplaytimetracker;


import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeEventHandler implements Listener {
    DataAccumulatorBungee da = new DataAccumulatorBungee();
    @EventHandler
    public void onPlayerLoggedInEvent(PostLoginEvent event) {
        da.player_login_data(event.getPlayer());
    }
    @EventHandler
    public void onPlayerLoggedOutEvent(PlayerDisconnectEvent event) {
        da.player_logout_data(event.getPlayer());
    }
}