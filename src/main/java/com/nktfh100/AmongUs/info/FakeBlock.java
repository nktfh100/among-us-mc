package com.nktfh100.AmongUs.info;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.nktfh100.AmongUs.utils.Packets;

public class FakeBlock {

	private Location loc;
	private Block block;
	private WrappedBlockData prevData;
	private Material prevMat;
	private Material newMat;

	public FakeBlock(Location loc, Material prevMat, Material newMat, WrappedBlockData prevData) {
		this.loc = loc;
		this.prevMat = prevMat;
		this.newMat = newMat;
		this.prevData = prevData;
		this.block = loc.getBlock();
	}

	public void updateOldBlock() {
		this.prevMat = this.block.getType();
		this.prevData = WrappedBlockData.createData(this.block.getBlockData());
	}

	public void sendNewBlock(Player player) {
		Packets.sendPacket(player, Packets.BLOCK_CHANGE(this.loc, WrappedBlockData.createData(newMat)));
	}

	public void sendOldBlock(Player player) {
		Packets.sendPacket(player, Packets.BLOCK_CHANGE(this.loc, prevData));
	}

	public Location getLoc() {
		return loc;
	}

	public Material getPrevMat() {
		return prevMat;
	}

	public Material getNewMat() {
		return newMat;
	}

	public WrappedBlockData getPrevData() {
		return prevData;
	}

	public Block getBlock() {
		return block;
	}

}
