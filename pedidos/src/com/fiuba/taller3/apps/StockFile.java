package com.fiuba.taller3.apps;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class StockFile {

	private File file;
	private FileChannel lock;
	private FileLock activeLock;
	
	public StockFile(File f, FileChannel fc) {
		this.file = f;
		this.lock = fc;
		this.setActiveLock(null);
	}
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public FileChannel getLock() {
		return lock;
	}
	public void setLock(FileChannel lock) {
		this.lock = lock;
	}

	public FileLock getActiveLock() {
		return activeLock;
	}

	public void setActiveLock(FileLock activeLock) {
		this.activeLock = activeLock;
	}
	
	
}
