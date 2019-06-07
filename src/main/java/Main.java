import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Scanner;
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        JerseyClientWrapper client = new JerseyClientWrapper("http://localhost:8080/chat/server/");
        System.out.println(client.get("ping").readEntity(String.class));
        System.out.println(client.put(Entity.json(new User("yura", "123")), "user", MediaType.TEXT_PLAIN_TYPE));
        System.out.println(client.put(Entity.entity("kek", MediaType.TEXT_PLAIN), "echo", MediaType.TEXT_PLAIN_TYPE).readEntity(String.class));
}
}
