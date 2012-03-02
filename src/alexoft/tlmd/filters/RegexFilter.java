package alexoft.tlmd.filters;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import alexoft.tlmd.TlmdFilter;

public class RegexFilter extends TlmdFilter  implements Filter  {

	@Override
	public boolean isLoggable(LogRecord record) {
		String m = record.getMessage();
		if(m.matches(this.getExpression())){
			this.write(record);
			return false;
		}
		return true;
	}

}
