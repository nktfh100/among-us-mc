package com.nktfh100.AmongUs.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class ArmorStandManipulate implements Listener {
	@EventHandler
	public void onClick(PlayerArmorStandManipulateEvent ev) {
		if (ev.getRightClicked().getCustomName() != null && ev.getRightClicked().getCustomName().startsWith("camera_armor_stand")) {
			ev.setCancelled(true);
		}
	}
}
