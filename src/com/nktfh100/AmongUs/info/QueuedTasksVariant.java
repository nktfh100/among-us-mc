package com.nktfh100.AmongUs.info;

import java.util.ArrayList;

public class QueuedTasksVariant {

	private Arena arena;
	private ArrayList<String> queuedTasks = new ArrayList<String>();
	private Integer id;
	private String configId;

	public QueuedTasksVariant(Arena arena, ArrayList<String> queuedTasks, String configId, Integer id) {
		this.queuedTasks = queuedTasks;
		this.configId = configId;
		this.id = id;
		this.arena = arena;
	}

	public ArrayList<Task> getQueuedTasksTasks() {
		ArrayList<Task> out = new ArrayList<Task>();
		for (String id : this.queuedTasks) {
			if(this.arena.getTask(id) != null) {
				out.add(this.arena.getTask(id));				
			}
		}
		return out;
	}
	
	public void delete() {
		this.queuedTasks = null;
		this.configId = null;
		this.id = null;
		this.arena = null;
	}

	public ArrayList<String> getQueuedTasks() {
		return this.queuedTasks;
	}
	
	public void setQueuedTasks(ArrayList<String> newTasks) {
		this.queuedTasks = newTasks;
	}

	public String getConfigId() {
		return this.configId;
	}

	public Integer getId() {
		return this.id;
	}

}
