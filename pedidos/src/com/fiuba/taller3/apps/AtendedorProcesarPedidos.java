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

public class AtendedorProcesarPedidos {

	public static void main(String[] args) throws Exception {
		
		LoggerCrawler logger = new LoggerCrawler("Pedidos.log");
		PersistenciaDePedidos pedidos = new PersistenciaDePedidos();
		PersistenciaDeStock stock = new PersistenciaDeStock();
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare("pedidosAProcesar", true, false, false, null);
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
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println(" [x] Done");
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			}

			private void doWork(String message) throws InterruptedException, Exception {
				
				logger.getLogger().info("Voy a procesar el pedido : " + message);
				
				String[] parts = message.split(Pattern.quote("|"));
				
				Pedido p = new Pedido(parts[0], parts[1], parts[2]);
				p.setEstado(EstadoPedido.getValue(new Integer(parts[3])));
				
				if ( stock.getDisponibilidadDeProductos(p.getProductos()) ) {
					p.setEstado(EstadoPedido.ACEPTADO);
				}
				else {
					p.setEstado(EstadoPedido.RECHAZADO);
				}
				if ( pedidos.updatePedidoF(p) ) {
					logger.getLogger().info("Cambio el estado del pedido " + p.getIdPedido() 
						+ " a: " + p.getEstado());
				} else {
					logger.getLogger().warning("No se pudo hacer update dle estado del pedido");
				}

			}

		};
		
	    channel.basicConsume("pedidosAProcesar", false, consumer);
	}
}
