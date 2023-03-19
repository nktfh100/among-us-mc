package com.nktfh100.AmongUs.inventory.sabotages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class SabotageReactorInv extends SabotageInvHolder {

	private Integer taskNum;
	private Boolean isActive = false;
	private Arena arena;
	private Player player;

	private String tempActiveTitle;
	private Boolean removePlayerOnClose = true;

	public SabotageReactorInv(SabotageArena saboArena, Integer taskNum, Player player) {
		super(54, Main.getMessagesManager().getGameMsg("sabotageReactorsInvTitle", saboArena.getArena(), Utils.getSabotagePlaceholders(SabotageType.REACTOR_MELTDOWN)), saboArena.getArena(), saboArena);
		Utils.fillInv(this.inv);
		this.taskNum = taskNum;
		this.arena = saboArena.getArena();
		this.player = player;
		this.tempActiveTitle = "sabotageReactorsInvTitle";
		this.update();
	}

	public void handleClick(Player p) {
		if (!this.isActive) {
			Main.getSoundsManager().playSound("sabotageReactorClick", p, p.getLocation());
			this.isActive = true;
			this.sabotageArena.addPlayerActive(p, this.taskNum);
			this.update();
		}
	}

	private final static ArrayList<Integer> slots_ = new ArrayList<>(Arrays.asList(21, 22, 23, 30, 31, 32, 39, 40, 41));

	@Override
	public void update() {

		this.inv.setItem(8, Main.getItemsManager().getItem("reactorSabotage_info").getItem().getItem());

		String activeTitle = this.tempActiveTitle;
		String newTitleKey = "sabotageReactorsInvTitle";

		ItemInfoContainer infoItem = Main.getItemsManager().getItem("reactorSabotage_topItem");
		Material infoMat = infoItem.getItem().getMat();
		String infoName = infoItem.getItem().getTitle();
		ArrayList<String> infoLore = infoItem.getItem().getLore();

		ItemInfoContainer handItem = Main.getItemsManager().getItem("reactorSabotage_hand");
		Material handMat = handItem.getItem().getMat();
		String handName = handItem.getItem().getTitle();
		ArrayList<String> handLore = handItem.getItem().getLore();
		if (this.isActive) {
			if (this.sabotageArena.getTaskActive(this.taskNum == 0 ? 1 : 0)) {
				infoMat = infoItem.getItem3().getMat();
				infoName = infoItem.getItem3().getTitle();
				infoLore = infoItem.getItem3().getLore();

				handMat = handItem.getItem3().getMat();
				handName = handItem.getItem3().getTitle();
				handLore = handItem.getItem3().getLore();
				newTitleKey = "sabotageReactorsInvTitle2";
			} else {
				infoMat = infoItem.getItem2().getMat();
				infoName = infoItem.getItem2().getTitle();
				infoLore = infoItem.getItem2().getLore();

				handMat = handItem.getItem2().getMat();
				handName = handItem.getItem2().getTitle();
				handLore = handItem.getItem2().getLore();
				newTitleKey = "sabotageReactorsInvTitle1";
			}
		}
		if (!activeTitle.equals(newTitleKey)) {
			removePlayerOnClose = false;
			this.changeTitle(Main.getMessagesManager().getGameMsg(newTitleKey, this.arena, Utils.getSabotagePlaceholders(SabotageType.REACTOR_MELTDOWN)));
			Utils.fillInv(this.inv);
			this.player.openInventory(this.inv);
			this.tempActiveTitle = newTitleKey;
			removePlayerOnClose = true;
		}

		this.inv.setItem(4, Utils.createItem(infoMat, infoName, 1, infoLore));

		ItemStack item = Utils.createItem(handMat, handName, 1, handLore);

		SabotageReactorInv reactorInv = this;
		ClickAction ca = new ClickAction() {
			@Override
			public void execute(Player player) {
				reactorInv.handleClick(player);
			}
		};

		for (Integer slot : slots_) {
			Icon icon = new Icon(item);
			icon.addClickAction(ca);
			this.setIcon(slot, icon);
		}
	}

	@Override
	public void invClosed(Player player) {
		if (this.isActive && removePlayerOnClose) {
			this.sabotageArena.removePlayerActive(player, this.taskNum);
		}
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Integer getTaskNum() {
		return taskNum;
	}

}