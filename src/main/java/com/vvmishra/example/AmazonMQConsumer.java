package com.vvmishra.example;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

/**
 * Hello world!
 */
public class AmazonMQConsumer {
    // Specify the connection parameters.
    private final static String WIRE_LEVEL_ENDPOINT
            = "ssl://b-a37b689a-d1e9-426d-8415-87e3b4331492-1.mq.ap-south-1.amazonaws.com:61617";
    private final static String ACTIVE_MQ_USERNAME = "vvmishra";
    private final static String ACTIVE_MQ_PASSWORD = "Password@2244";
    private final static String RABBIT_MQ_PASSWORD = "password@2244";

    public static void main(String[] args) throws JMSException, InterruptedException, NoSuchAlgorithmException, IOException, KeyManagementException, TimeoutException {
//        final ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();
//        final PooledConnectionFactory pooledConnectionFactory = createPooledConnectionFactory(connectionFactory);
//
//        receiveMessage(connectionFactory);
//
//        pooledConnectionFactory.stop();

        final ConnectionFactory rabbitMQConnectionFactory = rabbitMQConnectionFactory();
        IntStream.iterate(0, e -> e +1).limit(7500).forEach(value -> {
            try {
                Thread.sleep(1000);
                publishMessageToRabbitMQ(rabbitMQConnectionFactory, value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void receiveMessage(ActiveMQConnectionFactory connectionFactory) throws JMSException, InterruptedException {
        // Establish a connection for the consumer.
        // Note: Consumers should not use PooledConnectionFactory.
        final Connection consumerConnection = connectionFactory.createConnection();
        consumerConnection.start();

        // Create a session.
        final Session consumerSession = consumerConnection
                .createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create a queue named "MyQueue".
        final Destination consumerDestination = consumerSession
                .createQueue("local-queue");
        // Create a message consumer from the session to the queue.
        final MessageConsumer consumer = consumerSession
                .createConsumer(consumerDestination);
        Instant now = Instant.now().plus(5, ChronoUnit.MINUTES);

        while (true) {

            final Message consumerMessage = consumer.receive(1000);

            if (consumerMessage != null) {
                // Receive the message when it arrives.
                final TextMessage consumerTextMessage = (TextMessage) consumerMessage;
                System.out.println("Message received: " + consumerTextMessage.getText());
                Thread.sleep(2000);
            }


            if (now.isBefore(Instant.now())) {
                break;
            }
        }

        // Clean up the consumer.
        consumer.close();
        consumerSession.close();
        consumerConnection.close();
    }

    private static PooledConnectionFactory createPooledConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        // Create a pooled connection factory.
        final PooledConnectionFactory pooledConnectionFactory =
                new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(connectionFactory);
        pooledConnectionFactory.setMaxConnections(10);
        return pooledConnectionFactory;
    }

    private static ActiveMQConnectionFactory createActiveMQConnectionFactory() {
        // Create a connection factory.
        final ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(WIRE_LEVEL_ENDPOINT);

        // Pass the username and password.
        connectionFactory.setUserName(ACTIVE_MQ_USERNAME);
        connectionFactory.setPassword(ACTIVE_MQ_PASSWORD);
        return connectionFactory;
    }

    private static ConnectionFactory rabbitMQConnectionFactory() throws NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory  = new ConnectionFactory();

        connectionFactory.setUsername(ACTIVE_MQ_USERNAME);
        connectionFactory.setPassword(RABBIT_MQ_PASSWORD);

        connectionFactory.setHost("b-ace70ba9-07ac-4edb-8bda-1d9da3aa1703.mq.us-east-1.amazonaws.com");
        connectionFactory.setPort(5671);

        connectionFactory.useSslProtocol();

        return connectionFactory;
    }

    private static void publishMessageToRabbitMQ(ConnectionFactory connectionFactory, int value) throws IOException, TimeoutException {
        com.rabbitmq.client.Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        String message = "hello world times: " + value + " times";
        System.out.println("publish: " + message);
        byte[] messageBodyBytes = message.getBytes();

        channel.basicPublish("", "testing-queue",
                new AMQP.BasicProperties.Builder()
                        .contentType("text/plain")
                        .userId(ACTIVE_MQ_USERNAME)
                        .build(),
                messageBodyBytes);

        channel.close();
        connection.close();
    }

}
