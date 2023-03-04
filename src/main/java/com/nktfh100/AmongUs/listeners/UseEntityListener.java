package com.nktfh100.AmongUs.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.DeadBody;
import com.nktfh100.AmongUs.info.FakeArmorStand;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class UseEntityListener extends PacketAdapter {
    public UseEntityListener(Plugin plugin, ListenerPriority priority) {
        super(plugin, priority, PacketType.Play.Client.USE_ENTITY);
    }

    public synchronized void onPacketReceiving(PacketEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player attacker = event.getPlayer();
                PlayerInfo attackerInfo = Main.getPlayersManager().getPlayerInfo(event.getPlayer());
                if (attackerInfo == null || attackerInfo.getArena() == null || attackerInfo.getArena().getPlayersInfo() == null) {
                    return;
                }

                Entity victimEnt = event.getPacket().getEntityModifier(event.getPlayer().getWorld()).read(0);

                Player victim = null;
                PlayerInfo victimInfo = null;
                if (victimEnt == null) { // fake entity - players in cameras / scan armor stands
                    int entityId = event.getPacket().getIntegers().read(0);
                    for (PlayerInfo pInfo1 : attackerInfo.getArena().getPlayersInfo()) {
                        outer: if (pInfo1 != attackerInfo) {
                            if (pInfo1.getIsInCameras() && pInfo1.getFakePlayerId().equals(entityId)) {
                                victim = pInfo1.getPlayer();
                                victimInfo = pInfo1;
                            } else if (pInfo1.getIsScanning()) {
                                for (FakeArmorStand fas : pInfo1.getScanArmorStands()) {
                                    if (fas.getEntityId() == entityId) {
                                        victim = pInfo1.getPlayer();
                                        victimInfo = pInfo1;
                                        break outer;
                                    }
                                }
                            }
                        }
                    }
                    if (victim == null) {
                        return;
                    }
                } else if (!(victimEnt instanceof Player)) {
                    if (attackerInfo.getIsIngame() && event.getPacket().getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.ATTACK) {
                        event.setCancelled(true);
                    }
                    return;
                }

                if (victim == null) {
                    victim = (Player) victimEnt;
                    victimInfo = Main.getPlayersManager().getPlayerInfo(victim);
                }

                if (event.getPacket().getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.ATTACK) {
                    if ((!attackerInfo.getIsIngame() && victimInfo.getIsIngame()) || (attackerInfo.getIsIngame() && !victimInfo.getIsIngame())) {
                        event.setCancelled(true);
                        return;
                    }
                    if (!attackerInfo.getIsIngame() || !victimInfo.getIsIngame()) {
                        return;
                    }
                    event.setCancelled(true);


                    if (attackerInfo.getArena().getGameState() != GameState.RUNNING || !attackerInfo.getIsImposter() || attackerInfo.isGhost() || victimInfo.isGhost() || victimInfo.getIsImposter()) {
                        return;
                    }

                    if (attackerInfo.getArena().getIsInMeeting()) {
                        return;
                    }

                    if (attackerInfo.getKillCoolDown() > 0) {
                        return;
                    }
                    if (attacker.getInventory().getItemInMainHand() == null || attacker.getInventory().getItemInMainHand().getItemMeta() == null) {
                        event.setCancelled(true);
                        return;
                    }
                    // check if item in attackers hand is the kill item
                    String itemName = attacker.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
                    ItemInfoContainer killItem = Main.getItemsManager().getItem("kill");
                    //if (attacker.getItemInHand().getType().equals(killItem.getItem2().getMat())) {
                    if (killItem.getItem2().getTitle(attackerInfo.getKillCoolDown().toString()).equals(itemName)) {

                        if (victimInfo.getIsInCameras()) {
                            victimInfo.getArena().getCamerasManager().playerLeaveCameras(victimInfo, false);
                        }

                        attackerInfo.setKillCoolDown(attackerInfo.getArena().getKillCooldown());
                        Location vicLoc = victim.getLocation();
                        attacker.teleport(new Location(victim.getWorld(), vicLoc.getX(), vicLoc.getY(), vicLoc.getZ(), attacker.getLocation().getYaw(), attacker.getLocation().getPitch()));
                        victim.getWorld().spawnParticle(Particle.BLOCK_CRACK, victim.getLocation().getX(), victim.getLocation().getY() + 1.3, victim.getLocation().getZ(), 30, 0.4D, 0.4D,
                                0.4D, Bukkit.createBlockData(Material.REDSTONE_BLOCK));

                        Main.getSoundsManager().playSound("playerDeathAttacker", attacker, victim.getLocation());
                        Main.getSoundsManager().playSound("playerDeathVictim", victim, victim.getLocation());

                        attackerInfo.getArena().playerDeath(attackerInfo, victimInfo, true);

                        for (PlayerInfo pInfo : attackerInfo.getArena().getPlayersInfo()) {
                            if (!pInfo.isGhost()) {
                                DeadBody db = attackerInfo.getArena().getDeadBodiesManager().isCloseToBody(pInfo.getPlayer().getLocation());
                                if (db != null) {
                                    pInfo.setCanReportBody(true, db);
                                }
                            }
                        }

                    }
                }
            }
        }.runTask(Main.getPlugin());
    }
}
