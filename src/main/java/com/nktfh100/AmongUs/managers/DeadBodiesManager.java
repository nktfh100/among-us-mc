package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;

import org.bukkit.Location;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.DeadBody;

public class DeadBodiesManager {

	private Arena arena;
	private ArrayList<DeadBody> bodies = new ArrayList<DeadBody>();
	public DeadBodiesManager(Arena arena) {
		this.arena = arena;
	}
	
	public DeadBody isCloseToBody(Location loc) { // maybe check if has line of sight or somethign
		for(DeadBody db : this.bodies) {
			if(loc.getWorld() == this.arena.getWorld() && loc.distance(db.getLocation()) <= this.arena.getReportDistance()) {
				return db;
			}
		}
		return null;
	}
	
	public void deleteAll() {
		for(DeadBody bd : this.bodies) {
			bd.delete();
		}
		this.bodies.clear();
	}
	
	public void addBody(DeadBody body) {
		this.bodies.add(body);
	}
	
	public ArrayList<DeadBody> getBodies() {
		return this.bodies;
	}

	public Arena getArena() {
		return arena;
	}
	
}
