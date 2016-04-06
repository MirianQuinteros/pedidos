package com.fiuba.taller3.apps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersistenciaDePedidos {

	private static final String fileName = "pedidos";
	private Map<Integer, Pedido> pedidos;
	private Map<EstadoPedido, List<Integer>> pedidosPerStatus;
	private Integer idCount;
	private FileReader fileReader;
	
	public PersistenciaDePedidos() throws FileNotFoundException {
		pedidos = new HashMap<Integer, Pedido>();
		pedidosPerStatus = new HashMap<EstadoPedido, List<Integer>>();
		pedidosPerStatus.put(EstadoPedido.RECIBIDO, new LinkedList<Integer>());
		pedidosPerStatus.put(EstadoPedido.ACEPTADO, new LinkedList<Integer>());
		pedidosPerStatus.put(EstadoPedido.RECHAZADO, new LinkedList<Integer>());
		pedidosPerStatus.put(EstadoPedido.ENTREGADO, new LinkedList<Integer>());
        fileReader = new FileReader(fileName);
		idCount = 0;
	}
	
	public synchronized int guardarpedidoF(Pedido pedido) {
		
	
		
		try (Writer writer = new BufferedWriter(
						new OutputStreamWriter(
						new FileOutputStream(fileName), "utf-8"))) {
			
		   writer.write("something");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return idCount;
			
       
		
	}
	
	public synchronized Pedido getPedidoF(Integer id) {
		try {
		 BufferedReader bufferedReader = new BufferedReader(fileReader);

	        String line;
			while((line = bufferedReader.readLine()) != null) {
	           // System.out.println(line);
				
	        }   

	        bufferedReader.close();         
			} catch(FileNotFoundException ex) {
	            System.out.println("Unable to open file '" + fileName + "'");                
	        } catch(IOException ex) {
	        	System.out.println("Error reading file '" + fileName + "'");
	        }
		return null;
	}
	
	public synchronized int guardarPedido(Pedido pedido) {
		
		pedido.setIdPedido(idCount++);
		
		pedidos.put(pedido.getIdPedido(), pedido);
		
		pedidosPerStatus.get(EstadoPedido.RECIBIDO).add(pedido.getIdPedido());
		
		return pedido.getIdPedido();
	}
	
	public Pedido getPedido(Integer id) {
		
		return pedidos.get(id);
	}
	
	public synchronized Set<Pedido> listPerStatus(EstadoPedido estado) {
		
		Set<Pedido> result = new HashSet<Pedido>();
		List<Integer> ids = pedidosPerStatus.get(estado);
		for (Integer i : ids) {
			result.add(this.pedidos.get(i));
		}
		return result;
	}
	
	public synchronized Set<Pedido> listPerUsuario(String user) {
		
		Set<Pedido> result = new HashSet<Pedido>();
		for (Integer i : pedidos.keySet()) {
			if (pedidos.get(i).getNombreSolicitante().equals(user)) {
				result.add(pedidos.get(i));
			}
		}
		return result;
	}
	
}
