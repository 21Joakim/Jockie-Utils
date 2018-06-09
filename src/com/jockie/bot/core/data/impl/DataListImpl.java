package com.jockie.bot.core.data.impl;

import java.util.List;
import java.util.Vector;

public class DataListImpl<Data> extends DataImpl<Data[], DataListImpl<Data>> {
	
	private List<Data> data = new Vector<>();
	
	public DataListImpl(Class<Data[]> clazz, String name) {
		super(clazz, name);
	}
	
	public DataListImpl(Class<Data[]> clazz) {
		this(clazz, null);
	}
	
	@SuppressWarnings("unchecked")
	public Data[] getSavableData() {
		return (Data[]) this.data.toArray();
	}
	
	public void setLoadableData(Data[] types) {
		for(Data data : types) {
			this.data.add(data);
		}
	}
	
	public List<Data> getList() {
		return this.data;
	}
	
	public DataListImpl<Data> self() {
		return this;
	}
}