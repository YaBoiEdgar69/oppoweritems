package me.yaboiedgar.oppoweritems;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OPICommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
			return true;
		}
		Player player = (Player) sender;
		if (!player.hasPermission("oppoweritems.oppoweritem")) {
			player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			return true;
		}
		if (args.length != 1) {
			player.sendMessage(ChatColor.GOLD + "Usage: /" + label +  " [tkwand, superfireball]");
			return true;
		}
		
		if (args[0].equals("help")) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
			"--- /" + label + " help ---\n\n"
			+ "&chelp&f - Opens this help menu.\n"
			+ "&csuperfireball&f - Lets you shoot a fireball that picks up any living entity it hits (unless the fireball already has an entity on it.)\n"
			+ "&ctkwand&f - Gives a wand that allows you to harness telekinetic abilities and control the position of all living entities. Visit &9https://tinyurl.com/33mjxnpa&f for more info on the controls.\n\n"
			+ "&lOPPowerItems plugin made by YaBoiEdgar. View my SpigotMC profile at &9https://www.spigotmc.org/members/yaboiedgar.1987748/"
			));
			return true;
		}
		
		Inventory inv = player.getInventory();
		
		if (inv.firstEmpty() == -1) {
			player.sendMessage(ChatColor.GOLD + "Make some room in your inventory and try again.");
			return true;
		}
		
		if (args[0].equals("tkwand")) {
			ItemStack item = new ItemStack(Material.END_ROD);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Telekinetic Wand");
			meta.addEnchant(Enchantment.DURABILITY, 1, false);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			List<String> lore = new ArrayList<String>();
			lore.add("");
			lore.add("This item belongs to the plugin OPPowerItems");
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			inv.addItem(item);
			player.sendMessage(ChatColor.AQUA + "You have recieved a telekinetic wand!");
			return true;
		}
		
		if (args[0].equals("superfireball")) {
			ItemStack item = new ItemStack(Material.FIRE_CHARGE);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Super Fireball");
			meta.addEnchant(Enchantment.DURABILITY, 1, false);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			List<String> lore = new ArrayList<String>();
			lore.add("");
			lore.add("This item belongs to the plugin OPPowerItems");
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			inv.addItem(item);
			player.sendMessage(ChatColor.AQUA + "You have recieved a super fireball!");
			return true;
		}
		
		player.sendMessage(ChatColor.GOLD + "Usage: /" + label +  " [tkwand, superfireball]");
		return true;
	}

}
