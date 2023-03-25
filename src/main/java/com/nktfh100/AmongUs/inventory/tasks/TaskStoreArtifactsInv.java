package com.nktfh100.AmongUs.inventory.tasks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskStoreArtifactsInv extends TaskInvHolder {

	private String activeArtifact = "";
	private HashMap<String, Boolean> artifactsState = new HashMap<String, Boolean>();
	private Boolean isDone = false;

	public TaskStoreArtifactsInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.artifactsState.put("diamond", false);
		this.artifactsState.put("purple", false);
		this.artifactsState.put("leaf", false);
		this.artifactsState.put("skull", false);
		this.update();
	}

	public void handleArtifactClick(String type) {
		if (this.isDone) {
			return;
		}
		this.activeArtifact = type;

		this.update();
	}

	public void handleArtifactTargetClick(String clickedType) {
		if (this.isDone) {
			return;
		}
		if (this.activeArtifact.equals(clickedType)) {

			Main.getSoundsManager().playSound("taskStoreArtifacts_artifactDone", this.getPlayerInfo().getPlayer(), this.getPlayerInfo().getPlayer().getLocation());
			this.activeArtifact = "";
			this.artifactsState.put(clickedType, true);
			this.isDone = true;
			for (Boolean bol : this.artifactsState.values()) {
				if (!bol) {
					this.isDone = false;
					break;
				}
			}
			this.update();
			this.checkDone();
		}
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskStoreArtifactsInv inv = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = inv.getTaskPlayer().getPlayerInfo().getPlayer();
					if (player.getOpenInventory().getTopInventory() == inv.getInventory()) {
						player.closeInventory();
					}
				}
			}.runTaskLater(Main.getPlugin(), 20L);
			return true;
		}
		return false;
	}

	@Override
	public void update() {
		TaskStoreArtifactsInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("storeArtifacts_info").getItem().getItem());

		ItemInfoContainer diamondItemInfo = Main.getItemsManager().getItem("storeArtifacts_diamond");
		ItemInfoContainer purpleItemInfo = Main.getItemsManager().getItem("storeArtifacts_purple");
		ItemInfoContainer skullItemInfo = Main.getItemsManager().getItem("storeArtifacts_skull");
		ItemInfoContainer leafItemInfo = Main.getItemsManager().getItem("storeArtifacts_leaf");

		if (!this.artifactsState.get("diamond")) { // not done
			Icon icon = new Icon(!this.activeArtifact.equals("diamond") ? diamondItemInfo.getItem().getItem() : Utils.enchantedItem(diamondItemInfo.getItem().getItem(), Enchantment.DURABILITY, 1));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactClick("diamond");
				}
			});
			this.setIcon(1, icon);
			this.setIcon(2, icon);
			this.setIcon(10, icon);
			this.setIcon(11, icon);

			icon = new Icon(diamondItemInfo.getItem2().getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactTargetClick("diamond");
				}
			});
			this.setIcon(41, icon);
			this.setIcon(42, icon);
			this.setIcon(50, icon);
			this.setIcon(51, icon);
		} else {
			Icon icon = new Icon(Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
			this.setIcon(1, icon);
			this.setIcon(2, icon);
			this.setIcon(10, icon);
			this.setIcon(11, icon);

			icon = new Icon(diamondItemInfo.getItem().getItem());
			this.setIcon(41, icon);
			this.setIcon(42, icon);
			this.setIcon(50, icon);
			this.setIcon(51, icon);
		}

		if (!this.artifactsState.get("purple")) {
			Icon icon = new Icon(!this.activeArtifact.equals("purple") ? purpleItemInfo.getItem().getItem() : Utils.enchantedItem(purpleItemInfo.getItem().getItem(), Enchantment.DURABILITY, 1));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactClick("purple");
				}
			});
			this.setIcon(0, icon);
			this.setIcon(9, icon);
			this.setIcon(18, icon);

			icon = new Icon(purpleItemInfo.getItem2().getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactTargetClick("purple");
				}
			});
			this.setIcon(7, icon);
			this.setIcon(16, icon);
			this.setIcon(25, icon);
		} else {
			Icon icon = new Icon(Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
			this.setIcon(0, icon);
			this.setIcon(9, icon);
			this.setIcon(18, icon);

			icon = new Icon(purpleItemInfo.getItem().getItem());
			this.setIcon(7, icon);
			this.setIcon(16, icon);
			this.setIcon(25, icon);
		}

		if (!this.artifactsState.get("skull")) {
			Icon icon = new Icon(!this.activeArtifact.equals("skull") ? skullItemInfo.getItem().getItem() : Utils.enchantedItem(skullItemInfo.getItem().getItem(), Enchantment.DURABILITY, 1));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactClick("skull");
				}
			});
			this.setIcon(38, icon);
			this.setIcon(39, icon);
			this.setIcon(47, icon);
			this.setIcon(48, icon);

			icon = new Icon(skullItemInfo.getItem2().getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactTargetClick("skull");
				}
			});
			this.setIcon(34, icon);
			this.setIcon(35, icon);
			this.setIcon(43, icon);
			this.setIcon(44, icon);

		} else {
			Icon icon = new Icon(Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
			this.setIcon(38, icon);
			this.setIcon(39, icon);
			this.setIcon(47, icon);
			this.setIcon(48, icon);

			icon = new Icon(skullItemInfo.getItem().getItem());
			this.setIcon(34, icon);
			this.setIcon(35, icon);
			this.setIcon(43, icon);
			this.setIcon(44, icon);
		}

		if (!this.artifactsState.get("leaf")) {
			Icon icon = new Icon(!this.activeArtifact.equals("leaf") ? leafItemInfo.getItem().getItem() : Utils.enchantedItem(leafItemInfo.getItem().getItem(), Enchantment.DURABILITY, 1));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactClick("leaf");
				}
			});
			this.setIcon(27, icon);
			this.setIcon(28, icon);
			this.setIcon(36, icon);
			this.setIcon(37, icon);
			this.setIcon(45, icon);
			this.setIcon(46, icon);

			icon = new Icon(leafItemInfo.getItem2().getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleArtifactTargetClick("leaf");
				}
			});
			this.setIcon(14, icon);
			this.setIcon(15, icon);
			this.setIcon(23, icon);
			this.setIcon(24, icon);
			this.setIcon(32, icon);
			this.setIcon(33, icon);

		} else {
			Icon icon = new Icon(Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
			this.setIcon(27, icon);
			this.setIcon(28, icon);
			this.setIcon(36, icon);
			this.setIcon(37, icon);
			this.setIcon(45, icon);
			this.setIcon(46, icon);

			icon = new Icon(leafItemInfo.getItem().getItem());
			this.setIcon(14, icon);
			this.setIcon(15, icon);
			this.setIcon(23, icon);
			this.setIcon(24, icon);
			this.setIcon(32, icon);
			this.setIcon(33, icon);
		}
	}

	@Override
	public void invClosed() {
	}
}