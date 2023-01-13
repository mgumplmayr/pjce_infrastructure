package at.jku.itproj;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.springframework.stereotype.Service;

@Service("MQTTDelegate")
public class MQTTDelegate implements JavaDelegate {
    private BlockingConnection connection;
    private String message;
    private String topic;

            @Override
            public void execute(final DelegateExecution execution) throws Exception {
                System.out.println("Starting MQTTDelegate");
                topic = execution.getCurrentActivityName();
                final Object username = execution.getVariable("username");
                message = "Hello, my name is: " + username +" and my process ID is: "+execution.getProcessInstanceId();
                System.out.println(message +" will be sent via MQTT");

                MQTT mqtt = new MQTT();
                mqtt.setHost("localhost", 1883);
                connection = mqtt.blockingConnection();
                connection.connect();
                connection.publish(topic, message.getBytes(), QoS.AT_LEAST_ONCE, false);
            }
        }
