package com.shadowolfyt.zander.discord.moderation;

import com.shadowolfyt.zander.ZanderMain;
import com.shadowolfyt.zander.discord.DiscordMain;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommandAdminLog implements Listener {
    private ZanderMain plugin;
    public CommandAdminLog(ZanderMain plugin) {
        this.plugin = plugin;
        DiscordMain.getInstance();
    }

    @EventHandler
    public void ServerCommand (PlayerCommandPreprocessEvent event) {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        String formatteddate = date.format(dateformat);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Admin Event Log");
        embed.setDescription(event.getPlayer().getDisplayName() + " typed this command: " + event.getMessage());
//        embed.setFooter("Executed: " + formatteddate);
        embed.setColor(Color.blue);

        TextChannel textChannel = DiscordMain.getInstance().getJda().getTextChannelsByName(plugin.getConfig().getString("discord.adminlogchannel"), true).get(0);
        textChannel.sendMessage(embed.build()).queue();
    }
}
