package com.nktfh100.AmongUs.inventory.sabotages;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class SabotageCommsInv extends SabotageInvHolder {
	private static HashMap<Integer, Integer> locationToSlot = new HashMap<Integer, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2546975934120412999L;

		{
			put(0, 47);
			put(1, 38);
			put(2, 29);
			put(3, 20);
			put(4, 21);
			put(5, 22);
			put(6, 23);
			put(7, 24);
			put(8, 33);
			put(9, 42);
			put(10, 51);
		}
	};
	private Boolean isActive = false;
	private Integer activeLocation = 5;
	private Integer rightLocation = 0;

	public SabotageCommsInv(SabotageArena saboArena) {
		super(54, Main.getMessagesManager().getGameMsg("sabotageCommsInvTitle", saboArena.getArena(), Utils.getSabotagePlaceholders(SabotageType.COMMUNICATIONS)), saboArena.getArena(), saboArena);
		Utils.fillInv(this.inv);
		Boolean isOk = false;
		while (!isOk) {
			this.rightLocation = Utils.getRandomNumberInRange(0, 10);

			int diff = 0;
			if (this.activeLocation <= this.rightLocation) {
				diff = this.rightLocation - this.activeLocation;
			}
			if (this.activeLocation >= this.rightLocation) {
				diff = this.activeLocation - this.rightLocation;
			}
			if (diff > 2) {
				isOk = true;
				break;
			}
		}
		ItemInfoContainer insideCubeItem = Main.getItemsManager().getItem("commsSabotage_insideCube");
		ItemStack item = Utils.createItem(insideCubeItem.getItem().getMat(), insideCubeItem.getItem().getTitle(), 1, insideCubeItem.getItem().getLore());
		this.inv.setItem(30, item);
		this.inv.setItem(31, item);
		this.inv.setItem(32, item);
		this.inv.setItem(39, item);
		this.inv.setItem(41, item);
		this.inv.setItem(48, item);
		this.inv.setItem(49, item);
		this.inv.setItem(50, item);

		this.inv.setItem(40, Utils.createItem(insideCubeItem.getItem2().getMat(), insideCubeItem.getItem2().getTitle(), 1, insideCubeItem.getItem2().getLore()));

		this.inv.setItem(8, Main.getItemsManager().getItem("commsSabotage_info").getItem().getItem());
		this.update();
	}

	public void locationClick(Player player, Integer loc) {
		if (this.isActive && loc != this.activeLocation) {
			if (loc == this.activeLocation - 1 || loc == this.activeLocation + 1) {
				Main.getSoundsManager().playSound("sabotageCommsClick", player, player.getLocation());
				this.activeLocation = loc;
				this.isActive = false;
				if (loc == rightLocation) {
					if (this.activeLocation == rightLocation) {
						this.sabotageArena.taskDone(player);
					}
				}
			}
		} else if (!this.isActive && loc == this.activeLocation) {
			this.isActive = true;
		}
		this.update();
	}

	@Override
	public Inventory getInventory() {
		return this.inv;
	}

	@Override
	public void update() {

		ItemInfoContainer handleItem = Main.getItemsManager().getItem("commsSabotage_handle");
		ItemInfoContainer outerCubeItem = Main.getItemsManager().getItem("commsSabotage_outerCube");

		SabotageCommsInv commsInv = this;
		for (int i = 0; i < 11; i++) {
			ItemStack item = new ItemStack(Material.AIR);
			if (this.activeLocation == i) {
				if (this.isActive) {
					item = Utils.createItem(handleItem.getItem2().getMat(), handleItem.getItem2().getTitle(), 1, handleItem.getItem2().getLore());
					item = Utils.enchantedItem(item, Enchantment.DURABILITY, 10);
				} else {
					item = Utils.createItem(handleItem.getItem().getMat(), handleItem.getItem().getTitle(), 1, handleItem.getItem().getLore());
				}
			} else {
				if (this.isActive && (this.activeLocation - 1 == i || this.activeLocation + 1 == i)) {
					item = Utils.createItem(outerCubeItem.getItem2().getMat(), outerCubeItem.getItem2().getTitle(), 1, outerCubeItem.getItem2().getLore());
				} else {
					item = Utils.createItem(outerCubeItem.getItem().getMat(), outerCubeItem.getItem().getTitle(), 1, outerCubeItem.getItem().getLore());
				}
			}

			Icon icon = new Icon(item);
			final Integer loc_ = i;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					commsInv.locationClick(player, loc_);
				}
			});
			this.setIcon(SabotageCommsInv.getSlotFromLocation(i), icon);
		}
	}

	@Override
	public void invClosed(Player player) {
	}

	public static Integer getSlotFromLocation(Integer loc) {
		return locationToSlot.get(loc);
	}

	public Integer getActiveLocation() {
		return activeLocation;
	}

	public void setActiveLocation(Integer activeLocation) {
		this.activeLocation = activeLocation;
	}

	public Integer getRightLocation() {
		return rightLocation;
	}

	public void setRightLocation(Integer rightLocation) {
		this.rightLocation = rightLocation;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}