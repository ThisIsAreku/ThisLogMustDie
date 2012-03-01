package alexoft.tlmd;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class MasterFilter implements Filter {
	private List<Filter> filters;
	
	public MasterFilter(){
		this.filters = new ArrayList<Filter>();
	}
	public void addFilter(Filter filter){
		this.filters.add(filter);
	}
	public Integer filterCount(){
		return this.filters.size();
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		for(Filter f:this.filters){
			if(!f.isLoggable(record)){
				return false;
			}
		}
		return true;
	}

}
