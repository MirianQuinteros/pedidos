package com.fiuba.taller3.apps;

import java.util.Random;

import com.rabbitmq.client.*;

public class Comprador {

	public static void main(String[] args) throws Exception {
		
		String nombreUsuarioComprador = null;
		String productosPedidos = null;
		if (args.length != 2) {
			printHelp();
			return;
		}
		
		nombreUsuarioComprador = args[0];		
		productosPedidos = args[1];
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare("pedidos", true, false, false, null);
		
		Long id = new Random().nextLong();
		
		String message = id.toString().concat("|")
				.concat(nombreUsuarioComprador)
				.concat("|").concat(productosPedidos);
		
		channel.basicPublish("", "pedidos", null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + message + "'");
		System.out.println(" Tour TrackID is: " + id);
		channel.close();
		connection.close();
		
	}

	private static void printHelp() {
		
		System.out.println("El comprador debe ejecutarse con los siguientes argumentos:");
		System.out.println("comprador <nombre de usuario> <idProducto>:<cantidad de producto>");
		System.out.println("Si se desea pedir mas de un tipo de producto, la ultima estructura "
				+ "\"<idProducto>:<cantidad de producto>\" debe separarse con ; sin espacios");
		System.out.println("Ejemplo: "
				+ "comprador maria2001 4:1;2:2");
	}
	
}
