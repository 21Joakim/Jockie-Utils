package com.jockie.bot.core.data.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jockie.bot.core.data.Data;

public class DataHandler {
	
	private static String dataPath = "./data/";
	
	private static List<Data<?>> data = new ArrayList<>();
	
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
		DataLoader.saveObject(new File(DataHandler.getPath(data)), data.getSavableData());
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static synchronized void load(Data data) throws IOException {
		File file = new File(DataHandler.getPath(data));
		if(file.getParentFile() != null && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		if(!file.exists()) {
			Object actualData = data.getSavableData();
			
			if(actualData instanceof Object[]) {
				DataLoader.createFileList(file);
			}else if(actualData instanceof Collection) {
				DataLoader.createFileList(file);
			}else{
				DataLoader.createFileObject(file);
			}
			
			System.out.println(DataHandler.getPath(data) + " does not exist, creating empty file!");
		}
		
		data.setLoadableData(DataLoader.loadObject(file, data.getType()));
	}
}