package com.nktfh100.AmongUs.enums;

public enum CosmeticType {

	KILL_SWORD("kill_sword");
	
	private String name;

	private CosmeticType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
