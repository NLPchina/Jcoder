//package org.nlpcn.jcoder.util.websocket;
//
//
//import java.io.IOException;
//
//import javax.websocket.MessageHandler;
//import javax.websocket.Session;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//public class WebSocketStringHandler implements MessageHandler.Whole<String> {
//	
//	private static final Logger LOG = LoggerFactory.getLogger(WebSocketConfigurator.class) ;
//    
//    
//    protected Session session;
//    
//    
//    public WebSocketStringHandler(String uu32, Session session) {
//        this.session = session;
//    }
//
//    public Session getSession() {
//        return session;
//    }
//    
//
//    public void depose() {
//    	try {
//    		LOG.info(session.getId()+" exit");
//			session.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	
//    }
//
//	@Override
//	public void onMessage(String message) {
//		LOG.info(session.getId()+" send message : "+message);
//	}
//
//}