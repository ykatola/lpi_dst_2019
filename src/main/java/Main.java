import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws RemoteException, NotBoundException {
        RMIClient client = RMIClient.newRMIClient("localhost", 4321);
        client.checkMessagesAndFiles();
        while (true) {
            System.out.println("Enter command: ");
            String command = sc.nextLine();
            String response = client.sendRequest(command);
            System.out.println(response);
        }
    }
}
