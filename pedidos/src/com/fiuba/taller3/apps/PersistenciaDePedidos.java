package com.fiuba.taller3.apps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PersistenciaDePedidos {

	private static String folder = "pedidoFiles/";
	private Map<String, File> readers;
	private Map<String, FileChannel> readersLocks;
	private FileChannel createFileLock;
	private RandomAccessFile cfaccess;
	
	public PersistenciaDePedidos() throws IOException {
		
		cfaccess = new RandomAccessFile("nf.lock", "rw");
		createFileLock = cfaccess.getChannel();
		
        cargarArchivosDisponibles();
		
	}

	private void cargarArchivosDisponibles() throws IOException {

		readers = new HashMap<String, File>();
		readersLocks = new HashMap<String, FileChannel>();
		
		BufferedReader bufferedReader = null;
		
		try {
			
			File archivo = new File(folder);
			
			if (archivo.exists() && archivo.isDirectory()) {
				for (File file : archivo.listFiles() ){
					if (file.getName().endsWith(".lock")) {
						
					} else {
					String index = file.getName().
							substring(file.getName().length() - 3);
					readers.put(index, file);
					FileChannel ch = 
							new RandomAccessFile(folder + "/" + index + ".lock", "rw")
							.getChannel();
					readersLocks.put(index, ch);
					}
				}
			} else {
				File dir = new File(folder);
				dir.mkdir();
			}
		} catch (Exception e ) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null)
				bufferedReader.close();
		}

	}
	
	public boolean updatePedidoF(Pedido pedido) {
		
		Boolean isOk = true;
		String idText = pedido.getIdPedido().toString();
		String index = idText.substring(idText.length() - 3);
		
        boolean dataFound = false;
        
		try {
			FileChannel ch = readersLocks.get(index);
			if (ch == null) {
				if (!searchFile(index)) {
					return !isOk;
				}
			}
			FileLock lock = readersLocks.get(index).lock();
			
				System.out.println("el archivo a modificar es : " + readers.get(index).getName());
				String originalName = folder + "/" + readers.get(index).getName();
				String tmpName = folder + "/" + readers.get(index).getName() + ".tmp";
				
				if (!readers.get(index).renameTo(new File(tmpName)) ) {
					System.out.println("No pude renombrar archivo");
				}
				
				File modified = new File(originalName);
				BufferedReader file = new BufferedReader(new FileReader(tmpName));
				
				FileWriter fw = new FileWriter(modified, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter writer = new PrintWriter(bw);
				
				String line;

			    while ((line = file.readLine()) != null) {
			    	if (line.startsWith(pedido.getIdPedido().toString())) {
			    		System.out.println("encontre el registro  a modificar");
			    		line = line.replace(EstadoPedido.RECIBIDO
							.toString(), pedido.getEstado().toString());
			    		writer.println(line);
			    		dataFound = true;
			    	} else {
			    		writer.println(line);
			    	}
			    }
			    file.close();
			    writer.close();
			    bw.close();
			    fw.close();
			    readers.put(index, modified);
			    new File(tmpName).delete();
				lock.release();
			return isOk && dataFound;
		} catch (IOException e) {
			return !isOk;
		} catch (Exception e) {
			e.printStackTrace();
			return !isOk;
		}
		
	}

	public boolean guardarpedidoF(Pedido pedido) {

		Boolean isOk = true;
		String idText = pedido.getIdPedido().toString();
		String index = idText.substring(idText.length() - 3);
		
		if ( readers.get(index) == null) {
			try {
				if ( !searchFile(index) ) {
					if ( !crearArchivo(pedido.getIdPedido()) )
						return !isOk;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileLock lock = readersLocks.get(index).lock();
				
				try (FileWriter fw = new FileWriter(readers.get(index), true);
					    BufferedWriter bw = new BufferedWriter(fw);
					    PrintWriter out = new PrintWriter(bw)) {
					
					StringBuffer ss = new StringBuffer();
					for (ProductoAPedir ped : pedido.getProductos()) { 
						ss.append(ped.getIdProducto()).append(":")
						  .append(ped.getCantidad()).append(";");
					}
					out.println(pedido.getIdPedido() + "|" + pedido.getNombreSolicitante()
							+ "|" + ss.toString() + "|" + pedido.getEstado() );
					
				} catch (FileNotFoundException e) {
					return !isOk;
				} catch (Exception e) {
					return !isOk;
				}
				lock.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return isOk;
	}
	
	private boolean searchFile(String index) throws FileNotFoundException {
		
		File archivo = new File(folder);
		if (archivo.exists() && archivo.isDirectory()) {
			for (File file : archivo.listFiles() ) {
				if (file.getName().contains(index) && !file.getName().endsWith(".lock")) {
					this.readers.put(index, file);
					FileChannel ch = new RandomAccessFile(folder + "/" + index + ".lock", "rw")
									.getChannel();
					this.readersLocks.put(index, ch);
					return true;
				}
			}
		}
		return false;
	}

	private boolean crearArchivo(Long idPedido) {
	
		boolean result = false;
		try {
			FileLock lock = createFileLock.lock();

				String idText = idPedido.toString();
				String index = idText.substring(idText.length() - 3);
				String fileName = folder + "/PED" + index;
				File archivo = new File(fileName);
				if (!archivo.exists() && readers.get(index) == null) {
					archivo.createNewFile();
					readers.put(index, archivo);
					
					FileChannel ch = 
							new RandomAccessFile(folder + "/" + index + ".lock", "rw")
							.getChannel();
					readersLocks.put(index, ch);
					result = true;
				}
				
				lock.release();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@SuppressWarnings("resource")
	public Pedido getPedidoF(Integer id) {

		String index = id.toString().substring(id.toString().length() - 3 );
		
		Pedido p = null;

		try {
			FileLock lock = readersLocks.get(index).lock();
							
			FileReader in = new FileReader(readers.get(index));
			
			String line = null;
			BufferedReader bufRead = new BufferedReader(in);
				while ( (line = bufRead.readLine()) != null) {
					String[] pairs = line.split(Pattern.quote("|"));
					if (pairs[0].equals(id.toString())) {
						p = new Pedido(pairs[0], pairs[1], pairs[2]);
						p.setEstado(EstadoPedido.getValue(new Integer(pairs[3])));
						break;
					}
				}
				lock.release();

		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + readers.get(index)
					+ "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + readers.get(index)
					+ "'");
		} catch (Exception e) {
		}
		return p;
	}
	
}
