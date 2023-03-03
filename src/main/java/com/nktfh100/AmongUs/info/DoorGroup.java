package com.nktfh100.AmongUs.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DoorGroup implements Comparable<DoorGroup> {

	private Arena arena;
	private LocationName locName;
	private String configId;
	private Integer id;
	private ArrayList<Door> doors = new ArrayList<Door>();
	private Integer closeTimer = 0;

	// uuid - timer: per player
	private HashMap<String, Integer> coolDownTimer = new HashMap<String, Integer>();

	public DoorGroup(Arena arena, LocationName locName, String configId, Integer id) {
		this.arena = arena;
		this.configId = configId;
		this.id = id;
		this.locName = locName;
	}

	public void closeDoors(Boolean sound) {
		for (Door d : this.doors) {
			d.closeDoor(sound);
		}
	}

	public void openDoors(Boolean sound) {
		for (Door d : this.doors) {
			d.openDoor(sound);
		}
	}

	public void addDoor(Door d) {
		this.doors.add(d);
		Collections.sort(this.doors);
	}

	public Door getDoor(Integer id) {
		return this.doors.get(id);
	}

	public Integer getCooldownTimer(String uuid) {
		return this.coolDownTimer.get(uuid);
	}

	public void setCooldownTimer(String uuid, Integer closeTimer) {
		this.coolDownTimer.put(uuid, closeTimer);
	}

	public HashMap<String, Integer> getCooldownTimer() {
		return this.coolDownTimer;
	}

	public Arena getArena() {
		return arena;
	}

	public ArrayList<Door> getDoors() {
		return this.doors;
	}

	public Integer getId() {
		return id;
	}

	public String getConfigId() {
		return configId;
	}

	@Override
	public int compareTo(DoorGroup vg) {
		return this.id.compareTo(vg.getId());
	}

	public LocationName getLocName() {
		return locName;
	}

	public Integer getCloseTimer() {
		return closeTimer;
	}

	public void setCloseTimer(Integer closeTimer) {
		this.closeTimer = closeTimer;
	}
	public void delete() {
		this.arena = null;
		this.locName = null;
		this.configId = null;
		this.id = null;
		this.doors = null;
		this.closeTimer = null;
	}
}
