package com.jockie.bot.core.utility;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.impl.DummyCommand;

public class LoaderUtility {
	
	private LoaderUtility() {}
	
	/**
	 * @return true if clazz implements interfaze checked recursively till the super class is either null or Object
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
				Class<?> clazz2 = classLoader.loadClass(info.toString());
				
				if(LoaderUtility.isDeepImplementation(clazz2, clazz)) {
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
	
	public static List<ICommand> generateDummyCommands(ICommand command) {
		List<ICommand> dummyCommands = new ArrayList<>();
		
		if(!(command instanceof DummyCommand)) {
			List<IArgument<?>> arguments = new ArrayList<>();
			if(command.getArguments().length > 0) {
				for(int i = 0; i < command.getArguments().length; i++) {
					IArgument<?> argument = command.getArguments()[i];
					if(argument.hasDefault()) {
						arguments.add(argument);
					}
				}
				
				if(arguments.size() > 0) {
					List<IArgument<?>> args = new ArrayList<>();
			    	for(int i = 1, max = 1 << arguments.size(); i < max; ++i) {
			    	    for(int j = 0, k = 1; j < arguments.size(); ++j, k <<= 1) {
			    	        if((k & i) != 0) {
			    	        	args.add(arguments.get(j));
			    	        }
			    	    }
			    	    
			    	    dummyCommands.add(new DummyCommand(command, args.toArray(new IArgument[0])));
						
						args.clear();
			    	}
				}
			}
		}
		
		return dummyCommands;
	}
}