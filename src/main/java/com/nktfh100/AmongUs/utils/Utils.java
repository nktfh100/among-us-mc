package com.nktfh100.AmongUs.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.TaskPlayer;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.ColorInfo;
import com.nktfh100.AmongUs.main.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class Utils {

	public static HashMap<String, String> getTaskPlaceholders(TaskPlayer task) {
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("%task_name%", Main.getMessagesManager().getTaskName(task.getActiveTask().getTaskType().toString()));
		placeholders.put("%task_location%", task.getActiveTask().getLocationName().getName());
		return placeholders;
	}

	public static HashMap<String, String> getSabotagePlaceholders(SabotageType sabotage) {
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("%sabotage_name%", Main.getMessagesManager().getTaskName(sabotage.toString()));
		placeholders.put("%sabotage_title%", Main.getMessagesManager().getSabotageTitle(sabotage));
		return placeholders;
	}

	public static Material getStateBlock(GameState state) {
		switch (state) {
			case WAITING:
				return Material.LIME_STAINED_GLASS;
			case STARTING:
				return Material.BLUE_STAINED_GLASS;
			case RUNNING:
				return Material.RED_STAINED_GLASS;
			case FINISHING:
				return Material.YELLOW_STAINED_GLASS;
			default:
				return Material.WHITE_STAINED_GLASS;
		}
	}

	public static ChatColor getStateColor(GameState state) {
		switch (state) {
			case WAITING:
				return ChatColor.GREEN;
			case STARTING:
				return ChatColor.BLUE;
			case RUNNING:
				return ChatColor.RED;
			case FINISHING:
				return ChatColor.YELLOW;
			default:
				return ChatColor.WHITE;
		}
	}

	public static ArrayList<ColorInfo> getPlayersColors() {
		ArrayList<ColorInfo> colors = Main.getConfigManager().getAllColors();
		Collections.shuffle(colors);
		return colors;
	}

	public static ItemStack getArmorColor(ColorInfo color, Material type_) {
		ItemStack armor = new ItemStack(type_, 1);
		LeatherArmorMeta lch = (LeatherArmorMeta) armor.getItemMeta();
		lch.setColor(color.getArmorColor());
		armor.setItemMeta(lch);
		return armor;
	}

	public static String locationToStringB(Location loc) {
		if (loc == null || loc.getWorld() == null) {
			return "()";
		}
		String output = "(" + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")";
		return output;
	}

	public static ItemStack setItemName(ItemStack item, String name, ArrayList<String> lore) {
		if (item != null && item.getType() != Material.AIR) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
			ArrayList<String> metaLore = new ArrayList<String>();

			for (String lore_ : lore) {
				metaLore.add(lore_);
			}
			meta.setLore(metaLore);
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemStack setItemLore(ItemStack item, ArrayList<String> lore) {
		if (item != null && item.getType() != Material.AIR) {
			ItemMeta meta = item.getItemMeta();
			ArrayList<String> metaLore = new ArrayList<String>();
			for (String lore_ : lore) {
				metaLore.add(ChatColor.translateAlternateColorCodes('&', lore_));
			}
			meta.setLore(metaLore);
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemStack enchantedItem(ItemStack item, Enchantment ench, int lvl) {
		if (item != null && item.getType() != Material.AIR && item.getItemMeta() != null) {
			ItemMeta meta = item.getItemMeta();
			meta.addEnchant(ench, lvl, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemStack createItem(Material mat, String name) {
		ItemStack item = new ItemStack(mat);
		if (item != null && item.getItemMeta() != null && item.getType() != Material.AIR) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			item.setItemMeta(meta);
		}
		return (item);
	}

	public static ItemStack createItem(Material mat, String name, int amount) {
		ItemStack item = createItem(mat, name);
		item.setAmount(amount);
		return (item);
	}

	public static ItemStack createItem(Material mat, String name, int amount, String... lore) {
		ItemStack item = createItem(mat, name, amount);
		if (item != null && item.getItemMeta() != null && item.getType() != Material.AIR) {
			ItemMeta meta = item.getItemMeta();

			ArrayList<String> metaLore = new ArrayList<String>();

			for (String lorecomments : lore) {
				metaLore.add(lorecomments);
			}
			meta.setLore(metaLore);
			item.setItemMeta(meta);
		}
		return (item);
	}

	public static ItemStack createItem(Material mat, String name, int amount, ArrayList<String> lore) {
		ItemStack item = createItem(mat, name, amount);
		if (item != null && item.getItemMeta() != null && item.getType() != Material.AIR) {
			ItemMeta meta = item.getItemMeta();

			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		return (item);
	}

	public static ItemStack createEnchantedItem(Material mat, String name, Enchantment ench, int lvl, String... lore) {
		ItemStack item = createItem(mat, name, 1);
		if (item != null && item.getItemMeta() != null && item.getType() != Material.AIR) {
			ItemMeta meta = item.getItemMeta();
			meta.addEnchant(ench, lvl, true);
			ArrayList<String> metaLore = new ArrayList<String>();

			for (String lorecomments : lore) {
				metaLore.add(lorecomments);
			}
			meta.setLore(metaLore);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
		}
		return (item);
	}

	public static ItemStack addItemFlag(ItemStack item, ItemFlag... flag) {
		if (item != null && item.getItemMeta() != null && item.getType() != Material.AIR) {
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(flag);
			item.setItemMeta(meta);
		}
		return item;
	}

	public static float getRandomFloat(float min, float max) {
		Random rand = new Random();
		return rand.nextFloat() * (max - min) + min;
	}

	public static int getRandomNumberInRange(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	final static String colors_ = "123456789abcdfe";
	
	public static String getRandomColors() {
		String str = "";
		for (int i = 0; i < 4; i++) {
			String char_ = String.valueOf(colors_.charAt(getRandomNumberInRange(0, colors_.length() - 1)));
			str = str + "&" + char_;
		}
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	final static char[] chars_ = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890".toCharArray();

	public static String getRandomString(int length) {
		String randomString = "";

		final Random random = new Random();
		for (int i = 0; i < length; i++) {
			randomString = randomString + chars_[random.nextInt(chars_.length)];
		}

		return randomString;
	}

	public static String reverseString(String str) {

		StringBuilder strBuilder = new StringBuilder();

		strBuilder.append(str);
		strBuilder = strBuilder.reverse();
		return strBuilder.toString();

	}

	public static ItemStack createSkull(String base64, String name, int amount, ArrayList<String> lore) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);
		if (base64.isEmpty()) return head;

		SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
		assert skullMeta != null;

		if (Main.getVersion()[0] >= 19) {
			String skinJson = new String(Base64.getDecoder().decode(base64));
			JsonObject skinObject = JsonParser.parseString(skinJson).getAsJsonObject();
			String url = skinObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

			PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
			PlayerTextures textures = profile.getTextures();

			try {
				URL urlObject = new URL(url);
				textures.setSkin(urlObject);
				profile.setTextures(textures);
				skullMeta.setOwnerProfile(profile);

			} catch (MalformedURLException e) {
				Logger.logError(java.util.logging.Level.WARNING, "An error occurred while parsing a head! Head value: " + base64, e);
				return null;
			}

		} else {
			GameProfile profile = new GameProfile(UUID.randomUUID(), name);
			profile.getProperties().put("textures", new Property("textures", base64));

			try {
				Field profileField = skullMeta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				profileField.set(skullMeta, profile);

			} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
				Logger.logError(java.util.logging.Level.WARNING, "An error occurred while parsing a head! Head value: " + base64, error);
				return null;
			}
		}

		skullMeta.setDisplayName(name);
        ArrayList<String> metaLore = new ArrayList<>(lore);
		skullMeta.setLore(metaLore);
		head.setItemMeta(skullMeta);
		return head;
	}

	public static ItemStack createSkull(String url, String name, int amount, String... lore) {
		ArrayList<String> loreArray = new ArrayList<>(Arrays.asList(lore));
		return createSkull(url, name, amount, loreArray);
	}

	public static ItemStack getHead(String player) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
		try {
			SkullMeta skull = (SkullMeta) item.getItemMeta();
			skull.setOwningPlayer(Bukkit.getPlayer(player));
			item.setItemMeta(skull);
			return item;
		} catch (Exception e) {
			Logger.log(Level.INFO,"Couldn't get " + player + "'s head");
			e.printStackTrace();
			return item;
		}
	}

	public static void addBorder(Inventory inv, Integer slots, Material mat) {
		ItemStack border = Utils.createItem(mat, " ");
		if (slots == 45) {
			for (int slot = 0; slot < inv.getSize(); slot++) {
				if (inv.getItem(slot) == null) {
					if ((slot >= 0 && slot <= 8) || (slot >= 36 && slot <= 44) || (slot % 9 == 0) || ((slot - 8) % 9 == 0)) {
						inv.setItem(slot, border);
					}
				}
			}
		} else if (slots == 54) {
			for (int slot = 0; slot < inv.getSize(); slot++) {
				if (inv.getItem(slot) == null) {
					if ((slot >= 0 && slot <= 8) || (slot >= 45 && slot <= 53) || (slot % 9 == 0) || ((slot - 8) % 9 == 0)) {
						inv.setItem(slot, border);
					}
				}
			}
		}

	}

	public static void addBorder(Inventory inv, Integer slots) {
		addBorder(inv, slots, Material.BLUE_STAINED_GLASS_PANE);
	}

	public static void fillInv(Inventory inv) {
		fillInv(inv, Material.BLACK_STAINED_GLASS_PANE);
	}

	public static void fillInv(Inventory inv, Material mat) {
		ItemStack item = Utils.createItem(mat, " ");
		for (int slot = 0; slot < inv.getSize(); slot++) {
			inv.setItem(slot, item);
		}
	}

	public static ArrayList<Integer> generateLights() {
		ArrayList<Integer> out = new ArrayList<Integer>();
		Integer offNum = getRandomNumberInRange(2, 5);
		for (int i = 0; i < 5; i++) {
			if (offNum > 0) {
				out.add(0);
				offNum--;
			} else {
				out.add(1);
			}
		}
		Collections.shuffle(out);
		return out;
	}

	public static void sendActionBar(Player player, String message) {
		if (!player.isOnline())
			return;

		if (message == null)
			message = "";

		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}

	public static ArmorStand createArmorStand(Location loc) {
		ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as.setBasePlate(false);
		as.setArms(false);
		as.setGravity(false);
		as.setCollidable(false);
		as.setInvulnerable(true);
		as.setVisible(false);
		as.setSilent(true);
		as.setCustomNameVisible(false);
		return as;
	}

	// Stole from:
	// https://github.com/DuffyScottC/GridCircleGenerator/blob/delop/src/dpr212/GenerateCircle.java
	public static ArrayList<Location> generateHollowCircle(Location loc, int radius, int height) {
		ArrayList<Location> coordinates = new ArrayList<Location>();
		int y = loc.getBlockY();
		for (int yi = 0; yi < height; yi++) {

			int x = radius;
			int z = 0;
			int err = 1 - x;

			while (x >= z) {

				coordinates.add(new Location(loc.getWorld(), x + loc.getBlockX(), y, -z + loc.getBlockZ()));
				coordinates.add(new Location(loc.getWorld(), z + loc.getBlockX(), y, -x + loc.getBlockZ()));
				coordinates.add(new Location(loc.getWorld(), -z + loc.getBlockX(), y, -x + loc.getBlockZ()));
				coordinates.add(new Location(loc.getWorld(), -x + loc.getBlockX(), y, -z + loc.getBlockZ()));
				coordinates.add(new Location(loc.getWorld(), -x + loc.getBlockX(), y, z + loc.getBlockZ()));
				coordinates.add(new Location(loc.getWorld(), -z + loc.getBlockX(), y, x + loc.getBlockZ()));
				coordinates.add(new Location(loc.getWorld(), z + loc.getBlockX(), y, x + loc.getBlockZ()));
				coordinates.add(new Location(loc.getWorld(), x + loc.getBlockX(), y, z + loc.getBlockZ()));

				z += 1;
				if (err <= 0) {
					err += 2 * z + 1;
				} else {
					x -= 1;
					err += 2 * (z - x) + 1;
				}

			}
			y++;
		}
		return coordinates;
	}

	public static boolean hasChangedBlockCoordinates(Location fromLoc, Location toLoc) {
		return !(fromLoc.getWorld().equals(toLoc.getWorld()) && fromLoc.getBlockX() == toLoc.getBlockX() && fromLoc.getBlockZ() == toLoc.getBlockZ());
	}

	// 0 = inside
	// 1 = on point
	// 2 = outside
	public static Integer isInsideCircle(Location center, Double r, Location loc) {
		Double output = (Math.pow(loc.getX() - center.getX(), 2) + Math.pow(loc.getZ() - center.getZ(), 2));
		Double r1 = Math.pow(r, 2);
		if (output == r1) {
			return 1;
		}
		if (output < r1) {
			return 0;
		} else {
			return 2;
		}
	}

	public static String[] getSkinData(String name) {
		try {
			String UUIDJson = IOUtils.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + name), StandardCharsets.UTF_8);
			JsonObject uuidObject = new JsonParser().parse(UUIDJson).getAsJsonObject();
			String dashlessUuid = uuidObject.get("id").getAsString();

			String profileJson = IOUtils.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + dashlessUuid + "?unsigned=false"), StandardCharsets.UTF_8);
			JsonObject profileObject = new JsonParser().parse(profileJson).getAsJsonObject();
			String texture = profileObject.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
			String signature = profileObject.getAsJsonArray("properties").get(0).getAsJsonObject().get("signature").getAsString();

			return new String[] { texture, signature };
		} catch (IOException e) {
			if (!Main.getConfigManager().getHidePlayerSkinNotFoundMessage()) {
				Logger.log(Level.WARNING, "Could not get skin data for: " + name);
			}
            return new String[] {"", ""};
        }
    }

	public static ArrayList<Block> blocksFromTwoPoints(Location loc1, Location loc2) {
		ArrayList<Block> blocks = new ArrayList<Block>();

		if (loc1 == null || loc2 == null) {
			return blocks;
		}
		if (loc1.getWorld() == null || loc2.getWorld() == null) {
			return blocks;
		}

		int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
		int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());

		int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
		int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());

		int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
		int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());

		for (int x = bottomBlockX; x <= topBlockX; x++) {
			for (int z = bottomBlockZ; z <= topBlockZ; z++) {
				for (int y = bottomBlockY; y <= topBlockY; y++) {
					Block block = loc1.getWorld().getBlockAt(x, y, z);
					blocks.add(block);
				}
			}
		}

		return blocks;
	}

	public static Boolean isLocationZero(Location loc) {
		if (loc.getBlockX() == 0) {
			if (loc.getBlockY() == 0) {
				if (loc.getBlockZ() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public static String replaceColors(String message) {
		Pattern HEX_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");
		Matcher match = HEX_PATTERN.matcher(message);

		while (match.find()) {
			String color = message.substring(match.start(), match.end());
			message = message.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
		}

		return ChatColor.translateAlternateColorCodes('&', message);
	}
}