package com.nktfh100.AmongUs.inventory;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.enums.CosmeticType;
import com.nktfh100.AmongUs.info.CosmeticItem;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.CosmeticsManager;
import com.nktfh100.AmongUs.utils.Utils;

public class CosmeticSelectorInv extends CustomHolder {

	private static final Integer pageSize = 28;

	private PlayerInfo pInfo;
	private Integer page = 1;

	public CosmeticSelectorInv(PlayerInfo pInfo) {
		super(54, Main.getMessagesManager().getGameMsg("cosmeticsSelectorInvTitle", null, null));
		this.pInfo = pInfo;
		Utils.addBorder(this.inv, 54, Main.getItemsManager().getItem("cosmeticsSelector_border").getItem().getMat());
		this.update();
	}

	public void update() {
		CosmeticSelectorInv inv = this;
		this.clearInv();
		Utils.addBorder(this.inv, 54, Main.getItemsManager().getItem("cosmeticsSelector_border").getItem().getMat());
		CosmeticsManager cosmeticsManager = Main.getCosmeticsManager();

		ArrayList<CosmeticItem> items_ = cosmeticsManager.getOrderedCosmetics(CosmeticType.KILL_SWORD);
		Integer totalItems = items_.size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		if (totalPages > 1) {
			final Integer currentPage_ = this.page;
			if (this.page > 1) {
				ItemInfo item_ = Main.getItemsManager().getItem("cosmeticsSelector_prevPage").getItem();
				Icon icon = new Icon(item_.getItem(this.page + "", totalPages + ""));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.setPage(currentPage_ - 1);
					}
				});
				this.setIcon(item_.getSlot(), icon);
			}
			if (this.page < totalPages) {
				ItemInfo item_ = Main.getItemsManager().getItem("cosmeticsSelector_nextPage").getItem();
				Icon icon = new Icon(item_.getItem(this.page + "", totalPages + ""));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.setPage(currentPage_ + 1);
					}
				});
				this.setIcon(item_.getSlot(), icon);
			}
		}

		Integer startIndex = (this.page - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		Integer slot = 10;
		for (int i = startIndex; i <= endIndex; i++) {
			CosmeticItem cosmeticItem = items_.get(i);
			Boolean isUnlocked = false;
			if (cosmeticItem.getPrice() == 0 && (cosmeticItem.getPermission().isEmpty() || this.pInfo.getPlayer().hasPermission(cosmeticItem.getPermission()))) {
				isUnlocked = true;
			} else if (cosmeticItem.getPrice() == 0 && pInfo.getPlayer().hasPermission(cosmeticItem.getPermission())) {
				isUnlocked = true;
			} else if (this.pInfo.getStatsManager().getUnlockedCosmetics().contains(cosmeticItem.getKey())) {
				isUnlocked = true;
			}
			Boolean isSelected = false;
			if (this.pInfo.getStatsManager().getSelectedCosmetic(CosmeticType.KILL_SWORD) != null
					&& this.pInfo.getStatsManager().getSelectedCosmetic(CosmeticType.KILL_SWORD).equals(cosmeticItem.getKey())) {
				isSelected = true;
			}
			if (this.pInfo.getStatsManager().getSelectedCosmetic(CosmeticType.KILL_SWORD) == null
					&& cosmeticItem.getKey().equals(Main.getCosmeticsManager().getDefaultCosmetic(CosmeticType.KILL_SWORD))) {
				isSelected = true;
			}

			ItemStack item = Utils.createItem(cosmeticItem.getMat(), cosmeticItem.getDisplayName());
			ArrayList<String> lore = cosmeticItem.getLore3();
			if (isUnlocked) {
				lore = cosmeticItem.getLore2();
			}
			if (isSelected) {
				lore = cosmeticItem.getLore();
				Utils.enchantedItem(item, Enchantment.DURABILITY, 1);
			}
			Utils.setItemLore(item, lore);

			Icon icon = new Icon(item);

			if (!isSelected) {
				final Boolean isUnlocked_ = isUnlocked;
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						if (isUnlocked_) {
							pInfo.getStatsManager().selectCosmetic(CosmeticType.KILL_SWORD, cosmeticItem.getKey());
							inv.update();
							HashMap<String, String> placeholders = new HashMap<>();
							placeholders.put("%cosmetic_name%", cosmeticItem.getName());
							placeholders.put("%player_coins%", String.valueOf(Main.getPlayerPointsApi().look(player.getUniqueId())));
							player.sendMessage(Main.getMessagesManager().getGameMsg("selectedCosmetic", null, placeholders));
						} else {
							HashMap<String, String> placeholders = new HashMap<>();
							placeholders.put("%cosmetic_name%", cosmeticItem.getName());
							placeholders.put("%player_coins%", String.valueOf(Main.getPlayerPointsApi().look(player.getUniqueId()) - cosmeticItem.getPrice()));
							placeholders.put("%cosmetic_price%", String.valueOf(cosmeticItem.getPrice()));
							if (cosmeticItem.getPermission().isEmpty()) {
								if (Main.getPlayerPointsApi().look(player.getUniqueId()) >= cosmeticItem.getPrice()) {
									pInfo.getStatsManager().unlockCosmetic(CosmeticType.KILL_SWORD, cosmeticItem.getKey());
									pInfo.getStatsManager().selectCosmetic(CosmeticType.KILL_SWORD, cosmeticItem.getKey());
									Main.getPlayerPointsApi().take(player.getUniqueId(), cosmeticItem.getPrice());
									inv.update();
									player.sendMessage(Main.getMessagesManager().getGameMsg("playerBoughtCosmetic", null, placeholders));
								} else {
									player.sendMessage(Main.getMessagesManager().getGameMsg("notEnoughCoins", null, placeholders));
								}
							} else if (player.hasPermission(cosmeticItem.getPermission())) {
								if (cosmeticItem.getPrice() > 0) {
									if (Main.getPlayerPointsApi().look(player.getUniqueId()) >= cosmeticItem.getPrice()) {
										pInfo.getStatsManager().unlockCosmetic(CosmeticType.KILL_SWORD, cosmeticItem.getKey());
										pInfo.getStatsManager().selectCosmetic(CosmeticType.KILL_SWORD, cosmeticItem.getKey());
										Main.getPlayerPointsApi().take(player.getUniqueId(), cosmeticItem.getPrice());
										inv.update();
										player.sendMessage(Main.getMessagesManager().getGameMsg("playerBoughtCosmetic", null, placeholders));
									} else {
										player.sendMessage(Main.getMessagesManager().getGameMsg("notEnoughCoins", null, placeholders));
									}
								} else {
									pInfo.getStatsManager().selectCosmetic(CosmeticType.KILL_SWORD, cosmeticItem.getKey());
									inv.update();
									HashMap<String, String> placeholders1 = new HashMap<>();
									placeholders1.put("%cosmetic_name%", cosmeticItem.getName());
									placeholders1.put("%player_coins%", String.valueOf(Main.getPlayerPointsApi().look(player.getUniqueId())));
									player.sendMessage(
											Main.getMessagesManager().getGameMsg("selectedCosmetic", null, placeholders1));
								}
							}
						}
					}
				});
			}
			this.setIcon(slot, icon);
			slot++;
			if(slot == 17 || slot == 26 || slot == 35) {
				slot+=2;
			}
//			if (slot >= 43) {
//				slot = 10;
//			}
		}
		ItemInfo coinsItem = Main.getItemsManager().getItem("cosmeticsSelector_coins").getItem();
		Icon icon = new Icon(coinsItem.getItem(Main.getPlayerPointsApi().look(this.pInfo.getPlayer().getUniqueId()) + "", null));
		this.setIcon(coinsItem.getSlot(), icon);
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer newPage) {
		this.page = newPage;
		this.update();
	}

}