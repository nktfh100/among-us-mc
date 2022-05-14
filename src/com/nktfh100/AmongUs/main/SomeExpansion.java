package com.nktfh100.AmongUs.main;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nktfh100.AmongUs.enums.StatInt;
import com.nktfh100.AmongUs.info.PlayerInfo;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class SomeExpansion extends PlaceholderExpansion {

	private Plugin plugin;

	/**
	 * Since we register the expansion inside our own plugin, we can simply use this
	 * method here to get an instance of our plugin.
	 *
	 * @param plugin The instance of our plugin.
	 */
	public SomeExpansion(Plugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Because this is an internal class, you must override this method to let
	 * PlaceholderAPI know to not unregister your expansion class when
	 * PlaceholderAPI is reloaded
	 *
	 * @return true to persist through reloads
	 */
	@Override
	public boolean persist() {
		return true;
	}

	/**
	 * Because this is a internal class, this check is not needed and we can simply
	 * return {@code true}
	 *
	 * @return Always true since it's an internal class.
	 */
	@Override
	public boolean canRegister() {
		return true;
	}

	/**
	 * The name of the person who created this expansion should go here. <br>
	 * For convienience do we return the author from the plugin.yml
	 * 
	 * @return The name of the author as a String.
	 */
	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	/**
	 * The placeholder identifier should go here. <br>
	 * This is what tells PlaceholderAPI to call our onRequest method to obtain a
	 * value if a placeholder starts with our identifier. <br>
	 * The identifier has to be lowercase and can't contain _ or %
	 *
	 * @return The identifier in {@code %<identifier>_<value>%} as String.
	 */
	@Override
	public String getIdentifier() {
		return "amongus";
	}

	/**
	 * This is the version of the expansion. <br>
	 * You don't have to use numbers, since it is set as a String.
	 *
	 * For convienience do we return the version from the plugin.yml
	 *
	 * @return The version as a String.
	 */
	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	private Integer getStatMySql(Player player, StatInt key) {
		Integer out = 0;
		try {
			Connection connection = Main.getConfigManager().getMysql_connection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM stats WHERE UUID = ?");
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				out = rs.getInt(key.getName());
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}

	private Integer getStatFlatFile(Player player, StatInt key) {
		File statsFile = new File(Main.getPlugin().getDataFolder() + File.separator + "stats", player.getUniqueId().toString() + ".yml");
		if (statsFile.exists()) {
			YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
			return statsConfig.getInt(key.getName(), 0);
		} else {
			return 0;
		}
	}

	/**
	 * This is the method called when a placeholder with our identifier is found and
	 * needs a value. <br>
	 * We specify the value identifier in this method. <br>
	 * Since version 2.9.1 can you use OfflinePlayers in your requests.
	 *
	 * @param player     A {@link org.bukkit.Player Player}.
	 * @param identifier A String containing the identifier/value.
	 *
	 * @return possibly-null String of the requested identifier.
	 */
	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (player == null) {
			return null;
		}
		identifier = identifier.toLowerCase();

		Boolean isValid = false;
		StatInt statInt_ = null;
		for (StatInt statIntE : StatInt.values()) {
			if (identifier.equals(statIntE.getName())) {
				isValid = true;
				statInt_ = statIntE;
				break;
			}
		}
		if (identifier.contains("time_played_")) {
			Double out = 0D;
			if (Main.getConfigManager().getMysql_enabled()) {
				out = (double) this.getStatMySql(player, StatInt.TIME_PLAYED);
			} else {
				out = (double) this.getStatFlatFile(player, StatInt.TIME_PLAYED);
			}
			if (identifier.contains("minutes")) {
				out = out / 60D;
			} else if (identifier.contains("hours")) {
				out = (out / 60D) / 60D;
			} else if (identifier.contains("days")) {
				out = ((out / 60D) / 60D / 24D);
			}
			BigDecimal a = new BigDecimal(out);
			BigDecimal roundOff = a.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			out = roundOff.doubleValue();
			return out + "";
		} else if (identifier.equalsIgnoreCase("color")) {
			PlayerInfo pInfo_ = Main.getPlayersManager().getPlayerInfo(player);
			String out = ChatColor.WHITE + "";
			if(pInfo_ != null && pInfo_.getIsIngame()) {
				out = pInfo_.getColor().getChatColor() + "";
			}
			return out;
		}
		if (isValid) {
			if (Main.getConfigManager().getMysql_enabled()) {
				return this.getStatMySql(player, statInt_) + "";
			} else {
				return this.getStatFlatFile(player, statInt_) + "";
			}
		}
		return 0 + "";
	}
}