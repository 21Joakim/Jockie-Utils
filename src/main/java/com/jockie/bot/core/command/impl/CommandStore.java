package com.jockie.bot.core.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.Initialize;
import com.jockie.bot.core.command.SubCommand;
import com.jockie.bot.core.command.impl.factory.MethodCommandFactory;
import com.jockie.bot.core.module.IModule;
import com.jockie.bot.core.module.Module;
import com.jockie.bot.core.utility.LoaderUtility;

public class CommandStore {
	
	public static CommandStore of(String packagePath) {
		return new CommandStore().loadFrom(packagePath);
	}
	
	public static CommandStore of(String packagePath, boolean subPackages) {
		return new CommandStore().loadFrom(packagePath, subPackages);
	}
	
	private static final BiFunction<Method, Object, ? extends MethodCommand> DEFAULT_CREATE_FUNCTION = (method, module) -> {
		return MethodCommandFactory.getDefaultFactory().create(getCommandName(method), method, module);
	};
	
	private static String getCommandName(Method method) {
		return method.getName().replace("_", " ");
	}
	
	private static Method getCommandCreateMethod(Method[] methods) {
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
	
	private static Method getOnCommandLoadMethod(Method[] methods) {
		for(Method method : methods) {
			if(method.getName().equals("onCommandLoad")) {
				if(method.getParameterCount() == 1) {
					if(method.getParameterTypes()[0].isAssignableFrom(MethodCommand.class)) {
						return method;
					}
				}
			}
		}
		
		return null;
	}
	
	private static Method getOnModuleLoad(Method[] methods) {
		for(Method method : methods) {
			if(method.getName().equals("onModuleLoad")) {
				if(method.getParameterCount() == 0) {
					return method;
				}
			}
		}
		
		return null;
	}
	
	private static ICommand getSubCommandRecursive(ICommand start, String[] path) {
		Objects.requireNonNull(path.length, "path must not be null");
		
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
	
	public static List<ICommand> loadModule(Object module) {
		Objects.requireNonNull(module);
		
		List<ICommand> commands = new ArrayList<>();
		Class<?> moduleClass = module.getClass();
		
		Method[] methods = moduleClass.getDeclaredMethods();
		
		Method createCommand = getCommandCreateMethod(methods);
		Method onCommandLoadMethod = getOnCommandLoadMethod(methods);
		Method onModuleLoad = getOnModuleLoad(methods);
		
		BiFunction<Method, Object, ? extends MethodCommand> createFunction;
		if(createCommand != null) {
			createFunction = (method, container) -> {
				MethodCommand result = null;
				
				try {
					Class<?>[] types = createCommand.getParameterTypes();
					if(types.length == 1) {
						if(types[0].isAssignableFrom(Method.class)) {
							result = (MethodCommand) createCommand.invoke(container, method);
						}
					}else if(types.length == 2) {
						if(types[0].isAssignableFrom(Method.class) && types[1].isAssignableFrom(String.class)) {
							result = (MethodCommand) createCommand.invoke(container, method, getCommandName(method));
						}
					}
				}catch(InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
				
				if(result == null) {
					result = DEFAULT_CREATE_FUNCTION.apply(method, container);
				}
				
				return result;
			};
		}else{
			createFunction = DEFAULT_CREATE_FUNCTION;
		}
		
		Map<String, MethodCommand> methodCommands = new HashMap<>();
		Map<String, MethodCommand> methodCommandsNamed = new HashMap<>();
		
		for(Method method : methods) {
			if(method.isAnnotationPresent(Command.class)) {
				MethodCommand command = createFunction.apply(method, module);
				
				if(method.isAnnotationPresent(SubCommand.class)) {
					SubCommand subCommandAnnotation = method.getAnnotation(SubCommand.class);
					String[] path = subCommandAnnotation.value();
					
					if(path.length > 0) {
						ICommand parent = getSubCommandRecursive(methodCommandsNamed.get(path[0]), Arrays.copyOfRange(path, 1, path.length));
						if(parent != null) {
							/* TODO: Implement a proper way of handling this, commands should not have to extend CommandImpl */
							if(parent instanceof CommandImpl) {
								((CommandImpl) parent).addSubCommand(command);
							}else{
								System.err.println("Sub command (" + command.getCommand() + ") parent does not implement CommandImpl");
							}
						}else{
							System.err.println("Sub command (" + command.getCommand() + ") does not have a valid command path");
						}
					}else{
						System.err.println("Sub command (" + command.getCommand() + ") does not have a command path");
					}
				}
				
				methodCommands.put(method.getName(), command);
				methodCommandsNamed.put(command.getCommand(), command);
			}
		}
		
		for(Method method : methods) {
			if(method.isAnnotationPresent(Initialize.class)) {
				Initialize initialize = method.getAnnotation(Initialize.class);
				if(initialize.all()) {
					for(MethodCommand command : methodCommands.values()) {
						try {
							method.invoke(module, command);
						}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}else if(initialize.value().length == 0) {
					if(methodCommands.containsKey(method.getName())) {
						MethodCommand command = methodCommands.get(method.getName());
						
						try {
							method.invoke(module, command);
						}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}else{
					for(int i = 0; i < initialize.value().length; i++) {
						String name = initialize.value()[i];
						if(methodCommands.containsKey(name)) {
							MethodCommand command = methodCommands.get(name);
							
							try {
								method.invoke(module, command);
							}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		commands.addAll(methodCommands.values());
		
		if(onCommandLoadMethod != null) {
			for(ICommand command : commands) {
				try {
					onCommandLoadMethod.invoke(module, (MethodCommand) command);
				}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		for(Class<?> clazz : moduleClass.getClasses()) {
			if(LoaderUtility.isDeepImplementation(clazz, ICommand.class)) {
				try {
					try {
						Constructor<?> constructor = clazz.getDeclaredConstructor();
						
						commands.add((ICommand) constructor.newInstance());
						
						continue;
					}catch(NoSuchMethodException | SecurityException e) {}
					
					try {
						Constructor<?> constructor = clazz.getDeclaredConstructor(moduleClass);
						
						commands.add((ICommand) constructor.newInstance(module));
						
						continue;
					}catch(NoSuchMethodException | SecurityException e) {}
				}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(onModuleLoad != null) {
			try {
				onModuleLoad.invoke(module);
			}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		return commands;
	}
	
	private Set<ICommand> commands = new HashSet<ICommand>();
	
	public CommandStore loadFrom(String packagePath) {
		return this.loadFrom(packagePath, true);
	}
	
	public CommandStore loadFrom(String packagePath, boolean subPackages) {
		List<ICommand> commands = new ArrayList<>();
		
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
				
				if(LoaderUtility.isDeepImplementation(loadedClass, ICommand.class)) {
					try {
						commands.add((ICommand) loadedClass.getConstructor().newInstance());
					}catch(Exception e1) {
						throw new Exception("Failed to load class " + loadedClass, e1);
					}
				}else if(loadedClass.isAnnotationPresent(Module.class) || LoaderUtility.isDeepImplementation(loadedClass, IModule.class)) {
					try {
						commands.addAll(CommandStore.loadModule(loadedClass.getConstructor().newInstance()));
					}catch(Exception e1) {
						throw new Exception("Failed to load class " + loadedClass, e1);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return this.addCommands(commands);
	}
	
	@SuppressWarnings("unchecked")
	public CommandStore addCommands(Object... objects) {
		for(Object object : objects) {
			if(object instanceof Collection) {
				this.addCommands(((Collection<Object>) object).toArray(new Object[0]));
				
				continue;
			}
			
			if(object instanceof ICommand) {
				ICommand command = (ICommand) object;
				
				this.commands.add(command.getTopParent());
				
				continue;
			}
			
			Class<?> objectClass = object.getClass();
			if(objectClass.isAnnotationPresent(Module.class) || LoaderUtility.isDeepImplementation(objectClass, IModule.class)) {
				this.commands.addAll(CommandStore.loadModule(object));
				
				continue;
			}
			
			System.err.println(object.getClass() + " is not a command or command container");
		}
		
		return this;
	}
	
	public CommandStore addCommands(Collection<Object> objects)  {
		return this.addCommands(objects.toArray(new Object[0]));
	}
	
	public CommandStore removeCommands(ICommand... commands) {
		for(ICommand command : commands) {
			this.commands.remove(command.getTopParent());
		}
		
		return this;
	}
	
	public CommandStore removeCommands(Collection<ICommand> commands) {
		return this.removeCommands(commands.toArray(new ICommand[0]));
	}
	
	public Set<ICommand> getCommands() {
		return Collections.unmodifiableSet(this.commands);
	}
}