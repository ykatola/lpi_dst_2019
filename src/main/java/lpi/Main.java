package lpi;

import lpi.client.MessageClient;
import lpi.client.impl.*;
import lpi.client.utils.RequestHandler;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final String DEFAULT_IP = "localhost";

    public static void main(String[] args) {
        while (true) {
            System.out.println("Choose number to select client you want to use: ");
            System.out.println("1. RMI Client.");
            System.out.println("2. TCP Client.");
            System.out.println("3. REST Client.");
            System.out.println("4. MQ Client.");
            System.out.println("5. SOAP Client.");
            System.out.println("6. Exit.");
            int num = sc.nextInt();
            MessageClient<String> messageClient = null;
            System.out.println("Enter number of port you want to use: ");
            int port = sc.nextInt();
            System.out.println("Enter host you want connect to (or type 'd' to use default - localhost");
            String ip;
            String ipInput = sc.next();
            if (ipInput.equals("d")) {
                ip = DEFAULT_IP;
            } else {
                ip = ipInput;
            }
            switch (num) {
                case 1:
                    messageClient = RMIClient.newClient(ip, port);
                    break;
                case 2:
                    messageClient = TCPClient.newClient(ip, port);
                    break;
                case 3:
                    messageClient = JerseyClient.newClient(String.format("http://%s:%s/chat/server/", ip, port));
                    break;
                case 4:
                    messageClient = MQClient.newClient(String.format("tcp://%s:%s", ip, port));
                    break;
                case 5:
                    messageClient = SOAPClient.newClient(String.format("http://%s:%s/chat?wsdl", ip, port));
                    break;
                case 6:
                    System.exit(0);
                    break;
                default:
                    throw new IllegalArgumentException("Choose one of available clients!");
            }
            sc.nextLine();
            RequestHandler requestHandler = new RequestHandler(messageClient);
            boolean go = true;
            while (go) {
                System.out.println("Enter command: ");
                String command = sc.nextLine();
                String response = requestHandler.executeRequest(command);
                if (response.equals("exit")) {
                    go = false;
                } else {
                    System.out.println(response);
                }
            }
        }
    }
}
