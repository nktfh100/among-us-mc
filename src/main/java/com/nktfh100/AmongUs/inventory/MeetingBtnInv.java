package com.nktfh100.AmongUs.inventory;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class MeetingBtnInv extends CustomHolder {

	private Arena arena;
	private PlayerInfo pInfo;

	public MeetingBtnInv(Arena arena, PlayerInfo pInfo) {
		super(45, Main.getMessagesManager().getGameMsg("meetingButtonInvTitle", arena, null, pInfo.getPlayer()));
		this.arena = arena;
		this.pInfo = pInfo;
		Utils.fillInv(this.inv);
		this.update();
	}

	private static ArrayList<Integer> slotsBW = new ArrayList<Integer>(Arrays.asList(39, 38, 29, 20, 11, 2, 3, 4, 5, 6, 15, 24, 33, 42, 41));
	private static ArrayList<Integer> slotsBtn = new ArrayList<Integer>(Arrays.asList(12, 13, 14, 21, 22, 23, 30, 31, 32));

	public void update() {
		Integer color = 0;
		for (Integer slot : slotsBW) {
			Material mat = color == 0 ? Material.YELLOW_CONCRETE : Material.BLACK_CONCRETE;
			this.inv.setItem(slot, Utils.createItem(mat, " "));
			color = color == 0 ? 1 : 0;
		}
		ItemInfoContainer infoItem = Main.getItemsManager().getItem("meetingButton_info");
		String value = pInfo.getPlayer().getName();
		String value1 = pInfo.getMeetingsLeft() + "";
		String value2 = arena.getMeetingManager().getMeetingCooldownTimer() + "";
		Integer amount = arena.getMeetingManager().getMeetingCooldownTimer();
		if (amount == 0) {
			amount = 1;
		}
		if (arena.getMeetingManager().getMeetingCooldownTimer() > 0) {
			this.inv.setItem(40, Utils.createItem(infoItem.getItem3().getMat(), infoItem.getItem3().getTitle(value, value1, value2, null, null), amount, infoItem.getItem3().getLore(value, value1, value2, null, null)));
		} else {
			if (arena.getSabotageManager().getIsSabotageActive()) {
				this.inv.setItem(40,
						Utils.createItem(infoItem.getItem2().getMat(), infoItem.getItem2().getTitle(value, value1, value2, null, null), amount, infoItem.getItem2().getLore(value, value1, value2, null, null)));
			} else {
				this.inv.setItem(40, Utils.createItem(infoItem.getItem().getMat(), infoItem.getItem().getTitle(value, value1, value2, null, null), amount, infoItem.getItem().getLore(value, value1, value2, null, null)));
			}
		}

		ItemInfoContainer buttonItem = Main.getItemsManager().getItem("meetingButton_button");
		Icon icon = new Icon(Utils.createItem(arena.canPlayerUseButton(pInfo) ? buttonItem.getItem2().getMat() : buttonItem.getItem().getMat(),
				arena.canPlayerUseButton(pInfo) ? buttonItem.getItem2().getTitle() : buttonItem.getItem().getTitle(), 1,
				arena.canPlayerUseButton(pInfo) ? buttonItem.getItem2().getLore() : buttonItem.getItem().getLore()));
		if (arena.canPlayerUseButton(pInfo)) {
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					arena.getMeetingManager().callMeeting(player, false, null);
				}
			});
		}
		for (Integer slot : slotsBtn) {
			this.setIcon(slot, icon);
		}
	}

	public Arena getArena() {
		return arena;
	}

	public PlayerInfo getpInfo() {
		return pInfo;
	}
}