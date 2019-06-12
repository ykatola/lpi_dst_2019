package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.client.model.Users;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JerseyClient implements MessageClient<String> {

    private WebTarget webTarget;

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
        return webTarget.path("/echo").request(MediaType.TEXT_PLAIN_TYPE)
                .post(Entity.text(message), String.class);
    }

    @Override
    public String login(String receiver, String password) {
        UserInfo userInfo = new UserInfo();
        userInfo.setLogin(receiver);
        userInfo.setPassword(password);
        Entity userInfoEntity = Entity.entity(userInfo, MediaType.APPLICATION_JSON_TYPE);
        Response response = webTarget.path("/user")
                .request()
                .put(userInfoEntity, Response.class);
        this.webTarget.register(HttpAuthenticationFeature.basic(userInfo.getLogin(), userInfo.getPassword()));
        return response.readEntity(String.class);
    }

    @Override
    public String message(String receiver, String message) {
        Response response = webTarget.path(receiver).path("/messages")
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(message, MediaType.TEXT_PLAIN_TYPE), Response.class);
        return response.readEntity(String.class);
    }

    @Override
    public String list() {
        Response response = webTarget.path("/users").request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
        Users users = response.readEntity(Users.class);
        return String.join(",", users.getItems());
    }

    @Override
    public void exit() {

    }
}
