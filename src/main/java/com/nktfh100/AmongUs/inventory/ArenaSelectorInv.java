package com.nktfh100.AmongUs.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.ArenaManager;
import com.nktfh100.AmongUs.utils.Utils;

public class ArenaSelectorInv extends CustomHolder {

	public ArenaSelectorInv() {
		super(45, Main.getMessagesManager().getGameMsg("arenasSelectorInvTitle", null, null, null));
	}

	public void update() {
		this.clearInv();
		Material mat = Main.getItemsManager().getItem("arenasSelector_border").getItem().getMat();
		Utils.addBorder(this.inv, 45, mat);

		ArenaManager arenaManager = Main.getArenaManager();

		final Boolean showRunning = Main.getConfigManager().getShowRunningArenas();
		for (Arena arena : arenaManager.getAllArenas()) {
			if (arena == null || arena.getPlayersInfo() == null || arena.getMaxPlayers() == null) {
				return;
			}
			Boolean canJoin = true;
			if (arena.getGameState() == GameState.RUNNING || arena.getGameState() == GameState.FINISHING) {
				canJoin = false;
			}
			if (arena.getPlayersInfo().size() == arena.getMaxPlayers()) {
				canJoin = false;
			}
			if (!showRunning && !canJoin) {
				continue;
			}
			String gameStateStr = Main.getMessagesManager().getGameState(arena.getGameState());
			ItemInfo arenaItem = canJoin ? Main.getItemsManager().getItem("arenasSelector_arena").getItem2() : Main.getItemsManager().getItem("arenasSelector_arena").getItem();
			ItemStack arenaItemS = arenaItem.getItem(arena.getDisplayName(), arena.getPlayersInfo().size() + "", arena.getMaxPlayers() + "", "" + Utils.getStateColor(arena.getGameState()), gameStateStr);
			Icon icon = new Icon(arenaItemS);

			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					arena.playerJoin(player);
				}
			});
			this.addIcon(icon);
		}
	}
}