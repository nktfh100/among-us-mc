package com.nktfh100.AmongUs.info;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.utils.Utils;

public class VitalsPlayerInfo implements Comparable<VitalsPlayerInfo> {

	private Player player;
	private Integer joinedId;
	private ColorInfo color;
	private ItemStack headItem;
	private Boolean isDead;
	private Boolean isDC;

	public VitalsPlayerInfo(PlayerInfo pInfo) {
		this.player = pInfo.getPlayer();
		this.joinedId = pInfo.getJoinedId();
		if (pInfo.getHead() != null) {
			this.headItem = pInfo.getHead().clone();
		} else {
			this.headItem = Utils.getHead(pInfo.getPlayer().getName());
		}
		this.isDead = pInfo.isGhost();
		this.color = pInfo.getColor();
		this.isDC = false;
	}

	public Player getPlayer() {
		return player;
	}

	public ItemStack getHeadItem() {
		return this.headItem;
	}

	public void setHeadItem(ItemStack headItem) {
		this.headItem = headItem;
	}

	public Boolean getIsDead() {
		return isDead;
	}

	public void setIsDead(Boolean isDead) {
		this.isDead = isDead;
	}

	public Boolean getIsDC() {
		return isDC;
	}

	public void setIsDC(Boolean isDC) {
		this.isDC = isDC;
	}

	public ColorInfo getColor() {
		return color;
	}

	public void setColor(ColorInfo color) {
		this.color = color;
	}

	@Override
	public int compareTo(VitalsPlayerInfo o) {
		return this.joinedId.compareTo(o.getJoinedId());
	}

	public Integer getJoinedId() {
		return joinedId;
	}

}
