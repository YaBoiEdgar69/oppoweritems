package me.yaboiedgar.oppoweritems;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class OPICompleter implements TabCompleter {
	
	List<String> cmdArgs = new ArrayList<String>();

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmdArgs.isEmpty()) {
			cmdArgs.add("help");
			cmdArgs.add("superfireball");
			cmdArgs.add("tkwand");
		}
		
		List<String> result = new ArrayList<String>();
		if (args.length == 1) {
			for (String arg : cmdArgs) {
				if (arg.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(arg);
			}
			return result;
		}
		
		return null;
	}

}
