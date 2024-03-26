package me.yaboiedgar.oppoweritems.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.EntityDismountEvent;

import me.yaboiedgar.oppoweritems.Main;

public class SuperFireball implements Listener {
	
	List<Fireball> SuperFireballs = new ArrayList<Fireball>();
	
	private void checkY(Fireball superfireball) { // Explode fireball if it escapes build limits
		new BukkitRunnable() {
			Location deathLoc;
			World deathWorld;
			
			public void run() {
				if (superfireball.getLocation().getY() < superfireball.getWorld().getMaxHeight() && superfireball.getLocation().getY() > superfireball.getWorld().getMinHeight())
					return;
				
				deathLoc = superfireball.getLocation();
				deathWorld = superfireball.getWorld();
				
				SuperFireballs.remove(superfireball);
				superfireball.remove();
				deathWorld.createExplosion(deathLoc, 1, true, true, superfireball);
				this.cancel();
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
		} catch (NullPointerException e) { // Can be caused if there is no meta, lore, etc
			return;
		}
		if (player.getInventory().getItemInMainHand().getType() != Material.FIRE_CHARGE)
			return;
		if (!player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "Super Fireball"))
			return;
		
		event.setCancelled(true);
		
		Location loc = player.getLocation();
		loc.setY(loc.getY() + 1);
		
		
		Fireball fireball = (Fireball) loc.getWorld().spawnEntity(loc, EntityType.FIREBALL);
		fireball.setShooter(player);
		SuperFireballs.add(fireball);
		
		if (player.isSneaking()) {
			fireball.setVelocity(loc.getDirection());
			checkY(fireball);
			return;
		}
		if (player.isSprinting()) {
			fireball.setVelocity(loc.getDirection().multiply(2));
			checkY(fireball);
			return;
		}
		fireball.setVelocity(loc.getDirection().multiply(1.5));
		checkY(fireball);
	}
	
	@EventHandler
	public void onCollide(ProjectileHitEvent event) {
		if (!SuperFireballs.contains(event.getEntity())) {
			return;
		}
		
		if (event.getEntity().getPassengers().size() != 0) {
			if (!event.getEntity().getPassengers().contains(event.getHitEntity())) {
				SuperFireballs.remove(event.getEntity());
				return;
			}
		}
		if (!(event.getHitEntity() instanceof Mob) && !(event.getHitEntity() instanceof Player)) {
			SuperFireballs.remove(event.getEntity());
			return;
		}
		if (event.getHitEntity() instanceof Player) {
			Player player = (Player) event.getHitEntity();
			if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
				event.setCancelled(true);
				return;
			}
		}
		
		event.setCancelled(true);
		event.getEntity().addPassenger(event.getHitEntity());
	}
	
	@EventHandler
	public void onDismount(EntityDismountEvent event) {
		if (!(event.getDismounted() instanceof Fireball))
			return;
		
		Fireball superfireball = (Fireball) event.getDismounted();
		
		if (!SuperFireballs.contains(superfireball))
			return;
		
		event.setCancelled(true);
	}
}
