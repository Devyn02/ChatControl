package kangarko.chatcontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Filter;

import kangarko.chatcontrol.filter.ConsoleFilter;
import kangarko.chatcontrol.filter.Log4jFilter;
import kangarko.chatcontrol.hooks.AuthMeHook;
import kangarko.chatcontrol.hooks.EssentialsHook;
import kangarko.chatcontrol.hooks.ProtocolLibHook;
import kangarko.chatcontrol.hooks.VaultHook;
import kangarko.chatcontrol.listener.ChatListener;
import kangarko.chatcontrol.listener.CommandListener;
import kangarko.chatcontrol.listener.PlayerListener;
import kangarko.chatcontrol.model.ConfHelper;
import kangarko.chatcontrol.model.ConfHelper.IllegalLocaleException;
import kangarko.chatcontrol.model.ConfHelper.InBuiltFileMissingException;
import kangarko.chatcontrol.model.Settings;
import kangarko.chatcontrol.model.SettingsConsole;
import kangarko.chatcontrol.rules.ChatCeaser;
import kangarko.chatcontrol.utils.Common;
import kangarko.chatcontrol.utils.Permissions;
import kangarko.chatcontrol.utils.UpdateCheck;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

// TODO implement death messages
// TODO replace remap
// TODO implement lag checker
// FIXME disappearing comments from settings
public class ChatControl extends JavaPlugin {

	private static ChatControl instance;

	// Player IP, Time
	public static HashMap<String, Long> ipLastLogin = new HashMap<>();

	// Player Name, Player Cache
	private static HashMap<String, PlayerCache> playerData = new HashMap<>();

	public static boolean muted = false;

	private EssentialsHook ess;
	private VaultHook vault;
	private AuthMeHook authMe;
	
	private ChatFormatter formatter;
	public ChatCeaser chatCeaser;

	public void onEnable() {
		try {
			instance = this;

			ConfHelper.loadAll();

			chatCeaser = new ChatCeaser("rules.txt");
			chatCeaser.load();

			for (Player pl : getOnlinePlayers())
				getDataFor(pl);

			if (doesPluginExist("Essentials"))
				ess = new EssentialsHook();

			if (doesPluginExist("Vault"))
				vault = new VaultHook();
			
			if (doesPluginExist("AuthMe"))
				authMe = new AuthMeHook();

			getServer().getPluginManager().registerEvents(new ChatListener(), this);
			getServer().getPluginManager().registerEvents(new PlayerListener(), this);
			getServer().getPluginManager().registerEvents(new CommandListener(), this);

			if (SettingsConsole.FILTER_ENABLED || SettingsConsole.FILTER_COLORS_TO_ANSI) {
				try {
					Log4jFilter.init();
					Common.Debug("Console filtering now using Log4j Filter.");
				} catch (NoClassDefFoundError err) {
					Filter filter = new ConsoleFilter();

					if (SettingsConsole.FILTER_FILTER_PLUGINS)
						for (Plugin plugin : getServer().getPluginManager().getPlugins())
							plugin.getLogger().setFilter(filter);

					Bukkit.getLogger().setFilter(filter);
					Common.Debug("Console filtering initiated (MC 1.6.4 and lower).");
				}
			}

			if (Settings.Packets.DISABLE_TAB_COMPLETE) {
				if (doesPluginExist("ProtocolLib")) {
					if (new File("spigot.yml").exists())
						Common.LogInFrame(false, "&aIf you want to disable tab complete, set", "&bcommands.tab-complete &ato 0 in &fspigot.yml &afile.",
								"&aFunction in ChatControl was disabled.");
					else
						ProtocolLibHook.init();
				} else
					Common.LogInFrame(false, "Cannot enable packet features!", "Required plugin missing: ProtocolLib");
			}

			if (Settings.Chat.Formatter.ENABLED) {
				if (vault != null) {
					if (doesPluginExist("ChatManager"))
						Common.LogInFrame(true, "Detected &fChatManager&c! Please copy", "settings from it to ChatControl", "and disable the plugin afterwards!");
					else {
						formatter = new ChatFormatter();
						getServer().getPluginManager().registerEvents(formatter, this);
					}
				} else
					Common.LogInFrame(false, "You need Vault to enable ChatFormatter.");
			}

			if (Settings.Messages.TIMED_ENABLED)
				scheduleTimedMessages();

			getCommand("chatcontrol").setExecutor(new CommandsHandler());

			if (Settings.Updater.ENABLED)
				getServer().getScheduler().runTaskAsynchronously(this, new UpdateCheck("https://raw.github.com/kangarko/ChatControl/master/plugin.yml"));

			Common.addLoggingPrefix();
			Common.LogInFrame(false, "&aThis is a beta version, it might contains bugs!");

		} catch (Throwable t) {
			t.printStackTrace();
			Common.Log("&4!----------------------------------------------!");
			Common.Log(" &cError loading ChatControl, plugin is disabled!");
			Common.Log("&4!----------------------------------------------!");

			if (t instanceof InvalidConfigurationException) {
				Common.Log(" &cIt seems like your config is not a valid YAML.");
				Common.Log(" &cUse online services like");
				Common.Log(" &chttp://yaml-online-parser.appspot.com/");
				Common.Log(" &cto check for syntax errors!");

			} else if (t instanceof IllegalLocaleException) {
				Common.Log(" &cChatControl doesn't have the locale: " + Settings.LOCALIZATION_SUFFIX);

			} else if (t instanceof InBuiltFileMissingException) {
				Common.Log(" &c" + t.getMessage());
				Common.Log(" &cTo fix it, create a blank file with");
				Common.Log(" &cthe name &f" + ((InBuiltFileMissingException) t).file + " &cin plugin folder.");
				Common.Log(" &cIt will be filled with default values.");
				Common.Log(" &ePlease inform the developer about this error.");

			} else
				Common.Log(" &cThe error was: " + t.getMessage());
			Common.Log("&4!----------------------------------------------!");
			getPluginLoader().disablePlugin(this);
		}
	}

	public void onDisable() {
		muted = false;
		playerData.clear();
		ipLastLogin.clear();

		ess = null;
		instance = null;

		UpdateCheck.needsUpdate = false;
		getServer().getScheduler().cancelTasks(this);
	}

	private void scheduleTimedMessages() {
		final HashMap<String, Integer> broadcasterIndexes = new HashMap<String, Integer>();
		final HashMap<String, List<String>> broadcasterCache = new HashMap<>();
		Random rand = new Random();

		if (!Settings.Messages.TIMED_RANDOM_ORDER)
			for (String world : Settings.Messages.TIMED.keySet())
				broadcasterIndexes.put(world, 0);

		if (Settings.Messages.TIMED_RANDOM_NO_REPEAT)
			for (String world : Settings.Messages.TIMED.keySet())
				broadcasterCache.put(world, new ArrayList<String>(Settings.Messages.TIMED.get(world)));

		if (Settings.VERBOSE)
			for (String world : Settings.Messages.TIMED.keySet()) {
				Common.Log("&fMessages for: " + world);

				for (String msg : Settings.Messages.TIMED.get(world))
					Common.Log(" - " + msg);
			}

		new BukkitRunnable() {

			@Override
			public void run() {
				for (String world : Settings.Messages.TIMED.keySet()) {
					List<String> msgs = Settings.Messages.TIMED.get(world);
					if (msgs.size() == 0)
						continue;

					String msg = Settings.Messages.TIMED_PREFIX + " ";

					if (Settings.Messages.TIMED_RANDOM_ORDER) {
						if (Settings.Messages.TIMED_RANDOM_NO_REPEAT) {
							List<String> worldCache = broadcasterCache.get(world);

							if (worldCache.size() == 0)
								worldCache.addAll(msgs);

							int cacheRand = rand.nextInt(worldCache.size());

							msg += worldCache.get(cacheRand);
							worldCache.remove(cacheRand);
						} else
							msg += msgs.get(rand.nextInt(msgs.size()));
					} else {
						int last = broadcasterIndexes.get(world);

						if (msgs.size() < last + 1)
							last = 0;

						msg = Settings.Messages.TIMED_PREFIX + " " + msgs.get(last);

						broadcasterIndexes.put(world, last + 1);
					}

					if (msg.equals(Settings.Messages.TIMED_PREFIX + " ")) // is empty
						continue;

					if (world.equalsIgnoreCase("global")) {
						for (Player online : getOnlinePlayers()) {
							if (Settings.Messages.TIMED.keySet().contains(online.getWorld().getName()) || !Common.hasPerm(online, Permissions.VIEW_TIMED_MESSAGES))
								continue;

							Common.tell(online, msg);
						}
					} else {
						World bukkitworld = getServer().getWorld(world);

						if (bukkitworld == null)
							Common.Warn("World \"" + world + "\" doesn't exist. No timed messages broadcast.");
						else
							for (Player online : bukkitworld.getPlayers())
								if (Common.hasPerm(online, Permissions.VIEW_TIMED_MESSAGES))
									Common.tell(online, msg);
					}
				}
			}
		}.runTaskTimer(this, 20, 20 * Settings.Messages.TIMED_DELAY_SECONDS);
	}

	public boolean canSoundNotify(String pl) {
		if (!Settings.SoundNotify.ONLY_WHEN_AFK)
			return true;

		return ess.isAfk(pl);
	}

	public Player getReplyTo(Player pl) {
		if (ess == null)
			return null;

		return ess.getReplyTo(pl.getName());
	}

	public String formatPlayerVariables(Player pl, String message) {
		if (formatter == null)
			return message;

		return formatter.replacePlayerVariables(pl, message);
	}

	public boolean doesPluginExist(String pluginName) {
		Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);

		if (plugin != null && plugin.isEnabled()) {
			Common.Log("&fHooked with: " + pluginName);
			return true;
		}
		return false;
	}
	
	public VaultHook getVaultHook() {
		return vault;
	}
	
	public AuthMeHook getAuthMeHook() {
		return authMe;
	}

	// ------------------------ static ------------------------

	public static PlayerCache getDataFor(Player pl) {
		return getDataFor(pl.getName());
	}

	public static PlayerCache getDataFor(String pl) {
		PlayerCache cache = playerData.get(pl);

		if (cache == null) {
			cache = new PlayerCache();
			playerData.put(pl, cache);
		}

		return playerData.get(pl);
	}

	@SuppressWarnings("deprecation")
	public static Player[] getOnlinePlayers() {
		return Bukkit.getOnlinePlayers();
	}

	public static ChatControl instance() {
		if (instance == null) {
			instance = new ChatControl();

			Common.Warn("ChatControl instance is null! Was the plugin reloaded? Creating new.");
		}

		return instance;
	}
}
