package com.nktfh100.AmongUs.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EntityListeners extends PacketAdapter {
    private static final PacketType[] ENTITY_PACKETS = { PacketType.Play.Server.ENTITY_EQUIPMENT, PacketType.Play.Server.ANIMATION, PacketType.Play.Server.COLLECT,
            PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, PacketType.Play.Server.ENTITY_VELOCITY, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.ENTITY_LOOK,
            PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.ENTITY_HEAD_ROTATION, PacketType.Play.Server.ENTITY_STATUS, PacketType.Play.Server.ATTACH_ENTITY,
            PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.ENTITY_EFFECT, PacketType.Play.Server.REMOVE_ENTITY_EFFECT, PacketType.Play.Server.BLOCK_BREAK_ANIMATION,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK };

    public EntityListeners(Plugin plugin, ListenerPriority priority) {
        super (plugin, priority, ENTITY_PACKETS);
    }

    public void onPacketSending(PacketEvent event) {
        try {
            if (event.getPlayer() == null || event.getPlayer().getWorld() == null) {
                return;
            }

            World world = event.getPlayer().getWorld();
            Entity entity = event.getPacket().getEntityModifier(world).read(0);

            if (entity instanceof Player) {
                PlayerInfo sendPacketPlayerInfo = Main.getPlayersManager().getPlayerInfo((Player) entity);
                if (sendPacketPlayerInfo != null) {
                    if (sendPacketPlayerInfo.getIsIngame() && sendPacketPlayerInfo.isGhost()) {
                        PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(event.getPlayer());
                        if (!pInfo.isGhost()) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
