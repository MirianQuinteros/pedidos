package com.fiuba.taller3.apps;

public class Consultar {

	public static void main(String[] args) throws Exception {

		String idPedido = null;

		if (args.length != 1) {
			printHelp();
			return;
		}

		idPedido = args[0];

		RPCClient estadoPedidoRPC = null;
		String response = null;
		try {
			estadoPedidoRPC = new RPCClient();

			System.out.println(" [x] Consultando estado para trackID: "
					+ idPedido);
			response = estadoPedidoRPC.call(idPedido);
			System.out.println(" [.] Estado '" + response + "'");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (estadoPedidoRPC != null) {
				try {
					estadoPedidoRPC.close();
				} catch (Exception ignore) {
				}
			}
		}
	}

	private static void printHelp() {

		System.out
				.println("El programa consultar debe ejecutarse con los siguientes argumentos:");
		System.out.println("consultar <idPedido>");
		System.out.println("Ejemplo: consultar 23885758902764");
	}
}
