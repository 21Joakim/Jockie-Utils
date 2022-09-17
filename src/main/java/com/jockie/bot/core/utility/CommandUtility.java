package com.jockie.bot.core.utility;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.command.SubCommand;

import net.dv8tion.jda.internal.utils.Checks;

/**
 * Methods used internally for loading commands and modules
 */
/* TODO: Move all class related utility methods to another more specific utility class */
public class CommandUtility {
	
	private CommandUtility() {}
	
	public static char getPrimitiveClassSymbol(@Nonnull Class<?> clazz) {
		Checks.notNull(clazz, "clazz");
		
		if(!clazz.isPrimitive()) {
			throw new IllegalArgumentException("The provided class is not a primitive class");
		}
		
		if(clazz.equals(boolean.class)) return 'Z';
		if(clazz.equals(byte.class)) return 'B';
		if(clazz.equals(short.class)) return 'S';
		if(clazz.equals(int.class)) return 'I';
		if(clazz.equals(long.class)) return 'J';
		if(clazz.equals(float.class)) return 'F';
		if(clazz.equals(double.class)) return 'D';
		if(clazz.equals(char.class)) return 'C';
		
		throw new IllegalStateException("No known class symbol for: " + clazz);
	}
	
	public static String getClassSymbol(@Nonnull Class<?> clazz) {
		Checks.notNull(clazz, "clazz");
		
		if(clazz.isArray()) {
			return clazz.getName();
		}
		
		if(clazz.isPrimitive()) {
			return String.valueOf(CommandUtility.getPrimitiveClassSymbol(clazz));
		}
		
		return "L" + clazz.getName() + ";";
	}
	
	/**
	 * @param clazz the class to get as an array, if the type is already an array
	 * it will return it as the next layer, String[] would become String[][]
	 * 
	 * @return the provided clazz as an array
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public static <T> Class<T[]> getClassAsArray(@Nonnull Class<T> clazz) {
		Checks.notNull(clazz, "clazz");
		
		try {
			return (Class<T[]>) Class.forName("[" + CommandUtility.getClassSymbol(clazz));
		}catch(ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * @param clazz primitive data type
	 * 
	 * @return the boxed (wrapper) version of the provided primitive class
	 */
	public static Class<?> getBoxedClass(Class<?> clazz) {
		Checks.notNull(clazz, "clazz");
		
		if(!clazz.isPrimitive()) {
			throw new IllegalArgumentException("The provided class is not a primitive class");
		}
		
		if(clazz.equals(boolean.class)) return Boolean.class;
		if(clazz.equals(byte.class)) return Byte.class;
		if(clazz.equals(short.class)) return Short.class;
		if(clazz.equals(int.class)) return Integer.class;
		if(clazz.equals(long.class)) return Long.class;
		if(clazz.equals(float.class)) return Float.class;
		if(clazz.equals(double.class)) return Double.class;
		if(clazz.equals(char.class)) return Character.class;
		
		throw new IllegalStateException("No known boxed class for: " + clazz);
	}
	
	/**
	 * @param clazz primitive data type
	 * 
	 * @return the Java default value for the provided primitive data type
	 */
	@Nullable
	public static Object getDefaultValue(@Nonnull Class<?> clazz) {
		Checks.notNull(clazz, "clazz");
		
		if(!clazz.isPrimitive()) return null;
		if(clazz.equals(boolean.class)) return false;
		if(clazz.equals(byte.class)) return (byte) 0;
		if(clazz.equals(short.class)) return (short) 0;
		if(clazz.equals(int.class)) return 0;
		if(clazz.equals(long.class)) return 0L;
		if(clazz.equals(float.class)) return 0.0F;
		if(clazz.equals(double.class)) return 0.0D;
		if(clazz.equals(char.class)) return '\u0000';
		
		throw new IllegalStateException("No known default value for: " + clazz);
	}
	
	/**
	 * @param parameterizedType the type
	 * 
	 * @return the generic classes of the provided type, 
	 * if the generic type is not a class it will be null instead of the class
	 */
	@Nonnull
	public static Class<?>[] getGenericClasses(ParameterizedType parameterizedType) {
		Type[] types = parameterizedType.getActualTypeArguments();
		
		Class<?>[] classes = new Class<?>[types.length];
		for(int i = 0; i < types.length; i++) {
			Type type = types[i];
			if(type instanceof Class) {
				classes[i] = (Class<?>) type;
			}else{
				classes[i] = null;
			}
		}
		
		return classes;
	}
	
	/**
	 * @param type the type
	 * 
	 * @return the generic classes of the provided type, 
	 * if the generic type is not a class it will be null instead of the class
	 */
	@Nonnull
	public static Class<?>[] getGenericClasses(@Nonnull Type type) {
		Checks.notNull(type, "type");
		
		if(type instanceof ParameterizedType) {
			return CommandUtility.getGenericClasses((ParameterizedType) type);
		}
		
		return new Class[0];
	}
	
	/**
	 * @return true if an instance of clazz would be an instance of otherClazz
	 */
	public static boolean isInstanceOf(@Nonnull Class<?> clazz, @Nonnull Class<?> otherClazz) {
		Checks.notNull(clazz, "clazz");
		Checks.notNull(otherClazz, "otherClazz");
		
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
	@Nonnull
	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> getClassesImplementing(@Nonnull Class<?>[] classes, @Nonnull Class<T> interfaze) {
		Checks.noneNull(classes, "classes");
		Checks.notNull(interfaze, "interfaze");
		
		Checks.check(interfaze.isInterface(), "interfaze is not an interface");
		
		List<Class<T>> foundClasses = new ArrayList<>();
		for(Class<?> clazz : classes) {
			if(CommandUtility.isInstanceOf(clazz, interfaze)) {
				foundClasses.add((Class<T>) clazz);
			}
		}
		
		return foundClasses;
	}
	
	/**
	 * Convert the method name to a command name, this will convert
	 * <b>hello_there</b> and <b>helloThere</b> to <b>hello there</b>
	 * 
	 * @param method the method to convert to a command name
	 * 
	 * @return the command name for the provided method
	 */
	@Nonnull
	public static String getCommandName(@Nonnull Method method) {
		Checks.notNull(method, "method");
		
		String methodName = method.getName();
		if(methodName.contains("_")) {
			return methodName.replace("_", " ");
		}
		
		StringBuilder name = new StringBuilder(methodName.length());
		for(char character : methodName.toCharArray()) {
			if(Character.isUpperCase(character)) {
				name.append(' ').append(Character.toLowerCase(character));
			}else{
				name.append(character);
			}
		}
		
		return name.toString();
	}
	
	public static boolean isCommandCreateMethod(Method method) {
		if(!method.getName().equals("createCommand")) {
			return false;
		}
		
		if(!CommandUtility.isInstanceOf(method.getReturnType(), IMethodCommand.class)) {
			return false;
		}
		
		Class<?>[] types = method.getParameterTypes();
		if(types.length == 1) {
			return types[0].isAssignableFrom(Method.class);
		}
		
		if(types.length == 2) {
			return types[0].isAssignableFrom(Method.class) && types[1].isAssignableFrom(String.class);
		}
		
		return false;
	}
	
	public static boolean isCommandLoadMethod(Method method) {
		if(!method.getName().equals("onCommandLoad")) {
			return false;
		}
		
		if(method.getParameterCount() != 1) {
			return false;
		}
		
		return method.getParameterTypes()[0].isAssignableFrom(ICommand.class);
	}
	
	public static boolean isModuleLoadMethod(Method method) {
		if(!method.getName().equals("onModuleLoad")) {
			return false;
		}
		
		return method.getParameterCount() == 0;
	}
	
	/* TODO: Move this to some more general utility class */
	public static <T> T find(T[] values, Predicate<T> predicate) {
		for(T value : values) {
			if(predicate.test(value)) {
				return value;
			}
		}
		
		return null;
	}
	
	public static List<Method> getCommandMethods(Method[] methods, boolean subCommand) {
		List<Method> commandMethods = new ArrayList<>();
		for(Method method : methods) {
			if(!method.isAnnotationPresent(Command.class)) {
				continue;
			}
			
			if(method.isAnnotationPresent(SubCommand.class) != subCommand) {
				continue;
			}
			
			commandMethods.add(method);
		}
		
		return commandMethods;
	}
	
	public static List<Method> getCommandMethods(Method[] methods) {
		return CommandUtility.getCommandMethods(methods, false);
	}
	
	public static List<Method> getSubCommandMethods(Method[] methods) {
		return CommandUtility.getCommandMethods(methods, true);
	}
	
	public static ICommand getSubCommandRecursive(ICommand start, String[] path) {
		Objects.requireNonNull(path, "path must not be null");
		
		if(path.length == 0 || start == null) {
			return start;
		}
		
		String startPath = path[0];
		for(ICommand subCommand : start.getSubCommands()) {
			if(!subCommand.getCommand().equalsIgnoreCase(startPath)) {
				continue;
			}
			
			String[] newPath = new String[path.length - 1];
			System.arraycopy(path, 1, newPath, 0, path.length - 1);
			
			return CommandUtility.getSubCommandRecursive(subCommand, newPath);
		}
		
		return start;
	}
}