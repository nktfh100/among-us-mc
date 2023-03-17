package com.nktfh100.AmongUs.holograms;

import com.nktfh100.AmongUs.main.Main;
import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class DecentHolograms implements ImposterHologram, Listener {
    private final eu.decentsoftware.holograms.api.holograms.Hologram hologram;

    public DecentHolograms(Location location, String name) {
        this.hologram = DHAPI.createHologram(name, location);
    }

    @Override
    public void setLocation(Location location) {
        this.hologram.setLocation(location);
    }

    @Override
    public Location getLocation() {
        return this.hologram.getLocation();
    }

    @Override
    public void deleteHologram() {
        this.hologram.delete();
    }

    @Override
    public void showTo(Player player) {
        this.hologram.setShowPlayer(player);
        this.hologram.removeHidePlayer(player);
    }

    @Override
    public void hideTo(Player player) {
        this.hologram.removeShowPlayer(player);
        this.hologram.setHidePlayer(player);
    }

    @Override
    public void clearVisibility(boolean visibleByDefault) {
        this.hologram.getHidePlayers().clear();
        this.hologram.getShowPlayers().clear();
        this.hologram.setDefaultVisibleState(visibleByDefault);
    }

    @Override
    public void setGlobalVisibility(boolean visibleByDefault) {
        this.hologram.setDefaultVisibleState(visibleByDefault);
    }

    @Override
    public boolean isVisibleTo(Player player) {
        return this.hologram.isVisible(player);
    }

    @Override
    public boolean isDeleted() {
        return !this.hologram.isEnabled();
    }

    @Override
    public void addLineWithText(String text) {
        DHAPI.addHologramLine(this.hologram, text);
    }

    @Override
    public void addLineWithItem(ItemStack item) {
        DHAPI.addHologramLine(this.hologram, item);
    }

    @Override
    public void setHologramClickListener(HologramClickListener listener) {
        Main.getHologramListener().addListener(this.hologram.getName(), listener);
    }
}
