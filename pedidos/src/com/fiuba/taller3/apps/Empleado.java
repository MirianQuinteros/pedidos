package com.fiuba.taller3.apps;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Empleado {
	
	public static void main(String[] args) throws Exception {

		String idPedido = null;

		if (args.length != 1) {
			printHelp();
			return;
		}

		idPedido = args[0];

		System.out.println(idPedido);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare("entregasRealizadas", true, false, false, null);
		String message = idPedido;
		channel.basicPublish("", "entregasRealizadas", null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + message + "'");
		channel.close();
		connection.close();

	}

	private static void printHelp() {

		System.out
				.println("El empleado debe ejecutarse con los siguientes argumentos:");
		System.out
				.println("empleado <id pedido>");

	}
}
