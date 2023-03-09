package com.nktfh100.AmongUs.holograms;

import com.nktfh100.AmongUs.main.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ImposterHologram {
    static ImposterHologram createHologram(Location location, String name) {
        if (Main.getHologramsPlugin().equals("DecentHolograms")) {
            return new DecentHolograms(location, name);
        } else {
            return new HolographicDisplays(location);
        }
    }

    void setLocation(Location location);

    Location getLocation();

    void deleteHologram();

    void showTo(Player player);

    void hideTo(Player player);

    void clearVisibility(boolean visibleByDefault);

    void setGlobalVisibility(boolean visibleByDefault);

    boolean isVisibleTo(Player player);

    boolean isDeleted();

    void addLineWithText(String text);

    void addLineWithItem(ItemStack item);

    void setHologramClickListener(HologramClickListener listener);
}