package com.nktfh100.AmongUs.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.nktfh100.AmongUs.main.Main;

public class AdminCommandTab implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> COMMANDS = new ArrayList<>();
		int argIndex = 0;
		if (args.length == 1) {
			if (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin")) {
				COMMANDS.add("listarenas");
				COMMANDS.add("createarena");
				COMMANDS.add("setmainlobby");
				COMMANDS.add("addlocation");
				COMMANDS.add("setsetting");
				COMMANDS.add("setup");
				COMMANDS.add("reload");
			}
			if (sender.hasPermission("amongus.admin.startgame") || sender.hasPermission("amongus.admin")) {
				COMMANDS.add("start");
			}
			if (sender.hasPermission("amongus.admin.startgame") || sender.hasPermission("amongus.admin")) {
				COMMANDS.add("endgame");
			}

			argIndex = 0;

		} else if (args.length == 2) {
			COMMANDS = Main.getArenaManager().getAllArenasNames();
			argIndex = 1;
		} else if (args.length == 3 && args[0].equalsIgnoreCase("setsetting") && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
			COMMANDS.addAll(AdminCommand.settings);
			argIndex = 2;
		} else if (args.length == 3 && args[0].equalsIgnoreCase("setup") && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
			COMMANDS.add("tasks");
			COMMANDS.add("sabotages");
			COMMANDS.add("spawns");
			COMMANDS.add("locations");
			COMMANDS.add("vents");
			COMMANDS.add("cameras");
			COMMANDS.add("doors");
			argIndex = 2;
		}

		final List<String> completions = new ArrayList<>();
		StringUtil.copyPartialMatches(args[argIndex], COMMANDS, completions);

		Collections.sort(completions);
		return completions;
	}
}