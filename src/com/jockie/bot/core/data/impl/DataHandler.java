package com.jockie.bot.core.data.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.data.Data;

public class DataHandler {
	
	private static String dataPath = "./data/";
	
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	private static List<Data<?>> data = new ArrayList<>();
	
	private static boolean updateScheduled = true;
	private static int updateInterval = 60;
	private static TimeUnit updateUnit = TimeUnit.SECONDS;
	
	private static ScheduledFuture<?> scheduledFuture;
	
	private static boolean initalized = false;
	
	private static String getPath(Data<?> data) {
		String path = DataHandler.dataPath;
		
		if(data.getPath() != null && data.getPath().length() > 0) {
			path += data.getPath() + "/";
		}
		
		if(data.getName() != null && data.getName().length() > 0) {
			path += data.getName();
		}else{
			path += "Undefined";
		}
		
		path += ".json";
		
		return path;
	}
	
	private static void startScheduledUpdate() {
		if(DataHandler.updateScheduled) {
			if(DataHandler.scheduledFuture == null) {
				DataHandler.scheduledFuture = DataHandler.executor.scheduleAtFixedRate(new Runnable() {
					public void run() {
						try {
							DataHandler.saveAll();
						}catch(IOException e) {
							e.printStackTrace();
						}
					}
				}, DataHandler.updateInterval, DataHandler.updateInterval, DataHandler.updateUnit);
			}
		}
	}
	
	private static void stopScheduledUpdate() {
		if(DataHandler.scheduledFuture != null) {
			DataHandler.scheduledFuture.cancel(false);
			
			DataHandler.scheduledFuture = null;
		}
	}
	
	public static void init() {
		if(!DataHandler.initalized) {
			DataHandler.initalized = true;
			
			try {
				File dataPath = new File(DataHandler.dataPath);
				if(!dataPath.exists()) {
					dataPath.mkdirs();
				}
				
				DataHandler.loadAll();
				
				DataHandler.startScheduledUpdate();
				
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						DataHandler.saveAll();
					}catch(IOException e) {
						e.printStackTrace();
					}
				}));
			}catch(IOException e) {
				e.printStackTrace();
				
				throw new RuntimeException();
			}
		}
	}
	
	public static void setUpdate(boolean enabled) {
		DataHandler.updateScheduled = enabled;
		
		if(DataHandler.initalized) {
			if(enabled) {
				DataHandler.startScheduledUpdate();
			}else{
				DataHandler.stopScheduledUpdate();
			}
		}
	}
	
	public static void setUpdate(int time, TimeUnit unit) {
		DataHandler.updateInterval = time;
		DataHandler.updateUnit = unit;
	}
	
	public static void setDataPath(String path) {
		DataHandler.dataPath = path;
	}
	
	public static String getDataPath() {
		return DataHandler.dataPath;
	}
	
	public static void addData(Data<?> data) {
		DataHandler.data.add(data);
	}
	
	public static void saveAll() throws IOException {
		for(Data<?> data : DataHandler.data) {
			DataHandler.save(data);
		}
	}
	
	public static void loadAll() throws IOException {
		for(Data<?> data : DataHandler.data) {
			DataHandler.load(data);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static synchronized void save(Data data) throws IOException {
		DataLoader.saveObject(new File(DataHandler.getPath(data)), data.save());
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static synchronized void load(Data data) throws IOException {
		File file = new File(DataHandler.getPath(data));
		if(!file.exists()) {
			Object actualData = data.save();
			
			if(actualData instanceof Object[]) {
				DataLoader.createFileList(file);
			}else if(actualData instanceof Collection) {
				DataLoader.createFileList(file);
			}else{
				DataLoader.createFileObject(file);
			}
			
			System.out.println(DataHandler.getPath(data) + " does not exist, creating empty file!");
		}
		
		data.load(DataLoader.loadObject(file, data.getType()));
	}
}