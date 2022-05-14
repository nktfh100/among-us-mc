package com.nktfh100.AmongUs.info;

import org.bukkit.Location;

public class LocationName {

	private String id;
	private String name;
	private Location location;
	
	public LocationName(String id, String name, Location location) {
		this.id = id;
		this.name = name;
		this.location = location;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
