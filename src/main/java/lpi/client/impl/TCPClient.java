package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.client.utils.ProtocolManager;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TCPClient implements MessageClient<String> {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final String ip;
    private final int port;
    private boolean isLoggedIn;
    private Timer timer;
    private static final String PATH_TO_SAVE_FILES = "Some path";

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
            return e.getCause().getMessage();
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
            return e.getCause().getMessage();
        }
    }

    private void readResponseMessage() {

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
            return e.getCause().getMessage();
        }
        byte[] response;
        try {
            int contentSize = in.readInt();
            response = new byte[contentSize];
            in.readFully(response);
        } catch (IOException e) {
            return e.getCause().getMessage();
        }
        return new String(response);
    }

    @Override
    public String login(String receiver, String password) {
        byte[] byteCommand = new byte[1];
        byteCommand[0] = 5;
        byte[] response;
        try {
            response = concatArray(byteCommand, serialize(new String[]{receiver, password}));
        } catch (IOException e) {
            return e.getCause().getMessage();
        }
        try {
            out.writeInt(response.length);
            out.write(response);
        } catch (IOException e) {
            return e.getCause().getMessage();
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
        byte[] response;
        try {
            response = concatArray(byteCommand, serialize(new String[]{receiver, message}));
        } catch (IOException e) {
            return e.getCause().getMessage();
        }
        try {
            out.writeInt(response.length);
            out.write(response);
        } catch (IOException e) {
            return e.getCause().getMessage();
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

    @Override
    public String sendFile(String receiver, File file) throws IOException {
        byte[] response = handleFile(receiver, file);
        try {
            out.writeInt(response.length);
            out.write(response);
        } catch (IOException e) {
            return e.getCause().getMessage();
        }
        return readResponseWithCode();
    }

    @Override
    public void exit() {
        try {
            timer.cancel();
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listenTo() {
        TimerTask timerTaskPoll = new TimerTask() {
            @Override
            public void run() {
                if (isLoggedIn) {
                    checkResponses();
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTaskPoll, 500, 500);
    }

    private void checkResponses() {
        checkMessage();
        checkFile();
        readResponseMessage();
    }

    private void checkFile() {
        byte[] dataToSend = new byte[]{30};
        try {
            out.writeInt(dataToSend.length);
            out.write(dataToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            int contentSize = in.readInt();
            byte[] message = new byte[contentSize];
            in.readFully(message);
            Optional<ProtocolManager.Response> serverResponse = toResponse(message);
            if (!serverResponse.isPresent()) {
                System.out.println(parseFile(message));
            }
        } catch (IOException e) {
            System.out.println(e.getCause().getMessage());
        }
    }

    private String parseFile(byte[] message) {
        Object[] objectFile;
        try {
            objectFile = deserialize(message, Object[].class);
            return "File " + objectFile[1] + " was received successfully!";
        } catch (IOException | ClassNotFoundException e) {
            return e.getMessage();
        }
    }

    private void checkMessage() {
        byte[] dataToSend = new byte[]{25};
        try {
            out.writeInt(dataToSend.length);
            out.write(dataToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            int contentSize = in.readInt();
            byte[] message = new byte[contentSize];
            in.readFully(message);
            Optional<ProtocolManager.Response> serverResponse = toResponse(message);
            if (!serverResponse.isPresent()) {
                System.out.println(parseList(message, ":"));
            }
        } catch (IOException e) {
            System.out.println(e.getCause().getMessage());
        }
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
                return serverResponse.get().information;
            }
        } catch (IOException e) {
            return e.getCause().getMessage();
        }
    }

    private String parseList(byte[] message, String delimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] userList;
        try {
            userList = deserialize(message, String[].class);
        } catch (IOException | ClassNotFoundException e) {
            return e.getCause().getMessage();
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

    private byte[] handleFile(String receiver, File file) throws IOException {
        byte[] commandArray = new byte[]{20};
        String fileName = file.getName();
        byte[] fileContent = readFileToByteArray(file);
        byte[] requestParam = serialize(new Object[]{receiver, fileName, fileContent});
        return concatArray(commandArray, requestParam);
    }

    private byte[] readFileToByteArray(File file) {
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try {
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
        return bArray;
    }

}
