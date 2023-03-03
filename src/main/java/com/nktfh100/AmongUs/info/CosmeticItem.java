package com.nktfh100.AmongUs.info;

import java.util.ArrayList;

import org.bukkit.Material;

public class CosmeticItem {

	private String key;
	private Material mat;
	private String displayName;
	private String name;
	private int slot;
	private ArrayList<String> lore;
	private ArrayList<String> lore2;
	private ArrayList<String> lore3;
	private int price;
	private String permission;

	public CosmeticItem(String key, Material mat, String displayName,String name, int slot, ArrayList<String> lore, ArrayList<String> lore2, ArrayList<String> lore3, int price, String permission) {
		this.key = key;
		this.mat = mat;
		this.displayName = displayName;
		this.name = name;
		this.slot = slot;
		this.lore = lore;
		this.lore2 = lore2;
		this.lore3 = lore3;
		this.price = price;
		this.permission = permission;
	}
	
	public Material getMat() {
		return mat;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getSlot() {
		return slot;
	}

	public ArrayList<String> getLore() {
		return lore;
	}

	public ArrayList<String> getLore2() {
		return lore2;
	}

	public ArrayList<String> getLore3() {
		return lore3;
	}

	public int getPrice() {
		return price;
	}

	public String getPermission() {
		return permission;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

}
