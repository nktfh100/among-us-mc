package com.nktfh100.AmongUs.events;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class SignChange implements Listener {

	@EventHandler
	public void onSignChange(SignChangeEvent ev) {
		if (ev.getPlayer().hasPermission("amongus.admin") || ev.getPlayer().hasPermission("amongus.admin.setup")) {
			if (ev.getLine(0) != null && ev.getLine(0).equals("[au]")) {
				if (ev.getLine(1) != null && !ev.getLine(1).equals("")) {
					Arena arena = Main.getArenaManager().getArenaByName(ev.getLine(1));
					if (arena != null) {
						List<String> signs_ = arena.getArenaConfig().getStringList("signs");
						Block block = ev.getBlock();
						signs_.add(block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ());
						final String arenaName = arena.getName();
						arena.getArenaConfig().set("signs", signs_);
						arena.saveConfig();
						arena.addSign(block.getLocation());
						ev.getPlayer().sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully added sign at " + Utils.locationToStringB(block.getLocation()));
						new BukkitRunnable() {
							@Override
							public void run() {
								Main.getArenaManager().getArenaByName(arenaName).updateSigns();
							}
						}.runTaskLater(Main.getPlugin(), 20L);
					}
				}
			}
		}
	}

}
