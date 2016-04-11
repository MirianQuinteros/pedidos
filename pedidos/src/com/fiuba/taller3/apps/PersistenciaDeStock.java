package com.fiuba.taller3.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PersistenciaDeStock {

	private static final Integer LIMIT = 5;
	private static String folder = "stockF";
	private Map<Integer, StockCluster> clusters;
	
	public PersistenciaDeStock() throws IOException {
		
		clusters = new HashMap<Integer, StockCluster>();
		loadFilesAndLocks();
	}
		
	private void loadFilesAndLocks() throws IOException {
		
		try {
			File archivo = new File(folder);	
			if (archivo.exists() && archivo.isDirectory()) {
				for (File file : archivo.listFiles() ) {
					if (file.getName().endsWith(".lock")) {
						
					} else {
					//filename PRD003-002
					String clusterId = file.getName().substring(3); //003-002
					String productCode = clusterId.substring(0, clusterId.indexOf("-")); //003
					String fileNumber = clusterId.substring(clusterId.indexOf("-")  + 1); // 002
					Integer pc = new Integer(productCode);
					Integer fn = new Integer(fileNumber);
					
					StockCluster stockCluster = new StockCluster(pc);
					FileChannel ch = 
							new RandomAccessFile(folder + "/" + file.getName() + ".lock", "rw")
							.getChannel();
					
					if (clusters.get(pc) == null) {
						clusters.put(pc, stockCluster);
					}
					clusters.get(pc).getFiles().put(fn, new StockFile(file, ch));
					}
				}
			} else {
				File dir = new File(folder);
				dir.mkdir();
			}
		} catch (Exception e ) {
			e.printStackTrace();
		}
		
	}
	
	public int agregarStock(Integer idProducto, Integer cantidadProducto ) {
		
		if ( clusters.get(idProducto) == null ) {
		
			crearArchivo(idProducto, cantidadProducto);
			return idProducto;
		
		} else {
			
			Integer cantFiles = clusters.get(idProducto).getMaxClusters();
			Integer part = cantidadProducto / LIMIT;
			int i = LIMIT;
			
			while ( i != 0 ) {
				
				Integer randomIndex = new Random().nextInt(cantFiles - 1);
				
				try {
					
					StockFile file = clusters.get(idProducto).getFiles().get(randomIndex);
					FileLock lock = file.getLock().lock();
					
						String originalName = folder + "/" + file.getFile().getName();
						String tmpName = folder + "/" + file.getFile().getName() + ".tmp";

						if ( ! file.getFile().renameTo(new File(tmpName)) ) {
							System.out.println("No pude renombrar archivo");
						}
						
						i--;
						File modified = new File(originalName);
						BufferedReader filereader = new BufferedReader(
								new FileReader(tmpName));
						PrintWriter writer = new PrintWriter(modified, "UTF-8");

						String line = filereader.readLine();
						Integer lastCount = new Integer(line);
						if ( cantidadProducto < part ) {
							part = cantidadProducto;
						}
						writer.println(lastCount + part);
						cantidadProducto = cantidadProducto - part;
						
						filereader.close();
						writer.close();
						clusters.get(idProducto).getFiles()
								.get(randomIndex).setFile(modified);
						new File(tmpName).delete();
						lock.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return idProducto;
	}

	private boolean crearArchivo(Integer idProducto, Integer cantidadProducto) {
		
		boolean result = false;
		try {
			System.out.println("Agregando nuevo producto");
			RandomAccessFile a = new RandomAccessFile( folder + "/newfile.lock", "rw");
			
			FileLock lock = a.getChannel().lock();
			

				clusters.put(idProducto, new StockCluster(idProducto));
				
				Integer part = cantidadProducto / LIMIT;
				System.out.println("agregare : " + part);
				int count = 0;
				
				while ( cantidadProducto > 0 ) {
					
					String fileName = folder + "/PRD" + idProducto + "-" + count;
					File archivo = new File(fileName);
						
					if ( !archivo.exists() ) {
						
						archivo.createNewFile();
						if (cantidadProducto > part) {
							cantidadProducto = cantidadProducto - part;
						} else {
							part = cantidadProducto;
							cantidadProducto = -1;
						}
						PrintWriter writer = new PrintWriter(archivo, "UTF-8");
						writer.write(part.toString());
						FileChannel ch = 
									new RandomAccessFile(fileName + ".lock", "rw")
									.getChannel();
						clusters.get(idProducto).getFiles().put(count, new StockFile(archivo, ch));
						result = true;
						writer.close();
						count++;
					}
				}
				
				lock.release();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public boolean getDisponibilidadDeProductos( Set<ProductoAPedir> pedidos ) {
		
		boolean isOk = true;
		Map<Integer, StockFile> pedidoFiles = new HashMap<Integer, StockFile>();

		for (ProductoAPedir pedido : pedidos) {
			
			Integer key = pedido.getIdProducto();
			StockCluster cluster = clusters.get(key);
			
			if ( cluster  == null || cluster.getFiles().isEmpty() ) {
				
				System.out.println("Todavia no tenemos nada de ese producto");
				return !isOk;
				
			} else {
				
				Integer intentos = 2;
				boolean found = false;
				Integer randomIndex = new Random().nextInt(cluster.getMaxClusters() - 1);
				
				try {
					
					while ( intentos != 0 && !found ) {
						intentos--;
						StockFile stockFile = cluster.getFiles().get(randomIndex);
						FileLock lock = stockFile.getLock().lock();
					
							
							BufferedReader file = new BufferedReader(
									new FileReader(stockFile.getFile()));

							String line = file.readLine();
							
							if (line == null) {
								
								randomIndex = new Random().nextInt(cluster.getMaxClusters() -1);
							
							} else {
								
								Integer cantProductoDisponible = new Integer(line);
								
								if ( cantProductoDisponible < pedido.getCantidad() ) {
									randomIndex = new Random().nextInt(cluster.getMaxClusters() -1);
									
								} else {
									found = true;
									stockFile.setActiveLock(lock);
									pedidoFiles.put(key,stockFile);
								}
							}
							
							file.close();
							if (!found) lock.release();
					}
				} catch (IOException e) {
					
				}
			}
		}
		
		if (pedidoFiles.size() == pedidos.size()) { //obtuve todos los locks
			for (ProductoAPedir p : pedidos) {
				try {
					tomarDelStock(p.getIdProducto(), p.getCantidad(),
						pedidoFiles.get(p.getIdProducto()) );
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return isOk;
		} else {
			//hago release de todos los locks
			for ( StockFile sf : pedidoFiles.values() ) {
				try {
					sf.getActiveLock().release();
					sf.setActiveLock(null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return !isOk;
		}
	}
	

	private boolean tomarDelStock(Integer idProducto, Integer cantidadProducto,
			StockFile files) throws IOException {
		
		String originalName = folder + "/" + files.getFile().getName();
		String tmpName = folder + "/" + files.getFile().getName() + ".tmp";

		if ( ! files.getFile().renameTo(new File(tmpName)) ) {
			System.out.println("No pude renombrar archivo");
			files.getActiveLock().release();
			files.setActiveLock(null);
			return false;
		}

		File modified = new File(originalName);
		BufferedReader filereader = new BufferedReader(new FileReader(tmpName));
		PrintWriter writer = new PrintWriter(modified, "UTF-8");

		String line = filereader.readLine();
		Integer lastCount = new Integer(line);
		Integer result = lastCount - cantidadProducto;
		writer.println( result.toString() );
		
		filereader.close();
		writer.close();
		files.setFile(modified);
		new File(tmpName).delete();
		files.getActiveLock().release();
		files.setActiveLock(null);
		return true;
	}

}
