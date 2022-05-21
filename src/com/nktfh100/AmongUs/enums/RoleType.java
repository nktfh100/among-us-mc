package com.nktfh100.AmongUs.enums;

public enum RoleType {
	CREWMATE(""), IMPOSTER(""), SHAPESHIFTER("Shapeshifter"), ENGINEER("Engineer"), GUARDIAN_ANGEL("GuardianAngel"), SCIENTIST("Scientist");

	private final String name; // For messages.yml

	private RoleType(String name_) {
		this.name = name_;
	}

	public String getName() {
		return name;
	}
}
