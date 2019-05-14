import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

class RequestHandler {

    private static boolean isWaitingEcho = false;
    private static boolean isWaitingMessage = false;
    private static boolean isWaitingList = false;
    static boolean isLoggedIn = false;

    static byte[] executeRequest(String command) {
        Optional<ProtocolManager.Request> request = getCommand(command);

        if (!request.isPresent()) {
            throw new IllegalArgumentException("Command " + command + " is unknown.");
        }

        switch (request.get()) {
            case LIST:
                isWaitingList = true;
                return new byte[]{request.get().value};
            case PING:
                return new byte[]{request.get().value};
            case RECEIVE_MESSAGE:
                isWaitingMessage = true;
                return new byte[]{request.get().value};
            case ECHO:
                isWaitingEcho = true;
                return handleEcho(command, request.get());
            case FILE:
                try {
                    return handleFile(command, request.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:
                return handleLikeOtherResponses(command, request.get());
        }
    }

    static Optional<ProtocolManager.Response> mapToResponse(byte[] message) {
        List<ProtocolManager.Response> responses = Arrays.asList(ProtocolManager.Response.values());
        Optional<ProtocolManager.Response> response = responses.stream()
                .filter(response1 -> response1.responseCode == (int) message[0])
                .findFirst();
        if (response.isPresent() && response.get().equals(ProtocolManager.Response.LOGIN_OK)) {
            isLoggedIn = true;
        }
        return response;
    }

    static String parseUnknownResponse(byte[] message) throws IOException, ClassNotFoundException {
        if (isWaitingEcho) {
            isWaitingEcho = false;
            return new String(message);
        } else if (isWaitingList) {
            isWaitingList = false;
            return parseLongResponse(message, ", ");
        } else if (isWaitingMessage) {
            isWaitingMessage = false;
            return parseLongResponse(message, ": ");
        } else {
            Object[] o = deserialize(message, Object[].class);
            return Objects.toString(o);
        }
    }

    private static String parseLongResponse(byte[] message, String delimiter) throws IOException, ClassNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();
        String[] userList = deserialize(message, String[].class);
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

    private static String[] toArray(String source, String splitter, int from) {
        List<String> params = Arrays.asList(Arrays.copyOfRange(source.split(splitter), from, source.length()));
        params = params.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return params.toArray(new String[0]);
    }

    private static <T> T deserialize(byte[] data, Class<T> tClass) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (T) is.readObject();
    }

    private static <T> byte[] serialize(T parameters) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(out)) {
            os.writeObject(parameters);
            return out.toByteArray();
        }
    }

    private static Optional<ProtocolManager.Request> getCommand(String that) {
        if (that == null || that.isEmpty()) {
            throw new IllegalArgumentException("Command should be not null and not empty!");
        }
        String[] arguments = that.split(" ");
        String command = arguments[0];
        List<ProtocolManager.Request> requestCommands = Arrays.asList(ProtocolManager.Request.values());
        return requestCommands.stream()
                .filter(request -> request.type.equalsIgnoreCase(command))
                .findFirst();
    }

    private static byte[] handleFile(String command, ProtocolManager.Request request) throws IOException {
        String[] params = toArray(command, " ", 1);
        byte[] commandArray = new byte[]{request.value};
        String receiver = params[0];
        File file = new File(params[1]);
        String fileName = file.getName();
        byte[] fileContent = readFileToByteArray(file);
        byte[] requestParam = serialize(new Object[]{receiver, fileName, fileContent});
        return concatArray(commandArray, requestParam);
    }

    private static byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        return bArray;
    }

    private static byte[] handleLikeOtherResponses(String command, ProtocolManager.Request request) {
        byte[] byteCommand = new byte[1];
        byteCommand[0] = request.value;
        byte[] response = null;
        try {
            response = concatArray(byteCommand, serialize(toArray(command, " ", 1)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static byte[] handleEcho(String command, ProtocolManager.Request request) {
        byte[] byteCommand = new byte[1];
        byteCommand[0] = request.value;
        byte[] param = command.substring(command.indexOf(" ") + 1).getBytes();
        return concatArray(byteCommand, param);
    }

    private static byte[] concatArray(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }
}
