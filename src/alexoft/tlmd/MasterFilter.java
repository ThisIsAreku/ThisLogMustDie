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
		for (Filter f : this.filters) {
			if (!f.isLoggable(record)) {
				return false;
			}
		}
		return true;
	}

	public void r(String m) {
		try {
			// Create file
			FileWriter fstream = new FileWriter("out.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(m+"\r\n");
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
}