package com.jockie.bot.core.data.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataLoader {
	
	private static Gson gson = new Gson();
	
	public static <T> List<T> loadList(File file, Class<T[]> clazz) throws IOException {
		FileReader reader = new FileReader(file);
		
		T[] data = DataLoader.gson.fromJson(reader, clazz);
		
		reader.close();
		
		return Arrays.asList(data);
	}
	
	public static <T> T loadObject(File file, Class<T> clazz) throws IOException {
		FileReader reader = new FileReader(file);
		
		T data = DataLoader.gson.fromJson(new FileReader(file), clazz);
		
		reader.close();
		
		return data;
	}
	
	public static <T> void saveList(File file, Collection<T> list) throws IOException {
		Type type = new TypeToken<List<T>>(){}.getType();
		
		FileWriter writer = new FileWriter(file);
		
		DataLoader.gson.toJson(list, type, writer);
		
		writer.close();
	}
	
	public static <T> void saveObject(File file, T object) throws IOException {
		Type type = new TypeToken<T>(){}.getType();
		
		FileWriter writer = new FileWriter(file);
		
		DataLoader.gson.toJson(object, type, writer);
		
		writer.close();
	}
	
	public static void createFileList(File file) throws IOException {
		DataLoader.gson.newJsonWriter(new FileWriter(file)).beginArray().endArray().close();
	}
	
	public static void createFileObject(File file) throws IOException {
		DataLoader.gson.newJsonWriter(new FileWriter(file)).beginObject().endObject().close();
	}
}