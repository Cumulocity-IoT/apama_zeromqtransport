package com.softwareag.zmqtransport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.softwareag.connectivity.AbstractTransport;
import com.softwareag.connectivity.MapHelper;
import com.softwareag.connectivity.Message;
import com.softwareag.connectivity.PluginConstructorParameters.TransportConstructorParameters;


public class ZMQClientPlugin extends AbstractTransport{

	
	private int port;
	private String host;
	private String topic;
	private Thread clientThread;
	
	
	public ZMQClientPlugin(Logger logger, TransportConstructorParameters params) throws IllegalArgumentException, Exception {
		super(logger, params);
		port = (int)MapHelper.getInteger(config, "port");
		host = (String)MapHelper.getString(config, "host");
		topic = (String)MapHelper.getString(config, "topic");

	}

	public void sendBatchTowardsTransport(List<Message> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hostReady() throws IOException {
		clientThread = new Thread(){
		    public void run(){
		    	// Prepare our context and subscriber
		        Context context = ZMQ.context(1);
		    	Socket subscriber = context.socket(ZMQ.SUB);
		
		    	subscriber.connect("tcp://" + host + ":" + port);
		    	String [] topics = topic.split(",");
		    	logger.info("Starting ZMQ Subscriber on port " + port + " for topics " + Arrays.toString(topics));
		    	for (int i = 0; i < topics.length; i++) {
		    		subscriber.subscribe(topics[i].getBytes());
				}
		    	subscriber.subscribe(topic.getBytes());
		    	
		        while (!Thread.currentThread().isInterrupted()) {
		        	String topic = subscriber.recvStr ();
		            byte[] zmqByte = subscriber.recv();
		            logger.debug("Received Topic: " + topic +" Bytes: " + new String (zmqByte));
		            Message m = new Message(zmqByte);
		            synchronized(hostSide) {
						hostSide.sendBatchTowardsHost(Collections.singletonList(m));
					}
		        }
		        
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
