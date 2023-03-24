package com.nktfh100.AmongUs.api.events;

import com.nktfh100.AmongUs.enums.GameEndReasons;
import com.nktfh100.AmongUs.enums.GameEndWinners;
import com.nktfh100.AmongUs.info.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AUArenaEnd extends Event {
    private final Arena arena;
    private final GameEndReasons reason;
    private final GameEndWinners winners;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public AUArenaEnd(Arena arena, GameEndReasons reason, GameEndWinners winners) {
        this.arena = arena;
        this.reason = reason;
        this.winners = winners;
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

    public GameEndReasons getReason() {
        return reason;
    }

    public GameEndWinners getWinners() {
        return winners;
    }
}
