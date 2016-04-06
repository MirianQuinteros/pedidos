package com.fiuba.taller3.apps;

import java.io.IOException;
import java.util.regex.Pattern;

import com.rabbitmq.client.*;

public class AtendedorConsultasStatus {

	public static void main(String[] args) throws Exception {
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare("consultas", true, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	    channel.basicQos(1);
	    
	    Connection connectionToEstado = factory.newConnection();
	    Channel channelEstado = connectionToEstado.createChannel();
	    channelEstado.queueDeclare("estadosPedido", true, false, false, null);
	    channelEstado.basicQos(1);

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
				
				String[] pair = message.split(Pattern.quote("|"));
				
				if (pair.length != 2) {
					System.out.println("Algo salio mal con el formato de la consulta");
				}
				channelEstado.basicPublish("", "estadosPedido", null, message.getBytes("UTF-8"));
				System.out.println(" [x] Sent '" + message + "'");
				
			}
		};
	    channel.basicConsume("consultas", false, consumer);
	}
}
