import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.util.StaticValue;

import com.sun.javafx.collections.MappingChange.Map;

public class Test {
	
	

	private static final Logger LOG = Logger.getLogger(StaticValue.class);


	public static void main(String[] args) throws ParseException, IOException {
		  
		ProcessBuilder pb = new ProcessBuilder("mvn" ,"-f","pom.xml","dependency:copy-dependencies");
		
		
		pb.directory(new File("/Users/sunjian/.jcoder/lib"));

		pb.redirectErrorStream(true);

		/* Start the process */
		Process proc = pb.start();

		/* Read the process's output */
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		while ((line = in.readLine()) != null) {
			LOG.info(line);
		}

		/* Clean-up */
		proc.destroy();

	}

}
