package com.fiuba.taller3.apps;

public class ProductoAPedir {

	private Integer idProducto;
	private Integer cantidad;
	
	public ProductoAPedir(Integer id, Integer cantidad) {
		this.idProducto = id;
		this.cantidad = cantidad;
	}
	
	public Integer getIdProducto() {
		return idProducto;
	}
	public void setIdProducto(Integer id) {
		this.idProducto = id;
	}
	public Integer getCantidad() {
		return cantidad;
	}
	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}
	
}
