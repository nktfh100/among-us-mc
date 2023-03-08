package com.nktfh100.AmongUs.info;

import com.nktfh100.AmongUs.holograms.ImposterHologram;
import org.bukkit.Location;

public class Vent implements Comparable<Vent> {

	private Arena arena;
	private VentGroup ventGroup;
	private Location loc;
	private Location playerLoc;
	private LocationName locName;
	private Integer id;
	private String configId;
	private ImposterHologram holo;

	public Vent(Arena arena, VentGroup ventGroup, Location loc, LocationName locName, Integer id, String configId) {
		this.arena = arena;
		this.ventGroup = ventGroup;
		this.loc = loc;
		this.locName = locName;
		this.id = id;
		this.configId = configId;
		this.playerLoc = new Location(loc.getWorld(), loc.getX(), loc.getY() - 1.85, loc.getZ(), loc.getYaw(), loc.getPitch());
	}

	public void delete() {
		this.arena = null;
		this.ventGroup = null;
		this.loc = null;
		this.playerLoc = null;
		this.locName = null;
		this.id = null;
		this.configId = null;
		this.holo = null;
	}

	public Arena getArena() {
		return arena;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public Integer getId() {
		return id;
	}

	public ImposterHologram getHolo() {
		return holo;
	}

	public void setHolo(ImposterHologram holo) {
		this.holo = holo;
	}

	public String getConfigId() {
		return configId;
	}

	public LocationName getLocName() {
		return locName;
	}

	public void setLocName(LocationName locName) {
		this.locName = locName;
	}

	@Override
	public int compareTo(Vent v) {
		return this.id.compareTo(v.getId());
	}

	public Location getPlayerLoc() {
		return playerLoc;
	}

	public VentGroup getVentGroup() {
		return ventGroup;
	}
}
