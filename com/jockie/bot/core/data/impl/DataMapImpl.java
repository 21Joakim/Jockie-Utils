package com.jockie.bot.core.data.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DataMapImpl<Key, Data> extends DataImpl<Data[]> {
	
	private Map<Key, Data> data = new ConcurrentHashMap<>();
	
	private Function<Data, Key> function;
	
	public DataMapImpl(Function<Data, Key> function, Class<Data[]> clazz, String name) {
		super(clazz, name);
		
		this.function = function;
	}
	
	public DataMapImpl(Function<Data, Key> function, Class<Data[]> clazz) {
		this(function, clazz, null);
	}
	
	@SuppressWarnings("unchecked")
	public Data[] save() {
		return (Data[]) this.data.values().toArray();
	}
	
	public void load(Data[] types) {
		for(Data data : types) {
			this.data.put(this.function.apply(data), data);
		}
	}
	
	public Map<Key, Data> getMap() {
		return this.data;
	}
	
	public Data getByKey(Key key) {
		return this.data.get(key);
	}
	
	public void add(Data data) {
		this.data.put(this.function.apply(data), data);
	}
	
	public Data remove(Key key) {
		return this.data.remove(key);
	}
	
	public void removeByData(Data data) {
		this.remove(this.function.apply(data));
	}
}