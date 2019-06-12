package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.server.rmi.IServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class RMIClient implements MessageClient<String> {

    private IServer proxy;
    private String sessionId;

    private RMIClient(String ip, int port) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(ip, port);
        proxy = (IServer) registry.lookup(IServer.RMI_SERVER_NAME);
    }

    public static RMIClient newRMIClient(String ip, int port) throws RemoteException, NotBoundException {
        return new RMIClient(ip, port);
    }


    @Override
    public String ping() {
        try {
            proxy.ping();
        } catch (RemoteException e) {
            return e.getCause().getMessage();
        }
        return "Ping is okay";
    }

    @Override
    public String echo(String message) {
        try {
            return proxy.echo(message);
        } catch (RemoteException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String login(String receiver, String password) {
        if (sessionId == null) {
            try {
                sessionId = proxy.login(receiver, password);
            } catch (RemoteException e) {
                return e.getCause().getMessage();
            }
            return "Successful registration!";
        } else {
            try {
                sessionId = proxy.login(receiver, password);
                return "Successful login!";
            } catch (RemoteException e) {
                return e.getCause().getMessage();
            }
        }
    }

    @Override
    public String message(String receiver, String message) {

        try {
            proxy.sendMessage(sessionId, new IServer.Message(receiver, message));
            return "Message sent to " + receiver;
        } catch (RemoteException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String list() {
        try {
            return Arrays.toString(proxy.listUsers(sessionId));
        } catch (RemoteException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public void exit() {
        try {
            proxy.exit(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
