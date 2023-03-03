package com.nktfh100.AmongUs.info;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.nktfh100.AmongUs.utils.Utils;

public class Camera implements Comparable<Camera> {

	public final static String camera = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmFlM2EzYTRhMWFhNTBkODVkYmNkYWM4ZGE2M2Q3Y2JmZDQ1ZTUyMGRmZWMyZDUwYmVkZjhlOTBlOGIwZTRlYSJ9fX0=";
	public final static String lampOn = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGUyYzE4YWIzNTk0OWJmOWY5ZTdkNmE2OWI4ODVjY2Q4Y2MyZWZiOTQ3NTk0NmQ3ZDNmYjVjM2ZlZjYxIn19fQ==";
	public final static String lampOff = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRmMTRlNTAxN2IyNzliMDNkYWM5N2Q0MjliNGE1ZmE2YzM5OGFkNTY4ZWE0M2U3YzQwNjgzYzczOThjMTYyNyJ9fX0=";

	private Arena arena;
	private Integer id;
	private String configKey;
	private Location viewLoc;
	private Location camLoc;
	private Location lampLoc;
	private LocationName locName;
	private ArmorStand armorStand = null;
	private ArmorStand lampArmorStand = null;
	private Boolean isActive = false;

	private ArrayList<FakeBlock> fakeBlocks = new ArrayList<FakeBlock>();
	private ArrayList<FakeBlock> fakeAirBlocks = new ArrayList<FakeBlock>();

	public Camera(Arena arena, Integer id, Location viewLoc, Location camLoc, Location lampLoc, LocationName locName, String configKey) {
		this.arena = arena;
		this.id = id;
		this.camLoc = camLoc;
		this.lampLoc = lampLoc;
		this.locName = locName;
		this.configKey = configKey;

		Vector dir = viewLoc.getDirection().normalize();
		this.viewLoc = viewLoc.add(dir.multiply(0.56D));
	}

	public void createArmorStand() {
		this.armorStand = Utils.createArmorStand(this.camLoc);
		this.armorStand.getEquipment().setHelmet(Utils.createSkull(camera, "", 1, ""));
		this.armorStand.setHeadPose(new EulerAngle(Math.toRadians(this.camLoc.getPitch()), 0.0D, 0.0D));
		this.armorStand.setCustomName("camera_armor_stand" + this.id);

		this.lampArmorStand = Utils.createArmorStand(this.lampLoc);
		this.lampArmorStand.getEquipment().setHelmet(Utils.createSkull(lampOff, "", 1, ""));
		this.lampArmorStand.setHeadPose(new EulerAngle(Math.toRadians(this.lampLoc.getPitch()), 0.0D, 0.0D));
		this.lampArmorStand.setCustomName("camera_armor_stand1" + this.id);
	}

	public void deleteArmorStands() {
		if (this.armorStand != null) {
			this.armorStand.remove();
		}
		if (this.lampArmorStand != null) {
			this.lampArmorStand.remove();
		}
	}

	public void updateLamp() {
		Boolean isActive_ = this.arena.getCamerasManager().getPlayersInCameras().size() > 0;
		this.lampArmorStand.getEquipment().setHelmet(Utils.createSkull(isActive_ ? lampOn : lampOff, "", 1, ""));
	}

	public void addFakeBlock(Location loc, Material oldMat, Material newMat, WrappedBlockData oldBlockData) {
		this.fakeBlocks.add(new FakeBlock(loc, oldMat, newMat, oldBlockData));
	}

	public void showFakeBlocks(Player player) {
		for (FakeBlock fb : this.fakeBlocks) {
			fb.sendNewBlock(player);
		}
	}

	public void hideFakeBlocks(Player player) {
		for (FakeBlock fb : this.fakeBlocks) {
			fb.sendOldBlock(player);
		}
	}

	public void addFakeAirBlock(Location loc) {
		this.fakeAirBlocks.add(new FakeBlock(loc, loc.getBlock().getType(), Material.BARRIER, WrappedBlockData.createData(loc.getBlock().getBlockData())));
	}

	public void showFakeAirBlocks(Player player) {
		for (FakeBlock fb : this.fakeAirBlocks) {
			fb.sendNewBlock(player);
		}
	}

	public void hideFakeAirBlocks(Player player) {
		for (FakeBlock fb : this.fakeAirBlocks) {
			fb.sendOldBlock(player);
		}
	}

	public void updateViewLoc(Location viewLoc) {
		Vector dir = viewLoc.getDirection().normalize();
		this.viewLoc = viewLoc.add(dir.multiply(0.56D));
	}

	public void updateCamLoc(Location camLoc) {
		this.camLoc = camLoc;
		this.deleteArmorStands();
		this.createArmorStand();
	}

	public void updateLampLoc(Location lampLoc) {
		this.lampLoc = lampLoc;
		this.deleteArmorStands();
		this.createArmorStand();
	}
	
	public void delete() {
		this.deleteArmorStands();
		this.arena = null;
		this.id = null;
		this.configKey = null;
		this.viewLoc = null;
		this.camLoc = null;
		this.lampLoc = null;
		this.locName = null;
		this.armorStand = null;
		this.lampArmorStand = null;
		this.isActive = false;
		this.fakeBlocks = null;
		this.fakeAirBlocks = null;
	}

	public Arena getArena() {
		return arena;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean is) {
		this.isActive = is;
	}

	public LocationName getLocName() {
		return locName;
	}

	public void setLocName(LocationName locName) {
		this.locName = locName;
	}

	public Integer getId() {
		return id;
	}

	public String getConfigKey() {
		return configKey;
	}

	public Location getViewLoc() {
		return this.viewLoc;
	}

	public Location getCamLoc() {
		return camLoc;
	}

	public Location getLampLoc() {
		return lampLoc;
	}

	public ArrayList<FakeBlock> getFakeBlocks() {
		return this.fakeBlocks;
	}

	public void deleteFakeBlocks() {
		this.fakeBlocks = new ArrayList<FakeBlock>();
	}

	public ArrayList<FakeBlock> getFakeAirBlocks() {
		return fakeAirBlocks;
	}

	public void deleteFakeAirBlocks() {
		this.fakeAirBlocks = new ArrayList<FakeBlock>();
	}

	@Override
	public int compareTo(Camera c) {
		return this.id.compareTo(c.getId());
	}

}
