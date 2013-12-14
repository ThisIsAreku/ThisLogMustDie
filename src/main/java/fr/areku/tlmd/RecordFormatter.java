package fr.areku.tlmd;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class RecordFormatter extends SimpleFormatter {
	private boolean trimColorCodes = false;

	public RecordFormatter() {
		super();
	}

	public RecordFormatter(boolean trimColorCodes) {
		super();
		this.trimColorCodes = trimColorCodes;
	}

	@Override
	public synchronized String format(LogRecord record) {
		StringBuilder b = new StringBuilder();
		b.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date(
				record.getMillis())));
		b.append("[" + record.getLevel().toString().toUpperCase() + "] ");
		if (this.trimColorCodes) {
			b.append(ColorConverter.StripAllColor(record.getMessage()));
		} else {
			b.append(record.getMessage());
		}
		return b.toString();
	}

}
