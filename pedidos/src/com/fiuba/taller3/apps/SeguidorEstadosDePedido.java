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

public class SeguidorEstadosDePedido {

public static void main(String[] args) throws Exception {
		
		PersistenciaDePedidos pedidos = new PersistenciaDePedidos();
		LoggerCrawler logger = new LoggerCrawler(SeguidorEstadosDePedido.class.getSimpleName() + ".log");
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.queueDeclare("estadosPedido", true, false, false, null);
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
				if (pair.length != 3) {
					System.out.println("Algo salio mal, no puedo updetear el estado del pedido");
					return;
				}
				if ( pair[1].equals("a") ) { //Operacion de actualizacion
					actualizarEstado(pair[2], new Integer(pair[0]));
				}
				
				if (pair[1].equals("c")) { //operacion de consulta
					//enviar por una cola la respuesta
				}
			}

			private void actualizarEstado(String op, Integer id) {
				switch (op) {
				case "e":
					if ( pedidos.getPedido(id) != null) {
						pedidos.getPedido(id).setEstado(EstadoPedido.ENTREGADO);
					}
					break;
				case "rb":
					if ( pedidos.getPedido(id) != null) {
						pedidos.getPedido(id).setEstado(EstadoPedido.RECIBIDO);
					}
					break;
				case "rz":
					if ( pedidos.getPedido(id) != null) {
						pedidos.getPedido(id).setEstado(EstadoPedido.RECHAZADO);
					}
					break;
				case "a" :
					if ( pedidos.getPedido(id) != null) {
						pedidos.getPedido(id).setEstado(EstadoPedido.ACEPTADO);
					}
					break;
				default:
					break;
				}
				
				logger.getLogger().info("El pedido " + id + " esta : " + pedidos.getPedido(id).getEstado());
			
			}
		};
	    channel.basicConsume("estadosPedido", false, consumer); 
	}
}
