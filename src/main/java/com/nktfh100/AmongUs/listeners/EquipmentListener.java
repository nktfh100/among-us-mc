package com.nktfh100.AmongUs.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class EquipmentListener extends PacketAdapter {
    public EquipmentListener(Plugin plugin, ListenerPriority priority) {
        super(plugin, priority, PacketType.Play.Server.ENTITY_EQUIPMENT);
    }

    public void onPacketSending(PacketEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(event.getPlayer());
        if (pInfo != null && pInfo.getIsIngame()) {
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> newSlotStack = new ArrayList<>();

            for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : event.getPacket().getSlotStackPairLists().read(0)) {
                if (pair.getFirst() == EnumWrappers.ItemSlot.MAINHAND) {
                    newSlotStack.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, new ItemStack(Material.AIR, 1)));
                } else if (pair.getFirst() == EnumWrappers.ItemSlot.OFFHAND) {
                    newSlotStack.add(new Pair<>(EnumWrappers.ItemSlot.OFFHAND, new ItemStack(Material.AIR, 1)));
                } else {
                    newSlotStack.add(pair);
                }
            }
            event.getPacket().getSlotStackPairLists().write(0, newSlotStack);
        }
    }

}
