package com.nktfh100.AmongUs.info;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.main.Main;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SabotageTask {

	private Integer id;
	private Location location;
	private SabotageType sabotageType;
	private Integer timer;
	private Boolean hasTimer;
	private Arena arena;
	private Hologram holo;
	private TouchHandler touchHandler;

	public SabotageTask(SabotageType sabotageType, Integer id, Integer timer) {
		this.id = id;
		this.sabotageType = sabotageType;
		this.timer = timer;
		this.hasTimer = timer > 0;
	}

	public void setInfo(Location location, Arena arena) {
		this.location = location;
		this.arena = arena;
		SabotageTask sabotage = this;
		this.touchHandler = new TouchHandler() {
			@Override
			public void onTouch(Player p) {
				if (sabotage.getArena().getGameState() == GameState.RUNNING) {
					sabotage.getArena().getSabotageManager().sabotageHoloClick(p, sabotage.getId());
				} else {
					if (p.hasPermission("amongus.admin")) {
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(p);
						if (!pInfo.getIsIngame()) {
							p.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Sabotage holo click " + sabotage.sabotageType.toString() + " " + sabotage.getId());
						}
					}
				}
			}
		};
	}

	public void setHolo(Hologram holo) {
		this.holo = holo;
	}

	public SabotageType getSabotageType() {
		return this.sabotageType;
	}

	public Location getLocation() {
		return this.location;
	}

	public Hologram getHolo() {
		return this.holo;
	}

	public Arena getArena() {
		return arena;
	}

	public TouchHandler getTouchHandler() {
		return touchHandler;
	}

	public Integer getTimer() {
		return timer;
	}

	public Boolean getHasTimer() {
		return hasTimer;
	}

	public Integer getId() {
		return id;
	}
}
