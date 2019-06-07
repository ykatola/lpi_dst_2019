import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JerseyClientWrapper {

    private WebTarget webTarget;

    public JerseyClientWrapper(String baseUrl) {
        Client client = ClientBuilder.newClient();
        webTarget = client.target(baseUrl);
    }

    public Response get(String path) {
        return webTarget.path(path).request().get();
    }

    public Response post(String path, Entity entity, MediaType mediaType) {
        return webTarget.path(path).request(mediaType).post(entity);
    }

    public Response put(Entity entity, String path, MediaType mediaType) {
        return webTarget.path(path).request(mediaType).put(entity);
    }

}