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

public class AtendedorPedidos {

	public static void main(String[] args) throws Exception {
		
		LoggerCrawler logger = new LoggerCrawler("Pedidos.log");
		PersistenciaDePedidos pedidos = new PersistenciaDePedidos();
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare("pedidos", true, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	    channel.basicQos(1);
	    
		Connection connectionToProcesar = factory.newConnection();
		Channel channelProcesar = connectionToProcesar.createChannel();
		channelProcesar.queueDeclare("pedidosAProcesar", true, false, false, null);
		channelProcesar.basicQos(1);

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
				
				logger.getLogger().info("Se recibio el pedido : " + message);
				
				String[] part = message.split(Pattern.quote("|"));
				
				Pedido p = new Pedido(part[0], part[1], part[2]);
				p.setEstado(EstadoPedido.RECIBIDO);
				
				if ( !pedidos.guardarpedidoF(p) ) {
					logger.getLogger().info("Error al guardar el pedido");
				}
				
				message = message.concat("|" + p.getEstado().ordinal());
				channelProcesar.basicPublish("", "pedidosAProcesar", null, message.getBytes());
			}
			
		};
		
	    channel.basicConsume("pedidos", false, consumer);
	}
}
