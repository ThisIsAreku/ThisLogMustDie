package alexoft.tlmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.LogRecord;

public class TlmdFilter {
	private String expression;
	private Map<?, ?> params;
	private File output;
	private boolean logToFile = false;

	public void initialize(String expression, Map<?, ?> params) {
		this.expression = expression;
		this.params = params;

		/*Main.log(Level.WARNING, "expr : " + expression);
		for(Entry<?, ?> e: params.entrySet())
			Main.log(Level.WARNING, "\t" + e.getKey() + "=>" + e.getValue());*/
		try {
			if (params.containsKey("log-to-file")) {
				File f = new File(params.get("log-to-file").toString());
				if (!f.exists()) {
					if (f.getParentFile() != null)
						if (!f.getParentFile().exists())
							f.getParentFile().mkdirs();					
					f.createNewFile();
				}
				this.setLogFile(f);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setLogFile(File output) {
		if (output != null) {
			this.output = output;
			this.logToFile = true;
		}
	}

	public String getExpression() {
		return this.expression;
	}

	public Map<?, ?> getParams() {
		return this.params;
	}

	public void write(LogRecord record) {
		if(!this.logToFile) return;
		try {
			FileWriter fstream = new FileWriter(this.output, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(new RecordFormatter().format(record));
			out.newLine();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
