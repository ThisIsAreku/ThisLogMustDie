package alexoft.tlmd.filters;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LevelFilter implements Filter  {
	private String expression;

	public LevelFilter(String expression){
		this.expression = expression;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		Level m = record.getLevel();
		return !m.toString().equalsIgnoreCase(this.expression);
	}

}
