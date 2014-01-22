package chatcontrol.Utils;

import java.io.File;
import java.util.Arrays;

import chatcontrol.ChatControl;

public class ConfigUpdater {

	public static Status status;
	public static String latestVersion = ChatControl.plugin.getDescription().getVersion();

	public enum Status {
		SUCCESS("§aConfiguration was updated for version " + ChatControl.plugin.getDescription().getVersion()),
		ERROR("&c Config was NOT updated! Please regenerate it."),
		TOO_OLD("§4Your ChatControl version is too old. We cannot update your configuration. Consider regenerating your config.yml."),
		INVALID("&4!!Critical Warning!! &cYour configuration seems to be invalid! Consider regenerating it before any damage occur."),
		//UNKNOWN("&eWarning: Configuration version was not recognized by ChatControl updater! Consider regenerating it!"),
		DISABLED(),
		UPDATE_NOT_NECESSARY();

		String msg;
		private Status(String msg) {
			this.msg = msg;
		}
		private Status() {
		}
	}

	public static void configCheck() {
		String plVersion = ChatControl.Config.getString("Do_Not_Change_Version_Number");

		if(!ChatControl.Config.getBoolean("Miscellaneous.Automatically_Update_Config")){
			status = Status.DISABLED;
			return;
		}
		
		if(plVersion.contains("SNAPSHOT")) {
			status = Status.DISABLED;
			return;
		}
		
		// TODO A better way to make it?
		if (!latestVersion.equals(plVersion)) {
			try {
				if (plVersion.equals("4.1.5")) {
					updateConfigTo416();
					updateConfigTo418();
					updateConfigTo419();
					updateConfigTo422();
					updateConfigTo423();
					updateConfigTo424();
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equals("4.1.6")) {
					updateConfigTo418();
					updateConfigTo419();
					updateConfigTo422();
					updateConfigTo423();
					updateConfigTo424();
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equals("4.1.7")) {
					updateConfigTo418();
					updateConfigTo419();
					updateConfigTo422();
					updateConfigTo423();
					updateConfigTo424();
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equals("4.1.8")) {
					updateConfigTo419();
					updateConfigTo422();
					updateConfigTo423();
					updateConfigTo424();
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.1.9") || plVersion.equalsIgnoreCase("4.2.1")) {
					updateConfigTo422();
					updateConfigTo423();
					updateConfigTo424();
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.2.2")) {
					updateConfigTo423();
					updateConfigTo424();
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.2.3")) {
					updateConfigTo424();
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.2.4")) {
					updateConfigTo430();
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.3.0") || plVersion.equalsIgnoreCase("4.3.1")) {
					updateConfigTo432();
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.3.2") || plVersion.equalsIgnoreCase("4.3.3") || plVersion.equalsIgnoreCase("4.3.4") || plVersion.equalsIgnoreCase("4.3.5")) {
					updateConfigTo436();
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.3.6")) {
					updateConfigTo437();
					updateConfigTo438();
				} else if (plVersion.equalsIgnoreCase("4.3.7")) {
					updateConfigTo438();
				} else if (Integer.valueOf(plVersion.replace(".", "")) < 415) {
					status = Status.TOO_OLD;
				} else if (Integer.valueOf(plVersion.replace(".", "")) > Integer.valueOf(latestVersion.replace(".", ""))) {
					status = Status.INVALID;
				}
			} catch (Exception ex) {
				status = Status.ERROR;
				Common.Log("&cUnable to update ChatControl configuration.", ex);
			} finally {
				try {
					ChatControl.plugin.saveConfig();
					status = Status.SUCCESS;
				} catch (Exception e) {
					e.printStackTrace();
					status = Status.ERROR;
				}
			}
		} else {
			status = Status.UPDATE_NOT_NECESSARY;
		}
		if(status == null){
			return;
		}
		if (!status.equals(Status.DISABLED) && !status.equals(Status.UPDATE_NOT_NECESSARY)) {
			Common.Log(status.msg);
			if(status.equals(Status.SUCCESS)) {
				updateVersionMark();			
			}
		}
	}

	public static void updateConfigTo416() {
		ChatControl.Config.set("Anti_Ad.Command_Whitelist", "[]");
	}

	public static void updateConfigTo418() {
		ChatControl.Config.set("Console.Filter_Plugin_Messages", true);
		ChatControl.Config.set("Console.Filter_Messages", "[]");
	}

	public static void updateConfigTo419() {
		ChatControl.Config.set("Signs.Advertising_Check", true);
		ChatControl.Config.set("Signs.Rewrite_Lines", true);
		ChatControl.Config.set("Signs.Rewrite_Text", "Advertising was:&4detected.:Please do not:advertise.");
	}

	public static void updateConfigTo422() {
		ChatControl.Config.set("Protect.Prevent_Tab_Complete", true);
	}

	public static void updateConfigTo423() {
		ChatControl.Config.set("Localization.Cannot_Command_While_Muted", "&7You cannot use this command while the chat is muted.");
		ChatControl.Config.set("Mute.Disabled_Commands_During_Mute", "[]");
		ChatControl.Config.set("Localization.Dupe_Sign", "&cPlease do not repeat the same text on sign.");
		ChatControl.Config.set("Signs.Duplication_Check", false);
	}

	public static void updateConfigTo424() {
		ChatControl.Config.set("Anti_Ad.Filter_Pre_Process", "[(\\[\\])]");			
	}

	public static void updateConfigTo430() {
		ChatControl.Config.set("Messages.Common.Join_Message", "default");
		ChatControl.Config.set("Messages.Common.Quit_Message", "default");
		ChatControl.Config.set("Messages.Common.Kick_Message", "default");			
		ChatControl.Config.set("Clear.Do_Not_Clear_For_Staff", "false");			
		ChatControl.Config.set("Localization.Staff_Chat_Clear_Message", "&7^----- [ == &fChat was cleared by %player &7== ] -----^");
		ChatControl.Config.set("Localization.Console", "&4server");
		Common.Log("&cWARNING! You was runnnig an very old version of ChatControl, configuration might not get updated correctly! It is strongly advised to regenerate your config.");
	}

	public static void updateConfigTo432() {
		ChatControl.Config.set("Grammar.Replace_Characters", true);			
		ChatControl.Config.set("Grammar.Replace_With_Smileys", true);

		ChatControl.Config.set("Grammar.Replace_List.dis", "this");
		ChatControl.Config.set("Grammar.Replace_List.wanna", "want");
		ChatControl.Config.set("Grammar.Replace_List.gonna", "going");
		ChatControl.Config.set("Grammar.Replace_List.(can|may|would you like if) i (have|be|become|get|has) (op|admin|mod|builder)", "can i has nodus?");
		ChatControl.Config.set("Grammar.Replace_List.(do|are) you (need|wish|looking for) (any|some|one|good) (op|ops|operators|admins|mods|builders|new people|ateam)", "u need some pig zaps");
		ChatControl.Config.set("Grammar.Replace_List.this server (is bad|sucks)", "i just griefed some crap castle /w nodus u noob kiddos");
		ChatControl.Config.set("Grammar.Replace_List.owner (fucks|sucks) (kids|dicks|birds)", "piewdiepie sucks");
	}

	public static void updateConfigTo436() {
		ChatControl.Config.set("Localization.Successful_Console_Clear", "%prefix &7Console was successfuly cleared.");			
		ChatControl.Config.set("Clear.Amount_Of_Lines_To_Clear_In_Console", 300);
	}

	public static void updateConfigTo437() {
		ChatControl.Config.set("Chat.Strip_Unicode", true);
		ChatControl.Config.set("Anti_Swear.Command_Whitelist", Arrays.asList("/register", "/reg", "/login", "/l"));
	}

	public static void updateConfigTo438() {
		ChatControl.Config.set("Broadcast_Silent_Mute", "&cInitiated global chat mute.");
		ChatControl.Config.set("Broadcast_Silent_Unmute", "&cGlobal chat mute was cancelled.");
		ChatControl.Config.set("Broadcast_Silent_Clear", "&cThe game chat was cleared.");
		ChatControl.Config.set("Reload_Failed", null);
		Common.Log("&cNOTICE: Configuration was rearranged in version 4.3.8 along with some minor changes, it is highly recommended to regenerate it!");
	}

	public static void updateVersionMark() {
		try {		
			ChatControl.Config.set("Do_Not_Change_Version_Number", latestVersion);
			ChatControl.Config.save(new File(ChatControl.plugin.getDataFolder(), "config.yml"));
		} catch (Exception ex) {
			Common.Log("&cUnable to update ChatControl configuration.", ex);
		}
	}	
}