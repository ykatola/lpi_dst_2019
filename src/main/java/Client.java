import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client implements AutoCloseable {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final String ip;
    private final int port;
    private volatile boolean isConnectionOpen = false;
    private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    private Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static Client newClient(String ip, int port) {
        return new Client(ip, port);
    }

    public void openConnection() {
    //Create socket connection
        try {
            socket = new Socket(ip, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            isConnectionOpen = true;
            if (socket.isConnected()) {
                System.out.println("Socket is created and connected to " + socket.getRemoteSocketAddress());
            }
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + ip);
            System.exit(1);
        } catch  (IOException e) {
            System.out.println("No I/O");
            System.exit(1);
        }
    }

    public void sendRequest(String command) {
        if (!isConnectionOpen) {
            throw new RuntimeException("Please, open connection before doing anything!");
        }
        byte[] commandToSent = RequestHandler.executeRequest(command);

        try {
            out.writeInt(commandToSent.length);
            out.write(commandToSent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String readResponse() {
        try {
            int contentSize = in.readInt();
            byte[] message = new byte[contentSize];
            in.readFully(message);
            Optional<ProtocolManager.Response> serverResponse = RequestHandler.mapToResponse(message);
            if (!serverResponse.isPresent()) {
                return RequestHandler.parseUnknownResponse(message);
            } else {
                return serverResponse.get().information;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ProtocolManager.Response.COMMON_BAD_RESPONSE.information;
    }

    public synchronized void listenToMessages() {
        ses.scheduleAtFixedRate(() -> {
            try {
                if (RequestHandler.isLoggedIn) {
                    sendRequest(ProtocolManager.Request.RECEIVE_MESSAGE.type);
                    String response = readResponse();
                    if (!response.equals("Empty message"))
                        System.out.println(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        try {
            ses.shutdown();
            out.close();
            in.close();
            socket.close();
            isConnectionOpen = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}