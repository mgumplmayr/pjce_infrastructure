package at.jku.itproj;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Bean;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service("MQTTDelegate")
public class MQTTDelegate implements JavaDelegate {
    private static BlockingConnection connection;
    private static String variable;

            @Override
            public void execute(final DelegateExecution execution) throws Exception {
                System.out.println("Starting MQTTDelegate");
                final Object username = execution.getVariable("username");
                variable = "Hello, my name is: " + username +" and this was sent by MQTTDelegate";
                System.out.println(variable+" will be sent via MQTT");

                MQTT mqtt = new MQTT();
                mqtt.setHost("localhost", 1883);
                connection = mqtt.blockingConnection();
                connection.connect();
                connection.publish("test", variable.getBytes(), QoS.AT_LEAST_ONCE, false);
            }
        }
