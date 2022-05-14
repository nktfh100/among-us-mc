package com.nktfh100.AmongUs.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.main.Main;

public class ItemsManager {

	private final static ArrayList<String> nums = new ArrayList<String>(Arrays.asList("", "2", "3"));
	private HashMap<String, ItemInfoContainer> items = new HashMap<String, ItemInfoContainer>();

	public void loadItems() {
		File itemsConfigFIle = new File(Main.getPlugin().getDataFolder(), "items.yml");
		if (!itemsConfigFIle.exists()) {
			try {
				Main.getPlugin().saveResource("items.yml", false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsConfigFIle);
		try {
			this.items = new HashMap<String, ItemInfoContainer>();

			ConfigurationSection itemsSC = itemsConfig.getConfigurationSection("items");
			Set<String> itemsKeys = itemsSC.getKeys(false);
			for (String key : itemsKeys) {
				try {
					ConfigurationSection itemSC = itemsSC.getConfigurationSection(key);

					ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();

					for (String num : nums) {
						Integer slot = itemSC.getInt("slot", 0);
						Boolean isHead = false;
						String matStr = itemSC.getString("mat" + num, "BARRIER");
						Material mat_;
						if (matStr.startsWith("@") && matStr.endsWith("@")) {
							isHead = true;
							matStr = matStr.replace("@", "");
							mat_ = Material.PLAYER_HEAD;
						} else {
							if (Main.getPlugin().getServer().getVersion().contains("1.13") && matStr.contains("_sign")) {
								mat_ = Material.getMaterial(matStr.split("_")[1]);
							} else {
								mat_ = Material.getMaterial(matStr);
							}
						}

						String title = ChatColor.translateAlternateColorCodes('&', itemSC.getString("title" + num, " "));
						ArrayList<String> lore = new ArrayList<String>(itemSC.getStringList("lore" + num));
						for (int ii = 0; ii < lore.size(); ii++) {
							lore.set(ii, ChatColor.translateAlternateColorCodes('&', lore.get(ii)));
						}
						ItemInfo itemInfo = new ItemInfo(slot, mat_, title, lore);
						if (isHead) {
							itemInfo.setHeadInfo(matStr);
						}
						items.add(itemInfo);
					}

					this.items.put(key, new ItemInfoContainer(items.get(0), items.get(1), items.get(2)));
				} catch (Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().log(Level.SEVERE, "Something is wrong with your items.yml file! (" + key + ")");
					Main.getPlugin().getPluginLoader().disablePlugin(Main.getPlugin());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().log(Level.SEVERE, "Something is wrong with your items.yml file!");
			Main.getPlugin().getPluginLoader().disablePlugin(Main.getPlugin());
		}
	}

	public ItemInfoContainer getItem(String key) {
		ItemInfoContainer out = this.items.get(key);
		if (out == null) {
			Main.getPlugin().getLogger().warning("Item '" + key + "' is missing from your items.yml file!");
			ItemInfo itemInfo = new ItemInfo(0, Material.BARRIER, "ITEM MISSING", new ArrayList<String>());
			return new ItemInfoContainer(itemInfo, itemInfo, itemInfo);
		}
		return out;
	}

	public void delete() {
		this.items.clear();
		this.items = null;
	}
}
