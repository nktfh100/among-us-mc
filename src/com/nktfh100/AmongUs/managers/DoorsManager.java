package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.DoorGroup;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class DoorsManager {

	private Arena arena;
	private ArrayList<DoorGroup> doorGroups = new ArrayList<DoorGroup>();

	public DoorsManager(Arena arena) {
		this.arena = arena;
	}

	public void closeDoorGroup(Player player, Integer id) {
		if (this.arena.getGameState() == GameState.RUNNING) {
			DoorGroup doorGroup = this.doorGroups.get(id);
			if (doorGroup.getCloseTimer() == 0 && doorGroup.getCooldownTimer(player.getUniqueId().toString()) == 0) {

				// update item in inventory
				int s_ = 9;
				for (DoorGroup dg : this.getDoorGroups()) {
					if (dg.getId() == id) {
						ItemStack item = this.getSabotageDoorItem(player, id);
						player.getInventory().setItem(s_, item);
						break;
					}
					s_++;
				}

				doorGroup.setCloseTimer(arena.getDoorCloseTime());
				for (PlayerInfo pInfo : arena.getGameImposters()) {
					if (pInfo.getPlayer() != player) {
						doorGroup.setCooldownTimer(pInfo.getPlayer().getUniqueId().toString(), arena.getDoorCloseTime());
					}
				}
				doorGroup.setCooldownTimer(player.getUniqueId().toString(), arena.getDoorCooldown());
				doorGroup.closeDoors(true);
				if (arena.getSabotageManager().getSabotageCoolDownTimer(player) <= arena.getDoorCloseTime()) {
					arena.getSabotageManager().setSabotageCoolDownTimer(player.getUniqueId().toString(), arena.getDoorCloseTime());
				}
			}
		}
	}

//	public void openDoorGroup(Player player, Integer id) {
//		DoorGroup doorGroup = this.doorGroups.get(id);
//		doorGroup.setCloseTimer(0);
//		doorGroup.openDoors(true);
//	}

	public ItemStack getSabotageDoorItem(Player player, Integer doorGroupId) {
		DoorGroup doorGroup = arena.getDoorsManager().getDoorGroup(doorGroupId);
		ItemInfoContainer doorItem = Main.getItemsManager().getItem("sabotage_door");

		Integer cooldownInt = doorGroup.getCooldownTimer(player.getUniqueId().toString());
		if (cooldownInt == null) {
			cooldownInt = this.arena.getDoorCooldown();
		}
		String cooldownStr = cooldownInt.toString();
		String locName = doorGroup.getLocName().getName();

		Material mat = cooldownInt == 0 ? doorItem.getItem2().getMat() : doorItem.getItem().getMat();
		String title = cooldownInt == 0 ? doorItem.getItem2().getTitle(locName, cooldownStr) : doorItem.getItem().getTitle(locName, cooldownStr);
		ArrayList<String> lore = cooldownInt == 0 ? doorItem.getItem2().getLore(locName, cooldownStr) : doorItem.getItem().getLore(locName, cooldownStr);
		return Utils.createItem(mat, title, cooldownInt > 0 ? cooldownInt : 1, lore);
	}

	public void openDoorsForce() {
		for (DoorGroup dg : this.doorGroups) {
			dg.openDoors(false);
			dg.setCloseTimer(0);
			for (String uuid_ : dg.getCooldownTimer().keySet()) {
				dg.setCooldownTimer(uuid_, arena.getDoorCooldown());
			}
		}
	}

	public void resetDoors() {
		for (DoorGroup dg : this.doorGroups) {
			if (dg.getCloseTimer() > 0) {
				dg.openDoors(false);
			}
			dg.getCooldownTimer().clear();
			dg.setCloseTimer(0);
		}
	}

	public void addImposter(String uuid) {
		for (DoorGroup dg : this.doorGroups) {
			dg.setCooldownTimer(uuid, 0);
		}
	}

	public void removeImposter(String uuid) {
		for (DoorGroup dg : this.doorGroups) {
			dg.getCooldownTimer().remove(uuid);
		}
	}

	public void addDoorGroup(DoorGroup dg) {
		this.doorGroups.add(dg);
		Collections.sort(this.doorGroups);
	}

	public DoorGroup getDoorGroup(Integer id) {
		return this.doorGroups.get(id);
	}

	public void delete() {
		for (DoorGroup dg : doorGroups) {
			dg.delete();
		}
		this.doorGroups = null;
		this.arena = null;
	}

	public Arena getArena() {
		return arena;
	}

	public ArrayList<DoorGroup> getDoorGroups() {
		return doorGroups;
	}
}
