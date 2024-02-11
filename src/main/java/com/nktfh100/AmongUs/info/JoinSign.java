package com.nktfh100.AmongUs.info;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;
import org.bukkit.block.data.type.WallSign;

public class JoinSign {

	Arena arena;
	Location loc;
	
	public JoinSign(Arena arena, Location loc) {
		this.arena = arena;
		this.loc = loc;
	}
	
	public void update() {
		Sign sign = (Sign) this.getBlock().getState();
		for (int ii = 0; ii < 4; ii++) {
			sign.setLine(ii, Main.getMessagesManager().getSignLine(ii, this.arena));
		}
		sign.update();

		if (this.getBlock().getType().toString().contains("SIGN")) {
			BlockData data = this.getBlock().getBlockData();
			if (data instanceof WallSign) {
				Directional directional = (Directional) data;
				Block blockBehind = this.getBlock().getRelative(directional.getFacing().getOppositeFace());
				blockBehind.setType(Utils.getStateBlock(this.arena.getGameState()), true);
			}
		}
	}
	
	
	public Location getLocation() {
		return this.loc;
	}
	
	public Block getBlock() {
		return this.loc.getBlock();
	}
	
	public Arena getArena() {
		return this.arena;
	}
}
