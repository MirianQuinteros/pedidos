package com.fiuba.taller3.apps;

import java.util.HashMap;
import java.util.Map;

public class StockCluster {
	
	private Integer idProducto;
	private Map<Integer, StockFile> files;
	
	public StockCluster(Integer id ) {
		this.idProducto = id;
		files = new HashMap<Integer, StockFile>();
	}
	
	public Integer getIdProducto() {
		return idProducto;
	}
	public void setIdProducto(Integer idProducto) {
		this.idProducto = idProducto;
	}
	public Map<Integer, StockFile> getFiles() {
		return files;
	}
	public void setFiles(Map<Integer, StockFile> content) {
		this.files = content;
	}
	public Integer getMaxClusters() {
		return files.values().size();
	}

}
