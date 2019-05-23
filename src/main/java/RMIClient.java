import lpi.server.rmi.IServer;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class RMIClient {

    private IServer proxy;
    private String sessionId;

    private RMIClient(String ip, int port) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(ip, port);
        proxy = (IServer) registry.lookup(IServer.RMI_SERVER_NAME);
    }

    public static RMIClient newRMIClient(String ip, int port) throws RemoteException, NotBoundException {
        return new RMIClient(ip, port);
    }

    public String sendRequest(String command) {
        Optional<ProtocolManager.Request> request = RequestHelper.mapToRequest(command);
        if(!request.isPresent()) {
            return "Unknown command " + command;
        }
        String[] parameters = RequestHelper.splitCommand(command);
        if (parameters.length != request.get().argumentsAmount) {
            throw new RuntimeException("Command " + command + " must contain " + request.get().argumentsAmount + " arguments!");
        }
        switch (request.get()) {
            case ECHO:
                try {
                    return proxy.echo(parameters[0]);
                } catch (RemoteException e) {
                    return e.getMessage();
                }
            case PING:
                try {
                    proxy.ping();
                } catch (RemoteException e) {
                    return e.getMessage();
                }
                return "Ping is okay";
            case LOGIN:
                if (sessionId == null) {
                    try {
                        sessionId = proxy.login(parameters[0], parameters[1]);
                    } catch (RemoteException e) {
                        return e.getMessage();
                    }
                    return "Successful registration!";
                } else {
                    try {
                        sessionId = proxy.login(parameters[0], parameters[1]);
                        return "Successful login!";
                    } catch (RemoteException e) {
                        return e.getCause().getMessage();
                    }
                }
            case MSG:
                StringBuilder stringBuilder = new StringBuilder();
                int i = 1;
                while (i < parameters.length) {
                    stringBuilder.append(parameters[i]);
                    i++;
                }
                try {
                    proxy.sendMessage(sessionId, new IServer.Message(parameters[0], stringBuilder.toString()));
                    return "Message sent to " + parameters[0];
                } catch (RemoteException e) {
                    return e.getCause().getMessage();
                }
            case FILE:
                try {
                    proxy.sendFile(sessionId, new IServer.FileInfo(parameters[0], new File(parameters[1])));
                    return "File sent to " + parameters[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
            case LIST:
                try {
                    return Arrays.toString(proxy.listUsers(sessionId));
                } catch (RemoteException e) {
                    return e.getCause().getMessage();
                }
                default: return "Unknown command";
        }
    }

    public void checkMessagesAndFiles() {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                try {
                    IServer.Message response = proxy.receiveMessage(sessionId);
                    if (response != null) {
                        System.out.println(response);
                    }
                } catch (RemoteException ignored) {

                }
            }
        };
        Timer timer = new Timer("Timer");

        long delay  = 1000L;
        long period = 1000L;
        timer.scheduleAtFixedRate(repeatedTask, delay, period);
    }

}
