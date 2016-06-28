import org.apache.log4j.Logger;
import org.nlpcn.jcoder.util.StaticValue;

public class TestLoggerFormat {

	private static final Logger LOG = Logger.getLogger(StaticValue.class);

	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			LOG.info("message:" + i);
		}
	}
}
