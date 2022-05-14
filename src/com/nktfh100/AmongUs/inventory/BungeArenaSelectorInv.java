package com.nktfh100.AmongUs.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.BungeArena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.BungeArenaManager;
import com.nktfh100.AmongUs.utils.Utils;

public class BungeArenaSelectorInv extends CustomHolder {

	public BungeArenaSelectorInv() {
		super(45, Main.getMessagesManager().getGameMsg("arenasSelectorInvTitle", null, null));
	}

	public void update() {
		this.clearInv();
		Material mat = Main.getItemsManager().getItem("arenasSelector_border").getItem().getMat();
		Utils.addBorder(this.inv, 45, mat);

		BungeArenaManager arenaManager = Main.getBungeArenaManager();

		final Boolean showRunning = Main.getConfigManager().getShowRunningArenas();
		for (BungeArena arena : arenaManager.getAllArenas()) {
			Boolean canJoin = true;
			if (arena.getGameState() == GameState.RUNNING || arena.getGameState() == GameState.FINISHING) {
				canJoin = false;
			}
			if (arena.getCurrentPlayers() == arena.getMaxPlayers()) {
				canJoin = false;
			}
			if (!showRunning && !canJoin) {
				continue;
			}
			String gameStateStr = Main.getMessagesManager().getGameState(arena.getGameState());
			ItemInfo arenaItem = canJoin ? Main.getItemsManager().getItem("arenasSelector_arena").getItem2() : Main.getItemsManager().getItem("arenasSelector_arena").getItem();
			ItemStack arenaItemS = arenaItem.getItem(arena.getName(), arena.getCurrentPlayers() + "", arena.getMaxPlayers() + "", "" + Utils.getStateColor(arena.getGameState()), gameStateStr);
			Icon icon = new Icon(arenaItemS);

			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					Main.sendPlayerToArena(player, arena.getServer());
				}
			});
			this.addIcon(icon);
		}
	}
}