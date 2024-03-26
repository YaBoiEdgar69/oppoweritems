package me.yaboiedgar.oppoweritems;

import org.bukkit.plugin.java.JavaPlugin;

import me.yaboiedgar.oppoweritems.listeners.SuperFireball;
import me.yaboiedgar.oppoweritems.listeners.TKWand;

public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		TKWand tkwand = new TKWand();
		
		this.getCommand("oppoweritem").setExecutor(new OPICommand());
		this.getCommand("oppoweritem").setTabCompleter(new OPICompleter());
		
		this.getServer().getPluginManager().registerEvents(new SuperFireball(), this);
		this.getServer().getPluginManager().registerEvents(tkwand, this);
		
		tkwand.checkSBalls();
	}
	
	@Override
	public void onDisable() {
		
	}
}
