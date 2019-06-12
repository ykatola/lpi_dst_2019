package lpi.client.impl;

import lpi.client.MessageClient;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.io.Serializable;

public class MQClient implements MessageClient<String> {

    private static final String LIST_USERS_QUEUE = "chat.listUsers";
    private static final String SEND_MSG_QUEUE = "chat.sendMessage";
    private static final String SEND_PING_QUEUE = "chat.diag.ping";
    private static final String SEND_ECHO_QUEUE = "chat.diag.echo";
    private static final String LOGIN_QUEUE = "chat.login";

    private Session session;

    private MQClient(String brokerUrl) {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        try {
            Connection connection = activeMQConnectionFactory.createConnection();
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
            Message msg = session.createMessage();
            return sendMessage(SEND_PING_QUEUE, msg);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public String echo(String letter) {
        try {
            Message msg = session.createTextMessage(letter);
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
            Message message = session.createMessage();
            return sendMessage(LIST_USERS_QUEUE, message);
        } catch (JMSException e) {
            return e.getCause().getMessage();
        }
    }

    @Override
    public void exit() {
        try {
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private String sendMessage(String queueName, Message msg) {
        Destination targetQueue;
        try {
            targetQueue = session.createQueue(queueName);

            Destination replyQueue = session.createTemporaryQueue();
            MessageProducer producer = session.createProducer(targetQueue);
            MessageConsumer consumer = session.createConsumer(replyQueue);
            msg.setJMSReplyTo(replyQueue);
            producer.send(msg);
            Message message = consumer.receive();
            String content;
            consumer.close();
            producer.close();
            if (message instanceof TextMessage) {
                content = ((TextMessage) message).getText();
            } else if (message instanceof MapMessage) {
                content = ((MapMessage) message).getString("message");
            } else if (message != null) {
                Serializable obj = ((ObjectMessage) message).getObject();
                if (obj instanceof String[]) {
                    String[] users = (String[]) obj;
                    content = String.join(", ", users);
                } else {
                    throw new IOException("Unexpected content: " + obj);
                }
            } else {
                throw new IOException("Unexpected message type: " + message.getClass());
            }
            return content;

        } catch (JMSException | IOException e) {
            return e.getCause().getMessage();
        }
    }
}
