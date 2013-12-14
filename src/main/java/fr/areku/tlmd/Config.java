package fr.areku.tlmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	public static boolean force_filters;
	public static long force_filters_intv;
	public static boolean summaryOnStart;
	public static boolean check_plugin_updates;
	public static boolean separate_logs;
	public static String separate_logs_dir;
	public static boolean remove_colors_on_file;
	public static boolean remove_from_main_file;
	public static boolean use_color_codes;
	
	public static void loadConfig() {
		try {
			File file = new File(Main.instance.getDataFolder(), "config.yml");
			if (!Main.instance.getDataFolder().exists()) {
				Main.instance.getDataFolder().mkdirs();
			}
			if (!file.exists()) {
				copy(Main.instance.getResource("config.yml"), file);
			}

			Main.instance.getConfig().load(file);

			YamlConfiguration defaults = new YamlConfiguration();
			defaults.load(Main.instance.getResource("config.yml"));
			Main.instance.getConfig().addDefaults(defaults);
			Main.instance.getConfig().options().copyDefaults(true);

			Config.force_filters = Main.instance.getConfig().getBoolean(
					"force-filter.enable");
			Config.force_filters_intv = Main.instance.getConfig().getLong(
					"force-filter.interval");
			Config.summaryOnStart = Main.instance.getConfig().getBoolean(
					"summary-on-start");
			Config.check_plugin_updates = Main.instance.getConfig().getBoolean(
					"check-plugin-updates");

			Config.separate_logs = Main.instance.getConfig().getBoolean(
					"separate-logs.enable");
			Config.separate_logs_dir = Main.instance.getConfig().getString(
					"separate-logs.directory");
			Config.remove_colors_on_file = Main.instance.getConfig().getBoolean(
					"separate-logs.remove-colors-on-file");
			Config.remove_from_main_file = Main.instance.getConfig().getBoolean(
					"separate-logs.remove-from-main-file");

			Config.use_color_codes = Main.instance.getConfig().getBoolean(
					"use-color-codes");

			/*
			 * try{ if (!((CraftServer)
			 * getServer()).getReader().getTerminal().isAnsiSupported()) { if
			 * (this.use_color_codes) { log(Level.WARNING,
			 * "Color codes may not be supported by your system"); } }
			 * }catch(Exception e){ //silent fail, when launched headless }
			 */

			Main.instance.getConfig().save(file);
		} catch (FileNotFoundException e) {
			Main.log("Cannot found the filter...");
			Main.instance.Disable();
		} catch (IOException e) {
			Main.logException(e, "Cannot create a default filters...");
			Main.instance.Disable();
		} catch (InvalidConfigurationException e) {
			Main.logException(e, "Config error");
			Main.instance.Disable();
		}
	}

	public static void copy(InputStream src, File dst) throws IOException {
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
