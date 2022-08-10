package com.vvmishra.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;

import javax.jms.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Hello world!
 */
public class AmazonMQConsumer {
    // Specify the connection parameters.
    private final static String WIRE_LEVEL_ENDPOINT
            = "ssl://b-a37b689a-d1e9-426d-8415-87e3b4331492-1.mq.ap-south-1.amazonaws.com:61617";
    private final static String ACTIVE_MQ_USERNAME = "vvmishra";
    private final static String ACTIVE_MQ_PASSWORD = "Password@2244";

    public static void main(String[] args) throws JMSException, InterruptedException {
        final ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();
        final PooledConnectionFactory pooledConnectionFactory = createPooledConnectionFactory(connectionFactory);

        receiveMessage(connectionFactory);

        pooledConnectionFactory.stop();
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

}
