package com.nktfh100.AmongUs.events;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLogin implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent ev) {
        if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby() && Main.getArenaManager().getAllArenas().size() > 0) {
            Arena arena = Main.getArenaManager().getAllArenas().iterator().next();
            if (arena.getGameState() == GameState.RUNNING || arena.getGameState() == GameState.FINISHING) {
                if (ev.getPlayer().hasPermission("amongus.admin") || ev.getPlayer().hasPermission("amongus.admin.setup")) {
                    ev.allow();
//					players.put(ev.getPlayer().getUniqueId().toString(), new PlayerInfo(ev.getPlayer()));
                } else {
                    ev.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Arena running");
                }
            } else if (arena.getPlayers().size() == arena.getMaxPlayers()) {
                if (ev.getPlayer().hasPermission("amongus.admin") || ev.getPlayer().hasPermission("amongus.admin.setup")) {
                    ev.allow();
//					players.put(ev.getPlayer().getUniqueId().toString(), new PlayerInfo(ev.getPlayer()));
                } else {
                    ev.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Arena is full!");
                }
            }
        }
    }
}
