package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.client.additional.soap.*;
import sun.misc.IOUtils;

import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SOAPClient implements MessageClient<String> {

    private IChatServer serverProxy;
    private String sessionId;
    private String user;
    private Timer timer = new Timer();

    private SOAPClient(String url) {
        try {
            ChatServer serverWrapper = new ChatServer(new URL(url));
            this.serverProxy = serverWrapper.getChatServerProxy();
        } catch (WebServiceException | MalformedURLException e) {
            exit();
        }
    }

    public static SOAPClient newClient(String url) {
        return new SOAPClient(url);
    }

    @Override
    public String ping() {
        serverProxy.ping();
        return "PING is okay";
    }

    @Override
    public String echo(String message) {
        return serverProxy.echo(message);
    }

    @Override
    public String login(String receiver, String password) {
        if (sessionId == null) {
            try {
                user = receiver;
                sessionId = serverProxy.login(receiver, password);
            } catch (ArgumentFault | LoginFault | ServerFault argumentFault) {
                argumentFault.printStackTrace();
            }
            return "Logged in successfully with session id - " + sessionId;
        } else {
            return "You have logged in already.";
        }
    }

    @Override
    public String message(String receiver, String message) {
        if (sessionId == null) {
            return "You should login before doing this action";
        }
        Message messageToSend = new Message();
        messageToSend.setSender(user);
        messageToSend.setReceiver(receiver);
        messageToSend.setMessage(message);
        try {
            serverProxy.sendMessage(sessionId, messageToSend);
            return "Message to " + receiver + " was sent";
        } catch (ArgumentFault | ServerFault argumentFault) {
            return argumentFault.getMessage();
        }
    }

    @Override
    public String list() {
        try {
            if (sessionId == null) {
                return "You should login before doing this action";
            }
            return String.join(", ", serverProxy.listUsers(sessionId));
        } catch (ArgumentFault | ServerFault argumentFault) {
            return argumentFault.getMessage();
        }
    }

    @Override
    public String sendFile(String receiver, File file) {
        FileInfo fileInfo = new FileInfo();
        try {
            fileInfo.setFileContent(IOUtils.readNBytes(new FileInputStream(file), 0));
        } catch (IOException e) {
            return e.getMessage();
        }
        try {
            serverProxy.sendFile(sessionId, new FileInfo());
            return "File was sent successfully";
        } catch (ArgumentFault | ServerFault argumentFault) {
            return argumentFault.getMessage();
        }
    }

    @Override
    public void exit() {
        try {
            user = null;
            timer.cancel();
            serverProxy.exit(sessionId);
            System.out.println("Exited from " + getClass().getSimpleName());
        } catch (ArgumentFault | ServerFault argumentFault) {
            argumentFault.printStackTrace();
        }
    }

    @Override
    public void listenTo() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (sessionId != null) {
                        Message message = serverProxy.receiveMessage(sessionId);
                        if (message != null) {
                            System.out.println("You have got a new message from " + message.getSender() + ": " + message.getMessage());
                        }

                        FileInfo fileInfo = serverProxy.receiveFile(sessionId);
                        if (fileInfo != null) {
                            System.out.println("You have got a new file from " + fileInfo.getSender() + " - " + fileInfo.getFilename());
                        }
                    }
                } catch (ArgumentFault | ServerFault argumentFault) {
                    argumentFault.printStackTrace();
                } catch (WebServiceException e) {
                    exit();
                    cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 500, 500);
    }
}
