package com.nktfh100.AmongUs.inventory;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomHolder implements InventoryHolder {

	protected Inventory inv;
	protected Integer size;
	protected String title;
	protected final HashMap<Integer, Icon> icons = new HashMap<>();

	public CustomHolder(Integer size, String title) {
		this.inv = Bukkit.createInventory(this, size, title);
		this.size = size;
		this.title = title;
	}

	public void changeTitle(String newTitle) {
		this.inv = Bukkit.createInventory(this, size, newTitle);
		this.title = newTitle;
		for (Integer i : this.icons.keySet()) {
			this.inv.setItem(i, this.icons.get(i).itemStack);
		}
	}

	public void changeSize(int newSize) {
		this.size = newSize;
		this.inv = Bukkit.createInventory(this, newSize, this.title);
	}

	public void clearInv() {
		this.inv.clear();
		this.icons.clear();
	}

	public void setIcon(int pos, Icon icon) {
		if(icon != null) {
			this.icons.put(pos, icon);			
			this.inv.setItem(pos, icon.itemStack);
		}
	}

	public void addIcon(Icon icon) {
		for (int i = 0; i < this.size; i++) {
			if (this.inv.getItem(i) == null || this.inv.getItem(i).getType() == Material.AIR) {
				this.icons.put(i, icon);
				this.inv.setItem(i, icon.itemStack);
				return;
			}
		}
	}

	public Icon getIcon(int pos) {
		return this.icons.get(pos);
	}

	public void removeIcon(int pos) {
		this.icons.remove(pos);
	}

	@Override
	public Inventory getInventory() {
		return this.inv;
	}

}
