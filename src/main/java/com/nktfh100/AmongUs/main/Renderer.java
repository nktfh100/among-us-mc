package com.nktfh100.AmongUs.main;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursor.Type;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import com.nktfh100.AmongUs.enums.SabotageLength;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.info.SabotageTask;
import com.nktfh100.AmongUs.info.Task;
import com.nktfh100.AmongUs.info.TaskPlayer;

public class Renderer extends MapRenderer {

	private static Byte getCardinalDirection(Player player) {
		double rotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
		if (rotation < 0.0D) {
			rotation += 360.0D;
		}
		if ((0.0D <= rotation) && (rotation < 22.5D)) {
			return 4; // west
		}
		if ((22.5D <= rotation) && (rotation < 67.5D)) {
			return 6;
		}
		if ((67.5D <= rotation) && (rotation < 112.5D)) {
			return 8; // north
		}
		if ((112.5D <= rotation) && (rotation < 157.5D)) {
			return 10;
		}
		if ((157.5D <= rotation) && (rotation < 202.5D)) {
			return 12; // east
		}
		if ((202.5D <= rotation) && (rotation < 247.5D)) {
			return 14;
		}
		if ((247.5D <= rotation) && (rotation < 292.5D)) {
			return 0; // south
		}
		if ((292.5D <= rotation) && (rotation < 337.5D)) {
			return 2;
		}
		if ((337.5D <= rotation) && (rotation < 360.0D)) {
			return 4; // west
		}
		return null;
	}

	private Boolean didSet = false;

	@Override
	public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);

		if (pInfo == null || pInfo.getArena() == null) {
			return;
		}

		Integer centerX;
		Integer centerZ;
		if (pInfo.getArena().getMoveMapWithPlayer()) {
			Location loc_ = player.getLocation();
			centerX = loc_.getBlockX();
			centerZ = loc_.getBlockZ();
			mapView.setCenterX(centerX);
			mapView.setCenterZ(centerZ);
		} else {
			centerX = pInfo.getArena().getMapCenter().getBlockX();
			centerZ = pInfo.getArena().getMapCenter().getBlockZ();
			if (!didSet) {
				mapView.setCenterX(centerX);
				mapView.setCenterZ(centerZ);
				didSet = true;
			}
		}
		MapCursorCollection cursors = new MapCursorCollection();
		Boolean isCommsDisabled = false;
		if (pInfo.getArena().getSabotageManager().getIsSabotageActive()) {
			if (pInfo.getArena().getSabotageManager().getActiveSabotage().getType() == SabotageType.COMMUNICATIONS) {
				isCommsDisabled = true;
			}
		}
		if (pInfo.getArena().getTasksManager().getTasksForPlayer(player) != null && !isCommsDisabled) {
			for (TaskPlayer tasksAssignment : pInfo.getArena().getTasksManager().getTasksForPlayer(player)) {
				if (tasksAssignment.getIsDone()) {
					continue;
				}

				Task task = tasksAssignment.getActiveTask();
				int taskX = task.getLocation().getBlockX();
				int taskZ = task.getLocation().getBlockZ();

				int mapX = 0;
				int mapY = 0;

				int difX = Math.abs(taskX - centerX);
				int difZ = Math.abs(taskZ - centerZ);
				String label = "";
				if (mapView.getScale() == Scale.CLOSEST) {
					difX = difX * 2;
					difZ = difZ * 2;
					if (difX > 126) { // 256 / 2
						difX = 126;
						difX = difX - 5;
						// label = null;
					}
					if (difZ > 126) {
						difZ = 126;
						difZ = difZ - 5;
						// label = null;
					}

				}
				if (taskX > centerX) {
					mapX = mapX + difX;
				} else {
					mapX = mapX - difX;
				}

				if (taskZ > centerZ) {
					mapY = mapY + difZ;
				} else {
					mapY = mapY - difZ;
				}

				cursors.addCursor(new MapCursor((byte) mapX, (byte) mapY, (byte) 8, Type.BANNER_YELLOW, true, label));
			}
		}

		// add active sabotage
		if (pInfo.getArena().getSabotageManager().getIsSabotageActive()) {
			SabotageArena activeSabotageAr = pInfo.getArena().getSabotageManager().getActiveSabotage();
			ArrayList<SabotageTask> saboTasks = new ArrayList<SabotageTask>(Arrays.asList(activeSabotageAr.getTask1()));
			if (activeSabotageAr.getLength() != SabotageLength.SINGLE) {
				saboTasks.add(activeSabotageAr.getTask2());
			}
			for (SabotageTask saboTask : saboTasks) {

				int locX = saboTask.getLocation().getBlockX();
				int locZ = saboTask.getLocation().getBlockZ();

				int mapX = 0;
				int mapY = 0;

				int difX = Math.abs(locX - centerX);
				int difZ = Math.abs(locZ - centerZ);
				if (mapView.getScale() == Scale.CLOSEST) {
					difX = difX * 2;
					difZ = difZ * 2;
					// if its out of view
					if (difX > 126) { // 256 / 2
						continue;
					}
					if (difZ > 126) {
						continue;
					}

				}
				if (locX > centerX) {
					mapX = mapX + difX;
				} else {
					mapX = mapX - difX;
				}

				if (locZ > centerZ) {
					mapY = mapY + difZ;
				} else {
					mapY = mapY - difZ;
				}

				cursors.addCursor(new MapCursor((byte) mapX, (byte) mapY, (byte) 8, Type.BANNER_RED, true, ""));
			}
		}

		int playerX;
		int playerZ;
		if (pInfo.getArena().getMoveMapWithPlayer()) {
			playerX = centerX;
			playerZ = centerZ;
		} else {
			Location loc_ = player.getLocation();
			playerX = loc_.getBlockX();
			playerZ = loc_.getBlockZ();
		}

		int mapX = 0;
		int mapY = 0;

		int difX = Math.abs(playerX - centerX);
		int difZ = Math.abs(playerZ - centerZ);
		if (mapView.getScale() == Scale.CLOSEST) {
			difX = difX * 2;
			difZ = difZ * 2;
			if (difX > 126) { // 256 / 2
				difX = 126;
				difX = difX - 5;
			}
			if (difZ > 126) {
				difZ = 126;
				difZ = difZ - 5;
			}

		}
		if (playerX > centerX) {
			mapX = mapX + difX;
		} else {
			mapX = mapX - difX;
		}

		if (playerZ > centerZ) {
			mapY = mapY + difZ;
		} else {
			mapY = mapY - difZ;
		}

		cursors.addCursor(new MapCursor((byte) (mapX), (byte) (mapY), (byte) getCardinalDirection(player), Type.BLUE_POINTER, true));

//			for (PlayerInfo pInfo1 : pInfo.getArena().getPlayersInfo()) {
//				Type type_ = Type.GREEN_POINTER;
//				if (pInfo1.getTeam() != pInfo.getTeam()) {
//					type_ = Type.RED_POINTER;
//					if (!pInfo1.isSpotted()) {
//						continue;
//					}
//				}else {
//					if(pInfo1.IsDowned()) {
//						type_ = Type.WHITE_CROSS;
//					}
//				}
//				if (pInfo1.isSpectating()) {
//					continue;
//				}
//
//				Player player1 = pInfo1.getPlayer();
//				if (player1.getName().equals(player.getName())) {
//					continue;
//				}
//				int player1X = player1.getLocation().getBlockX();
//				int player1Z = player1.getLocation().getBlockZ();
//
//				int mapX = 0;
//				int mapY = 0;
//
//				int difX = Math.abs(player1X - playerX);
//				int difZ = Math.abs(player1Z - playerZ);
//				if (mapView.getScale() == Scale.CLOSEST) {
//					difX = difX * 2;
//					difZ = difZ * 2;
//					if (difX > 126) {
//						difX = 126;
//						difX = difX - 4;
//						continue;
//
//					}
//					if (difZ > 126) {
//						difZ = 126;
//						difZ = difZ - 4;
//						continue;
//					}
//
//				}
//				if (player1X > playerX) {
//					mapX = mapX + difX;
//				} else {
//					mapX = mapX - difX;
//				}
//
//				if (player1Z > playerZ) {
//					mapY = mapY + difZ;
//				} else {
//					mapY = mapY - difZ;
//				}
//				cursors.addCursor(new MapCursor((byte) mapX, (byte) mapY, (byte) getCardinalDirection(player1), type_, true));
//			}

		mapCanvas.setCursors(cursors);
	}

}
