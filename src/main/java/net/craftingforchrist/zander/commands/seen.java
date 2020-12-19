package net.craftingforchrist.zander.commands;

import com.google.common.collect.ImmutableSet;
import net.craftingforchrist.zander.ZanderBungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class seen extends Command implements TabExecutor {
    public seen() {
        super("seen");
    }
    private static ZanderBungeeMain plugin;

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;

            if (args.length == 0) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Please specify a player."));
                return;
            } else if (args.length == 1) {
                //
                // Database Query
                // Check the database to see if the args matches to a player that has logged in.
                //
                try {
                    PreparedStatement findplayerstatement = plugin.getInstance().getConnection().prepareStatement("select username from playerdata where username=?;");
                    findplayerstatement.setString(1, args[0]);
                    ResultSet findplayerresults = findplayerstatement.executeQuery();

                    if (findplayerresults.next()) {
                        //
                        // Database Query
                        // Grab the player from args and get their last seen time in seconds and the last Server I was on.
                        //
                        try {
                            PreparedStatement findlastplayedstatement = plugin.getInstance().getConnection().prepareStatement("select playerdata.username, (select server from gamesessions where playerid = (select id from playerdata where username=?) order by sessionend asc limit 1) as 'server', TIME_TO_SEC(timediff(NOW(), lp.lp_timestamp)) as 'lastplayed' from (select session.playerid, max(if(session.sessionend, session.sessionend, NOW())) as 'lp_timestamp' from gamesessions session group by session.playerid) as lp left join playerdata on playerdata.id = lp.playerid where username=?;");
                            findlastplayedstatement.setString(1, args[0]);
                            findlastplayedstatement.setString(2, args[0]);
                            ResultSet results = findlastplayedstatement.executeQuery();

                            if (results.next()) {
                                int seentime = results.getInt("lastplayed");
                                String server = results.getString("server");
                                String capServer = server.substring(0, 1).toUpperCase() + server.substring(1);

                                if (seentime == 0) {
                                    player.sendMessage(new TextComponent(ChatColor.GRAY + args[0] + " is currently " + ChatColor.GREEN + "online " + ChatColor.GRAY + "on " + ChatColor.AQUA + capServer + ChatColor.GRAY + "."));
                                } else {
                                    Period difference = new Period(TimeUnit.SECONDS.toMillis(seentime));

                                    PeriodFormatter formatter = new PeriodFormatterBuilder()
                                            .appendYears().appendSuffix(" year, ", " years, ")
                                            .appendMonths().appendSuffix(" month, ", " months, ")
                                            .appendWeeks().appendSuffix(" week, ", " weeks, ")
                                            .appendDays().appendSuffix(" day, ", " days, ")
                                            .appendHours().appendSuffix(" hour, ", " hours, ")
                                            .appendMinutes().appendSuffix(" minute, ", " minutes, ")
                                            .appendSeconds().appendSuffix(" second", " seconds")
                                            .printZeroNever()
                                            .toFormatter();

                                    player.sendMessage(new TextComponent(ChatColor.GRAY + args[0] + " was last seen " + ChatColor.YELLOW + formatter.print(difference) + ChatColor.GRAY + " ago on " + ChatColor.AQUA + capServer + ChatColor.GRAY + "."));
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        player.sendMessage(new TextComponent(ChatColor.RED + "The requested player does not exist or has not logged in."));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        if (args.length > 2 || args.length == 0 ) {
            return ImmutableSet.of();
        }

        //
        // Database Query
        // Grab all players username from the database.
        //
        try {
            PreparedStatement findplayersstatement = plugin.getInstance().getConnection().prepareStatement("select username from playerdata order by username asc;");
            ResultSet results = findplayersstatement.executeQuery();

            if (results.next()) {
                Set<String> matches = new HashSet<>();
                if (args.length == 1) {
                    while (results.next()) {
                        if (args[0].startsWith(args[0])) {
                            matches.add(results.getString("username"));
                        }
                    }
                }
                return matches;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}