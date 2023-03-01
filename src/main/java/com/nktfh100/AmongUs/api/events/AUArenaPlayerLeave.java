package com.nktfh100.AmongUs.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.nktfh100.AmongUs.info.Arena;

public class AUArenaPlayerLeave extends Event {

	private Arena arena;
	private Player player;
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	public AUArenaPlayerLeave(Arena arena, Player player) {
		this.arena = arena;
		this.player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	public Arena getArena() {
		return arena;
	}

	public Player getPlayer() {
		return player;
	}
}
