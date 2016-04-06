package com.fiuba.taller3.apps;

import java.io.IOException;
import java.util.regex.Pattern;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ManejadorDeStock {
	
	public static void main(String[] args) throws Exception {
		
		PersistenciaDeStock stock = new PersistenciaDeStock();
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.queueDeclare("cambiosStock", false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

	    channel.basicQos(1);

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
				} finally {
					System.out.println(" [x] Done");
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			}

			private void doWork(String message) throws InterruptedException {
				
				String[] pair = message.split(Pattern.quote("|"));
				if ( pair.length != 3) {
					System.out.println("Algo fallo en procesar el cambio de stock" + pair);
					return;
				}
				if ( pair[2].equals("add") ) {
					stock.agregarStock(new Integer(pair[0]), new Integer(pair[1]));
					System.out.println("Se agrego " + pair[1] + " del producto " + pair[0]);
				}
				
			}
		};
	    channel.basicConsume("cambiosStock", false, consumer);
	}
}
