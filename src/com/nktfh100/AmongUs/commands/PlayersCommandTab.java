package com.nktfh100.AmongUs.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.nktfh100.AmongUs.main.Main;

public class PlayersCommandTab implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> COMMANDS = new ArrayList<>();
		int arg = 0;
		if (args.length == 1) {
			COMMANDS.add("join");
			COMMANDS.add("leave");
			COMMANDS.add("arenas");
			COMMANDS.add("joinrandom");
			arg = 0;

		} else if (args.length == 2) {
			if (Main.getConfigManager().getBungeecord()) {
				if(Main.getBungeArenaManager() != null && Main.getBungeArenaManager().getAllArenasServerNames() != null) {
					COMMANDS = Main.getBungeArenaManager().getAllArenasServerNames();					
				}
			} else {
				if(Main.getArenaManager() != null && Main.getArenaManager().getAllArenasNames() != null) {
					COMMANDS = Main.getArenaManager().getAllArenasNames();					
				}
			}
			arg = 1;
		}

		final List<String> completions = new ArrayList<>();
		StringUtil.copyPartialMatches(args[arg], COMMANDS, completions);

		Collections.sort(completions);
		return completions;
	}
}