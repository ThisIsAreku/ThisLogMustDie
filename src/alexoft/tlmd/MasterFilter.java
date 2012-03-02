package alexoft.tlmd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class MasterFilter implements Filter {
	private List<Filter> filters;

	public MasterFilter() {
		this.filters = new ArrayList<Filter>();
	}

	public void addFilter(Filter filter) {
		this.filters.add(filter);
	}

	public Integer filterCount() {
		return this.filters.size();
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		try {
			for (Filter f : this.filters) {
				if (!f.isLoggable(record)) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			write(e.getMessage());
			return true;
		}
	}

	public static void write(String message) {
		try {
			FileWriter fstream = new FileWriter("tlmd.log", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(message);
			out.newLine();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}