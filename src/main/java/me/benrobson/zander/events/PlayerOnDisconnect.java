package me.benrobson.zander.events;

import me.benrobson.zander.ConfigurationManager;
import me.benrobson.zander.ZanderBungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerOnDisconnect implements Listener {
    private ZanderBungeeMain plugin = ZanderBungeeMain.getInstance();

    @EventHandler
    public void PlayerOnDisconnect(PlayerDisconnectEvent event) {
        String developmentprefix = ConfigurationManager.getConfig().getString("developmentprefix");
        ProxiedPlayer player = event.getPlayer();

        if (player.isConnected()) return;

        //
        // Database Query
        // End the players session.
        //
        try {
            PreparedStatement updatestatement = plugin.getConnection().prepareStatement("UPDATE gamesessions SET sessionend = NOW() where player_id = (select id from playerdata where uuid = ?) AND sessionend is null");
            updatestatement.setString(1, player.getUniqueId().toString());
            updatestatement.executeUpdate();
            plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', developmentprefix + " " + player.getDisplayName() + " has left the server. Session has ended, logging in the database."));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
