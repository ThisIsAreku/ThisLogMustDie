package alexoft.tlmd.filters;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import alexoft.tlmd.TlmdFilter;

public class LevelFilter extends TlmdFilter  implements Filter  {
	@Override
	public boolean isLoggable(LogRecord record) {
		Level m = record.getLevel();
		if(m.toString().equalsIgnoreCase(this.getExpression())){
			this.write(record);
			this.getParent().incrementFilteredLogCount();
			return false;
		}
		return true;
	}

}
