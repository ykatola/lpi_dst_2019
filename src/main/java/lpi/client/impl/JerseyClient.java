package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.client.model.*;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;

public class JerseyClient implements MessageClient<String> {

    private WebTarget webTarget;
    private User user;

    private JerseyClient(String baseUrl) {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(baseUrl);
    }

    public static JerseyClient newClient(String baseUrl) {
        return new JerseyClient(baseUrl);
    }

    @Override
    public String ping() {
        return webTarget.path("ping").request().get(String.class);
    }

    @Override
    public String echo(String message) {
        return webTarget.path("/echo").request(MediaType.TEXT_PLAIN)
                .post(Entity.text(message), String.class);
    }

    @Override
    public String login(String login, String password) {
        user = new User();
        user.setLogin(login);
        user.setPassword(password);
        Entity userInfoEntity = Entity.entity(user, MediaType.APPLICATION_JSON);
        Response response = webTarget.path("/user")
                .request()
                .put(userInfoEntity, Response.class);
        webTarget.register(HttpAuthenticationFeature.basic(user.getLogin(), user.getPassword()));
        return response.readEntity(String.class);
    }

    @Override
    public String message(String receiver, String message) {
        Response response = webTarget.path(receiver).path("/messages")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(message, MediaType.TEXT_PLAIN), Response.class);
        return response.readEntity(String.class);
    }

    @Override
    public String list() {
        Response response = webTarget.path("/users").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
        if (response.getStatus() == 200) {
            Items users = response.readEntity(Items.class);
            return String.join(",", users.getItems());
        } else {
            return response.getStatus() + ": " + response.readEntity(String.class);
        }
    }

    @Override
    public String sendFile(String receiver, File file) throws IOException {
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String fileContent = encoder.encodeToString(Files.readAllBytes(file.toPath()));
        FileInfo fileInfo = new FileInfo(receiver, file.getName(), fileContent);
        Response response = webTarget.path(receiver).path("files").request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(fileInfo, MediaType.APPLICATION_JSON));
        return response.readEntity(String.class);
    }

    @Override
    public void exit() {
        System.out.println("Exited from " + getClass().getSimpleName());
    }

    @Override
    public void listenTo() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (user != null) {
                    Response messageCheckResponse = webTarget.path(user.getLogin()).path("messages")
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .get(Response.class);
                    if (messageCheckResponse.getStatus() == 200) {
                        Items messages = messageCheckResponse.readEntity(Items.class);
                        for (String messageId : messages.getItems()) {
                            Message message = webTarget.path(user.getLogin()).path("messages").path(messageId)
                                    .request()
                                    .accept(MediaType.APPLICATION_JSON)
                                    .get(Message.class);
                            System.out.println("You have got a message: " + message.getMessage() + ", from " + message.getSender());
                            webTarget.path(user.getLogin()).path("messages").path(messageId)
                                    .request()
                                    .delete();
                        }
                    }
                    Response fileCheckResponse = webTarget.path(user.getLogin()).path("files")
                            .request()
                            .accept(MediaType.APPLICATION_JSON)
                            .get(Response.class);
                    if (fileCheckResponse.getStatus() == 200) {
                        Items files = fileCheckResponse.readEntity(Items.class);
                        for (String fileId : files.getItems()) {
                            FileInfo message = webTarget.path(user.getLogin()).path("files").path(fileId)
                                    .request()
                                    .accept(MediaType.APPLICATION_JSON)
                                    .get(FileInfo.class);
                            System.out.println("You have got a new file: " + message.getFileName() + ", from " + message.getReceiver());
                            webTarget.path(user.getLogin()).path("files").path(fileId)
                                    .request()
                                    .delete();
                        }
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 500, 500);
    }

}
