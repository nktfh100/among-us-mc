package com.nktfh100.AmongUs.inventory;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.VitalsPlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.VitalsManager;
import com.nktfh100.AmongUs.utils.Utils;

public class VitalsInv extends CustomHolder {

	private static final Integer pageSize = 7;
	private static final ArrayList<ArrayList<Integer>> playersSlots = new ArrayList<ArrayList<Integer>>();
	static {
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(1, 10, 19, 28, 37)));
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(2, 11, 20, 29, 38)));
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(3, 12, 21, 30, 39)));
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(4, 13, 22, 31, 40)));
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(5, 14, 23, 32, 41)));
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(6, 15, 24, 33, 42)));
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(7, 16, 25, 34, 43)));
		playersSlots.add(new ArrayList<Integer>(Arrays.asList(8, 16, 26, 35, 44)));
	}

	private VitalsManager vitalsManager;
	private Integer currentPage = 1;

	public VitalsInv(VitalsManager vitalsManager, Player player) {
		super(54, Main.getMessagesManager().getGameMsg("vitalsInvTitle", null, null, player));
		this.vitalsManager = vitalsManager;
		Utils.fillInv(this.inv);
		this.update();
	}

	public void update() {
		VitalsInv inv_ = this;
		this.clearInv();
		Utils.fillInv(this.inv);
		
		if(this.vitalsManager.getPlayers().size() == 0) {
			return;
		}
		
		Integer totalItems = this.vitalsManager.getPlayers().size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		if (totalPages > 1) {
			if (this.currentPage > 1) {
				Icon icon = new Icon(Main.getItemsManager().getItem("vitals_prevPage").getItem().getItem());
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv_.setCurrentPage(inv_.getCurrentPage() - 1);
						inv_.update();
					}
				});
				this.setIcon(45, icon);
			}
			if (this.currentPage < totalPages) {
				Icon icon = new Icon(Main.getItemsManager().getItem("vitals_nextPage").getItem().getItem());
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv_.setCurrentPage(inv_.getCurrentPage() + 1);
						inv_.update();
					}
				});
				this.setIcon(53, icon);
			}
		}

		Integer startIndex = (this.currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<VitalsPlayerInfo> players_ = this.vitalsManager.getPlayers();

		ItemInfoContainer headItemInfo = Main.getItemsManager().getItem("vitals_playerHead");
		ItemInfoContainer playerItemInfo = Main.getItemsManager().getItem("vitals_player");

		Integer slot_ = 46;
		Integer i1 = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			final VitalsPlayerInfo vpi = players_.get(i);
			ItemStack item = vpi.getHeadItem().clone();
			ItemInfo headItem = headItemInfo.getItem();
			ItemStack playerItem = playerItemInfo.getItem().getItem();
			if (vpi.getIsDC()) {
				headItem = headItemInfo.getItem3();
				playerItem = playerItemInfo.getItem3().getItem();
			} else if (vpi.getIsDead()) {
				headItem = headItemInfo.getItem2();
				playerItem = playerItemInfo.getItem2().getItem();
			}
			Utils.setItemName(item, headItem.getTitle(vpi.getPlayer().getName(), vpi.getColor().getName(), vpi.getColor().getChatColor() + "", null, null),
					headItem.getLore(vpi.getPlayer().getName(), vpi.getColor().getName(), vpi.getColor().getChatColor() + "", null, null));
			this.inv.setItem(slot_, item);

			for (Integer slot : playersSlots.get(i1)) {
				this.inv.setItem(slot, playerItem);
			}

			i1++;
			slot_++;
		}

	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
}