package net.nightlyside.wg.regionhideplayer;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WGRegionHidePlayers extends JavaPlugin implements Listener{
	
	public StateFlag HIDEPLAYERS = new StateFlag("hideplayers", false);
	
	protected Logger log = this.getServer().getLogger();
	protected WorldGuardPlugin worldguard;
	protected WGCustomFlagsPlugin customflags;
	protected HashMap<Player, Boolean> seeingPlayers = new HashMap<Player, Boolean>(); 
	
	private boolean debug = true;
	
	@Override
    public void onDisable() {
		log.log(Level.INFO, "[WGRegionHidePlayers] Version {0} disabled.", getDescription().getVersion());
    }
	
	@Override
	public void onEnable()
	{
		worldguard = getWorldGuard();
		customflags = getWGCustomFlags();
		customflags.addCustomFlag(HIDEPLAYERS);

		getServer().getPluginManager().registerEvents(this, worldguard);
		
		log.log(Level.INFO, "[WGRegionHidePlayers] Version {0} enabled.", getDescription().getVersion());
	}
	
	private WGCustomFlagsPlugin getWGCustomFlags()
	{
	  Plugin plugin = getServer().getPluginManager().getPlugin("WGCustomFlags");
	  
	  if (plugin == null || !(plugin instanceof WGCustomFlagsPlugin))
	  {
		  log.info(String.format("[WGRegionHidePlayers] - Plugin disabled due to no WGCustomFlag dependency found!"));
		  Bukkit.getPluginManager().disablePlugin(this);
		  return null;
	  }

	  return (WGCustomFlagsPlugin) plugin;
	}
	
	protected WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	log.info(String.format("[WGRegionHidePlayers] - Plugin disabled due to no WorldGuard dependency found!"));
			Bukkit.getPluginManager().disablePlugin(this);
	        return null;
	    }
        Logger.getLogger("Minecraft").log(Level.INFO, "[WGRegionHidePlayers] WorldGuard hooked.");
	    return (WorldGuardPlugin) plugin;
	}
	
	public boolean canHideHere(Player player)
	{
		RegionManager regionManager = worldguard.getRegionManager(player.getWorld());
	    if (regionManager == null) {
	    	if(debug)
	    		player.sendMessage("RegionManagerNull");
	        return false;
	    }
	    
	    ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation());
	    if (set == null) {
	    	if(debug)
	    		player.sendMessage("RegionNull");
	        return false;
	    }
	    
	    State flag = set.getFlag(HIDEPLAYERS);
	    if (flag == null) {
	        return false;
	    }
	    
	    if (set.allows(HIDEPLAYERS))
	    {
	    	return true;
	    }
	    else
	    {
	    	if(debug)
	    		player.sendMessage("NoHideFlag");
	    	return false;
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void playerMoveCheck(PlayerMoveEvent event){
		if(canHideHere(event.getPlayer()))
		{
			if(!seeingPlayers.containsKey(event.getPlayer()))
			{
				if(debug)
					event.getPlayer().sendMessage("Enter hidden zone");
				seeingPlayers.put(event.getPlayer(), false);
				for(Player each : Bukkit.getOnlinePlayers())
				{
					each.hidePlayer(event.getPlayer());
					if(canHideHere(each))
						event.getPlayer().hidePlayer(each);
				}
			}
		}
		else
		{
			if(seeingPlayers.containsKey(event.getPlayer()))
			{
				if(debug)
					event.getPlayer().sendMessage("Leave hidden zone");
				seeingPlayers.remove(event.getPlayer());
				for(Player each : Bukkit.getOnlinePlayers())
				{
					if(!canHideHere(each))
						event.getPlayer().showPlayer(each);
					each.showPlayer(event.getPlayer());
				}
			}
		}
	}
	
	@Override
    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase("wgrhideplayers")||cmd.getName().equalsIgnoreCase("hideprg")) {
            if(sender instanceof Player) {
                if(sender.hasPermission("wgrhideplayers.see")) {
                	if(args.length==0)
                	{
                		sender.sendMessage("Usage: /hideprg <on/off>");
                	}
                	else if(args.length==1)
                	{
                		if(args[0].equalsIgnoreCase("off"))
                		{
                			for(Player each : Bukkit.getOnlinePlayers())
            				{
            					((Player) sender).showPlayer(each);
            					each.showPlayer(((Player) sender));
            				}
                			sender.sendMessage("§6Tu peux maintenant voir les autres joueurs dans la zone");
                		}else{
                			for(Player each : Bukkit.getOnlinePlayers())
            				{
                				each.hidePlayer(((Player) sender).getPlayer());
            					if(canHideHere(each))
            						((Player) sender).getPlayer().hidePlayer(each);
            				}
                			sender.sendMessage("§6Tu ne peux plus voir les joueurs dans la zone");
                		}
                	}
                	else
                	{
                		sender.sendMessage("§cIl y a trop d'arguments");
                		sender.sendMessage("Usage: /hideprg <on/off>");
                	}
                } else {
                    sender.sendMessage("§cTu n'as pas la permission de faire cela!");
                }
            } else {
                sender.sendMessage("You must be a player to run that command");
            }
            return true;
        }
        return false;
    }
}
