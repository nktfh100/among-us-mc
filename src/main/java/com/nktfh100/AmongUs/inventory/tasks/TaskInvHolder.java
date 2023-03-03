package com.nktfh100.AmongUs.inventory.tasks;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.CustomHolder;

public abstract class TaskInvHolder extends CustomHolder {

	protected Arena arena;
	protected TaskPlayer taskPlayer;
	protected PlayerInfo pInfo;
	
	public TaskInvHolder(int size, String title, Arena arena, TaskPlayer taskPlayer) {
		super(size, title);
		this.arena = arena;
		this.taskPlayer = taskPlayer;
		this.pInfo = taskPlayer.getPlayerInfo();
	}
	
	public Arena getArena() {
		return this.arena;
	}

	public TaskPlayer getTaskPlayer() {
		return this.taskPlayer;
	}

	public PlayerInfo getPlayerInfo() {
		return this.pInfo;
	}

	public abstract Boolean checkDone();
	
	public abstract void update();
	
	public abstract void invClosed();
}
