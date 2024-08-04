package me.vewa.rmbnametags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bstats.bukkit.Metrics;

public class RMBNametags extends JavaPlugin implements Listener {

    private ScoreboardManager manager;
    private Scoreboard board;
    private Team hiddenNamesTeam;
    private int displayTime;
    private String nameFormat;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        new Metrics(this, 22888);
        getServer().getPluginManager().registerEvents(this, this);

        manager = Bukkit.getScoreboardManager();
        board = manager.getMainScoreboard();
        hiddenNamesTeam = board.getTeam("hiddenNames");

        if (hiddenNamesTeam == null) {
            hiddenNamesTeam = board.registerNewTeam("hiddenNames");
        }

        hiddenNamesTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        for (Player player : Bukkit.getOnlinePlayers()) {
            hidePlayerName(player);
        }
    }

    @Override
    public void onDisable() {
        if (hiddenNamesTeam != null) {
            hiddenNamesTeam.unregister();
        }
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        displayTime = config.getInt("display-time", 3);
        nameFormat = ChatColor.translateAlternateColorCodes('&', config.getString("name-format", "&6{PLAYER_NAME}"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        hidePlayerName(event.getPlayer());
    }

    private void hidePlayerName(Player player) {
        hiddenNamesTeam.addEntry(player.getName());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player clickedPlayer = (Player) event.getRightClicked();
            Player clickingPlayer = event.getPlayer();
            showPlayerNameInActionbar(clickingPlayer, clickedPlayer);
        }
    }

    private void showPlayerNameInActionbar(Player clickingPlayer, Player clickedPlayer) {
        String name = nameFormat.replace("{PLAYER_NAME}", clickedPlayer.getName());
        clickingPlayer.sendActionBar(name);
        new BukkitRunnable() {
            @Override
            public void run() {
                clickingPlayer.sendActionBar("");
            }
        }.runTaskLater(this, displayTime * 20L);
    }
}
