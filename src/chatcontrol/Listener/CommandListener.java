package chatcontrol.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import chatcontrol.ChatControl;
import chatcontrol.Utils.Common;

public class CommandListener implements Listener{
			
	@EventHandler(ignoreCancelled=true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent e){
		if(!(Bukkit.getOnlinePlayers().length >= ChatControl.Config.getInt("Miscellaneous.Minimum_Players_To_Enable_Plugin"))){
			return;
		}
		if(Common.opsHasPermissions(e.getPlayer())){
			return;
		}
		
		if(ChatControl.muted){
			if (e.getPlayer().hasPermission("chatcontrol.bypass.mute")) {
				return;
			}
			for (String msg : ChatControl.Config.getStringList("Mute.Disabled_Commands_During_Mute")){
				if(e.getMessage().startsWith("/" + msg)){
					Common.sendMsg(e.getPlayer(), "Localization.Cannot_Command_While_Muted");
					e.setCancelled(true);
					return;
				}
			}
		}
		
		long cas = System.currentTimeMillis() / 1000L;
		
	    if((cas - ChatControl.data.get(e.getPlayer()).lastCommandTime) < ChatControl.Config.getLong("Commands.Command_Delay")){
			if(e.getPlayer().hasPermission("chatcontrol.bypass.time")){
				return;
			}			
			for (String sprava : ChatControl.Config.getStringList("Commands.Whitelist_Time")){
				if(e.getMessage().startsWith("/" + sprava)){
			    	return;
				}
			}
			e.getPlayer().sendMessage(ChatControl.Config.getString("Localization.Command_Message").replace("&", "�").replace("%prefix", Common.prefix()).replace("%time", String.valueOf(ChatControl.Config.getLong("Commands.Command_Delay") - (cas - ChatControl.data.get(e.getPlayer()).lastCommandTime))));  
		    e.setCancelled(true);
		    return;
	    } else {
	    	ChatControl.data.get(e.getPlayer()).lastCommandTime = cas;
	    }
	    
		if(ChatControl.Config.getBoolean("Commands.Block_Duplicate_Commands")){
			String sprava = e.getMessage().replaceAll("[.:_,!*�*><}{&#'$|\\/()]", "").toLowerCase();
			if(ChatControl.data.get(e.getPlayer()).lastCommand.equalsIgnoreCase(sprava)){
				if(e.getPlayer().hasPermission("chatcontrol.bypass.dupe")){
					return;
				}
				for (String whitelistedMsg : ChatControl.Config.getStringList("Commands.Whitelist_Duplication")){
					if(e.getMessage().startsWith("/" + whitelistedMsg)){
						return;
					}
				}
				Common.sendMsg(e.getPlayer(), "Localization.Dupe_Command");
				e.setCancelled(true);
				return;
			}
			ChatControl.data.get(e.getPlayer()).lastCommand = sprava;
		}
		
	    if(ChatControl.Config.getBoolean("Anti_Ad.Enabled_In_Commands")){
	    	if(Common.msgIsAd(e.getPlayer(), e.getMessage())){	   
				if(e.getPlayer().hasPermission("chatcontrol.bypass.ad")){
					return;
				}
				for(String whitelist : ChatControl.Config.getStringList("Anti_Ad.Command_Whitelist")){
					if(e.getMessage().startsWith(whitelist)){
						return;
					}
				}
				Common.customAction(e.getPlayer(), "Anti_Ad.Custom_Command", e.getMessage());
				Common.messages(e.getPlayer(), e.getMessage());
	    		e.setCancelled(true);
	    	}
	    }
	    
	    if(ChatControl.Config.getBoolean("Anti_Swear.Enabled_In_Commands")){
			if(e.getPlayer().hasPermission("chatcontrol.bypass.swear")){
				return;
			}
	    	for (String msg : ChatControl.Config.getStringList("Anti_Swear.Word_List")){
	    		if(e.getMessage().toLowerCase().matches(".*" + msg + ".*")){
	    			if(ChatControl.Config.getBoolean("Anti_Swear.Inform_Admins")){
	    				for(Player pl : Bukkit.getOnlinePlayers()){
	    					if(pl.hasPermission("chatcontrol.notify.swear") || (pl.isOp())){
	    						pl.sendMessage(ChatControl.Config.getString("Localization.Swear_Admin_Message").replace("%prefix", Common.prefix()).replace("&", "�").replace("%player", e.getPlayer().getName()).replace("%message", e.getMessage()));
	    					}
	    				}
	    			}
	    			Common.customAction(e.getPlayer(), "Anti_Swear.Custom_Command", e.getMessage());
	    			if(ChatControl.Config.getBoolean("Anti_Swear.Block_Message")){
	    				e.setCancelled(true);
	    			}
	    			if(ChatControl.Config.getBoolean("Anti_Swear.Warn_Player")){
	    				Common.sendMsg(e.getPlayer(), "Localization.Do_Not_Swear");
	    			}	    		
	    		}
	    	}
	    }
	}
}
