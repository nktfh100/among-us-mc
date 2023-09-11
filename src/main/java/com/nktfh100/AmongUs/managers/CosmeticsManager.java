package com.nktfh100.AmongUs.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import com.nktfh100.AmongUs.utils.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.enums.CosmeticType;
import com.nktfh100.AmongUs.info.CosmeticItem;
import com.nktfh100.AmongUs.main.Main;

import net.md_5.bungee.api.ChatColor;

public class CosmeticsManager {
	private HashMap<CosmeticType, ArrayList<CosmeticItem>> cosmeticsOrder = new HashMap<CosmeticType, ArrayList<CosmeticItem>>();
	
	private HashMap<CosmeticType, HashMap<String, CosmeticItem>> cosmetics = new HashMap<CosmeticType, HashMap<String, CosmeticItem>>();
	private HashMap<CosmeticType, String> defaultCosmetics = new HashMap<CosmeticType, String>();
	private HashMap<String, Integer> coins = new HashMap<String, Integer>();

	public void loadCosmetics() {
		File configFIle = new File(Main.getPlugin().getDataFolder(), "cosmetics.yml");
		if (!configFIle.exists()) {
			try {
				Main.getPlugin().saveResource("cosmetics.yml", false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFIle);
		try {
			this.cosmetics = new HashMap<CosmeticType, HashMap<String, CosmeticItem>>();
			for (CosmeticType cosmetic_ : CosmeticType.values()) {
				String type = cosmetic_.getName();
				ConfigurationSection typeSec = config.getConfigurationSection(type);
				if (typeSec == null) {
					continue;
				}
				this.cosmetics.put(CosmeticType.valueOf(type.toUpperCase()), new HashMap<String, CosmeticItem>());
				this.defaultCosmetics.put(CosmeticType.valueOf(type.toUpperCase()), config.getString("default_" + type));
				this.cosmeticsOrder.put(CosmeticType.valueOf(type.toUpperCase()), new ArrayList<CosmeticItem>());
				for (String itemKey : typeSec.getKeys(false)) {
					ConfigurationSection itemSec = typeSec.getConfigurationSection(itemKey);
					Material mat = Material.getMaterial(itemSec.getString("material", "BARRIER"));
					String displayName = ChatColor.translateAlternateColorCodes('&', itemSec.getString("display_name", "display name"));
					String name = itemSec.getString("name", "cosmetic name");
					int slot = itemSec.getInt("slot", 0);
					ArrayList<String> lore = (ArrayList<String>) itemSec.getStringList("lore");
					ArrayList<String> lore2 = (ArrayList<String>) itemSec.getStringList("lore2");
					ArrayList<String> lore3 = (ArrayList<String>) itemSec.getStringList("lore3");
					int price = itemSec.getInt("price", 0);
					String permission = itemSec.getString("permission", "");
					CosmeticItem cosmeticItem = new CosmeticItem(itemKey, mat, displayName, name, slot, lore, lore2, lore3, price, permission);
					this.cosmetics.get(CosmeticType.valueOf(type.toUpperCase())).put(itemKey, cosmeticItem);
					this.cosmeticsOrder.get(CosmeticType.valueOf(type.toUpperCase())).add(cosmeticItem);
				}
			}

			if (config.getConfigurationSection("coins") != null) {
				ConfigurationSection coinsSC = config.getConfigurationSection("coins");
				for (String key : coinsSC.getKeys(false)) {
					this.coins.put(key, coinsSC.getInt(key, 0));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.log(Level.SEVERE, "Something is wrong with your cosmetics.yml file!");
			Main.getPlugin().getPluginLoader().disablePlugin(Main.getPlugin());
		}
	}

	public void addCoins(String key, Player player) {
		if (Main.getIsPlayerPoints()) {
			if (this.coins.get(key) != null && this.coins.get(key) != 0) {
				Main.getPlayerPointsApi().give(player.getUniqueId(), this.coins.get(key));
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%coins%", String.valueOf(this.coins.get(key)));
				player.sendMessage(Main.getMessagesManager().getGameMsg("playerCoins", null, placeholders, player));
			}
		}
	}

	public String getDefaultCosmetic(CosmeticType key) {
		return this.defaultCosmetics.get(key);
	}

	public ArrayList<CosmeticItem> getAllCosmeticsFor(CosmeticType key) {
		return new ArrayList<CosmeticItem>(this.cosmetics.get(key).values());
	}

	public CosmeticItem getCosmeticItem(CosmeticType group, String key) {
		return this.cosmetics.get(group).get(key);
	}
	
	public ArrayList<CosmeticItem> getOrderedCosmetics(CosmeticType key) {
		return this.cosmeticsOrder.get(key);
	}

	public void delete() {
		this.cosmetics = null;
		this.defaultCosmetics = null;
		this.cosmeticsOrder = null;
	}
}
