package me.yaboiedgar.oppoweritems.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.yaboiedgar.oppoweritems.Main;

public class TKWand implements Listener {
	
	Map<Player, Entity> controlled = new HashMap<Player, Entity>();
	Map<Player, Integer> control_modes = new HashMap<Player, Integer>();
	
	Map<Player, BukkitRunnable> freeze_tasks = new HashMap<Player, BukkitRunnable>();
	Map<Entity, Location> controlled_locs = new HashMap<Entity, Location>();
	
	List<Snowball> sballs = new ArrayList<Snowball>();
	
	private void endControl(Entity entity, Player player, BukkitRunnable task) { // Remove entities from lists and cancel control task
		entity.setVelocity(controlled.get(player).getVelocity().zero());
		entity.teleport(controlled_locs.get(controlled.get(player)));
		task.cancel();
		freeze_tasks.remove(player);
		controlled_locs.remove(entity);
		
		controlled.remove(player);
		control_modes.remove(player);
	}
	
	private void freezeEntity(Entity entity, Player player) { // Freeze controlled entity in place
		BukkitRunnable task = new BukkitRunnable() {

			@Override
			public void run() {
				if (!player.isOnline()) {
					endControl(entity, player, this);
					return;
				}
				
				if (entity.getLocation().equals(controlled_locs.get(entity)))
					return;
				
				entity.teleport(controlled_locs.get(entity));
				entity.setFallDistance(0);
			}
			
		};
		freeze_tasks.put(player, task);
		task.runTaskTimer(Main.getPlugin(Main.class), 0, 0);
	}
	
	public void checkSBalls() { // Check each control snowball for their location, makes it so the player can only start controlling entities in a 100-block radius
		new BukkitRunnable() {
			
			List<Snowball> toRemove = new ArrayList<Snowball>();

			@Override
			public void run() {
				for (Snowball sball : sballs) {
					if (sball.getShooter() == null) {
						toRemove.add(sball);
						sball.remove();
					}
					
					Player shooter = (Player) sball.getShooter();
					
					if (!shooter.isOnline()) {
						toRemove.add(sball);
						sball.remove();
					}
					
					if (Math.abs(sball.getLocation().getX() - shooter.getLocation().getX()) >= 100 ||
						Math.abs(sball.getLocation().getY() - shooter.getLocation().getY()) >= 100 ||
						Math.abs(sball.getLocation().getZ() - shooter.getLocation().getZ()) >= 100) {
							toRemove.add(sball);
							sball.remove();
					}
				}
				
				for (Snowball sball : toRemove) {
					sballs.remove(sball);
				}
				toRemove.clear();
			}
			
		}.runTaskTimer(Main.getPlugin(Main.class), 0, 0);
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		try {
			if (!player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1).equals("This item belongs to the plugin OPPowerItems"))
				return;
		} catch (NullPointerException e) {
			return;
		}
		if (player.getInventory().getItemInMainHand().getType() != Material.END_ROD)
			return;
		if (!(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "Telekinetic Wand")))
			return;
		
		event.setCancelled(true);
		
		if (controlled.containsKey(player)) {
			event.getPlayer().sendMessage(ChatColor.GREEN + "Stopped controlling " + controlled.get(player).getName());	
			endControl(controlled.get(player), player, freeze_tasks.get(player));
			return;
		}
		
		Location loc = player.getLocation();
		loc.setY(loc.getY() + 2);
		
		if (loc.getBlock().getType() != Material.AIR) {
			player.sendMessage(ChatColor.GOLD + "Please make some room above you and try again.");
			return;
		}
		
		Snowball sball = (Snowball) loc.getWorld().spawnEntity(loc, EntityType.SNOWBALL);
		sball.setGravity(false);
		sball.setShooter(player);
		sballs.add(sball);
		
		sball.setVelocity(loc.getDirection().multiply(100));
	}
	
	@EventHandler
	public void onCollide(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Snowball))
			return;
		
		Snowball sball = (Snowball) event.getEntity();
		
		if (!sballs.contains(sball))
			return;
		if (event.getHitEntity() == null) {
			event.setCancelled(true);
			sballs.remove(sball);
			sball.remove();
			return;
		}
		if (!(event.getHitEntity() instanceof Mob) && !(event.getHitEntity() instanceof Player)) {
			event.setCancelled(true);
			sballs.remove(sball);
			sball.remove();
			return;
		}
		if (event.getHitEntity() instanceof Player) {
			Player player = (Player) event.getHitEntity();
			if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
				sballs.remove(sball);
				sball.remove();
				return;
			}
		}
		
		event.setCancelled(true);
		event.getEntity().remove();
		sballs.remove(sball);
		Player player = (Player) event.getEntity().getShooter();
		controlled.put(player, event.getHitEntity());
		control_modes.put(player, 0);
		
		controlled_locs.put(event.getHitEntity(), event.getHitEntity().getLocation());
		freezeEntity(event.getHitEntity(), player);
		
		player.sendMessage(ChatColor.AQUA + "You are now controlling " + event.getHitEntity().getName());
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (!controlled.containsKey(event.getPlayer()))
			return;
		
		Player player = event.getPlayer();
		Entity controlled_ent = controlled.get(player);
		Integer control_mode = control_modes.get(player);
		
		if (controlled_ent.isDead()) {
			player.sendMessage(ChatColor.GOLD + controlled_ent.getName() + " is no longer alive.");
			endControl(controlled_ent, player, freeze_tasks.get(player));
			return;
		}
		
		// MOVEMENT CONTROL
		if (control_mode == 0) { // Control Mode X/Z
			if (event.getFrom().getX() != event.getTo().getX()) {
				event.setCancelled(true);
				double moveAmnt = event.getTo().getX() - event.getFrom().getX();
			
				controlled_locs.put(controlled_ent, controlled_locs.get(controlled_ent).add(moveAmnt * 10, 0, 0));
			}
		
			if (event.getFrom().getZ() != event.getTo().getZ()) {
				event.setCancelled(true);
				double moveAmnt = event.getTo().getZ() - event.getFrom().getZ();
			
				controlled_locs.put(controlled_ent, controlled_locs.get(controlled_ent).add(0, 0, moveAmnt * 10));
			}
			return;
		}
		// Control Mode Y
		if (event.getFrom().getZ() != event.getTo().getZ()) {
			if (135 <= player.getLocation().getYaw() || player.getLocation().getYaw() <= -135) {
				event.setCancelled(true);
				double moveAmnt = event.getFrom().getZ() - event.getTo().getZ();
			
				controlled_ent.teleport(controlled_ent.getLocation().add(0, moveAmnt * 10, 0));
				controlled_locs.put(controlled_ent, controlled_locs.get(controlled_ent).add(0, moveAmnt * 10, 0));
			} else if (-45 <= player.getLocation().getYaw() && player.getLocation().getYaw() <= 45) {
				event.setCancelled(true);
				double moveAmnt = event.getTo().getZ() - event.getFrom().getZ();
				
				controlled_ent.teleport(controlled_ent.getLocation().add(0, moveAmnt * 10, 0));
				controlled_locs.put(controlled_ent, controlled_locs.get(controlled_ent).add(0, moveAmnt * 10, 0));
			}
		}
		if (event.getFrom().getX() != event.getTo().getX()) {
			if (-135 < player.getLocation().getYaw() && player.getLocation().getYaw() < -45) {
				event.setCancelled(true);
				double moveAmnt = event.getTo().getX() - event.getFrom().getX();
			
				controlled_ent.teleport(controlled_ent.getLocation().add(0, moveAmnt * 10, 0));
				controlled_locs.put(controlled_ent, controlled_locs.get(controlled_ent).add(0, moveAmnt * 10, 0));
			} else if (45 < player.getLocation().getYaw() && player.getLocation().getYaw() < 135) {
				event.setCancelled(true);
				double moveAmnt = event.getFrom().getX() - event.getTo().getX();
			
				controlled_ent.teleport(controlled_ent.getLocation().add(0, moveAmnt * 10, 0));
				controlled_locs.put(controlled_ent, controlled_locs.get(controlled_ent).add(0, moveAmnt * 10, 0));
			}
		}
	}
	
	@EventHandler
	public void onShift(PlayerToggleSneakEvent event) {
		if (!controlled.containsKey(event.getPlayer()))
			return;
		
		if (event.isSneaking()) {
			event.setCancelled(true);
			if (control_modes.get(event.getPlayer()) == 0) {
				control_modes.put(event.getPlayer(), 1);
				return;
			}
			control_modes.put(event.getPlayer(), 0);
		}
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player)
			if (controlled.containsKey((Player) event.getEntity())) {
				Player player = (Player) event.getEntity();
				player.sendMessage(ChatColor.GOLD + "You have died and lost control of " + controlled.get(player).getName());
				endControl(controlled.get(player), player, freeze_tasks.get(player));
				return;
			}
		
		if (!controlled.containsValue(event.getEntity()))
			return;
		
		for (Player player : controlled.keySet()) {
			if (controlled.get(player).equals(event.getEntity())) {
				player.sendMessage(ChatColor.GOLD + event.getEntity().getName() + " has died under your control.");
				endControl(controlled.get(player), player, freeze_tasks.get(player));
				return;
			}
		}
	}
}
