package lpi;

import lpi.client.MessageClient;
import lpi.client.impl.JerseyClient;
import lpi.client.impl.MQClient;
import lpi.client.impl.RMIClient;
import lpi.client.impl.TCPClient;
import lpi.client.utils.RequestHandler;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("Choose number to select client you want to use: ");
            System.out.println("1. RMI Client.");
            System.out.println("2. TCP Client.");
            System.out.println("3. REST Client.");
            System.out.println("4. MQ Client.");
            System.out.println("5. Exit.");
            int num = sc.nextInt();
            MessageClient<String> messageClient = null;
            switch (num) {
                case 1:
                    try {
                        messageClient = RMIClient.newRMIClient("localhost", 4321);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    messageClient = TCPClient.newClient("localhost", 4321);
                    break;
                case 3:
                    messageClient = JerseyClient.newClient("http://localhost:8080/chat/server/");
                    break;
                case 4:
                    messageClient = MQClient.newClient("tcp://localhost:61616");
                    break;
                case 5:
                    System.exit(0);
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
