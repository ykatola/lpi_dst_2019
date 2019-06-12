package lpi.client.utils;

import lpi.client.MessageClient;

import java.util.Optional;

public class RequestHandler {

    private MessageClient<String> messageClient;

    public RequestHandler(MessageClient<String> messageClient) {
        this.messageClient = messageClient;
    }

    public String executeRequest(String command) {
        Optional<ProtocolManager.Request> request = RequestHelper.mapToRequest(command);

        if (!request.isPresent()) {
            throw new IllegalArgumentException("Command " + command + " is unknown.");
        }

        String[] parameters = RequestHelper.splitCommand(command);
        if (parameters.length != request.get().argumentsAmount) {
            throw new RuntimeException("Command " + command + " must contain " + request.get().argumentsAmount + " arguments!");
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
            default:
                return null;
        }
    }

}
