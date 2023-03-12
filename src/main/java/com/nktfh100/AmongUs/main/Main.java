package com.nktfh100.AmongUs.main;

import java.util.logging.Level;

import com.nktfh100.AmongUs.listeners.*;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.nktfh100.AmongUs.commands.AdminCommand;
import com.nktfh100.AmongUs.commands.AdminCommandTab;
import com.nktfh100.AmongUs.commands.PlayersCommand;
import com.nktfh100.AmongUs.commands.PlayersCommandTab;
import com.nktfh100.AmongUs.events.VentureChatEvent_;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.Camera;
import com.nktfh100.AmongUs.managers.ArenaManager;
import com.nktfh100.AmongUs.managers.BungeArenaManager;
import com.nktfh100.AmongUs.managers.ConfigManager;
import com.nktfh100.AmongUs.managers.CosmeticsManager;
import com.nktfh100.AmongUs.managers.ItemsManager;
import com.nktfh100.AmongUs.managers.MessagesManager;
import com.nktfh100.AmongUs.managers.PlayersManager;
import com.nktfh100.AmongUs.managers.SoundsManager;
import com.nktfh100.AmongUs.utils.Metrics;

public class Main extends JavaPlugin {
	private static Plugin plugin;

	private static ConfigManager configManager;
	private static PlayersManager playersManager;
	private static ArenaManager arenaManager;
	private static BungeArenaManager bungeArenaManager;
	private static MessagesManager messagesManager;
	private static ItemsManager itemsManager;
	private static SoundsManager soundsManager;
	private static CosmeticsManager cosmeticsManager;

	private static Boolean isVentureChat = false;
	private static Boolean isPlaceHolderAPI = false;
	private static Boolean isPlayerPoints = false;

	private static Boolean isDecentHologram = false;
	private static Boolean isHolographicDisplays = false;

	private static PlayerPointsAPI playerPointsApi = null;

	public void onEnable() {
		plugin = this;

		if (getServer().getPluginManager().getPlugin("HolographicDisplays") != null) {
			isHolographicDisplays = true;
		} else if (getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
			isDecentHologram = true;
		} else {
			getLogger().log(Level.SEVERE, "You must have a holograms plugin installed. Please install the latest version of DecentHolograms or HolographicDisplays");
			getServer().getPluginManager().disablePlugin(this);
		}

		new Metrics(this, 12109);
		if (getServer().getPluginManager().getPlugin("VentureChat") != null) {
			isVentureChat = true;
			getServer().getPluginManager().registerEvents(new VentureChatEvent_(), this);
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new SomeExpansion(this).register();
			isPlaceHolderAPI = true;
		}

		if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
			isPlayerPoints = true;
			playerPointsApi = PlayerPoints.getInstance().getAPI();
		} else {
			Bukkit.getLogger().log(Level.WARNING, "The plugin 'PlayerPoints' is not preset, cosmetics will not work!");
		}
		cosmeticsManager = new CosmeticsManager();

		configManager = new ConfigManager(plugin.getConfig());

		configManager.loadConfig();
		if (!plugin.isEnabled()) {
			return;
		}
		if (configManager.getBungeecord()) {
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			if (configManager.getBungeecordIsLobby()) {
				bungeArenaManager = new BungeArenaManager(configManager.getGameServers());
				getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", bungeArenaManager);
			}

		}
		if (!plugin.isEnabled()) {
			return;
		}
		messagesManager = new MessagesManager();
		itemsManager = new ItemsManager();
		soundsManager = new SoundsManager();
		messagesManager.loadAll();
		itemsManager.loadItems();
		soundsManager.loadSounds();
		cosmeticsManager.loadCosmetics();
		arenaManager = new ArenaManager();
		arenaManager.loadArenas();
		playersManager = new PlayersManager();

		if (configManager.getBungeecord() && configManager.getBungeecordIsLobby()) {
			bungeArenaManager.createInventory();
			bungeArenaManager.updateArenaSelectorInv();
			if (arenaManager.getAllArenas().size() > 0) {
				arenaManager.sendBungeUpdate(arenaManager.getAllArenas().iterator().next());
			}
		}
		this.getCommand("aua").setExecutor(new AdminCommand());
		this.getCommand("aua").setTabCompleter(new AdminCommandTab());

		this.getCommand("au").setExecutor(new PlayersCommand());
		this.getCommand("au").setTabCompleter(new PlayersCommandTab());
		getServer().getPluginManager().registerEvents(playersManager, this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerDamage(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.BlockBreak(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.BlockPlace(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.HungerChange(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerDrop(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerRightClick(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerChat(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.InvClick(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerPickUp(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerSwapHand(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.SignChange(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.InvClose(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerMove(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.EntityInteract(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerSneak(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerCommand(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.ArmorStandManipulate(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.ServerPingEvent(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerEnterPortal(), this);

		Bukkit.getLogger().info("[AmongUs] Plugin made by nktfh100");
		Bukkit.getLogger().info("[AmongUs] Made with love in israel!");

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

		// hide items in players hands
		protocolManager.addPacketListener(new EquipmentListener(this, ListenerPriority.NORMAL));
		protocolManager.addPacketListener(new UseEntityListener(this, ListenerPriority.NORMAL));
		// disable hiting sound
		protocolManager.addPacketListener(new NamedSoundEffectListener(this, ListenerPriority.NORMAL));
		protocolManager.addPacketListener(new NamedEntitySpawnListener(this, ListenerPriority.NORMAL));
		protocolManager.addPacketListener(new EntityListeners(this, ListenerPriority.HIGHEST));
	}

	public static String getHologramsPlugin() {
		if (isHolographicDisplays) {
			return "HolographicDisplays";
		} else {
			return "DecentHolograms";
		}
	}

	public static void sendPlayerToLobby(Player player) {
		if (!plugin.isEnabled()) {
			return;
		}
		if (configManager.getBungeecord()) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(configManager.getBungeecordLobbyServer());
			player.sendPluginMessage(Main.getPlugin(), "BungeeCord", out.toByteArray());
		}
	}

	public static void sendPlayerToArena(Player player, String server) {
		if (!plugin.isEnabled()) {
			return;
		}
		if (configManager.getBungeecord()) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(server);
			player.sendPluginMessage(Main.getPlugin(), "BungeeCord", out.toByteArray());
		}
	}

	public static void reloadConfigs() {
		for (Arena arena : arenaManager.getAllArenas()) {
			arena.deleteHolograms();
			arena.endGame(false);
			for (Camera cam : arena.getCamerasManager().getCameras()) {
				cam.deleteArmorStands();
			}
		}
		configManager.loadConfigVars();
		messagesManager.loadAll();
		itemsManager.loadItems();
		soundsManager.loadSounds();
		arenaManager.loadArenas();
		cosmeticsManager.loadCosmetics();
	}

	private static Boolean runAsync = true;

	public void onDisable() {
		if (arenaManager != null) {
			for (Arena arena : arenaManager.getAllArenas()) {
				arena.deleteHolograms();
				arena.endGame(true);
				arena.delete();
			}
		}
		if (configManager != null) {
			configManager.delete();
		}
		if (messagesManager != null) {
			messagesManager.delete();
		}
		if (itemsManager != null) {
			itemsManager.delete();
		}
		if (soundsManager != null) {
			soundsManager.delete();
		}
		if (playersManager != null) {
			playersManager.delete();
		}
		if(cosmeticsManager != null) {
			cosmeticsManager.delete();
		}
	}

	public static Plugin getPlugin() {
		return plugin;
	}

	public static Boolean shouldRunAsync() {
		return runAsync;
	}

	public static MessagesManager getMessagesManager() {
		return messagesManager;
	}

	public static ItemsManager getItemsManager() {
		return itemsManager;
	}

	public static PlayersManager getPlayersManager() {
		return playersManager;
	}

	public static ArenaManager getArenaManager() {
		return arenaManager;
	}

	public static ConfigManager getConfigManager() {
		return configManager;
	}

	public static SoundsManager getSoundsManager() {
		return soundsManager;
	}

	public static Boolean getIsVentureChat() {
		return isVentureChat;
	}

	public static Boolean getIsPlaceHolderAPI() {
		return isPlaceHolderAPI;
	}

	public static BungeArenaManager getBungeArenaManager() {
		return bungeArenaManager;
	}

	public static Boolean getIsPlayerPoints() {
		return isPlayerPoints;
	}

	public static PlayerPointsAPI getPlayerPointsApi() {
		return playerPointsApi;
	}

	public static CosmeticsManager getCosmeticsManager() {
		return cosmeticsManager;
	}
}
