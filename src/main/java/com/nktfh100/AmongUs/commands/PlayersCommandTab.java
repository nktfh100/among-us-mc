package com.nktfh100.AmongUs.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.nktfh100.AmongUs.info.ColorInfo;
import com.nktfh100.AmongUs.info.PlayerInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.nktfh100.AmongUs.main.Main;

public class PlayersCommandTab implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) return null;

		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo((Player) sender);
		List<String> COMMANDS = new ArrayList<>();
		int arg = 0;
		if (args.length == 1) {
			COMMANDS.add("join");
			COMMANDS.add("leave");
			COMMANDS.add("arenas");
			COMMANDS.add("joinRandom");
			COMMANDS.add("cosmetics");
			COMMANDS.add("selectColor");
			arg = 0;

		} else if (args.length == 2) {
			if (Objects.equals(args[0], "join") || Objects.equals(args[0], "leave")) {
				if (Main.getConfigManager().getBungeecord()) {
					if(Main.getBungeArenaManager() != null && Main.getBungeArenaManager().getAllArenasServerNames() != null) {
						COMMANDS = Main.getBungeArenaManager().getAllArenasServerNames();
					}
				} else {
					if(Main.getArenaManager() != null && Main.getArenaManager().getAllArenasNames() != null) {
						COMMANDS = Main.getArenaManager().getAllArenasNames();
					}
				}
			} else if (Objects.equals(args[0], "selectColor") && pInfo.getIsIngame()) {
				for (ColorInfo color : pInfo.getArena().getColors_()) {
					COMMANDS.add(color.getKey());
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