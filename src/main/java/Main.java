import java.util.Scanner;
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
            try (Client client = Client.newClient("localhost", 4321)) {
                client.openConnection();
                client.listenToMessages();
                while (true) {
                    System.out.println("Enter command: ");
                    String command = sc.nextLine();
                    client.sendRequest(command);
                    System.out.println(client.readResponse());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
