package com.fiuba.taller3.apps;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Consultar {

public static void main(String[] args) throws Exception {
		
		String nombreUsuarioComprador = null;
		String idPedido = null;
		
		if (args.length != 2) {
			printHelp();
			return;
		}
		
		nombreUsuarioComprador = args[0];		
		idPedido = args[1];
		
		System.out.println(nombreUsuarioComprador);
		System.out.println(idPedido);
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare("consultas", true, false, false, null);

		Connection connectionReponse = factory.newConnection();
		Channel channelResponse = connectionReponse.createChannel();
		channelResponse.queueDeclare("respuestas", true, false, false, null);
		
		String message = nombreUsuarioComprador.concat("|").concat(
				idPedido);
		channel.basicPublish("", "consultas", null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent '" + message + "'");
		
		channel.close();
		connection.close();
		
		//Esperar respuesta
		
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
