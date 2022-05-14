package com.nktfh100.AmongUs.info;

import com.nktfh100.AmongUs.enums.GameState;

public class BungeArena {

	private String server;
	private String name;
	private GameState gameState;
	private Integer maxPlayers;
	private Integer currentPlayers;
	
	public BungeArena(String server, String name, GameState gameState, Integer maxPlayers, Integer currentPlayers) {
		this.server = server;
		this.setName(name);
		this.gameState = gameState;
		this.maxPlayers = maxPlayers;
		this.currentPlayers = currentPlayers;
	}

	
	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public String getServer() {
		return server;
	}

	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(Integer maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public Integer getCurrentPlayers() {
		return currentPlayers;
	}

	public void setCurrentPlayers(Integer currentPlayers) {
		this.currentPlayers = currentPlayers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
