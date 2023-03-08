package com.nktfh100.AmongUs.holograms;

import com.nktfh100.AmongUs.main.Main;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HolographicDisplays implements ImposterHologram {
    private final me.filoghost.holographicdisplays.api.hologram.Hologram hologram;

    public HolographicDisplays(Location location) {
        this.hologram = HolographicDisplaysAPI.get(Main.getPlugin()).createHologram(location);
    }

    @Override
    public void setLocation(Location location) {
        this.hologram.setPosition(location);
    }

    @Override
    public Location getLocation() {
        return this.hologram.getPosition().toLocation();
    }

    @Override
    public void deleteHologram() {
        this.hologram.delete();
    }

    @Override
    public void showTo(Player player) {
        this.hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
    }

    @Override
    public void hideTo(Player player) {
        this.hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN);
    }

    @Override
    public void clearVisibility(boolean visibleByDefault) {
        this.hologram.getVisibilitySettings().clearIndividualVisibilities();

        if (visibleByDefault) {
            this.hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
        } else {
            this.hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        }
    }

    @Override
    public void setGlobalVisibility(boolean visibleByDefault) {
        if (visibleByDefault) {
            this.hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
        } else {
            this.hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        }
    }

    @Override
    public boolean isVisibleTo(Player player) {
        return this.hologram.getVisibilitySettings().isVisibleTo(player);
    }

    @Override
    public boolean isDeleted() {
        return this.hologram.isDeleted();
    }

    @Override
    public void addLineWithText(String text) {
        this.hologram.getLines().appendText(text);
    }

    @Override
    public void addLineWithItem(ItemStack item) {
        this.hologram.getLines().appendItem(item);
    }
}
