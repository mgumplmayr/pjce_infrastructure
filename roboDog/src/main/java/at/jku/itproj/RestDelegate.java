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

    @Override
    public void execute(final DelegateExecution execution) throws Exception {

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8081/sendMessage"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"topic\":\"myTopic\",\"message\" : {\"data\":\"hello world\"}}"))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

    }
}
