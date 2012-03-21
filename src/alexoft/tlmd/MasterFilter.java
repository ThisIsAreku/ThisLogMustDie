package alexoft.tlmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class MasterFilter implements Filter {
	private List<Filter> filters;
	private Main parent;

	public MasterFilter(Main parent) {
		this.parent = parent;
		this.filters = new ArrayList<Filter>();
	}

	public void addFilter(Filter filter) {
		((TlmdFilter)filter).setParent(this);
		this.filters.add(filter);
	}

	public Integer filterCount() {
		return this.filters.size();
	}
	public void clearFilters(){
		for (Filter f : this.filters) {
			((TlmdFilter)f).disableFilter();
		}
		this.filters.clear();
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		try {
			//int i = 1;
			for (Filter f : this.filters) {
				if(((TlmdFilter)f).isDisabled()) {
					//writelog("Filter #" + i + " is disabled");
					continue;
				}
				if (!f.isLoggable(record)) {
					//writelog("Filter #" + i + " return false");
					return false;
				}
				//i++;
			}
			return true;
		} catch (Exception e) {
			writelog(e.getMessage());
			return true;
		}
	}
	public void writelog(String message) {
		try {
			FileWriter fstream = new FileWriter(new File(this.parent.getDataFolder(), "tlmd.log"), true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(message);
			out.newLine();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}