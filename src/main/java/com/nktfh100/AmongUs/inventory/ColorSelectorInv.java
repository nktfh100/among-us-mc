package com.nktfh100.AmongUs.inventory;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ColorInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class ColorSelectorInv extends CustomHolder {

	private Arena arena;

	public ColorSelectorInv(Arena arena) {
		super(18, Main.getMessagesManager().getGameMsg("colorSelectorInvTitle", null, null));
		this.arena = arena;
		Utils.fillInv(this.inv);
	}

	public void handleColorClick(PlayerInfo pInfo, ColorInfo color) {
		this.arena.updatePlayerColor(pInfo, color);
		Main.getSoundsManager().playSound("playerChangeColor", pInfo.getPlayer(), pInfo.getPlayer().getLocation());
		pInfo.setPreferredColor(color);
		this.update();
	}

	public void update() {
		ColorSelectorInv inv_ = this;
		this.clearInv();
		Utils.fillInv(this.inv);
		ItemInfoContainer colorItem = Main.getItemsManager().getItem("colorSelector_color");
		int i = 0;
		for (ColorInfo color : arena.getColors_()) {
			String title = colorItem.getItem().getTitle(color.getName(), color.getChatColor() + "");
			ArrayList<String> lore = colorItem.getItem().getLore(color.getName(), color.getChatColor() + "");
			Icon icon = new Icon(Utils.createItem(color.getWool(), title, 1, lore));

			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv_.handleColorClick(Main.getPlayersManager().getPlayerInfo(player), color);
				}
			});

			this.setIcon(i, icon);
			i++;
		}
	}
}