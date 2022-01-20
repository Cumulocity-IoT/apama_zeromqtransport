<img align="right"  src="apama.jpg">

# Apama ZeroMQ Connectivity Plug-in 

This Apama Connectivity Plug-in can be used to receive (subscribe) to [ZeroMQ](https://zeromq.org/) payload (binary) and map it to an Apama Events Type. This Plug-In can also send (publish) events to [ZeroMQ](https://zeromq.org/). Apama 10.0+ versions are supported.
______________________

These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.

## Prerequisits

- Apama Installation. Get the Apama Community Edition [here](https://www.apamacommunity.com/downloads/)
- Java SDK >= 1.8
- Apache Maven

## Build

Fetch  connectivity-plugins-api.jar from your Software AG Apama install directory (e.g. C:\Softwareag\Apama\lib) and install it as a Maven artifacts into local Maven repo:

	mvn install:install-file -Dfile=connectivity-plugins-api.jar -DgroupId=com.softwareag -DartifactId=connectivity-plugins-api -Dversion=10.5 -Dpackaging=jar
	
	mvn assembly:single
	
this should create the zmqclientplugin.jar in the targets folder of this project.

## Configuration 

The CorrelatorConfig.yaml file contains the configuration for different components: 

### CorrelatorConfig.yaml


All correlator specific parameters go here. E.g. which monitors should be injected:

	correlator:
     initialization:
        list:
            - ${APAMA_HOME}/monitors/ConnectivityPluginsControl.mon
            - ${APAMA_HOME}/monitors/ConnectivityPlugins.mon
            - ${PARENT_DIR}/zeromq.mon
        encoding: UTF8

Change log levels via:

	correlatorLogging:
	  .root:
	    level: INFO
	  apama.connectivity: DEBUG
	  connectivity.ZeroMQTransport.zmq: DEBUG
  
Which plug-ins should be loaded and where to find them:

	connectivityPlugins:
	  ZeroMQTransport:
	    directory: ${PARENT_DIR}/target/
	    classpath: zmqtransport.jar
	    class: com.softwareag.zmqtransport.ZMQClientPlugin
	    
	  jsonCodec:
	    directory: ${APAMA_HOME}/lib/
	    classpath:
	      - json-codec.jar
	    class: com.softwareag.connectivity.plugins.JSONCodec
	    
	  stringCodec:
	    libraryName: connectivity-string-codec
	    class: StringCodec
	    
	  diagnosticCodec:
	    libraryName: DiagnosticCodec
	    class: DiagnosticCodec

Chain definition.
To send events to the plug in use the chain name (in this case zmq) as channel. The plugin can be configured to subscribe and to publish events via ZeroMQ. The plug-in uses the pub/sub functionality of ZeroMQ which includes a topic prefix before each payload. In order to do that you need to configure the host port and topic. To map the incoming payload to a specific EventType subEventType can be should be used. 
	 
	startChains:
	  zmq:
	    - apama.eventMap:
	        allowMissing: true 
	    - jsonCodec 
	    - stringCodec:
	        nullTerminated: true 
	    - ZeroMQTransport:
	        subPort: ${zmqclientplugin.subPort}
	        subHost: ${zmqclientplugin.subHost}
	        subTopic: ${zmqclientplugin.subTopic}
	        subEventType: ${zmqclientplugin.subEventType}
	        pubPort: ${zmqclientplugin.pubPort}
	        pubHost: ${zmqclientplugin.pubHost}
	        pubTopic: ${zmqclientplugin.pubTopic}	
			  
### zmqclientplugin.properties

This config file contains mainly the Zeromq connection parameters.

	zmqclientplugin.subPort=7777
	zmqclientplugin.subHost=localhost
	zmqclientplugin.subTopic=weather
	zmqclientplugin.subEventType=Weather
	
	zmqclientplugin.pubPort=8888
	zmqclientplugin.pubHost=localhost
	zmqclientplugin.pubTopic=answer
	
### zeromq.mon

Is an example Monitor to test the ZeroMQ connection and the proper function of the plug in. It will receive weather events form the plugin chain and send it back to the transport with attaching "_ANSWER" to the message_id field.

## Test/Run

To test the plugin two Python scripts are included which act as publisher and subscriber. After those sripts are started start the correlator to have a event round trip 

	publisher.py -> apama_plugin_subscriber -> apama_plugin_publisher -> subscriber.py

The Python publisher.py will generate events with a JSON payload (Weather Data) and publish it as binary to the weather topic. Apama will subscribe to the weather topic parse the events using the string- and JSON-Codec to decode the payload.

### Start Python ZMQ Scripts

Apama comes with a Python3 distribution located under %InstallDir%/Apama/third_party/python. You can use that to start the scripts. One dependency is the zmq module. You can install it by 
	
	pip install zmq 
	
then start each script by:

	python subscriber.py
	python publisher.py


### Start the correlator

Open the Apama Command Prompt go to the same dir as this readme and execute:

	correlator --config CorrelatorConfig.yaml --config zmqclientplugin.properties
	
Now you should see the Events form the publisher logged into the correlator.log and the answers from Apama in the subscriber.py log.

Happy ZeroMQing :-) 

