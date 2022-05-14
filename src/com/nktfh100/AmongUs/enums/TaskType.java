package com.nktfh100.AmongUs.enums;

public enum TaskType {
	WIRING(TaskLength.COMMON), SCAN(TaskLength.LONG), DOWNLOAD_DATA(TaskLength.LONG), UPLOAD_DATA(TaskLength.LONG), DIVERT_POWER(TaskLength.SHORT), ACCEPT_DIVERTED_POWER(TaskLength.SHORT), UNLOCK_MANIFOLDS(TaskLength.SHORT),
	EMPTY_GARBAGE(TaskLength.SHORT), PRIME_SHIELDS(TaskLength.SHORT), CALIBRATE_DISTRIBUTOR(TaskLength.SHORT), START_REACTOR(TaskLength.LONG), CLEAR_ASTEROIDS(TaskLength.SHORT), REFUEL(TaskLength.LONG), FUEL(TaskLength.LONG),
	CLEAN_O2(TaskLength.SHORT), INSPECT_SAMPLE(TaskLength.LONG), SWIPE_CARD(TaskLength.COMMON), CHART_COURSE(TaskLength.SHORT), STABILIZE_STEERING(TaskLength.SHORT), FILL_CANISTERS(TaskLength.SHORT), INSERT_KEYS(TaskLength.COMMON),
	REPLACE_WATER_JUG(TaskLength.LONG), RECORD_TEMPERATURE(TaskLength.SHORT), REPAIR_DRILL(TaskLength.SHORT), MONITOR_TREE(TaskLength.SHORT), OPEN_WATERWAYS(TaskLength.LONG), REBOOT_WIFI(TaskLength.LONG), FIX_WEATHER_NODE(TaskLength.LONG),
	SWITCH_WEATHER_NODE(TaskLength.LONG), SCAN_BOARDING_PASS(TaskLength.COMMON), STORE_ARTIFACTS(TaskLength.SHORT);

	private TaskLength taskLength;

	private TaskType(TaskLength taskLength) {
		this.taskLength = taskLength;
	}

	public TaskLength getTaskLength() {
		return taskLength;
	}
}
