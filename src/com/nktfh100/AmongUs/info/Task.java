package com.nktfh100.AmongUs.info;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.TaskType;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Task implements Comparable<Task> {

	private String id;
	private ArrayList<QueuedTasksVariant> queuedTasksVariants = new ArrayList<QueuedTasksVariant>();
	private Boolean isEnabled;
	private LocationName locationName;
	private Location location;
	private TaskType taskType;
	private Arena arena;
	private Hologram holo;
	private TouchHandler touchHandler;

	// For all visual tasks
	private Boolean enableVisuals = true;

	// For asteroids
	private Location cannon1;
	private Location cannon2;
	private Long asteroidsLastTime = System.currentTimeMillis();
	private Integer activeCannon = 0;
	
	// for record temperature
	private Boolean isHot = false;
	
	public Task(String id) {
		this.id = id;
	}

	public void setInfo(TaskType taskType, Location location, LocationName locName, Arena arena, Boolean isEnabled, Boolean enableVisuals) {
		this.locationName = locName;
		this.location = location;
		this.taskType = taskType;
		this.arena = arena;
		this.isEnabled = isEnabled;
		this.enableVisuals = enableVisuals;
		Task task = this;
		this.touchHandler = new TouchHandler() {
			@Override
			public void onTouch(Player p) {
				if (task.getArena().getGameState() == GameState.RUNNING) {
					task.getArena().getTasksManager().taskHoloClick(p, task);
				} else {
					if (p.hasPermission("amongus.admin")) {
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(p);
						if (!pInfo.getIsIngame()) {
							p.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Task holo click " + task.getTaskType().toString());
						}
					}
				}
			}
		};
	}

	public void setAsteroidsInfo(Location cannon1, Location cannon2) {
		this.cannon1 = cannon1;
		this.cannon2 = cannon2;
	}

	public String getName() {
		String out = Main.getMessagesManager().getTaskName(this.getTaskType().toString());
		if (out == null) {
			return this.getTaskType().toString();
		}
		return out;
	}

	public void addQueuedTasksVariant(QueuedTasksVariant variant) {
		this.queuedTasksVariants.add(variant);
	}

	public QueuedTasksVariant getQueuedTasksVarient(Integer id) {
		return this.queuedTasksVariants.get(id);
	}

	public ArrayList<Task> getRandomQueuedTasks() {
		if (this.queuedTasksVariants.size() > 0) {
			ArrayList<Task> out = new ArrayList<Task>();
			for (String id : this.queuedTasksVariants.get(Utils.getRandomNumberInRange(0, this.queuedTasksVariants.size() - 1)).getQueuedTasks()) {
				out.add(this.arena.getTask(id));
			}
			return out;
		}
		return new ArrayList<Task>();
	}

	public QueuedTasksVariant getRandomTaskVariant() {
		if (this.queuedTasksVariants.size() > 0) {
			return this.queuedTasksVariants.get(Utils.getRandomNumberInRange(0, this.queuedTasksVariants.size() - 1));
		}
		return null;
	}

	public void delete() {
		for (QueuedTasksVariant qtv : this.queuedTasksVariants) {
			qtv.delete();
		}
		this.id = null;
		this.queuedTasksVariants = null;
		this.isEnabled = null;
		this.locationName = null;
		this.location = null;
		this.taskType = null;
		this.arena = null;
		this.holo = null;
		this.touchHandler = null;
		this.enableVisuals = true;
		this.cannon1 = null;
		this.cannon2 = null;
		this.activeCannon = null;
	}

	public ArrayList<QueuedTasksVariant> getQueuedTasksVariants() {
		return this.queuedTasksVariants;
	}

	public void setHolo(Hologram holo) {
		this.holo = holo;
	}

	public String getId() {
		return this.id;
	}

	public LocationName getLocationName() {
		return this.locationName;
	}

	public void setLocationName(LocationName locationName) {
		this.locationName = locationName;
	}

	public TaskType getTaskType() {
		return this.taskType;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location loc) {
		this.location = loc;
	}

	public Hologram getHolo() {
		return this.holo;
	}

	public Arena getArena() {
		return arena;
	}

	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public void setIsEnabled(Boolean is) {
		this.isEnabled = is;
	}

	public TouchHandler getTouchHandler() {
		return touchHandler;
	}

	public Boolean getEnableVisuals() {
		return enableVisuals;
	}

	public void setEnableVisuals(Boolean is) {
		this.enableVisuals = is;
	}

	@Override
	public int compareTo(Task t) {
		return this.id.compareTo(t.getId());
	}

	public Location getCannon1() {
		return cannon1;
	}

	public Location getCannon2() {
		return cannon2;
	}

	public void setCannon1(Location loc) {
		this.cannon1 = loc;
	}

	public void setCannon2(Location loc) {
		this.cannon2 = loc;
	}

	public Long getAsteroidsLastTime() {
		return this.asteroidsLastTime;
	}

	public void setAsteroidsLastTime(Long asteroidsLastTime) {
		this.asteroidsLastTime = asteroidsLastTime;
	}

	public Integer getActiveCannon() {
		return activeCannon;
	}

	public void setActiveCannon(Integer activeCannon) {
		this.activeCannon = activeCannon;
	}

	public Boolean getIsHot() {
		return isHot;
	}

	public void setIsHot(Boolean isHot) {
		this.isHot = isHot;
	}
}
