package ca.sperrer.basmc.sqlplaytimetracker;

import net.md_5.bungee.api.plugin.Plugin;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public final class SQLPlaytimeTracker extends Plugin {
    @Override
    public void onEnable() {
        getLogger().info("has loaded!");
        getProxy().getPluginManager().registerListener(this, new BungeeEventHandler());
        ScheduledTask scheduledTask = getProxy().getScheduler().schedule(this, DataAccumulatorBungee::update_playtime, 0, 1, TimeUnit.MINUTES);
    }
}
