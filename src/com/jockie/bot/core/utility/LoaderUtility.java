package com.jockie.bot.core.utility;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class LoaderUtility {
	
	private static boolean isDeepImplementation(Class<?> clazz, Class<?> clazz2) {
		while(!clazz2.equals(Object.class)) {
			for(Class<?> interfaze : clazz2.getInterfaces()) {
				if(interfaze.equals(clazz)) {
					return true;
				}
			}
			
			clazz2 = clazz2.getSuperclass();
		}
		
		return false;
	}
	
	public static <T> List<T> loadFrom(String packagePath, Class<T> clazz) {
		return LoaderUtility.loadFrom(packagePath, true, clazz);
	}
	
	@SuppressWarnings("unchecked") /* It is checked through isDeepImplementation */
	public static <T> List<T> loadFrom(String packagePath, boolean subPackages, Class<T> clazz) {
		List<T> objects = new ArrayList<>();
		
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			
			ImmutableSet<ClassInfo> classes;
			if(subPackages) {
				classes = ClassPath.from(classLoader).getTopLevelClassesRecursive(packagePath);
			}else{
				classes = ClassPath.from(classLoader).getTopLevelClasses(packagePath);
			}
			
			for(ClassInfo info : classes) {
				Class<?> clazz2 = classLoader.loadClass(info.toString());
				
				if(LoaderUtility.isDeepImplementation(clazz, clazz2)) {
					try {
						objects.add((T) clazz2.getConstructor().newInstance());
					}catch(Exception e1) {
						throw e1;
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return objects;
	}
}