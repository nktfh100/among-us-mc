package com.nktfh100.AmongUs.info;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

public class ColorInfo implements Comparable<ColorInfo> {

	private String key;
	private Integer i;
	private String name;
	private ChatColor chatColor;
	private Material glass;
	private Material wool;
	private Color armorColor;
	private String id;
	private String height;
	private String weight;
	private String bloodType;

	public ColorInfo(String key, Integer i, String name, ChatColor chatColor, Material glass, Material wool,Color armorColor, String id, String height, String weight, String bloodType) {
		this.key = key;
		this.i = i;
		this.name = name;
		this.chatColor = chatColor;
		this.glass = glass;
		this.wool = wool;
		this.armorColor = armorColor;
		this.id = id;
		this.height = height;
		this.weight = weight;
		this.bloodType = bloodType;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public Material getGlass() {
		return glass;
	}

	public Material getWool() {
		return wool;
	}

	public Color getArmorColor() {
		return armorColor;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public String getId() {
		return id;
	}

	public String getHeight() {
		return height;
	}

	public String getWeight() {
		return weight;
	}

	public String getBloodType() {
		return bloodType;
	}

	public Integer getI() {
		return i;
	}

	@Override
	public int compareTo(ColorInfo o) {
		return this.i.compareTo(o.getI());
	}

}
