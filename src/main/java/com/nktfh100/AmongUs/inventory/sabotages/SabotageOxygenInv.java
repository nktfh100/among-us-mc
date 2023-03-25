package com.nktfh100.AmongUs.inventory.sabotages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class SabotageOxygenInv extends SabotageInvHolder {

	private Integer taskNum;
	private ArrayList<Integer> code = new ArrayList<Integer>();
	private ArrayList<Integer> activeCode = new ArrayList<Integer>();
	private Boolean canClick = true;

	public SabotageOxygenInv(SabotageArena saboArena, Integer taskNum, ArrayList<Integer> code, Player p) {

		super(54, Main.getMessagesManager().getGameMsg("sabotageOxygenInvTitle", saboArena.getArena(), getPlaceholders(codeToStr(code), ""), p), saboArena.getArena(), saboArena);
		Utils.fillInv(this.inv);
		this.taskNum = taskNum;
		this.code = code;
		this.update();
	}

	private static HashMap<String, String> getPlaceholders(String codeToday, String userCode) {
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("%sabotage_name%", Main.getMessagesManager().getTaskName(SabotageType.OXYGEN.toString()));
		placeholders.put("%sabotage_title%", Main.getMessagesManager().getSabotageTitle(SabotageType.OXYGEN));
		placeholders.put("%code_today%", codeToday);
		placeholders.put("%user_code%", userCode);
		return placeholders;
	}

	private static String codeToStr(ArrayList<Integer> code_) {
		String c = "";
		for (Integer i : code_) {
			c = c + i.toString();
		}
		return c;
	}

	public void handleNumClick(Player p, Integer num) {
		if (!this.canClick) {
			return;
		}
		if (this.activeCode.size() < 5) {
			this.activeCode.add(num);
			Main.getSoundsManager().playSound("sabotageOxygenNumberClick", p, p.getLocation());
		}
		this.changeTitle(Main.getMessagesManager().getGameMsg("sabotageOxygenInvTitle", this.arena, getPlaceholders(codeToStr(this.code), codeToStr(this.activeCode)), p));
		Utils.fillInv(this.inv);
		this.update();
		p.openInventory(this.getInventory());
	}

	public void handleVClick(Player p) {
		if (!this.canClick) {
			return;
		}
		Main.getSoundsManager().playSound("sabotageOxygenAcceptClick", p, p.getLocation());
		if (this.activeCode.size() == 5) {
			Boolean isOk = true;
			for (int i = 0; i < this.code.size(); i++) {
				if (this.activeCode.get(i) != this.code.get(i)) {
					isOk = false;
					break;
				}
			}
			if (isOk) {
				this.canClick = false;
				this.getSabotageArena().taskDone(this.getTaskNum(), p);
				p.closeInventory();
//				SabotageOxygenInv oxygenInv = this;
//				new BukkitRunnable() {
//					@Override
//					public void run() {
//						if(oxygenInv.getSabotageArena().getArena().getSabotageManager().getIsSabotageActive()) {
//							oxygenInv.getSabotageArena().taskDone(oxygenInv.getTaskNum());							
//						}
//					}
//				}.runTaskLater(Main.getPlugin(), 25L);
				return;
			}
		}
		this.activeCode.clear();
		this.changeTitle(Main.getMessagesManager().getGameMsg("sabotageOxygenInvTitle", this.arena, getPlaceholders(codeToStr(this.code), codeToStr(this.activeCode)), p));
		Utils.fillInv(this.inv);
		this.update();
		p.openInventory(this.getInventory());
	}

	public void handleXClick(Player p) {
		if (!this.canClick) {
			return;
		}
		Main.getSoundsManager().playSound("sabotageOxygenCancelClick", p, p.getLocation());
		this.activeCode.clear();
		this.changeTitle(Main.getMessagesManager().getGameMsg("sabotageOxygenInvTitle", this.arena, getPlaceholders(codeToStr(this.code), codeToStr(this.activeCode)), p));
		Utils.fillInv(this.inv);
		this.update();
		p.openInventory(this.getInventory());
	}

	private static ArrayList<Integer> slots_ = new ArrayList<>(Arrays.asList(21, 22, 23, 30, 31, 32, 39, 40, 41, 49));

	@Override
	public void update() {
		SabotageOxygenInv oxygenInv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("oxygenSabotage_info").getItem().getItem());

		ItemInfo codeItem = Main.getItemsManager().getItem("oxygenSabotage_code").getItem();
		for (int i = 0; i < 5; i++) {
			if (this.activeCode.size() > i) {
				this.inv.setItem(2 + i, Main.getItemsManager().getItem("oxygenSabotage_button" + this.activeCode.get(i)).getItem().getItem());
			} else {
				this.inv.setItem(2 + i, codeItem.getItem("_", null));
			}
		}

		int i = 1;
		for (Integer slot : slots_) {
			ItemInfo buttonItem = Main.getItemsManager().getItem("oxygenSabotage_button" + i).getItem();
			ItemStack item = buttonItem.getItem(i + "", null);
			item.setAmount(i == 0 ? 1 : i);

			Icon icon = new Icon(item);
			final Integer num = i;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					oxygenInv.handleNumClick(player, num);
				}
			});

			this.setIcon(slot, icon);
			i++;
			if (i == 10) {
				i = 0;
			}
		}

		ItemStack cancelItemS = Main.getItemsManager().getItem("oxygenSabotage_cancel").getItem().getItem();
		Icon icon = new Icon(cancelItemS);
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				oxygenInv.handleXClick(player);
			}
		});
		this.setIcon(48, icon);

		ItemStack acceptItemS = Main.getItemsManager().getItem("oxygenSabotage_accept").getItem().getItem();
		icon = new Icon(acceptItemS);
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				oxygenInv.handleVClick(player);
			}
		});
		this.setIcon(50, icon);
	}

	@Override
	public void invClosed(Player player) {
	}

	public Integer getTaskNum() {
		return taskNum;
	}

	public ArrayList<Integer> getCode() {
		return code;
	}

	public ArrayList<Integer> getActiveCode() {
		return activeCode;
	}

	public void setActiveCode(ArrayList<Integer> activeCode) {
		this.activeCode = activeCode;
	}

}