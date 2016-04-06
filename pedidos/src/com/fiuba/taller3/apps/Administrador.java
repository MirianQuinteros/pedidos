package com.fiuba.taller3.apps;

import com.rabbitmq.client.*;

public class Administrador {

	public static void main(String[] args) throws Exception {

		String idProducto = null;
		String cantidadAAgregarAStock = null;

		if (args.length != 2) {
			printHelp();
			return;
		}

		idProducto = args[0];
		cantidadAAgregarAStock = args[1];

		System.out.println(idProducto);
		System.out.println(cantidadAAgregarAStock);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare("adminStock", true, false, false, null);
		String message = idProducto.concat("|").concat(cantidadAAgregarAStock);
		channel.basicPublish("", "adminStock", null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + message + "'");
		channel.close();
		connection.close();

	}

	private static void printHelp() {

		System.out
				.println("El administrador debe ejecutarse con los siguientes argumentos:");
		System.out
				.println("admin <idProducto> <Cantidad a agregar al stock>");

	}
}
