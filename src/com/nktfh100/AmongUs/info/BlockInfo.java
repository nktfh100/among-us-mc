package com.nktfh100.AmongUs.info;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockInfo {

	private Block block;
	private Material oldMat;
	private Material newMat;
	private BlockData oldBlockData;

	public BlockInfo(Block block, Material oldMat, Material newMat, BlockData oldBlockData) {
		this.block = block;
		this.oldMat = oldMat;
		this.newMat = newMat;
		this.oldBlockData = oldBlockData;
	}

	public void placeNewBlock() {
		this.block.setType(this.newMat);
	}

	public void placeOldBlock() {
		this.block.setType(this.oldMat);
		this.block.setBlockData(this.oldBlockData);
	}

	public Block getBlock() {
		return block;
	}

	public Material getOldMat() {
		return oldMat;
	}

	public Material getNewMat() {
		return newMat;
	}

	public BlockData getOldBlockData() {
		return oldBlockData;
	}
}
