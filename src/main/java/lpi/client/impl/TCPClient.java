package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.client.utils.ProtocolManager;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TCPClient implements MessageClient<String> {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final String ip;
    private final int port;
    private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private boolean isLoggedIn;

    private TCPClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        openConnection();
    }

    public static TCPClient newClient(String ip, int port) {
        return new TCPClient(ip, port);
    }

    private void openConnection() {
        try {
            socket = new Socket(ip, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            if (socket.isConnected()) {
                System.out.println("Socket is created and connected to " + socket.getRemoteSocketAddress());
            }
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + ip);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("No I/O");
            System.exit(1);
        }
    }

    @Override
    public String ping() {
        byte[] dataToSend = new byte[]{1};
        try {
            out.writeInt(dataToSend.length);
            out.write(dataToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readResponse();
    }

    private String readResponse() {
        try {
            int contentSize = in.readInt();
            byte[] message = new byte[contentSize];
            in.readFully(message);
            Optional<ProtocolManager.Response> serverResponse = toResponse(message);
            return serverResponse.map(response -> response.information).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Optional<ProtocolManager.Response> toResponse(byte[] message) {
        List<ProtocolManager.Response> responses = Arrays.asList(ProtocolManager.Response.values());
        Optional<ProtocolManager.Response> response = responses.stream()
                .filter(response1 -> response1.responseCode == (int) message[0])
                .findFirst();
        if (response.isPresent() && response.get().equals(ProtocolManager.Response.LOGIN_OK)) {
            isLoggedIn = true;
        }
        return response;
    }

    private byte[] concatArray(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    @Override
    public String echo(String message) {
        byte[] byteCommand = new byte[1];
        byteCommand[0] = 3;
        byte[] param = message.getBytes();
        byte[] dataToSend = concatArray(byteCommand, param);

        try {
            out.writeInt(dataToSend.length);
            out.write(dataToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] response = new byte[0];
        try {
            int contentSize = in.readInt();
            response = new byte[contentSize];
            in.readFully(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(response);
    }

    @Override
    public String login(String receiver, String password) {
        byte[] byteCommand = new byte[1];
        byteCommand[0] = 5;
        byte[] response = new byte[0];
        try {
            response = concatArray(byteCommand, serialize(new String[]{receiver, password}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.writeInt(response.length);
            out.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readResponseWithCode();
    }

    @Override
    public String message(String receiver, String message) {
        if (!isLoggedIn) {
            return "You should login before executing list command!";
        }
        byte[] byteCommand = new byte[1];
        byteCommand[0] = 15;
        byte[] response = new byte[0];
        try {
            response = concatArray(byteCommand, serialize(new String[]{receiver, message}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.writeInt(response.length);
            out.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readResponseWithCode();
    }

    @Override
    public String list() {
        if (!isLoggedIn) {
            return "You should login before executing list command!";
        }
        byte[] dataToSend = new byte[]{10};
        try {
            out.writeInt(dataToSend.length);
            out.write(dataToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int contentSize;
        byte[] message = new byte[0];
        try {
            contentSize = in.readInt();
            message = new byte[contentSize];
            in.readFully(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseList(message, ", ");
    }

    private <T> byte[] serialize(T parameters) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(out)) {
            os.writeObject(parameters);
            return out.toByteArray();
        }
    }

    private String readResponseWithCode() {
        try {
            int contentSize = in.readInt();
            byte[] message = new byte[contentSize];
            in.readFully(message);
            Optional<ProtocolManager.Response> serverResponse = toResponse(message);
            if (!serverResponse.isPresent()) {
                return "Unknown response";
            } else {
                isLoggedIn = true;
                return serverResponse.get().information;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown response";
    }

    private String parseList(byte[] message, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] userList = new String[0];
        try {
            userList = deserialize(message, String[].class);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        int count = 0;
        for (String userName : userList) {
            if (count == userList.length - 1) {
                stringBuilder.append(userName);
            } else {
                stringBuilder.append(userName).append(delimiter);
            }
            count++;
        }
        return stringBuilder.toString();
    }

    private <T> T deserialize(byte[] data, Class<T> tClass) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (T) is.readObject();
    }

}
