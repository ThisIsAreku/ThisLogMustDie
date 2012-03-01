package alexoft.tlmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import alexoft.tlmd.filters.ExactFilter;
import alexoft.tlmd.filters.LevelFilter;
import alexoft.tlmd.filters.LoggerFilter;
import alexoft.tlmd.filters.RegexFilter;

public class Main extends JavaPlugin {
	public static Logger l;
	public static String p_version;
	public static String b_version;
	public MasterFilter masterFilter;

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
		if (e instanceof SQLException) {
			log(Level.SEVERE, "SQLState: " + ((SQLException) e).getSQLState());
			log(Level.SEVERE,
					"Error Code: " + ((SQLException) e).getErrorCode());
		}
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
		this.masterFilter = new MasterFilter();
		loadConfig();
		loadMasterFilter();
	}

	public void Disable() {
		this.getPluginLoader().disablePlugin(this);
		this.setEnabled(false);
		return;
	}
	
	public void loadMasterFilter(){
		for(Plugin p : this.getServer().getPluginManager().getPlugins()){
			p.getLogger().setFilter(this.masterFilter);
		}
		this.getServer().getLogger().setFilter(this.masterFilter);
	}

	public void loadConfig() {
		try {
			File file = new File(this.getDataFolder(), "filters.yml");
			if (!this.getDataFolder().exists())
				this.getDataFolder().mkdirs();
			if (!file.exists())
				copy(this.getResource("filters.yml"),file);

			this.getConfig().load(file);

			List<Map<?,?>> filtersMS = this.getConfig().getMapList(
					"filters");
			int i=0;
			for (Map<?, ?> m : filtersMS) {
				i++;
				if(!(m.containsKey("type") && m.containsKey("expression"))) {
					log("Filter no." + i + " ignored");
					continue;
				}
				/*for(Entry<String,Object> e : m.entrySet()){
					log(e.getKey() + "=>" + e.getValue());
				}*/
				String type = m.get("type").toString();
				String expression = m.get("expression").toString();
				if ("ExactFilter".equalsIgnoreCase(type)) {
					boolean caseSensitive = false;
					if(m.containsKey("case-sensitive")){
						caseSensitive = Boolean.parseBoolean(m.get("case-sensitive").toString());
					}
					this.masterFilter.addFilter(new ExactFilter(expression, caseSensitive));
					
				} else if ("RegexFilter".equalsIgnoreCase(type)) {
					this.masterFilter.addFilter(new RegexFilter(expression));
					
				} else if ("LevelFilter".equalsIgnoreCase(type)) {
					this.masterFilter.addFilter(new LevelFilter(expression));

				} else if ("LoggerFilter".equalsIgnoreCase(type)) {
					boolean caseSensitive = false;
					if(m.containsKey("case-sensitive")){
						caseSensitive = Boolean.parseBoolean(m.get("case-sensitive").toString());
					}
					this.masterFilter.addFilter(new LoggerFilter(expression, caseSensitive));

				}else{
					log("Filter no." + i + " has incorrect type");
				}
			}
			log(this.masterFilter.filterCount() + " filter(s) loaded");
		} catch (FileNotFoundException e) {
			log("Cannot found the config...");
			this.Disable();
			return;
		} catch (IOException e) {
			log("Cannot create a default config...");
			this.Disable();
			return;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			log("Fill config before !");
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
