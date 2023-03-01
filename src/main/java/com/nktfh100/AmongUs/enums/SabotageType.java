package com.nktfh100.AmongUs.enums;

public enum SabotageType {
	LIGHTS(SabotageLength.SINGLE), COMMUNICATIONS(SabotageLength.SINGLE), REACTOR_MELTDOWN(SabotageLength.DOUBLE_SAME_TIME), OXYGEN(SabotageLength.DOUBLE);

	private SabotageLength sabotageLength;

	private SabotageType(SabotageLength sabotageLength) {
		this.sabotageLength = sabotageLength;
	}

	public SabotageLength getSabotageLength() {
		return sabotageLength;
	}
}
