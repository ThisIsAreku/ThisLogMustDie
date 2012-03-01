package alexoft.tlmd.filters;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LoggerFilter implements Filter  {
	private String expression;
	private boolean caseSensitive;

	public LoggerFilter(String expression, boolean caseSensitive){
		this.expression = expression;
		this.caseSensitive = caseSensitive;
	}
	public LoggerFilter(String expression, boolean caseSensitive, Level l){
		this.expression = expression;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		String m = record.getLoggerName();
		if(this.caseSensitive){
			return !m.equals(this.expression);
		}else{
			return !m.equalsIgnoreCase(this.expression);			
		}
	}

}
