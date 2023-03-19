package com.nktfh100.AmongUs.events;

import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();
        PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
        if (pInfo == null) {
            return;
        }
        if (pInfo.getIsIngame()) {
            pInfo.getArena().get_playersToDelete().add(player);
            pInfo.getArena().playerLeave(player, false, true, true);
        } else {
            Main.getPlayersManager().deletePlayer(player);
        }
    }
}
