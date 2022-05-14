package com.nktfh100.AmongUs.inventory.sabotages;

import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.inventory.CustomHolder;

public abstract class SabotageInvHolder extends CustomHolder {

	protected Arena arena;
	protected SabotageArena sabotageArena;

	public SabotageInvHolder(int size, String title, Arena arena, SabotageArena sabotageArena) {
		super(size, title);
		this.arena = arena;
		this.sabotageArena = sabotageArena;
	}


	public SabotageArena getSabotageArena() {
		return this.sabotageArena;
	}

	public Arena getArena() {
		return this.arena;
	}
	
	public abstract void update();
	
	public abstract void invClosed(Player player);
}
