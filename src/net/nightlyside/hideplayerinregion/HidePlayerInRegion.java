package net.nightlyside.hideplayerinregion;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.nightlyside.hideplayerinregion.events.RegionEnterEvent;
import net.nightlyside.hideplayerinregion.events.RegionLeaveEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class HidePlayerInRegion extends JavaPlugin{
	protected Logger log = this.getServer().getLogger();
	protected WorldGuardPlugin worldguard;
	protected WGCustomFlagsPlugin customflags;
	private OnPlayerEnterInHidingRegion listener;
	protected HashMap<Player, Boolean> seeingPlayers = new HashMap<Player, Boolean>();
	
	public static StateFlag HIDEPLAYERS = new StateFlag("hideplayers", true);

	@Override
    public void onDisable() {
		log.log(Level.INFO, "[HidePlayerInRegion] Version {0} disabled.", getDescription().getVersion());
    }
	
	@Override
	public void onEnable()
	{
		worldguard = getWorldGuard();
		customflags = getWGCustomFlags();
		customflags.addCustomFlag(HIDEPLAYERS);
		listener = new OnPlayerEnterInHidingRegion(this);
		getServer().getPluginManager().registerEvents(listener, this);
		log.log(Level.INFO, "[HidePlayerInRegion] Version {0} enabled.", getDescription().getVersion());
	}
	
	private WGCustomFlagsPlugin getWGCustomFlags()
	{
	  Plugin plugin = getServer().getPluginManager().getPlugin("WGCustomFlags");
	  
	  if (plugin == null || !(plugin instanceof WGCustomFlagsPlugin))
	  {
		  log.info(String.format("[HidePlayerInRegion] - Plugin disabled due to no WGCustomFlag dependency found!"));
		  Bukkit.getPluginManager().disablePlugin(this);
		  return null;
	  }

	  return (WGCustomFlagsPlugin) plugin;
	}
	
	protected WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	log.info(String.format("[HidePlayerInRegion] - Plugin disabled due to no WorldGuard dependency found!"));
			Bukkit.getPluginManager().disablePlugin(this);
	        return null;
	    }
        Logger.getLogger("Minecraft").log(Level.INFO, "[HidePlayerInRegion] WorldGuard hooked.");
	    return (WorldGuardPlugin) plugin;
	}
	
	public boolean canPlayerHide(Player player)
	{
	    if (worldguard!=null && player.hasPermission("hideplayerinregion.hide"))
	    {
	        Location loc = player.getLocation();
	        RegionManager mgr = worldguard.getGlobalRegionManager().get(loc.getWorld());
	        ApplicableRegionSet set = mgr.getApplicableRegions(loc);

	        return set.allows(DefaultFlag.BUILD);
	    }
	    return false;
	}
	
	public void togglePlayerVisibility(Player player, boolean visible)
	{
		if(visible)
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				player.showPlayer(p);
			}
		}
		else
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				if(!p.hasPermission("hideplayerinregion.nohide"))
					player.hidePlayer(p);
			}
		}
	}
	 
	@EventHandler
	public void onRegionEnter(RegionEnterEvent e)
	{
		e.getPlayer().sendMessage("§6You are in an invisible zone!");
		if(e.getRegion().getFlags().containsKey(HIDEPLAYERS))
		{
			if(!seeingPlayers.containsKey(e.getPlayer()))
				togglePlayerVisibility(e.getPlayer(), false);
		}
	}
	
	@EventHandler
	public void onRegionLeave(RegionLeaveEvent e)
	{
		if(e.getRegion().getFlags().containsKey(HIDEPLAYERS))
		{
			togglePlayerVisibility(e.getPlayer(), true);
			e.getPlayer().sendMessage("§6You are leaving the invisible zone!");;
		}
	}
	
	@Override
    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase("hideplayersinregion")||cmd.getName().equalsIgnoreCase("hidepir")) {
            if(sender instanceof Player) {
                if(sender.hasPermission("hideplayerinregion.see")) {
                	if(args.length==0)
                	{
                		sender.sendMessage("Usage: /hidepir <on/off>");
                	}
                	else if(args.length==1)
                	{
                		if(args[0].equalsIgnoreCase("off"))
                		{
                			if(seeingPlayers.containsKey(sender))
                				seeingPlayers.remove(sender);
                			seeingPlayers.put((Player) sender, true);
                			sender.sendMessage("§6You can now see other hidden players in this region");
                		}else{
                			if(seeingPlayers.containsKey(sender))
                				seeingPlayers.remove(sender);
                			sender.sendMessage("§6You cannot see anymore other hidden players in this region");
                		}
                	}
                	else
                	{
                		sender.sendMessage("§cThere are too much arguments");
                	}
                } else {
                    sender.sendMessage("§cYou don't have enough permissions to do that!");
                }
            } else {
                sender.sendMessage("You must be a player to run that command");
            }
            return true;
        }
        return false;
    }
}
