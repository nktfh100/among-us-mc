package com.nktfh100.AmongUs.info;

import java.util.ArrayList;
import java.util.logging.Level;

import com.nktfh100.AmongUs.utils.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class Door implements Comparable<Door> {

	private Arena arena;
	private Location corner1;
	private Location corner2;
	private ArrayList<BlockInfo> blocks = new ArrayList<BlockInfo>();
	private ArrayList<Block> blocks_ = new ArrayList<Block>();
	private Integer id;
	private String configId;
	private Boolean isClosed = false;
	private DoorGroup doorGroup;
	private Location midPoint;

	public Door(Arena arena, DoorGroup doorGroup, Location corner1, Location corner2, Integer id, String configId) {
		this.arena = arena;
		this.id = id;
		this.configId = configId;
		this.doorGroup = doorGroup;
		this.corner1 = corner1;
		this.corner2 = corner2;
		if (corner1 != null && corner2 != null) {
			if (!Utils.isLocationZero(corner1) && !Utils.isLocationZero(corner2)) {
				ArrayList<Block> blocks = Utils.blocksFromTwoPoints(corner1, corner2);
				if (blocks.size() < 50) {
					for (Block block : blocks) {
						this.blocks.add(new BlockInfo(block, block.getType(), Material.IRON_BLOCK, block.getBlockData().clone()));
						this.blocks_.add(block);
					}
				} else {
					Logger.log(Level.INFO,"Door " + doorGroup.getLocName().getName() + " - " + id + " has too many blocks! (> 50)");
				}
			}
			this.midPoint = calculateMidPoint();
		} else {
			if (this.corner1 != null) {
				this.midPoint = corner1;
			} else if (this.corner2 != null) {
				this.midPoint = corner2;
			} else {
				this.midPoint = new Location(arena.getWorld(), 0, 0, 0);
			}
		}
	}

	public void openDoor(Boolean sound) {
		this.isClosed = false;
		this.replaceBlocks(false);
		if (sound) {
			for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
				if (pInfo.getPlayer().getWorld() == this.midPoint.getWorld() && pInfo.getPlayer().getLocation().distance(this.midPoint) <= 8) {
					Main.getSoundsManager().playSound("doorOpen", pInfo.getPlayer(), this.midPoint);
				}
			}
		}
	}

	public void closeDoor(Boolean sound) {
		this.isClosed = true;
		this.replaceBlocks(true);
		if (sound) {
			for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
				if (pInfo.getPlayer().getWorld() == this.midPoint.getWorld() && pInfo.getPlayer().getLocation().distance(this.midPoint) <= 8) {
					Main.getSoundsManager().playSound("doorClose", pInfo.getPlayer(), this.midPoint);
				}
			}
		}
	}

	private void replaceBlocks(Boolean newBlock) {
		for (BlockInfo bi : this.blocks) {
			if (newBlock) {
				bi.placeNewBlock();
			} else {
				bi.placeOldBlock();
			}
		}
	}

	public void setCorner1(Location loc) {
		this.corner1 = loc;
		this.blocks.clear();
		this.blocks_.clear();
		if (this.corner1 != null && this.corner2 != null) {
			if (!Utils.isLocationZero(this.corner1) && !Utils.isLocationZero(this.corner2)) {
				ArrayList<Block> blocks = Utils.blocksFromTwoPoints(corner1, corner2);
				if (blocks.size() < 50) {
					for (Block block : blocks) {
						this.blocks.add(new BlockInfo(block, block.getType(), Material.IRON_BLOCK, block.getBlockData().clone()));
						this.blocks_.add(block);
					}
				}
			}
			this.midPoint = calculateMidPoint();
		} else {
			if (this.corner1 != null) {
				this.midPoint = corner1;
			} else if (this.corner2 != null) {
				this.midPoint = corner2;
			}
		}
	}

	public void setCorner2(Location loc) {
		this.corner2 = loc;
		this.blocks.clear();
		this.blocks_.clear();
		if (this.corner1 != null && this.corner2 != null) {
			if (!Utils.isLocationZero(this.corner1) && !Utils.isLocationZero(this.corner2)) {
				ArrayList<Block> blocks = Utils.blocksFromTwoPoints(corner1, corner2);
				if (blocks.size() < 50) {
					for (Block block : blocks) {
						this.blocks.add(new BlockInfo(block, block.getType(), Material.IRON_BLOCK, block.getBlockData().clone()));
						this.blocks_.add(block);
					}
				}
			}
			this.midPoint = calculateMidPoint();
		} else {
			if (this.corner1 != null) {
				this.midPoint = corner1;
			} else if (this.corner2 != null) {
				this.midPoint = corner2;
			}
		}
	}

	public Location calculateMidPoint() {
		Location out = this.corner1.clone().add(this.corner2);
		out.multiply(0.5);
		return out;
	}

	public Arena getArena() {
		return arena;
	}

	public Integer getId() {
		return id;
	}

	public String getConfigId() {
		return configId;
	}

	@Override
	public int compareTo(Door v) {
		return this.id.compareTo(v.getId());
	}

	public ArrayList<BlockInfo> getBlocks() {
		return blocks;
	}

	public ArrayList<Block> getBlocks_() {
		return this.blocks_;
	}

	public Boolean getIsClosed() {
		return isClosed;
	}

	public void setIsClosed(Boolean isClosed) {
		this.isClosed = isClosed;
	}

	public DoorGroup getDoorGroup() {
		return doorGroup;
	}

	public Location getCorner2() {
		return corner2;
	}

	public Location getCorner1() {
		return corner1;
	}

	public Location getMidPoint() {
		return this.midPoint;
	}
}
