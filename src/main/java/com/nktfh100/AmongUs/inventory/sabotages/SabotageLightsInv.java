package com.nktfh100.AmongUs.inventory.sabotages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class SabotageLightsInv extends SabotageInvHolder {

	private ArrayList<Integer> lightsSwitches = new ArrayList<Integer>(Arrays.asList(1, 1, 1, 1, 1));

	private HashMap<Player, Boolean> canClick = new HashMap<Player, Boolean>();

	public SabotageLightsInv(SabotageArena saboArena, Player p) {
		super(27, Main.getMessagesManager().getGameMsg("sabotageLightsInvTitle", saboArena.getArena(), Utils.getSabotagePlaceholders(SabotageType.LIGHTS), p), saboArena.getArena(), saboArena);
		Utils.fillInv(this.inv);
		this.lightsSwitches = Utils.generateLights();
		this.update();
		for (Player player : this.arena.getPlayers()) {
			this.canClick.put(player, true);
		}
	}

	public void LightSwitchClick(Player player, Integer i) {
		SabotageLightsInv lightsInv = this;
		if (this.lightsSwitches.get(i) != null) {
			if (this.canClick.get(player)) {
				this.canClick.put(player, false);
				new BukkitRunnable() {
					@Override
					public void run() {
						lightsInv.getCanClick().put(player, true);
						Main.getSoundsManager().playSound("sabotageLightsClick", player, player.getLocation());
						lightsInv.getLightsSwitches().set(i, lightsInv.getLightsSwitches().get(i) == 0 ? 1 : 0);
						lightsInv.update();
						for (Integer light_ : lightsInv.getLightsSwitches()) {
							if (light_ == 0) {
								return;
							}
						}
						lightsInv.getSabotageArena().taskDone(player);
					}
				}.runTaskLater(Main.getPlugin(), 5L);
			}
		}
	}

	@Override
	public Inventory getInventory() {
		return this.inv;
	}

	@Override
	public void update() {
		
		this.inv.setItem(8, Main.getItemsManager().getItem("lightsSabotage_info").getItem().getItem());
		
		ItemInfoContainer switchItem = Main.getItemsManager().getItem("lightsSabotage_switch");
		SabotageLightsInv lightsInv = this;
		int i = 11;
		int lightI = 0;
		for (Integer light_ : this.getLightsSwitches()) {
			Material mat = light_ == 0 ? switchItem.getItem().getMat() : switchItem.getItem2().getMat();
			String name = (light_ == 0 ? switchItem.getItem().getTitle() : switchItem.getItem2().getTitle());
			ArrayList<String> lore = (light_ == 0 ? switchItem.getItem().getLore() : switchItem.getItem2().getLore());
			Icon icon = new Icon(Utils.createItem(mat, name, 1, lore));
			final Integer lightI_ = lightI;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					lightsInv.LightSwitchClick(player, lightI_);
				}
			});
			this.setIcon(i, icon);
			i++;
			lightI++;
		}
	}

	@Override
	public void invClosed(Player player) {
	}

	public ArrayList<Integer> getLightsSwitches() {
		return lightsSwitches;
	}

	public HashMap<Player, Boolean> getCanClick() {
		return this.canClick;
	}

}