package com.mosquitto.controller;

import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mosquitto.MqttGateway;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

@RestController
public class MqttController {
	@Autowired
	MqttGateway mqtGateway;
	@PostMapping("/sendMessage")
	public ResponseEntity<?> publish(@RequestBody String mqttMessage){
		
		try {
		JsonObject convertObject = new Gson().fromJson(mqttMessage, JsonObject.class);
			System.out.println("Received REST message");
		mqtGateway.sendToMQTT(convertObject.get("message").toString(), convertObject.get("topic").toString());
		sendMessageBack(convertObject.get("message").toString());
		return ResponseEntity.ok("Success");
		}catch(Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.ok("Fail");
		}
	}

	private void sendMessageBack(String message) throws Exception {
		TimeUnit.SECONDS.sleep(10);
		System.out.println("Sending the following message back: "+"This message was sent back by the MQTT Broker: "+message);
		HttpRequest postRequest = HttpRequest.newBuilder()
				.uri(new URI("http://localhost:8090/sendMessage"))
				.POST(HttpRequest.BodyPublishers.ofString("This message was sent back by the MQTT Broker: "+message))
				.build();

		HttpClient httpClient = HttpClient.newHttpClient();
		httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
	}
}
