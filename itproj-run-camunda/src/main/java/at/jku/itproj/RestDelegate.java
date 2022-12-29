package at.jku.itproj;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service("restDelegate")
public class RestDelegate implements JavaDelegate {
    private static HttpURLConnection connection;
    private static String variable;

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        System.out.println("Starting RestDelegate");
        final Object username = execution.getVariable("username");
        variable = "Hello, my name is: " + username+" and this was sent by restDelegate";
        System.out.println(variable+" will be sent via REST");
        System.out.println(execution.getProcessInstanceId());


        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/sendMessage"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"topic\":\"myTopic\",\"message\" : {\"data\":\""+variable+"\"}}"))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
    }
}
