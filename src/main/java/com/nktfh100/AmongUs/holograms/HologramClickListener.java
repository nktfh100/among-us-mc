package com.nktfh100.AmongUs.holograms;

import eu.decentsoftware.holograms.event.HologramClickEvent;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLineClickEvent;

public interface HologramClickListener {
    void onClick(HologramLineClickEvent event);
    void onClick(HologramClickEvent event);
}
