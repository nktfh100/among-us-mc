package com.nktfh100.AmongUs.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NamedEntitySpawnListener extends PacketAdapter {
    public NamedEntitySpawnListener(Plugin plugin, ListenerPriority priority) {
        super(plugin, priority, PacketType.Play.Server.SPAWN_ENTITY);
    }

    public void onPacketSending(PacketEvent event) {
        Entity entity = event.getPacket().getEntityModifier(event.getPlayer().getWorld()).read(0);
        if (entity instanceof Player) {
            PlayerInfo pInfoSentTo = Main.getPlayersManager().getPlayerInfo(event.getPlayer());
            if (pInfoSentTo == null) {
                return;
            }
            Arena arena = pInfoSentTo.getArena();
            if (pInfoSentTo.getIsIngame() && arena.getGameState() == GameState.RUNNING) {
                PlayerInfo pInfoSpawned = Main.getPlayersManager().getPlayerInfo((Player) entity);
                if (pInfoSpawned == null) {
                    return;
                }
                if (pInfoSpawned.getArena() == arena) {
                    if (pInfoSpawned.getIsInVent() || pInfoSpawned.getIsInCameras()) {
                        event.setCancelled(true);
                        return;
                    }
                    if ((!pInfoSentTo.isGhost() && !pInfoSpawned.isGhost()) && !arena.getIsInMeeting()) {
                        if (!arena.getVisibilityManager().canSee(pInfoSentTo, pInfoSpawned)) {
                            event.setCancelled(true);
                            return;
                        }
                    } else if ((!pInfoSentTo.isGhost() && pInfoSpawned.isGhost())) {
                        event.setCancelled(true);
                        return;
                    }
                    if (pInfoSentTo.isGhost() && pInfoSpawned.isGhost()) {
                        event.setCancelled(false);
                    }
                }
            }
        }

    }
}
