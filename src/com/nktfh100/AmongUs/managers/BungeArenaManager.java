package com.nktfh100.AmongUs.managers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.BungeArena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.inventory.BungeArenaSelectorInv;
import com.nktfh100.AmongUs.utils.Utils;

public class BungeArenaManager implements PluginMessageListener {

	private HashMap<String, BungeArena> arenas = new HashMap<String, BungeArena>();

	private BungeArenaSelectorInv arenaSelectorInv;

	public BungeArenaManager(ArrayList<String> gameServers) {
		for (String server : gameServers) {
			arenas.put(server, new BungeArena(server, server, GameState.WAITING, 0, 1));
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
			String subchannel = in.readUTF();
			if (subchannel.equals("AmongUs")) {
				short len = in.readShort();
				byte[] data = new byte[len];
				in.readFully(data);

				String s = new String(data);

				String[] dataStr = s.split(",");
				if (dataStr.length == 5 && this.arenas.get(dataStr[0]) != null) {
					BungeArena ba = this.arenas.get(dataStr[0]);
					ba.setName(dataStr[1]);
					ba.setGameState(GameState.valueOf(dataStr[2]));
					ba.setCurrentPlayers(Integer.valueOf(dataStr[3]));
					ba.setMaxPlayers(Integer.valueOf(dataStr[4]));
					this.updateArenaSelectorInv();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public BungeArena getArenaWithMostPlayers() {
		if (this.arenas.size() == 0) {
			return null;
		}
		if (this.arenas.size() == 1) {
			return this.arenas.values().iterator().next();
		}
		ArrayList<BungeArena> arenas_ = new ArrayList<BungeArena>(this.getAllArenas());
		BungeArena arena = null;

		for (BungeArena arena_ : arenas_) {
			if (arena_.getGameState() == GameState.RUNNING || arena_.getGameState() == GameState.FINISHING) {
				continue;
			}
			if (arena_.getCurrentPlayers() == arena_.getMaxPlayers()) {
				continue;
			}
			if (arena == null) {
				arena = arena_;
				continue;
			}
			if (arena_.getCurrentPlayers() > arena.getCurrentPlayers()) {
				arena = arena_;
			}
		}

		return arena;
	}

	public BungeArena getRandomArena() {
		if (this.arenas.size() == 0) {
			return null;
		}
		if (this.arenas.size() == 1) {
			return this.arenas.values().iterator().next();
		}
		ArrayList<BungeArena> arenas_ = new ArrayList<BungeArena>();
		for (BungeArena ba : this.getAllArenas()) {
			if (ba.getGameState() == GameState.RUNNING || ba.getGameState() == GameState.FINISHING) {
				continue;
			}
			if (ba.getCurrentPlayers() == ba.getMaxPlayers()) {
				continue;
			}
			arenas_.add(ba);
		}
		if(arenas_.size() == 0) {
			return null;
		}
		if(arenas_.size() == 1) {
			return arenas_.get(0);
		}
		return arenas_.get(Utils.getRandomNumberInRange(0, arenas_.size() - 1));
	}

	public void createInventory() {
		this.arenaSelectorInv = new BungeArenaSelectorInv();
	}

	public void openArenaSelector(PlayerInfo pInfo) {
		if (this.arenaSelectorInv != null) {
			pInfo.getPlayer().openInventory(this.arenaSelectorInv.getInventory());
		}
	}

	public void updateArenaSelectorInv() {
		if (this.arenaSelectorInv != null) {
			this.arenaSelectorInv.update();
		}
	}

	public BungeArena getArenaByServer(String server) {
		if (this.arenas.size() > 0) {
			return (this.arenas.get(server));
		} else {
			return null;
		}
	}

	public Collection<BungeArena> getAllArenas() {
		return this.arenas.values();
	}

	public HashMap<String, BungeArena> getArenas_() {
		return this.arenas;
	}

	public ArrayList<String> getAllArenasServerNames() {
		return new ArrayList<String>(this.arenas.keySet());
	}

}
