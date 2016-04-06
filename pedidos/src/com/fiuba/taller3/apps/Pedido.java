package com.fiuba.taller3.apps;

import java.util.Set;

public class Pedido {

	private Integer idPedido;
	private EstadoPedido estado;
	private String nombreSolicitante;
	private Set<ProductoAPedir> productos;
	
	public EstadoPedido getEstado() {
		return estado;
	}
	public void setEstado(EstadoPedido estado) {
		this.estado = estado;
	}
	public String getNombreSolicitante() {
		return nombreSolicitante;
	}
	public void setNombreSolicitante(String nombreSolicitante) {
		this.nombreSolicitante = nombreSolicitante;
	}
	public Set<ProductoAPedir> getProductos() {
		return productos;
	}
	public void setProductos(Set<ProductoAPedir> productos) {
		this.productos = productos;
	}
	public Integer getIdPedido() {
		return idPedido;
	}
	public void setIdPedido(Integer id) {
		this.idPedido = id;
	}
	
	
}
