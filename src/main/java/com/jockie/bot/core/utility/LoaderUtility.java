package com.jockie.bot.core.utility;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class LoaderUtility {
	
	private LoaderUtility() {}
	
	/**
	 * @return true if class implements interface checked recursively till the super class is either null or Object
	 */
	public static boolean isDeepImplementation(Class<?> clazz, Class<?> interfaze) {
		while(clazz != null && !clazz.equals(Object.class)) {
			for(Class<?> interfaze2 : clazz.getInterfaces()) {
				if(interfaze2.equals(interfaze)) {
					return true;
				}
			}
			
			clazz = clazz.getSuperclass();
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
				Class<?> loadedClass = classLoader.loadClass(info.toString());
				
				if(LoaderUtility.isDeepImplementation(loadedClass, clazz)) {
					try {
						objects.add((T) loadedClass.getConstructor().newInstance());
					}catch(Exception e1) {
						throw new Exception("Failed to load class " + loadedClass, e1);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return objects;
	}
	
	public static <T extends Annotation> List<Object> loadWith(String packagePath, boolean subPackages, Class<T> annotation) {
		List<Object> objects = new ArrayList<>();
		
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			
			ImmutableSet<ClassInfo> classes;
			if(subPackages) {
				classes = ClassPath.from(classLoader).getTopLevelClassesRecursive(packagePath);
			}else{
				classes = ClassPath.from(classLoader).getTopLevelClasses(packagePath);
			}
			
			for(ClassInfo info : classes) {
				Class<?> loadedClass = classLoader.loadClass(info.toString());
				
				if(loadedClass.isAnnotationPresent(annotation)) {
					try {
						objects.add(loadedClass.getConstructor().newInstance());
					}catch(Exception e1) {
						throw new Exception("Failed to load class " + loadedClass, e1);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return objects;
	}
}