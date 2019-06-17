package lpi.client.impl;

import lpi.client.MessageClient;
import lpi.client.additional.rmi.IServer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

public class MQClient implements MessageClient<String> {

    private static final String LIST_USERS_QUEUE = "chat.listUsers";
    private static final String SEND_MSG_QUEUE = "chat.sendMessage";
    private static final String SEND_PING_QUEUE = "chat.diag.ping";
    private static final String SEND_ECHO_QUEUE = "chat.diag.echo";
    private static final String SEND_FILE_QUEUE = "chat.sendFile";
    private static final String LOGIN_QUEUE = "chat.login";
    private static final String MESSAGES_QUEUE = "chat.messages";
    private static final String FILES_QUEUE = "chat.files";

    private Session session;
    private Connection connection;

    private MQClient(String brokerUrl) {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        activeMQConnectionFactory.setTrustedPackages(Collections.singletonList("lpi.server.mq"));
        try {
            connection = activeMQConnectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static MQClient newClient(String brokerUrl) {
        return new MQClient(brokerUrl);
    }

    @Override
    public String ping() {
        try {
            javax.jms.Message msg = session.createMessage();
            return sendMessage(SEND_PING_QUEUE, msg);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String echo(String letter) {
        try {
            javax.jms.Message msg = session.createTextMessage(letter);
            return sendMessage(SEND_ECHO_QUEUE, msg);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String login(String receiver, String password) {
        MapMessage msg;
        try {
            msg = session.createMapMessage();
            msg.setString("login", receiver);
            msg.setString("password", password);
            return sendMessage(LOGIN_QUEUE, msg);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String message(String receiver, String message) {
        try {
            MapMessage msg = session.createMapMessage();
            msg.setString("receiver", receiver);
            msg.setString("message", message);
            return sendMessage(SEND_MSG_QUEUE, msg);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String list() {
        try {
            javax.jms.Message message = session.createMessage();
            return sendMessage(LIST_USERS_QUEUE, message);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String sendFile(String receiver, File file) throws IOException {
        try {
            ObjectMessage message = session.createObjectMessage();
            message.setObject(new IServer.FileInfo(receiver, file));
            return sendMessage(SEND_FILE_QUEUE, message);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public void exit() {
        try {
            session.close();
            connection.close();
            System.out.println("Exited from " + getClass().getSimpleName());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listenTo() {
        registerListeners();
    }

    private String sendMessage(String queueName, javax.jms.Message msg) {
        Destination targetQueue;
        try {
            targetQueue = session.createQueue(queueName);

            Destination replyQueue = session.createTemporaryQueue();
            MessageProducer producer = session.createProducer(targetQueue);
            MessageConsumer consumer = session.createConsumer(replyQueue);
            msg.setJMSReplyTo(replyQueue);
            producer.send(msg);
            javax.jms.Message message = consumer.receive();
            String content;
            consumer.close();
            producer.close();
            if (message instanceof TextMessage) {
                content = ((TextMessage) message).getText();
            } else if (message instanceof MapMessage) {
                content = ((MapMessage) message).getString("message");
            } else if (message instanceof ObjectMessage) {
                Serializable obj = ((ObjectMessage) message).getObject();
                if (obj instanceof String[]) {
                    String[] users = (String[]) obj;
                    content = String.join(", ", users);
                } else {
                    throw new IOException("Unexpected content: " + obj);
                }
            } else if (message != null) {
                content = "Ping is okay";
            } else {
                throw new IOException("Unexpected message type: " + message.getClass());
            }
            return content;

        } catch (JMSException |
                IOException e) {
            return e.getCause().getMessage();
        }
    }

    private void registerListeners() {
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination messageQueue = session.createQueue(MESSAGES_QUEUE);
            Destination fileQueue = session.createQueue(FILES_QUEUE);
            MessageConsumer messageConsumer = session.createConsumer(messageQueue);
            MessageConsumer fileConsumer = session.createConsumer(fileQueue);
            messageConsumer.setMessageListener(new MessageReceiver());
            fileConsumer.setMessageListener(new FileReceiver());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private class MessageReceiver implements MessageListener {
        @Override
        public void onMessage(javax.jms.Message message) {
            if (message instanceof MapMessage) {
                MapMessage mapMsg = (MapMessage) message;
                try {
                    String sender = mapMsg.getString("sender");
                    String message1 = mapMsg.getString("message");
                    System.out.println(sender + " " + message1);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FileReceiver implements MessageListener {
        @Override
        public void onMessage(javax.jms.Message message) {
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                try {
                    IServer.FileInfo fileInfo = (IServer.FileInfo) objectMessage.getObject();
                    System.out.println(fileInfo.getFilename() + " was sent by " + fileInfo.getReceiver());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
