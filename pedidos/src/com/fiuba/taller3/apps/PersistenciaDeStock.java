package com.fiuba.taller3.apps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PersistenciaDeStock {

	private Map<Integer, Integer> stock;
	
	public PersistenciaDeStock() {
		stock = new HashMap<Integer, Integer>();
	}
	
	public synchronized boolean quitarStock(Integer idProducto, Integer cantidadProducto) {
		
		if ( stock.get(idProducto) == null ) {
			return false;
		}
		if (stock.get(idProducto) >= cantidadProducto ) {
			stock.put(idProducto, stock.get(idProducto) - cantidadProducto);
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized int agregarStock(Integer idProducto, Integer cantidadProducto) {
		
		if ( stock.get(idProducto) == null ) {
			stock.put(idProducto, new Integer(0));
		}
		stock.put(idProducto, stock.get(idProducto) + cantidadProducto);
		return idProducto;
	}
	
	public synchronized boolean getDisponibilidadDeProductos( Set<ProductoAPedir> pedidos ) {
		
		boolean isOk = true;
		for (ProductoAPedir pedido : pedidos) {
			if (stock.get(pedido.getIdProducto()) == null ||
					stock.get(pedido.getIdProducto()) < pedido.getCantidad() ) {
				return false;
			}
		}
		
		return isOk;
	}
	
}
