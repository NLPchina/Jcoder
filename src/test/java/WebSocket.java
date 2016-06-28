import java.io.IOException;
import java.util.logging.Logger;

import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;

@ManagedService(path = "/chat")
public class WebSocket {

	private Logger logger = Logger.getLogger("websocket");

	@Ready
	public void onReady(final AtmosphereResource r) {
		logger.info("Browser " + r.uuid() + " connected.");
	}

	@Disconnect
	public void onDisconnect(AtmosphereResourceEvent event) {
		if (event.isCancelled()) {
			logger.info("Browser " + event.getResource().uuid() + " unexpectedly disconnected");
		} else if (event.isClosedByClient()) {
			logger.info("Browser " + event.getResource().uuid() + " closed the connection");
		}
	}

	@org.atmosphere.config.service.Message(encoders = { StringEncoder.class }, decoders = { StringDecoder.class })
	public Message onMessage(Message message) throws IOException {
		logger.info("recived "+message.toString());
		return message;
	}

}

class StringEncoder implements Encoder<String, String> {

	@Override
	public String encode(String s) {
		return null;
	}
}

class StringDecoder implements Decoder<String, String> {

	@Override
	public String decode(String s) {
		// TODO Auto-generated method stub
		return s;
	}
}
