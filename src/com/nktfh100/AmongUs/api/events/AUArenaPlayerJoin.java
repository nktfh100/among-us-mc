package com.nktfh100.AmongUs.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.nktfh100.AmongUs.info.Arena;

public class AUArenaPlayerJoin extends Event implements Cancellable {

	private Arena arena;
	private Player player;
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	private boolean isCancelled;

	public AUArenaPlayerJoin(Arena arena, Player player) {
		this.arena = arena;
		this.player = player;
		this.isCancelled = false;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
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
