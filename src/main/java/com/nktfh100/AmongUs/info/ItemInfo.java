package com.nktfh100.AmongUs.info;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.utils.Utils;

public class ItemInfo {

	private Boolean isHead = false;
	private String texture;
	private final Integer slot;
	private final Material mat;
	private final String title;
	private final ArrayList<String> lore;

	public ItemInfo(Integer slot, Material mat, String title, ArrayList<String> lore) {
		this.slot = slot;
		this.mat = mat;
		this.title = title;
		this.lore = lore;
	}

	public void setHeadInfo(String texture) {
		this.isHead = true;
		this.texture = texture;
	}

	public ItemStack getItem() {
		return getItem(null, null, null);
	}

	public ItemStack getItem(String value, String value1) {
		return getItem(value, value1, null);
	}

	public ItemStack getItem(String value, String value1, String value2) {
		return this.getItem(value, value1, value2, null, null);
	}
	
	public ItemStack getItem(String value, String value1, String value2, String value3, String value4) {
		if (this.isHead) {
			ItemStack out = Utils.createSkull(this.texture, this.getTitle(value, value1, value2, value3, value4), 1, this.getLore(value, value1, value2, value3, value4));
			Utils.addItemFlag(out, ItemFlag.HIDE_ATTRIBUTES);
			return out;
		} else {
			ItemStack out = Utils.createItem(this.getMat(), this.getTitle(value, value1, value2, value3, value4), 1, this.getLore(value, value1, value2, value3, value4));
			Utils.addItemFlag(out, ItemFlag.HIDE_ATTRIBUTES);
			return out;
		}
	}

	private String replaceValues(String line, String value, String value1, String value2, String value3, String value4) {
		if (line == null || line.isEmpty()) {
			return "";
		}
		if (value != null) {
			line = line.replace("%value%", value);
		}
		if (value1 != null) {
			line = line.replace("%value1%", value1);
		}
		if (value2 != null) {
			line = line.replace("%value2%", value2);
		}
		if (value3 != null) {
			line = line.replace("%value3%", value3);
		}
		if (value4 != null) {
			line = line.replace("%value4%", value4);
		}
		return line;
	}

	// ------- title -------

	public String getTitle() {
		if (this.title == null || this.title.isEmpty()) {
			return "";
		}
		return title;
	}

	public String getTitle(String value, String value1, String value2, String value3, String value4) {
		if (this.title == null || this.title.isEmpty()) {
			return "";
		}
		return replaceValues(this.title, value, value1, value2, value3, value4);
	}

	public String getTitle(String value, String value1) {
		return getTitle(value, value1, null, null, null);
	}

	public String getTitle(String value) {
		return getTitle(value, null);
	}

	// ------- lore -------

	public ArrayList<String> getLore() {
		if (this.lore == null || this.lore.size() == 0) {
			return new ArrayList<String>();
		}
		return lore;
	}

	public ArrayList<String> getLore(String value, String value1, String value2, String value3, String value4) {
		if (this.lore == null || this.lore.size() == 0) {
			return new ArrayList<String>();
		}
		ArrayList<String> newLore = new ArrayList<String>();
		for (String line : this.lore) {
			newLore.add(this.replaceValues(line, value, value1, value2, value3, value4));
		}
		return newLore;
	}

	public ArrayList<String> getLore(String value, String value1) {
		return getLore(value, value1, null, null, null);
	}

	public ArrayList<String> getLore(String value) {
		return getLore(value, null);
	}

	public Integer getSlot() {
		return slot;
	}

	public Material getMat() {
		return mat;
	}

	public Boolean getIsHead() {
		return isHead;
	}

	public String getTexture() {
		return texture;
	}
}
