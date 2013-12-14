package fr.areku.tlmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.areku.commons.UpdateChecker;

public class Main extends JavaPlugin {

	public final static String IGNORED_LOGGER = "passthru";
	public static Main instance;
	public MasterFilter masterFilter;
	public Map<String, Integer> filterCountMap = new HashMap<String, Integer>();

	public static void log(Level level, String m) {
		instance.getLogger().log(level, m);
	}

	public static void log(String m) {
		log(Level.INFO, m);
	}

	public static void logException(Exception e, String m) {
		log(Level.SEVERE, "---------------------------------------");
		log(Level.SEVERE, "--- an unexpected error has occured ---");
		log(Level.SEVERE, "-- please send line below to the dev --");
		log(Level.SEVERE, "ThisLogMustDie! version "
				+ instance.getDescription().getVersion());
		log(Level.SEVERE, "Bukkit version " + Bukkit.getServer().getVersion());
		log(Level.SEVERE, "Message: " + m);
		log(Level.SEVERE, e.toString() + " : " + e.getLocalizedMessage());
		for (StackTraceElement t : e.getStackTrace()) {
			log(Level.SEVERE, "\t" + t.toString());
		}
		log(Level.SEVERE, "---------------------------------------");
	}

	public static final Logger getIgnoredLogger() {
		return Logger.getLogger(IGNORED_LOGGER);
	}

	@Override
	public void onLoad() {
		instance = this;
		log("ThisIsAreku present "
				+ this.getDescription().getName().toUpperCase() + ", v"
				+ getDescription().getVersion());
		log("= " + this.getDescription().getWebsite() + " =");
		this.masterFilter = new MasterFilter();

		ColorConverter.initColorConverter();
		Config.loadConfig();
		loadFilters();
	}

	@Override
	public void onEnable() {
		this.getCommand("tlmd").setExecutor(this);
		startMetrics();
		if (Config.check_plugin_updates) {
			startUpdate();
		}

		initializeMasterFilter();

		if (Config.force_filters) {
			Bukkit.getServer().getScheduler()
					.scheduleSyncRepeatingTask(this, new Runnable() {

						@Override
						public void run() {
							initializeMasterFilter();
						}
					}, 5, Config.force_filters_intv * 20);
		}

	}

	public void Disable() {
		Bukkit.getScheduler().cancelTasks(this);
		try {
			this.getPluginLoader().disablePlugin(this);
			this.setEnabled(false);
		} catch (Exception e) {

		}
	}

	@Override
	public boolean onCommand(CommandSender cs, Command command, String label,
			String[] args) {

		if (!cs.isOp()) {
			sendMessage(cs, ChatColor.RED
					+ "You must be an OP to reload filters");
			return true;
		}
		if ((args.length == 0) || (args.length == 1 && "help".equals(args[0]))) {
			sendMessage(cs, ChatColor.YELLOW + "Usage: /tlmd reload");
			return true;
		}

		if ("reload".equals(args[0])) {
			initializeMasterFilter();
			loadFilters();
			sendMessage(cs, ChatColor.GREEN
					+ (this.masterFilter.filterCount() + " filter(s) loaded"));
			return true;
		}
		return super.onCommand(cs, command, label, args);
	}

	public void sendMessage(CommandSender cs, String m) {
		cs.sendMessage("[" + this.getName() + "] " + m);
	}

	public void startMetrics() {

		try {
			log("Starting Metrics");
			Metrics metrics = new Metrics(this);
			Metrics.Graph fitersCount = metrics
					.createGraph("Number of filters");
			for (final String name : filterCountMap.keySet()) {
				fitersCount.addPlotter(new Metrics.Plotter(name) {

					@Override
					public int getValue() {
						// System.out.println("metrics:"+name+":"+filterCountMap.get(name));
						return filterCountMap.get(name);
					}

				});
			}
			metrics.start();
		} catch (IOException e) {
			log("Cannot start Metrics...");
		}
	}

	public void startUpdate() {
		try {
			UpdateChecker update = new UpdateChecker(this);
			update.start();
		} catch (MalformedURLException e) {
			log("Cannot start Plugin Updater...");
		}
	}

	public void initializeMasterFilter() {
		String pname = "";
		try {
			for (Plugin p : this.getServer().getPluginManager().getPlugins()) {
				pname = p.toString();
				p.getLogger().setFilter(this.masterFilter);
				// i++;
			}
			this.getServer().getLogger().setFilter(masterFilter);
			Bukkit.getLogger().setFilter(masterFilter);
			Logger.getLogger("Minecraft").setFilter(masterFilter);
		} catch (Exception e) {
			log(Level.INFO, "Cannot load filter in '" + pname
					+ "'. Retrying later..");
		}
		this.getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new Runnable() {

					@Override
					public void run() {
						String pname = "";
						try {
							for (Plugin p : getServer().getPluginManager()
									.getPlugins()) {
								pname = p.toString();
								p.getLogger().setFilter(masterFilter);
							}
							getServer().getLogger().setFilter(masterFilter);
							Bukkit.getLogger().setFilter(masterFilter);
							Logger.getLogger("Minecraft").setFilter(
									masterFilter);
						} catch (Exception e) {
							log(Level.WARNING,
									"Cannot load filter in '"
											+ pname
											+ "'. The logs of this plugin will not be filtered");
						}

					}
				}, 1);
	}

	public void loadFilters() {
		try {
			File file = new File(this.getDataFolder(), "filters.yml");
			if (!this.getDataFolder().exists()) {
				this.getDataFolder().mkdirs();
			}
			if (!file.exists()) {
				Config.copy(this.getResource("filters.yml"), file);
			}

			this.getConfig().load(file);

			List<Map<?, ?>> filtersMS = this.getConfig().getMapList("filters");

			this.masterFilter.clearFilters();
			this.filterCountMap.clear();
			int i = 0;
			for (Map<?, ?> m : filtersMS) {
				i++;
				if (!(m.containsKey("type") && m.containsKey("expression"))) {
					log("Filter no." + i + " ignored");
					continue;
				}
				String type = bugfix_type(m.get("type").toString()); // fix
																		// after
																		// refatorization
				String expression = m.get("expression").toString();
				try {
					TlmdFilter filter = (TlmdFilter) Class.forName(
							"fr.areku.tlmd.filters." + type).newInstance();
					if (filter.initialize(expression, m)) {
						if (Config.summaryOnStart) {
							log("Filter #" + i + " (" + type + ") initialized");
						}
						incrementFilterCount(type);
						this.masterFilter.addFilter((Filter) filter);
					} else {
						log("Configuration of filter #" + i + " is incorrect");
					}
				} catch (ClassNotFoundException e) {
					log("Filter #" + i + " has incorrect type !");
				} catch (Exception e) {
					logException(e, "Filter type:" + type);
				}
			}
			log(this.masterFilter.filterCount() + " filter(s) loaded");
		} catch (FileNotFoundException e) {
			log("Cannot found the filter...");
			this.Disable();
		} catch (IOException e) {
			logException(e, "Cannot create a default filters...");
			this.Disable();
		} catch (InvalidConfigurationException e) {
			logException(e, "Fill filters before !");
			this.Disable();
		}
	}

	private void incrementFilterCount(String name) {
		if (!filterCountMap.containsKey(name))
			filterCountMap.put(name, 0);
		filterCountMap.put(name, filterCountMap.get(name) + 1);
		// System.out.println("incrementFilterCount:"+name);
	}

	private String bugfix_type(String t) {
		if ("AlterateFilter".equals(t))
			return "AlterationFilter";
		return t;
	}
}
