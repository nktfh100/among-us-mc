package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskStartReactorInv extends TaskInvHolder {

	private static ArrayList<Integer> slotsLeft = new ArrayList<Integer>(Arrays.asList(10, 11, 12, 19, 20, 21, 28, 29, 30));
	private static ArrayList<Integer> slotsRight = new ArrayList<Integer>(Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34));

	private static ArrayList<Integer> slotsProgressLeft = new ArrayList<Integer>(Arrays.asList(0, 9, 18, 27, 36));
	private static ArrayList<Integer> slotsProgressRight = new ArrayList<Integer>(Arrays.asList(8, 17, 26, 35, 44));

	private Boolean isDone = false;
	private BukkitTask playRunnable = null;
	private ArrayList<Integer> moves = new ArrayList<Integer>();
	private Integer activePlaySquare = -1; // 0 - 4
	private Integer activePlaySquareRight = -1;

	private Integer activeClickingSquare = 0; // 0 - 4
	private Boolean canClick = false;
	private Boolean showWrong = false;

	public TaskStartReactorInv(Arena arena, TaskPlayer taskPlayer, ArrayList<Integer> moves_) {
		super(45, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		
		this.inv.setItem(7, Main.getItemsManager().getItem("startReactor_info").getItem().getItem());
		
		this.moves = moves_;
		this.update();
		TaskStartReactorInv inv = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				inv.playSequence(taskPlayer.getPlayerInfo().getPlayer());
			}
		}.runTaskLater(Main.getPlugin(), 20L);
	}

	public static ArrayList<Integer> generateMoves() {
		ArrayList<Integer> out = new ArrayList<Integer>();
		for (int i = 0; i < 5; i++) {
			out.add(Utils.getRandomNumberInRange(0, 8));
		}
		return out;
	}

	public void playSequence(Player player) {
		final TaskStartReactorInv inv = this;

		this.canClick = false;
		this.setActivePlaySquare(-1);
		this.update();
		this.playRunnable = new BukkitRunnable() {
			Integer playSquare = 0;

			@Override
			public void run() {
				Main.getSoundsManager().playSound("taskStartReactorSquare" + (inv.getTaskPlayer().getMoves_().get(playSquare) + 1), player, player.getLocation());
				inv.setActivePlaySquare(playSquare);
				inv.update();

				new BukkitRunnable() {
					@Override
					public void run() {
						playSquare++;
						inv.setActivePlaySquare(-1);
						inv.setActiveClickingSquare(0);
						inv.update();
					}
				}.runTaskLater(Main.getPlugin(), 11L);

				if (playSquare + 1 > inv.getTaskPlayer().getReactorState_() || inv.getIsDone()) {
					inv.setCanClick(true);
					this.cancel();
				}
			}
		}.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	public void squareClick(Player player, Integer id) {
		final TaskStartReactorInv inv = this;
		if (!this.canClick || this.isDone) {
			return;
		}

//		Main.getSoundsManager().playSound("taskCleanO2LeafClick", player, player.getLocation());
		inv.setCanClick(false);
		if (moves.get(this.activeClickingSquare) == id) {
			Main.getSoundsManager().playSound("taskStartReactorSquare" + (id + 1), player, player.getLocation());
			this.activeClickingSquare = this.activeClickingSquare + 1;
			this.setActivePlaySquareRight(id);
			new BukkitRunnable() {
				@Override
				public void run() {
					inv.setActivePlaySquareRight(-1);
					if (inv.getActiveClickingSquare() > inv.getTaskPlayer().getReactorState_()) {
						inv.setCanClick(false);
						inv.getTaskPlayer().setReactorState_(inv.getTaskPlayer().getReactorState_() + 1);
						inv.setActiveClickingSquare(0);
						if (inv.getTaskPlayer().getReactorState_() >= 5) {
							inv.setIsDone(true);
							inv.checkDone();
						} else {
							new BukkitRunnable() {
								@Override
								public void run() {
									inv.playSequence(player);
								}
							}.runTaskLater(Main.getPlugin(), 12L);
						}
						inv.update();
					} else {
						inv.setCanClick(true);
					}
					inv.update();
				}
			}.runTaskLater(Main.getPlugin(), 10L);

		} else { // wrong
			Main.getSoundsManager().playSound("taskStartReactorClickWrong", player, player.getLocation());
			inv.setCanClick(false);
			this.showWrong = true;
			this.taskPlayer.updateTasksVars();
			this.moves = this.taskPlayer.getMoves_();
//			this.taskPlayer.setReactorState_(0);
			this.activeClickingSquare = 0;
			this.activePlaySquareRight = -1;
			new BukkitRunnable() {
				Integer state = 0;

				@Override
				public void run() {
					inv.setShowWrong(!inv.getShowWrong());
					if (state >= 3) {
						inv.setShowWrong(false);
						inv.playSequence(player);
						this.cancel();
					}
					inv.update();
					state++;
				}
			}.runTaskTimer(Main.getPlugin(), 10L, 10L);
		}

		this.checkDone();
		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskStartReactorInv inv = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = inv.getTaskPlayer().getPlayerInfo().getPlayer();
					if (player.getOpenInventory().getTopInventory() == inv.getInventory()) {
						player.closeInventory();
					}
				}
			}.runTaskLater(Main.getPlugin(), 20L);
			return true;
		}
		return false;
	}

	@Override
	public void update() {
		TaskStartReactorInv inv = this;
		ItemInfoContainer squareItem = Main.getItemsManager().getItem("startReactor_square");
		ItemStack squareItemS = squareItem.getItem().getItem();
		ItemStack squareItemS3 = squareItem.getItem3().getItem();

		for (int i = 0; i < slotsLeft.size(); i++) {
			Integer slot = slotsLeft.get(i);
			ItemStack item_ = squareItemS;
			if (this.activePlaySquare != -1) {
				if (i == moves.get(this.activePlaySquare)) {
					item_ = squareItemS3;
				}
			}
			this.inv.setItem(slot, item_);
		}

		ItemInfoContainer squareItemWrong = Main.getItemsManager().getItem("startReactor_squareWrong");
		ItemStack squareItemS2 = squareItem.getItem2().getItem();
		for (int i = 0; i < slotsRight.size(); i++) {
			ItemStack item_ = this.canClick ? squareItemS2 : squareItemS;
			if (this.activePlaySquareRight != -1) {
				if (i == this.activePlaySquareRight) {
					item_ = squareItemS3;
				}
			}
			if (this.showWrong) {
				item_ = squareItemWrong.getItem().getItem();
			}
			Icon icon = new Icon(item_);
			final Integer id = i;
			if (this.canClick) {
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.squareClick(player, id);
					}
				});
			}
			this.setIcon(slotsRight.get(i), icon);
		}

		ItemInfoContainer progressIndicatorItem = Main.getItemsManager().getItem("startReactor_progressIndicator");
		ItemStack progressIndicatorItemS = progressIndicatorItem.getItem().getItem();
		ItemStack progressIndicatorItem2S = progressIndicatorItem.getItem2().getItem();

		for (int i = 0; i < 5; i++) {
			if (i <= this.taskPlayer.getReactorState_()) {
				this.inv.setItem(slotsProgressLeft.get(i), progressIndicatorItem2S);
			} else {
				this.inv.setItem(slotsProgressLeft.get(i), progressIndicatorItemS);
			}
			// right
			if (i < this.getActiveClickingSquare()) {
				this.inv.setItem(slotsProgressRight.get(i), progressIndicatorItem2S);
			} else {
				this.inv.setItem(slotsProgressRight.get(i), progressIndicatorItemS);
			}
		}
	}

	@Override
	public void invClosed() {
		if (this.playRunnable != null) {
			this.playRunnable.cancel();
			this.playRunnable = null;
		}
	}

	public void setIsDone(Boolean is) {
		this.isDone = is;
	}

	public Boolean getIsDone() {
		return this.isDone;
	}

	public Integer getActivePlaySquare() {
		return activePlaySquare;
	}

	public void setActivePlaySquare(Integer activePlaySquare) {
		this.activePlaySquare = activePlaySquare;
	}

	public Integer getActiveClickingSquare() {
		return activeClickingSquare;
	}

	public void setActiveClickingSquare(Integer activeClickingSquare) {
		this.activeClickingSquare = activeClickingSquare;
	}

	public Boolean getCanClick() {
		return canClick;
	}

	public void setCanClick(Boolean canClick) {
		this.canClick = canClick;
	}

	public Integer getActivePlaySquareRight() {
		return activePlaySquareRight;
	}

	public void setActivePlaySquareRight(Integer activePlaySquareRight) {
		this.activePlaySquareRight = activePlaySquareRight;
	}

	public Boolean getShowWrong() {
		return showWrong;
	}

	public void setShowWrong(Boolean showWrong) {
		this.showWrong = showWrong;
	}
}