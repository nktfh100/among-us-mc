package com.nktfh100.AmongUs.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.PlayersManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.function.Predicate;

public class NamedSoundEffectListener extends PacketAdapter {
    public NamedSoundEffectListener(Plugin plugin, ListenerPriority priority) {
        super(plugin, priority, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        PlayersManager playersManager = Main.getPlayersManager();

        Sound sound = event.getPacket().getSoundEffects().read(0);
        if (event.getPacket().getSoundCategories().read(0) == EnumWrappers.SoundCategory.PLAYERS) {
            PlayerInfo pInfo = playersManager.getPlayerInfo(player);
            if (pInfo == null) {
                return;
            }
            if (pInfo.getIsIngame() && !pInfo.isGhost()) {
                StructureModifier<Integer> ints = event.getPacket().getIntegers();

                double x = ints.read(0) / 8D;
                double y = ints.read(1) / 8D;
                double z = ints.read(2) / 8D;

                Predicate<Entity> predicate = i -> (i instanceof Player && playersManager.getPlayerInfo((Player) i).isGhost());
                Collection<Entity> players_ = event.getPlayer().getWorld().getNearbyEntities(new Location(player.getWorld(), x, y, z), 1, 1, 1, predicate);
                if (players_.size() > 0) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (sound == Sound.ENTITY_PLAYER_ATTACK_NODAMAGE || sound == Sound.ITEM_ARMOR_EQUIP_GENERIC) {
            PlayerInfo pInfo = playersManager.getPlayerInfo(player);
            if (pInfo != null && pInfo.getIsIngame()) {
                event.setCancelled(true);
            }
        }
    }
}