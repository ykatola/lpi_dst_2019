package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.client.additional.rmi.IServer;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class RMIClient implements MessageClient<String> {

    private IServer proxy;
    private String sessionId;
    private Timer timer;


    private RMIClient(String ip, int port) {
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(ip, port);
            proxy = (IServer) registry.lookup(IServer.RMI_SERVER_NAME);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        timer = new Timer();
    }

    public static RMIClient newClient(String ip, int port) {
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
    public String sendFile(String receiver, File file) throws IOException {
        proxy.sendFile(sessionId, new IServer.FileInfo(receiver, file));
        return "File was sent to " + receiver;
    }

    @Override
    public void exit() {
        try {
            timer.cancel();
            proxy.exit(sessionId);
            System.out.println("Exited from " + getClass().getSimpleName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listenTo() {
        TimerTask timerTaskPoll = new TimerTask() {
            @Override
            public void run() {
                if (sessionId != null) {
                    try {
                        IServer.Message message = proxy.receiveMessage(sessionId);
                        IServer.FileInfo fileInfo = proxy.receiveFile(sessionId);
                        if (message != null) {
                            System.out.println("You have got a message: " + message.getMessage() + ", from " + message.getSender());
                        }
                        if (fileInfo != null) {
                            System.out.println("You have got a file: " + fileInfo.getFilename() + ", from " + fileInfo.getSender());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(timerTaskPoll, 500, 500);
    }
}
