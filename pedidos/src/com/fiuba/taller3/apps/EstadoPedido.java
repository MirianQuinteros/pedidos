package com.fiuba.taller3.apps;

public enum EstadoPedido {

	RECIBIDO, ACEPTADO, RECHAZADO, ENTREGADO;
	
	public static EstadoPedido getValue(int i) {
		return values()[i];
	}
}
