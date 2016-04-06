package com.fiuba.taller3.apps;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class AtendedorEmpleado {

public static void main(String[] args) throws Exception {
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare("entregasRealizadas", true, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	    channel.basicQos(1);

	    Connection connectionToPedido = factory.newConnection();
	    Channel channelPedido = connectionToPedido.createChannel();
	    channelPedido.queueDeclare("estadosPedido", true, false, false, null);
	    channelPedido.basicQos(1);

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				
				String message = new String(body, "UTF-8");

				System.out.println(" [x] Received '" + message + "'");
				try {
					 doWork(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println(" [x] Done");
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			}

			private void doWork(String message) throws InterruptedException, Exception {
				
				message = message.concat("|a|e");
				channelPedido.basicPublish("", "estadosPedido", null, message.getBytes("UTF-8"));
				System.out.println(" [x] Sent '" + message + "'");
			}
		};
	    channel.basicConsume("entregasRealizadas", false, consumer);
	}
}
