package lpi.client.utils;

import lpi.client.MessageClient;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class RequestHandler {

    private static final String EXIT = "exit";

    private MessageClient<String> messageClient;

    public RequestHandler(MessageClient<String> messageClient) {
        this.messageClient = messageClient;
        messageClient.listenTo();
    }

    public String executeRequest(String command) {
        Optional<ProtocolManager.Request> request = RequestHelper.mapToRequest(command);

        if (!request.isPresent()) {
            return "Command " + command + " is unknown. Please try one of the known commands: " +
                    Arrays.toString(ProtocolManager.Request.values());
        }

        String[] parameters = RequestHelper.splitCommand(command);
        if (parameters.length != request.get().argumentsAmount) {
           return "Command " + command + " must contain " + request.get().argumentsAmount + " arguments!";
        }

        switch (request.get()) {
            case PING:
                return messageClient.ping();
            case ECHO:
                return messageClient.echo(parameters[0]);
            case LOGIN:
                return messageClient.login(parameters[0], parameters[1]);
            case LIST:
                return messageClient.list();
            case MSG:
                return messageClient.message(parameters[0], parameters[1]);
            case FILE:
                File file = new File(parameters[1]);
                try {
                    return messageClient.sendFile(parameters[0], file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            case EXIT:
                messageClient.exit();
                return EXIT;
            default:
                return null;
        }
    }

}
