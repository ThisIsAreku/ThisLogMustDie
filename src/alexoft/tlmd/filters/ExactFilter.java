package alexoft.tlmd.filters;

import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import alexoft.tlmd.TlmdFilter;

public class ExactFilter extends TlmdFilter implements Filter  {
	private boolean caseSensitive;

	@Override
	public void initialize(String expression, Map<?, ?> params) {
		super.initialize(expression, params);
		if(this.getParams().containsKey("case-sensitive")){
			caseSensitive = Boolean.parseBoolean(this.getParams().get("case-sensitive").toString());
		}
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		String m = record.getMessage().trim();
		if(this.caseSensitive){
			if(m.equals(this.getExpression())){
				this.write(record);
				return false;
			}
		}else{
			if(m.equalsIgnoreCase(this.getExpression())){
				this.write(record);
				return false;
			}		
		}
		return true;
	}


}
