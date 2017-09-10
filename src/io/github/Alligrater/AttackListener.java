package io.github.Alligrater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AttackListener implements Listener{
	static HashMap<String, BossBar> Enemies = new HashMap<String, BossBar>();
	HashMap<String, UUID> Engaging = new HashMap<String, UUID>();
	HashMap<String, BukkitRunnable> Active = new HashMap<String, BukkitRunnable>();
	
	@EventHandler(priority=EventPriority.LOW)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof LivingEntity && (event.getEntityType() != EntityType.ENDER_DRAGON || event.getEntityType() != EntityType.WITHER)) {
			
			if(event.getDamager() instanceof Player || event.getDamager() instanceof Projectile) {
				Player player;
				if(event.getDamager() instanceof Player) {
					player = (Player) event.getDamager();
				}
				else  {
					if (!(((Projectile) event.getDamager()).getShooter() instanceof Player)) {
	                    return;
	                } else {
	                    player = (Player) ((Projectile) event.getDamager()).getShooter();
	                }
				}
				
				LivingEntity target = (LivingEntity)event.getEntity();
				boolean instakill = false;
				if(!Enemies.containsKey(player.getName())) {
					if(target.getHealth() - event.getDamage() > 0) {
						String name = "";
						if(target.getCustomName() != null) {
							name = target.getCustomName();
						}
						else {
							name = target.getName();
						}
						BossBar b = Bukkit.createBossBar(name, BarColor.RED, BarStyle.SEGMENTED_10, new BarFlag[0]);
						b.setProgress((target.getHealth()-event.getDamage())/target.getMaxHealth());
						b.setVisible(true);
						b.addPlayer(player);
						Enemies.put(player.getName(), b);
					}
					else {
						double percentage = target.getHealth()/target.getMaxHealth()*100;
						String name = "";
						if(target.getCustomName() != null) {
							name = target.getCustomName();
						}
						else {
							name = target.getName();
						}
						BossBar b = Bukkit.createBossBar(name, BarColor.GREEN, BarStyle.SEGMENTED_10, new BarFlag[0]);
						b.setProgress(target.getHealth()/target.getMaxHealth());
						
						if(percentage <= 100) {
							if(percentage > 62) {
								b.setColor(BarColor.GREEN);
							}
							else if(percentage > 25) {
								b.setColor(BarColor.YELLOW);
							}
							else {
								b.setColor(BarColor.RED);
							}
						}
						
						BukkitRunnable reduction = new BukkitRunnable() {
							@Override
							public void run() {
								b.setProgress(0.0);

							}
						};
						
						reduction.runTaskLater(Bukkit.getPluginManager().getPlugin("EnemyBossBar"), 1);
						b.setVisible(true);
						b.addPlayer(player);
						instakill = true;
						BukkitRunnable task = new BukkitRunnable() {
							@Override
							public void run() {
								b.removePlayer(player);

							}
						};
						
						task.runTaskLater(Bukkit.getPluginManager().getPlugin("EnemyBossBar"), 3);
					}

					
					
				}
				
				if(Engaging.containsKey(player.getName())) {
					if(event.getEntity().getUniqueId() != Engaging.get(player.getName())) {
						if(target.getHealth() - event.getDamage() > 0) {
							String name = "";
							if(target.getCustomName() != null) {
								name = target.getCustomName();
							}
							else {
								name = target.getName();
							}
							BossBar b = Enemies.get(player.getName());
							b.setTitle(name);
							b.setProgress((target.getHealth()-event.getDamage())/target.getMaxHealth());
						}
					}
				}

				if(!instakill) {
					Engaging.put(player.getName(), target.getUniqueId());
				}

				
				if(Active.containsKey(player.getName())) {
					Active.get(player.getName()).cancel();
					Active.remove(player.getName());
				}
				
				BukkitRunnable task = new BukkitRunnable() {

					@Override
					public void run() {
						if(Engaging.containsKey(player.getName())) {
							Engaging.remove(player.getName());
						}
						if(Enemies.containsKey(player.getName())) {
							Enemies.get(player.getName()).removePlayer(player);
							Enemies.remove(player.getName());
						}
						
						
					}
					
				};
				
				task.runTaskLater(Bukkit.getPluginManager().getPlugin("EnemyBossBar"), 80);
				Active.put(player.getName(), task);
			}
		}

	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof LivingEntity) {
			LivingEntity target = (LivingEntity)event.getEntity();
			UUID id = target.getUniqueId();
			if(Engaging.containsValue(target.getUniqueId())) {
				List<String> players = new ArrayList<String>();
				for(String s:Engaging.keySet()) {
					if(Engaging.get(s).equals(target.getUniqueId())) {
						if(Enemies.containsKey(s)) {
							String name = "";
							if(target.getCustomName() != null) {
								name = target.getCustomName();
							}
							else {
								name = target.getName();
							}
							double health = target.getHealth() - event.getFinalDamage();
							double percentage = health/target.getMaxHealth()*100;
							BossBar b = Enemies.get(s);
							String colorcode = "§a";
							if(health > 0) {
								if(percentage > 62) {
									b.setColor(BarColor.GREEN);
									colorcode = "§a";
								}
								else if(percentage > 21) {
									b.setColor(BarColor.YELLOW);
									colorcode = "§e";
								}
								else {
									b.setColor(BarColor.RED);
									colorcode = "§c";
								}
								b.setTitle(String.format("%s%s ▪ [%s%s]", name, colorcode, (int)percentage, "%"));
								b.setProgress((health)/target.getMaxHealth());
							}
							else {
								
								b.setProgress(0.0);
								BukkitRunnable task = new BukkitRunnable() {
									@Override
									public void run() {
										b.removePlayer(Bukkit.getPlayer(s));
									}
								};
								
								task.runTaskLater(Bukkit.getPluginManager().getPlugin("EnemyBossBar"), 3);
								players.add(s);

							}
							
							
						}
						
					}
				}
				for(String s: players) {
					Enemies.remove(s);
					Engaging.remove(s);
				}
			}
		}

	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		if(Enemies.containsKey(event.getPlayer().getName())) {
			BossBar b = Enemies.get(event.getPlayer().getName());
			b.removePlayer(event.getPlayer());
			Enemies.remove(event.getPlayer().getName());
		}
		if(Engaging.containsKey(event.getPlayer().getName())) {
			Engaging.remove(event.getPlayer().getName());
		}
	}	
	
	@EventHandler
	public void onRegen(EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof LivingEntity) {
			LivingEntity target = (LivingEntity)event.getEntity();
			if(Engaging.containsValue(target.getUniqueId())) {
				List<String> players = new ArrayList<String>();
				for(String s:Engaging.keySet()) {
					if(Engaging.get(s).equals(target.getUniqueId())) {
						if(Enemies.containsKey(s)) {
							String name = "";
							if(target.getCustomName() != null) {
								name = target.getCustomName();
							}
							else {
								name = target.getName();
							}
							double health = target.getHealth() + event.getAmount();
							double percentage = health/target.getMaxHealth()*100;
							BossBar b = Enemies.get(s);
							String colorcode = "§a";
							if(percentage <= 100) {
								if(percentage > 62) {
									b.setColor(BarColor.GREEN);
									colorcode = "§a";
								}
								else if(percentage > 25) {
									b.setColor(BarColor.YELLOW);
									colorcode = "§e";
								}
								else {
									b.setColor(BarColor.RED);
									colorcode = "§c";
								}
								b.setTitle(String.format("%s%s ▪ [%s%s]", name, colorcode, (int)percentage, "%"));
								b.setProgress((health)/target.getMaxHealth());
							}
							else {
								b.setTitle(String.format("%s%s ▪ [%s%s]", name, colorcode, 100, "%"));
								b.setProgress(1.0);
							}


						}
						
					}
				}
			}
		}
	}
	
	/*
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if(Engaging.containsKey(event.getPlayer().getName()) && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.BOW) {
			Player player = event.getPlayer();
			World world = player.getWorld();
			Entity target = null;
			for(Entity e : world.getLivingEntities()) {
				if(e.getUniqueId().equals(Engaging.get(player.getName()))) {
					target = e;
				}
			}
			if(target != null) {
				if(target.getLocation().distance(event.getTo()) > 15) {
					Engaging.remove(player.getName());
					if(Enemies.containsKey(player.getName())) {
						BossBar b = Enemies.get(player.getName());
						b.removePlayer(player);
						Enemies.remove(player.getName());
					}
				}
			}
		}
	}
	*/
	

}
