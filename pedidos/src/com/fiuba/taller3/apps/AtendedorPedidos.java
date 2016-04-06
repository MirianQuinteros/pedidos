package com.fiuba.taller3.apps;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
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
	    channel.queueDeclare("pedidos", false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	    channel.basicQos(1);
	    
//	    Connection connectionToEstado = factory.newConnection();
//	    Channel channelEstado = connectionToEstado.createChannel();
//	    channelEstado.queueDeclare("estadosPedido", true, false, false, null);
//	    channelEstado.basicQos(1);

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
				
				message = message.concat("|rb");
				
				String[] part = message.split(Pattern.quote("|"));

				Pedido p = new Pedido();
				
				p.setNombreSolicitante(part[0]);
				p.setProductos(armarProductos(part[1]));
				p.setEstado(EstadoPedido.RECIBIDO);
				
				pedidos.guardarPedido(p);

			}

			private Set<ProductoAPedir> armarProductos(String string) {

				Set<ProductoAPedir> result = new HashSet<ProductoAPedir>();
				String[] keyValue = string.split(Pattern.quote(";"));
				for (String s : keyValue) {
					String[] pair = s.split(Pattern.quote(":"));
					ProductoAPedir producto = new ProductoAPedir(new Integer(
							pair[0]), new Integer(pair[1]));
					result.add(producto);
				}
				return result;
			}
		};
		
	    channel.basicConsume("pedidos", false, consumer);
	}
}
