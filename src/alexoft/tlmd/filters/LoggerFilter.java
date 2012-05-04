package alexoft.tlmd.filters;

import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import alexoft.tlmd.TlmdFilter;

public class LoggerFilter extends TlmdFilter  implements Filter  {
	private boolean caseSensitive;

	@Override
	public boolean initialize(String expression, Map<?, ?> params) {
		if(!super.initialize(expression, params)) return false;
		if(this.getParams().containsKey("case-sensitive")){
			caseSensitive = Boolean.parseBoolean(this.getParams().get("case-sensitive").toString());
		}
		return true;
	}
	
	@Override
	public boolean isLoggable(LogRecord record) {
		String m = record.getLoggerName();
		if(this.caseSensitive){
			if(m.equals(this.getExpression())){
				this.write(record);
				this.getParent().incrementFilteredLogCount();
				return false;
			}
		}else{
			if(m.equalsIgnoreCase(this.getExpression())){
				this.write(record);
				this.getParent().incrementFilteredLogCount();
				return false;
			}		
		}
		return true;
	}

}
