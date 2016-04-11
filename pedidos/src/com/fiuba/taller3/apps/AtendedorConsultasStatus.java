package com.fiuba.taller3.apps;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;

public class AtendedorConsultasStatus {

	public static void main(String[] args) throws Exception {
		PersistenciaDePedidos pedidos = new PersistenciaDePedidos();
		Connection connection = null;
		Channel channel = null;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");

			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare("consultas", false, false, false, null);

			channel.basicQos(1);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume("consultas", false, consumer);

			System.out.println(" [x] Awaiting RPC requests");

			while (true) {
				String response = null;

				QueueingConsumer.Delivery delivery = consumer.nextDelivery();

				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();

				try {
					String message = new String(delivery.getBody(), "UTF-8");
					Integer id = Integer.parseInt(message);
					response = pedidos.getPedidoF(id).getEstado().toString();
				} catch (Exception e) {
					System.out.println(" [.] " + e.toString());
					response = "";
				} finally {
					channel.basicPublish("", props.getReplyTo(), replyProps,
							response.getBytes("UTF-8"));
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
							false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception ignore) {
				}
			}
		}
	}
}
