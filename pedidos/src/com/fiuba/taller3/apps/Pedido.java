package com.fiuba.taller3.apps;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Pedido implements Serializable {

	private static final long serialVersionUID = -5792936071834358022L;
	private Long idPedido;
	private EstadoPedido estado;
	private String nombreSolicitante;
	private Set<ProductoAPedir> productos;
	
	public Pedido(String id, String user, String pedidos) {
		
		this.setIdPedido(new Long(id));
		this.setNombreSolicitante(user);
		this.setProductos(armarProductos(pedidos));
		this.estado = null;
	}
	
	private Set<ProductoAPedir> armarProductos(String string) {

		Set<ProductoAPedir> result = new HashSet<ProductoAPedir>();
		String[] keyValue = string.split(Pattern.quote(";"));
		for (String s : keyValue) {
			String[] pair = s.split(Pattern.quote(":"));
			ProductoAPedir producto = new ProductoAPedir(new Integer(
					pair[0]), new Integer(pair[1]));
			result.add(producto);
		}
		return result;
	}
	
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
	public Long getIdPedido() {
		return idPedido;
	}
	public void setIdPedido(Long id) {
		this.idPedido = id;
	}
	
	
}
