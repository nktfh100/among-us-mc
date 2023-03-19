package com.nktfh100.AmongUs.events;

import com.comphenix.protocol.events.PacketContainer;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.PlayersManager;
import com.nktfh100.AmongUs.utils.Packets;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {
    private PlayersManager playersManager = Main.getPlayersManager();
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
        if (playersManager.getPlayerInfo(ev.getPlayer()) == null) {//players.get(ev.getPlayer().getUniqueId().toString()) == null) {
            playersManager.addPlayer(ev.getPlayer());
        } else {
            playersManager.getPlayerInfo(ev.getPlayer())._setPlayer(player);
        }
        if (Main.getConfigManager().getMysql_enabled()) {
            playersManager.getPlayerInfo(player).getStatsManager().mysql_registerPlayer(true);
        } else {
            playersManager.getPlayerInfo(player).getStatsManager().loadStats();
        }
        if (Main.getConfigManager().getGiveLobbyItems()) {
            player.getInventory().clear();

            ItemInfo arenasSelectorItem = Main.getItemsManager().getItem("arenasSelector").getItem();
            player.getInventory().setItem(Main.getConfigManager().getLobbyItemSlot("arenasSelector"), arenasSelectorItem.getItem());
            if (Main.getIsPlayerPoints()) {
                player.getInventory().setItem(Main.getConfigManager().getLobbyItemSlot("cosmeticsSelector"), Main.getItemsManager().getItem("cosmeticsSelector").getItem().getItem());
            }
        }
        if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby() && Main.getArenaManager().getAllArenas().size() > 0) {
            Arena arena = Main.getArenaManager().getAllArenas().iterator().next();
            if ((arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) && arena.getPlayersInfo().size() < arena.getMaxPlayers()) {
                arena.playerJoin(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.getArenaManager().sendBungeUpdate(arena);
                    }
                }.runTaskLater(Main.getPlugin(), 10L);
            }
        }
        if (!Main.getConfigManager().getBungeecord() || (Main.getConfigManager().getBungeecord() && Main.getConfigManager().getBungeecordIsLobby())) {
            if (Main.getConfigManager().getTpToLobbyOnJoin()) {
                if (Main.getConfigManager().getMainLobby() != null) {
                    player.teleport(Main.getConfigManager().getMainLobby());
                }
            }
            // update main lobby scoreboard
            if (Main.getConfigManager().getEnableLobbyScoreboard()) {
                for (PlayerInfo pInfo_ : playersManager.getPlayers().values()) {
                    if (!pInfo_.getIsIngame()) {
                        pInfo_.updateScoreBoard();
                    }
                }
                playersManager.getPlayerInfo(player)._setMainLobbyScoreboard();
            }
            if (!Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby() && Main.getConfigManager().getHidePlayersOutSideArena()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PacketContainer packet = Packets.REMOVE_PLAYER(player.getUniqueId());
                        for (Arena arena : Main.getArenaManager().getAllArenas()) {
                            for (Player p : arena.getPlayers()) {
                                Packets.sendPacket(p, packet);
                                Packets.sendPacket(player, Packets.REMOVE_PLAYER(p.getUniqueId()));
                            }
                        }

                    }
                }.runTaskLater(Main.getPlugin(), 5L);
            }
        }
    }
}
