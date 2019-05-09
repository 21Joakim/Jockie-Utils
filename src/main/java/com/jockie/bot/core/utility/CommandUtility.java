package com.jockie.bot.core.utility;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.SubCommand;
import com.jockie.bot.core.command.impl.MethodCommand;

import net.dv8tion.jda.core.utils.Checks;

/**
 * Methods used internally for loading commands and modules
 */
public class CommandUtility {
	
	private CommandUtility() {}
	
	/**
	 * @return true if clazz either implements or extends otherClazz checked recursively till the super class is null
	 */
	public static boolean isAssignableFrom(Class<?> clazz, Class<?> otherClazz) {
		while(clazz != null) {
			if(clazz.equals(otherClazz)) {
				return true;
			}
			
			for(Class<?> interfaze : clazz.getInterfaces()) {
				if(interfaze.equals(otherClazz)) {
					return true;
				}
			}
			
			clazz = clazz.getSuperclass();
		}
		
		return false;
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
			if(CommandUtility.isAssignableFrom(clazz, interfaze)) {
				foundClasses.add((Class<T>) clazz);
			}
		}
		
		return foundClasses;
	}
	
	public static String getCommandName(Method method) {
		return method.getName().replace("_", " ");
	}
	
	public static Method getCommandCreateMethod(Method[] methods) {
		for(Method method : methods) {
			if(method.getName().equals("createCommand")) {
				if(!method.getReturnType().isAssignableFrom(MethodCommand.class)) {
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
	
	public static Method getOnCommandLoadMethod(Method[] methods) {
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
	
	public static Method getOnModuleLoad(Method[] methods) {
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