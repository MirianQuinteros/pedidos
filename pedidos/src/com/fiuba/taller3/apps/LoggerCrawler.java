package com.fiuba.taller3.apps;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerCrawler {

	private FileHandler fh;
	private Logger logger;
	private SimpleFormatter formatter;
	
	public LoggerCrawler(String fileName) {			
		
		try {
			fh = new FileHandler(fileName);
			logger = Logger.getLogger(fileName);
			logger.addHandler(fh);
			formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);
			logger.setUseParentHandlers(false);
			
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
}
