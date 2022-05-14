package com.nktfh100.AmongUs.inventory;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Icon {

    public final ItemStack itemStack;

    public final ArrayList<ClickAction> clickActions = new ArrayList<>();

    public Icon(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    public void executeActions(Player player) {
    	for(ClickAction action : clickActions) {
    		action.execute(player);
    	}
    }

    public Icon addClickAction(ClickAction clickAction) {
        this.clickActions.add(clickAction);
        return this;
    }

    public ArrayList<ClickAction> getClickActions() {
        return this.clickActions;
    }
}
