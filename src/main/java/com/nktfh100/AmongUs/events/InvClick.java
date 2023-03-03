package com.nktfh100.AmongUs.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.info.DoorGroup;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.inventory.CustomHolder;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;

public class InvClick implements Listener {

	@EventHandler
	public void invClick(InventoryClickEvent ev) {
		Player player = (Player) ev.getWhoClicked();
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if (!pInfo.getIsIngame()) {
			// setup gui
			Inventory inv = ev.getClickedInventory();
			if (inv != null && inv.getHolder() != null && inv.getHolder() instanceof CustomHolder) {
				ev.setCancelled(true);
				CustomHolder customHolder = (CustomHolder) ev.getView().getTopInventory().getHolder();
				Icon icon = customHolder.getIcon(ev.getRawSlot());
				if (icon != null) {
					icon.executeActions(player);
				}
				return;
			}
		} else {
			ev.setCancelled(true);
			if (ev.getView().getTopInventory().getHolder() instanceof CustomHolder) {
				CustomHolder customHolder = (CustomHolder) ev.getView().getTopInventory().getHolder();
				Icon icon = customHolder.getIcon(ev.getRawSlot());
				if (icon != null) {
					icon.executeActions(player);
				}
			}

			/* ---------------------------------------------------- */

			else if (ev.getView().getType() == InventoryType.CRAFTING) {
				ItemStack itemClicked = ev.getCurrentItem();
				if (!pInfo.getIsImposter() || itemClicked == null || itemClicked.getType() == Material.AIR || itemClicked.getItemMeta() == null) {
					ev.setCancelled(true);
					return;
				}
				SabotageArena saboClicked = null;
				for (SabotageArena sa : pInfo.getArena().getSabotages()) {
					String name = Main.getMessagesManager().getTaskName(sa.getType().toString());
					if (itemClicked.isSimilar(pInfo.getArena().getSabotageManager().getSabotageItem(sa.getType(), name, pInfo.getArena().getSabotageManager().getSabotageCoolDownTimer(player)))) {
						saboClicked = sa;
						break;
					}
				}
				if (saboClicked != null) {
					if (pInfo.getArena().getSabotageManager().getSabotageCoolDownTimer(player) == 0) {
						pInfo.getArena().getSabotageManager().startSabotage(saboClicked);
						Main.getSoundsManager().playSound("imposterStartSabotage", player, player.getLocation());
					}
					return;
				}

				DoorGroup doorGroupClicked = null;
				for (DoorGroup dg : pInfo.getArena().getDoorsManager().getDoorGroups()) {
					if (itemClicked.isSimilar(pInfo.getArena().getDoorsManager().getSabotageDoorItem(player, dg.getId()))) {
						doorGroupClicked = dg;
						break;
					}
				}
				if (doorGroupClicked != null) {
					if (doorGroupClicked.getCooldownTimer(player.getUniqueId().toString()) == 0) {
						pInfo.getArena().getDoorsManager().closeDoorGroup(player, doorGroupClicked.getId());
						Main.getSoundsManager().playSound("imposterCloseDoor", player, player.getLocation());
					}
					return;
				}

			}

		}
	}
}
