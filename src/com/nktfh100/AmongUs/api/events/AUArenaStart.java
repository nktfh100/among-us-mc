package com.nktfh100.AmongUs.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.nktfh100.AmongUs.info.Arena;

public class AUArenaStart extends Event {

	private Arena arena;
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	public AUArenaStart(Arena arena) {
		this.arena = arena;
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
}
