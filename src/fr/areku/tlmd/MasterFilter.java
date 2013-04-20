package fr.areku.tlmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.ChatColor;

public class MasterFilter implements Filter {
	private File separateLogDir = null;
	private List<Filter> filters;
	private int filteredLog = 0;
	private int alteredLog = 0;
	private boolean initialized = false;

	public MasterFilter() {
		this.filters = new ArrayList<Filter>();
	}

	public void addFilter(Filter filter) {
		((TlmdFilter) filter).setParent(this);
		this.filters.add(filter);
	}

	public Integer filterCount() {
		return this.filters.size();
	}

	public void clearFilters() {
		for (Filter f : this.filters) {
			((TlmdFilter) f).disableFilter();
		}
		this.filters.clear();
	}

	public int AlteredLogCount() {
		int f = this.alteredLog;
		this.alteredLog = 0;
		return f;
	}

	public int FilteredLogCount() {
		int f = this.filteredLog;
		this.filteredLog = 0;
		return f;
	}

	public void incrementAlteredLogCount() {
		this.alteredLog++;
	}

	public void incrementFilteredLogCount() {
		this.filteredLog++;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		try {
			// int i = 1;
			for (Filter f : this.filters) {
				if (((TlmdFilter) f).isDisabled()) {
					// writelog("Filter #" + i + " is disabled");
					continue;
				}
				if (!f.isLoggable(record)) {
					// writelog("Filter #" + i + " return false");
					return false;
				}
				// i++;
			}
			if (Config.separate_logs) {
				if (Main.IGNORED_LOGGER.equals(record.getLoggerName()))
					return true;

				doLogSeparation(record);
				if (Config.remove_from_main_file) {
					if (!initialized) {
						String m = "ThisLogMustDie takes the reins of the log file :)";
						if (Config.use_color_codes) {
							m = ColorConverter
									.getColorFromChatColor(ChatColor.RED)
									+ m
									+ ColorConverter
											.getColorFromChatColor(ChatColor.RESET);
						}
						Main.getIgnoredLogger().log(
								new LogRecord(Level.INFO, m));
						initialized = true;
					}
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			writelog(e.getMessage());
			return true;
		}
	}

	public void writelog(String message) {
		try {
			FileWriter fstream = new FileWriter(new File(
					Main.instance.getDataFolder(), "tlmd.log"), true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(message);
			out.newLine();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doLogSeparation(LogRecord record) {
		try {
			if (separateLogDir == null)
				separateLogDir = new File(Config.separate_logs_dir);
			if (!separateLogDir.exists())
				separateLogDir.mkdirs();

			String logName = record.getLoggerName();
			if ("".equals(logName)) {
				logName = "unknown";
			}

			File logFile = new File(separateLogDir, logName + ".log");
			if (!logFile.exists())
				logFile.createNewFile();

			FileWriter fstream = new FileWriter(logFile, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(new RecordFormatter(Config.remove_colors_on_file)
					.format(record));
			out.newLine();
			out.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}

	}

}