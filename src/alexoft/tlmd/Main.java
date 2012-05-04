package alexoft.tlmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import alexoft.commons.UpdateChecker;

public class Main extends JavaPlugin {
	public static Logger l;
	public static String p_version;
	public static String b_version;
	public MasterFilter masterFilter;

	public boolean force_filters = false;
	public long force_filters_intv = 0;
	public boolean summaryOnStart = true;
	public boolean check_plugin_updates = true;
	public boolean use_color_codes = true;

	public static void log(Level level, String m) {
		l.log(level, m);
	}

	public static void log(String m) {
		log(Level.INFO, m);
	}

	public static void logException(Exception e, String m) {
		log(Level.SEVERE, "---------------------------------------");
		log(Level.SEVERE, "--- an unexpected error has occured ---");
		log(Level.SEVERE, "-- please send line below to the dev --");
		log(Level.SEVERE, "ThisLogMustDie! version " + p_version);
		log(Level.SEVERE, "Bukkit version " + b_version);
		log(Level.SEVERE, "Message: " + m);
		log(Level.SEVERE, e.toString() + " : " + e.getLocalizedMessage());
		for (StackTraceElement t : e.getStackTrace()) {
			log(Level.SEVERE, "\t" + t.toString());
		}
		log(Level.SEVERE, "---------------------------------------");
	}

	@Override
	public void onEnable() {
		l = this.getLogger();
		b_version = this.getServer().getVersion();
		p_version = this.getDescription().getVersion();
		log("ThisIsAreku present "
				+ this.getDescription().getName().toUpperCase() + ", v"
				+ p_version);
		log("= " + this.getDescription().getWebsite() + " =");

		this.getCommand("tlmd").setExecutor(this);

		this.masterFilter = new MasterFilter(this);

		loadConfig();
		loadFilters();
		initializeMasterFilter();
		startMetrics();
		if (this.check_plugin_updates)
			startUpdate();

		if (this.force_filters)
			this.getServer().getScheduler()
					.scheduleSyncRepeatingTask(this, new Runnable() {
						@Override
						public void run() {
							initializeMasterFilter();
						}
					}, 5, this.force_filters_intv * 20);
	}

	public void Disable() {
		this.getPluginLoader().disablePlugin(this);
		this.setEnabled(false);
		return;
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
			metrics.addCustomData(new Metrics.Plotter("Filtered log messages") {

				@Override
				public int getValue() {
					return masterFilter.FilteredLogCount();
				}

			});
			metrics.addCustomData(new Metrics.Plotter("Altered log messages") {

				@Override
				public int getValue() {
					return masterFilter.AlteredLogCount();
				}

			});
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
			if (!this.getDataFolder().exists())
				this.getDataFolder().mkdirs();
			if (!file.exists())
				copy(this.getResource("filters.yml"), file);

			this.getConfig().load(file);

			List<Map<?, ?>> filtersMS = this.getConfig().getMapList("filters");

			this.masterFilter.clearFilters();
			int i = 0;
			for (Map<?, ?> m : filtersMS) {
				i++;
				if (!(m.containsKey("type") && m.containsKey("expression"))) {
					log("Filter no." + i + " ignored");
					continue;
				}
				String type = m.get("type").toString();
				String expression = m.get("expression").toString();
				try {
					TlmdFilter filter = (TlmdFilter) Class.forName(
							"alexoft.tlmd.filters." + type).newInstance();
					if (filter.initialize(expression, m)) {
						if (this.summaryOnStart)
							log("Filter #" + i + " (" + type + ") initialized");
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
			return;
		} catch (IOException e) {
			logException(e, "Cannot create a default filters...");
			this.Disable();
			return;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			log("Fill filters before !");
			this.Disable();
			return;
		}
	}

	public void loadConfig() {
		try {
			File file = new File(this.getDataFolder(), "config.yml");
			if (!this.getDataFolder().exists())
				this.getDataFolder().mkdirs();
			if (!file.exists())
				copy(this.getResource("config.yml"), file);

			this.getConfig().load(file);

			YamlConfiguration defaults = new YamlConfiguration();
			defaults.load(this.getResource("config.yml"));
			this.getConfig().addDefaults(defaults);
			this.getConfig().options().copyDefaults(true);

			this.force_filters = this.getConfig().getBoolean(
					"force-filter.enable");
			this.force_filters_intv = this.getConfig().getLong(
					"force-filter.interval");
			this.summaryOnStart = this.getConfig().getBoolean(
					"summary-on-start");
			this.check_plugin_updates = this.getConfig().getBoolean(
					"check-plugin-updates");			

			this.use_color_codes = this.getConfig().getBoolean(
					"use-color-codes");

			this.getConfig().save(file);
		} catch (FileNotFoundException e) {
			log("Cannot found the filter...");
			this.Disable();
			return;
		} catch (IOException e) {
			logException(e, "Cannot create a default filters...");
			this.Disable();
			return;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			log("Fill filters before !");
			this.Disable();
			return;
		}
	}

	private void copy(InputStream src, File dst) throws IOException {
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = src.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		src.close();
		out.close();
	}

}
