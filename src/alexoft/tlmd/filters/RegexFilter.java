package alexoft.tlmd.filters;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class RegexFilter implements Filter  {
	private String expression;

	public RegexFilter(String expression){
		this.expression = expression;
	}
	public RegexFilter(String expression, Level l){
		this.expression = expression;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		String m = record.getMessage();
			return !m.matches(expression);
	}

}
