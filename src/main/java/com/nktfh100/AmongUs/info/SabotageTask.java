package com.nktfh100.AmongUs.info;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.holograms.HologramClickListener;
import com.nktfh100.AmongUs.holograms.ImposterHologram;

import com.nktfh100.AmongUs.main.Main;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLineClickEvent;
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
	private ImposterHologram holo;
	private HologramClickListener clickListener;

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
		this.clickListener = new HologramClickListener() {
			@Override
			public void onClick(HologramLineClickEvent event) {
				Player p = event.getPlayer();
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

			@Override
			public void onClick(HologramClickEvent event) {
				Player p = event.getPlayer();
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

	public void setHolo(ImposterHologram holo) {
		this.holo = holo;
	}

	public SabotageType getSabotageType() {
		return this.sabotageType;
	}

	public Location getLocation() {
		return this.location;
	}

	public ImposterHologram getHolo() {
		return this.holo;
	}

	public Arena getArena() {
		return arena;
	}

	public HologramClickListener getTouchHandler() {
		return clickListener;
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
