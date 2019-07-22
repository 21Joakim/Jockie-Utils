package com.jockie.bot.core.utility;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.command.SubCommand;

import net.dv8tion.jda.core.utils.Checks;

/**
 * Methods used internally for loading commands and modules
 */
public class CommandUtility {
	
	private CommandUtility() {}
	
	/**
	 * @param clazz the class to get as an array, if the type is already an array
	 * it will return it as the next layer, String[] would become String[][]
	 * 
	 * @return the provided clazz as an array
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T[]> getClassAsArray(Class<T> clazz) {
		Checks.notNull(clazz, "clazz");
		
		String className;
		if(clazz.isArray()) {
			className = clazz.getName();
		}else if(clazz.isPrimitive()) {
			if(clazz.equals(boolean.class)) {
				className = "Z";
			}else if(clazz.equals(byte.class)) {
				className = "B";
			}else if(clazz.equals(short.class)) {
				className = "S";
			}else if(clazz.equals(int.class)) {
				className = "I";
			}else if(clazz.equals(long.class)) {
				className = "J";
			}else if(clazz.equals(float.class)) {
				className = "F";
			}else if(clazz.equals(double.class)) {
				className = "D";
			}else if(clazz.equals(char.class)) {
				className = "C";
			}else{
				throw new IllegalStateException();
			}
		}else{
			className = "L" + clazz.getName() + ";";
		}
		
		try {
			return (Class<T[]>) Class.forName("[" + className);
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * @return the generic classes of the provided type, 
	 * if the generic type is not a class it will be a null instead of the class
	 */
	public static Class<?>[] getClasses(Type type) {
		if(type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			Class<?>[] classes = new Class<?>[typeArguments.length];
			
			for(int i = 0; i < typeArguments.length; i++) {
				Type typeArgument = typeArguments[i];
				
				if(typeArgument instanceof Class) {
					classes[i] = (Class<?>) typeArgument;
				}else{
					classes[i] = null;
				}
			}
			
			return classes;
		}
		
		return new Class[0];
	}
	
	/**
	 * @return true if an instance of clazz would be an instance of otherClazz
	 */
	public static boolean isInstanceOf(Class<?> clazz, Class<?> otherClazz) {
		return otherClazz.isAssignableFrom(clazz);
	}
	
	/**
	 * Filter the provided classes to find the ones which deep implement the provided interface
	 * 
	 * @param classes the classes to look through
	 * @param interfaze the interface the classes need to implement to be returned
	 * 
	 * @return a list of classes; classes that implemented the provided interface
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> getClassesImplementing(Class<?>[] classes, Class<T> interfaze) {
		Checks.check(interfaze.isInterface(), "interfaze is not an interface");
		
		List<Class<T>> foundClasses = new ArrayList<>();
		for(Class<?> clazz : classes) {
			if(CommandUtility.isInstanceOf(clazz, interfaze)) {
				foundClasses.add((Class<T>) clazz);
			}
		}
		
		return foundClasses;
	}
	
	public static String getCommandName(Method method) {
		return method.getName().replace("_", " ");
	}
	
	public static Method findCommandCreateMethod(Method[] methods) {
		for(Method method : methods) {
			if(method.getName().equals("createCommand")) {
				if(!CommandUtility.isInstanceOf(method.getReturnType(), IMethodCommand.class)) {
					continue;
				}
				
				Class<?>[] types = method.getParameterTypes();
				if(types.length == 1) {
					if(types[0].isAssignableFrom(Method.class)) {
						return method;
					}
				}else if(types.length == 2) {
					if(types[0].isAssignableFrom(Method.class) && types[1].isAssignableFrom(String.class)) {
						return method;
					}
				}
			}
		}
		
		return null;
	}
	
	public static Method findCommandLoadMethod(Method[] methods) {
		for(Method method : methods) {
			if(method.getName().equals("onCommandLoad")) {
				if(method.getParameterCount() == 1) {
					if(method.getParameterTypes()[0].isAssignableFrom(ICommand.class)) {
						return method;
					}
				}
			}
		}
		
		return null;
	}
	
	public static Method findModuleLoadMethod(Method[] methods) {
		for(Method method : methods) {
			if(method.getName().equals("onModuleLoad")) {
				if(method.getParameterCount() == 0) {
					return method;
				}
			}
		}
		
		return null;
	}
	
	public static List<Method> getCommandMethods(Method[] methods) {
		List<Method> commandMethods = new ArrayList<>();
		
		for(Method method : methods) {
			if(method.isAnnotationPresent(Command.class) && !method.isAnnotationPresent(SubCommand.class)) {
				commandMethods.add(method);
			}
		}
		
		return commandMethods;
	}
	
	public static List<Method> getSubCommandMethods(Method[] methods) {
		List<Method> subCommandMethods = new ArrayList<>();
		
		for(Method method : methods) {
			if(method.isAnnotationPresent(Command.class) && method.isAnnotationPresent(SubCommand.class)) {
				subCommandMethods.add(method);
			}
		}
		
		return subCommandMethods;
	}
	
	public static ICommand getSubCommandRecursive(ICommand start, String[] path) {
		Objects.requireNonNull(path, "path must not be null");
		
		if(path.length > 0 && start != null) {
			String thePath = path[0];
			for(ICommand subCommand : start.getSubCommands()) {
				if(subCommand.getCommand().equalsIgnoreCase(thePath)) {
					String[] newPath = new String[path.length - 1];
					
					System.arraycopy(path, 1, newPath, 0, path.length - 1);
					
					return getSubCommandRecursive(start, newPath);
				}
			}
		}
		
		return start;
	}
}