package com.nktfh100.AmongUs.holograms;

import com.nktfh100.AmongUs.main.Main;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class DecentHologramClickListeners implements Listener {
    private static final HashMap<String, HologramClickListener> listeners = new HashMap<>();

    public void addListener(String hologramName, HologramClickListener listener) {
        listeners.put(hologramName, listener);
    }

    @EventHandler
    public void onHologramClick(HologramClickEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                listeners.get(event.getHologram().getName()).onClick(event);
            }
        }.runTask(Main.getPlugin());
    }
}
