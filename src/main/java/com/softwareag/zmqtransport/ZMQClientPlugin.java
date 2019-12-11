package com.softwareag.zmqtransport;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.softwareag.connectivity.AbstractTransport;
import com.softwareag.connectivity.Message;
import com.softwareag.connectivity.PluginConstructorParameters.TransportConstructorParameters;
import com.softwareag.connectivity.util.MapExtractor;


public class ZMQClientPlugin extends AbstractTransport{

	
	final private int SUB_PORT;
	final private int PUB_PORT;
	
	final private String SUB_HOST;
	final private String PUB_HOST;
	
	final private  String SUB_TOPIC;
	final private  String PUB_TOPIC;
	
	final private  String SUB_EVENTTYPE;
	
	private Thread clientThread;
	private ZContext context;
	private Socket publisher;
	
	public ZMQClientPlugin(Logger logger, TransportConstructorParameters params) throws IllegalArgumentException, Exception {
		super(logger, params);
		MapExtractor config = new MapExtractor(params.getConfig(), "configuration");
		SUB_PORT = Integer.valueOf(config.getStringDisallowEmpty("subPort"));
		PUB_PORT = Integer.valueOf(config.getStringDisallowEmpty("pubPort"));
		
		SUB_HOST = config.getStringDisallowEmpty("subHost");
		PUB_HOST = config.getStringDisallowEmpty("pubHost");
		
		SUB_TOPIC = config.getStringDisallowEmpty("subTopic");
		PUB_TOPIC = config.getStringDisallowEmpty("pubTopic");
		SUB_EVENTTYPE = config.getStringDisallowEmpty("subEventType");
		
		
	}

	public void sendBatchTowardsTransport(List<Message> arg0) {
		for (Message message : arg0) {
			logger.info("Sending: " + new String((byte[]) message.getPayload()) + " To Topic: " + PUB_TOPIC);
			publisher.send(PUB_TOPIC.getBytes(),ZMQ.SNDMORE);
			publisher.send((byte[])message.getPayload());
		}
		
	}

	@Override
	public void hostReady() throws IOException {
		
		context = new ZContext();
		publisher = context.createSocket(SocketType.PUB);
		publisher.bind("tcp://" + PUB_HOST + ":" + PUB_PORT);
		
		clientThread = new Thread(){
		    public void run(){
		    	// Prepare our context and subscriber
		        
		        Socket subscriber = context.createSocket(SocketType.SUB);
		    	
		        subscriber.connect("tcp://" + SUB_HOST + ":" + SUB_PORT);
		        
		    	String [] topics = SUB_TOPIC.split(",");
		    	logger.info("Starting ZMQ Subscriber on port " + SUB_PORT + " for topics " + Arrays.toString(topics));
		    	for (int i = 0; i < topics.length; i++) {
		    		subscriber.subscribe(topics[i].getBytes());
				}

		        while (!Thread.currentThread().isInterrupted()) {
		        	String topic = subscriber.recvStr();
		            byte[] zmqByte = subscriber.recv();
		            logger.debug("Received Topic: " + topic +" Bytes: " + new String (zmqByte));
		            Message m = new Message(zmqByte);
		            m.putMetadataValue("sag.type", SUB_EVENTTYPE);
		            synchronized(hostSide) {
						hostSide.sendBatchTowardsHost(Collections.singletonList(m));
					}
		        }
		        subscriber.close();
		    }
		  };

		  clientThread.start();
		  
		  
		
	}
	

	@Override
	public void shutdown() throws Exception {
		logger.info("Shutdown ZMQ Server ");
		clientThread.interrupt();
	}
	
	
	
	
}
