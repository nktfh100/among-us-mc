package com.nktfh100.AmongUs.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nktfh100.AmongUs.main.Main;

import mineverse.Aust1n46.chat.api.events.VentureChatEvent;

public class VentureChatEvent_ implements Listener {

	@EventHandler
	public void ventureChat(VentureChatEvent ev) {
		if (ev.getMineverseChatPlayer().getPlayer() == null) return;

		if (Main.getPlayersManager().getPlayerInfo(ev.getMineverseChatPlayer().getPlayer()).getIsIngame()) {
			ev.getRecipients().clear();
		}
	}
}
