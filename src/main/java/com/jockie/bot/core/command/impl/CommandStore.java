package com.jockie.bot.core.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.command.Ignore;
import com.jockie.bot.core.command.Initialize;
import com.jockie.bot.core.command.SubCommand;
import com.jockie.bot.core.command.factory.impl.MethodCommandFactory;
import com.jockie.bot.core.module.IModule;
import com.jockie.bot.core.module.Module;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.internal.utils.JDALogger;

/**
 * This contains a list of registered commands, works as a sort of container
 */
public class CommandStore {
	
	public static final Logger LOG = JDALogger.getLog(CommandStore.class);
	
	/**
	 * Load commands from the provided package and its sub-packages, equivalent to {@link #loadFrom(String)}
	 * 
	 * @param packagePath the java package path to load the commands from
	 * 
	 * @return the created {@link CommandStore}
	 */
	public static CommandStore of(String packagePath) {
		return new CommandStore().loadFrom(packagePath);
	}
	
	/**
	 * Load commands from the provided package, equivalent to {@link #loadFrom(String, boolean)}
	 * 
	 * @param packagePath the java package path to load the commands from
	 * @param subPackages whether or not to include sub-packages when loading the commands
	 * 
	 * @return the created {@link CommandStore}
	 */
	public static CommandStore of(String packagePath, boolean subPackages) {
		return new CommandStore().loadFrom(packagePath, subPackages);
	}
	
	private static final BiFunction<Method, Object, ? extends IMethodCommand> DEFAULT_CREATE_FUNCTION = (method, module) -> {
		return MethodCommandFactory.getDefault().create(method, CommandUtility.getCommandName(method), module);
	};
	
	private static void invokeRecursive(ICommand command, Object module, Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		method.invoke(module, command);
		
		for(ICommand subCommand : command.getSubCommands()) {
			CommandStore.invokeRecursive(subCommand, module, method);
		}
	}
	
	private static String getCommandLoadErrorMessage(Method method, Class<?> clazz, ICommand command) {
		String message = "Failed to load";
		
		if(method != null && clazz != null) {
			message += " method command " + clazz.getName() + "#" + method.getName();
		}else if(clazz != null) {
			if(clazz.isAnnotationPresent(Module.class) || CommandUtility.isInstanceOf(clazz, IModule.class)) {
				message += " module " + clazz.getName();
			}else{
				message += " command " + clazz.getName();
			}
		}
		
		if(command != null) {
			message += " (" + command.getCommand() + ")";
		}
		
		return message;
	}
	
	/**
	 * Load a module and get all the loaded commands
	 * 
	 * @param module the module to load
	 * 
	 * @return a list of commands loaded from the module
	 * 
	 * @throws Throwable if the module for any reason fails to load
	 */
	
	/* 
	 * TODO: I don't like the loading of modules with the @Initialize and stuff,
	 * should probably reconsider how this works.
	 * 
	 * My biggest issue with it is that modules can have both classes and methods as commands,
	 * this means that there is no good common ground for initializing them, sure everything could use
	 * CommandImpl but since commands don't necessarily need to extend CommandImpl (Because they can implement ICommand)
	 * it is not the best option.
	 */
	
	/*
	 * TODO: If a single command fails to load should that make the entire module fail?
	 */
	public static List<ICommand> loadModule(Object module) throws Throwable {
		Objects.requireNonNull(module);
		
		List<ICommand> commands = new ArrayList<>();
		Class<?> moduleClass = module.getClass();
		
		Method[] methods = moduleClass.getDeclaredMethods();
		Class<?>[] classes = moduleClass.getDeclaredClasses();
		
		Method createCommand = CommandUtility.findCommandCreateMethod(methods);
		Method onCommandLoadMethod = CommandUtility.findCommandLoadMethod(methods);
		Method onModuleLoad = CommandUtility.findModuleLoadMethod(methods);
		
		BiFunction<Method, Object, ? extends IMethodCommand> createFunction;
		if(createCommand != null) {
			createFunction = (method, container) -> {
				IMethodCommand result = null;
				
				try {
					Class<?>[] types = createCommand.getParameterTypes();
					if(types.length == 1) {
						if(types[0].isAssignableFrom(Method.class)) {
							result = (IMethodCommand) createCommand.invoke(container, method);
						}
					}else if(types.length == 2) {
						if(types[0].isAssignableFrom(Method.class) && types[1].isAssignableFrom(String.class)) {
							result = (IMethodCommand) createCommand.invoke(container, method, CommandUtility.getCommandName(method));
						}
					}
				}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOG.error("Failed to create method command", e);
				}
				
				if(result == null) {
					result = DEFAULT_CREATE_FUNCTION.apply(method, container);
				}
				
				return result;
			};
		}else{
			createFunction = DEFAULT_CREATE_FUNCTION;
		}
		
		Map<String, ICommand> moduleCommands = new HashMap<>();
		Map<String, ICommand> moduleCommandsNamed = new HashMap<>();
		
		/* Load method based commands */
		for(Method method : CommandUtility.getCommandMethods(methods)) {
			if(method.isAnnotationPresent(Ignore.class)) {
				continue;
			}
			
			IMethodCommand command = createFunction.apply(method, module);
			
			/* TODO: This would fail if there are multiple commands with the same name */
			moduleCommands.put(method.getName(), command);
			moduleCommandsNamed.put(command.getCommand(), command);
		}
		
		/* Load class based commands */
		for(Class<ICommand> commandClass : CommandUtility.getClassesImplementing(classes, ICommand.class)) {
			if(commandClass.isAnnotationPresent(Ignore.class)) {
				continue;
			}
			
			try {
				ICommand command;
				if(Modifier.isStatic(commandClass.getModifiers())) {
					Constructor<ICommand> constructor = commandClass.getDeclaredConstructor();
					
					command = constructor.newInstance();
				}else{
					Constructor<ICommand> constructor = commandClass.getDeclaredConstructor(moduleClass);
					
					command = constructor.newInstance(module);
				}
				
				/* TODO: This would fail if there are multiple commands with the same name */
				moduleCommands.put(commandClass.getSimpleName(), command);
				moduleCommandsNamed.put(command.getCommand(), command);
			}catch(Throwable e) {
				LOG.error("Failed to instantiate command", e);
			}
		}
		
		List<Method> subCommandMethods = CommandUtility.getSubCommandMethods(methods);
		subCommandMethods.sort(Comparator.comparingInt(method -> method.getAnnotation(SubCommand.class).value().length));
		
		/* Load commands marked as sub-commands */
		for(Method method : subCommandMethods) {
			if(method.isAnnotationPresent(Ignore.class)) {
				continue;
			}
			
			IMethodCommand command = createFunction.apply(method, module);
			SubCommand subCommand = method.getAnnotation(SubCommand.class);
			
			String[] path = subCommand.value();
			if(path.length == 0) {
				LOG.warn("[" + module.getClass().getSimpleName() + "] Sub command (" + command.getCommand() + ") does not have a command path");
				
				continue;
			}
				
			ICommand parent = CommandUtility.getSubCommandRecursive(moduleCommandsNamed.get(path[0]), Arrays.copyOfRange(path, 1, path.length));
			if(parent == null) {
				LOG.warn("[" + module.getClass().getSimpleName() + "] Sub command (" + command.getCommand() + ") does not have a valid command path");
				
				continue;
			}
			
			/* TODO: Implement a proper way of handling this, commands should not have to extend AbstractCommand */
			if(parent instanceof AbstractCommand) {
				((AbstractCommand) parent).addSubCommand(command);
			}else{
				LOG.warn("[" + module.getClass().getSimpleName() + "] Sub command (" + command.getCommand() + ") parent does not implement AbstractCommand");
			}
		}
		
		/* TODO: Should this also be called for sub-commands, or should this be handled through its parent? */
		for(Method method : methods) {
			if(method.isAnnotationPresent(Initialize.class)) {
				Class<?> type = method.getParameters()[0].getType();
				Initialize initialize = method.getAnnotation(Initialize.class);
				
				Consumer<ICommand> initializer = (command) -> {
					if(type.isInstance(command)) {
						/*
						 * The command's trigger could possibly change because things like 
						 * AbstractCommand#setCommand(String) and therefore we are saving 
						 * the key before to be able to remove it in case something goes wrong
						 */
						String key = command.getCommand();
						
						try {
							if(initialize.subCommands()) {
								if(initialize.recursive()) {
									CommandStore.invokeRecursive(command, module, method);
								}else{
									method.invoke(module, command);
									
									for(ICommand subCommand : command.getSubCommands()) {
										method.invoke(module, subCommand);
									}
								}
							}else{
								method.invoke(module, command);
							}
						}catch(Throwable e) {
							LOG.warn(CommandStore.getCommandLoadErrorMessage(null, null, command), e);
							
							/*
							 * Remove the command from the list of added commands,
							 * this is to prevent any unexpected behaviour due to it failing 
							 * the initialization. If the command fails initialization something
							 * needs to be fixed and it should not be added.
							 * 
							 * TODO: Should this remove the entire command or just remove the sub-command?
							 * This also includes sub-command, if any of the sub-commands of the command
							 * fail to initialize then the whole command is ignored, this may not be 
							 * expected behaviour but it's to prevent any unexpected behaviour in the command.
							 */
							moduleCommands.remove(key);
						}
					}
				};
				
				if(initialize.all()) {
					for(ICommand command : new ArrayList<>(moduleCommands.values())) {
						initializer.accept(command);
					}
				}else if(initialize.value().length == 0) {
					if(moduleCommands.containsKey(method.getName())) {
						initializer.accept(moduleCommands.get(method.getName()));
					}
				}else{
					for(String value : initialize.value()) {
						if(moduleCommands.containsKey(value)) {
							initializer.accept(moduleCommands.get(value));
						}
					}
				}
			}
		}
		
		commands.addAll(moduleCommands.values());
		
		/* TODO: Should this also be called for sub-commands, or should this be handled through its parent? */
		if(onCommandLoadMethod != null) {
			for(ICommand command : commands) {
				Class<?> type = onCommandLoadMethod.getParameters()[0].getType();
				if(type.isInstance(command)) {
					String key = command.getCommand();
					
					try {
						onCommandLoadMethod.invoke(module, command);
					}catch(Throwable e) {
						LOG.warn(CommandStore.getCommandLoadErrorMessage(null, null, command), e);
						
						moduleCommands.remove(key);
						
						continue;
					}
				}
			}
		}
		
		if(onModuleLoad != null) {
			onModuleLoad.invoke(module);
		}
		
		return commands;
	}
	
	private Set<ICommand> commands = new HashSet<ICommand>();
	
	/**
	 * Load all commands from the provided package and its sub-packages
	 * 
	 * @param packagePath the java package path to load the commands from
	 * 
	 * @return the {@link CommandStore} instance, useful for chaining
	 */
	public CommandStore loadFrom(String packagePath) {
		return this.loadFrom(packagePath, true);
	}
	
	/**
	 * Load all commands from the provided package
	 * 
	 * @param packagePath the java package path to load the commands from
	 * @param subPackages whether or not to include sub-packages when loading the commands
	 * 
	 * @return the {@link CommandStore} instance, useful for chaining
	 */
	public CommandStore loadFrom(String packagePath, boolean subPackages) {
		return this.loadFrom(ClassLoader.getSystemClassLoader(), packagePath, subPackages);
	}
	
	/**
	 * Load all commands from the provided package
	 * 
	 * @param classLoader the ClassLoader to load the classes with
	 * @param packagePath the java package path to load the commands from
	 * @param subPackages whether or not to include sub-packages when loading the commands
	 * 
	 * @return the {@link CommandStore} instance, useful for chaining
	 */
	public CommandStore loadFrom(ClassLoader classLoader, String packagePath, boolean subPackages) {
		List<ICommand> commands = new ArrayList<>();
		
		try {
			ImmutableSet<ClassInfo> classes;
			if(subPackages) {
				classes = ClassPath.from(classLoader).getTopLevelClassesRecursive(packagePath);
			}else{
				classes = ClassPath.from(classLoader).getTopLevelClasses(packagePath);
			}
			
			for(ClassInfo info : classes) {
				Class<?> loadedClass = classLoader.loadClass(info.toString());
				if(loadedClass.isAnnotationPresent(Ignore.class)) {
					continue;
				}
				
				if(CommandUtility.isInstanceOf(loadedClass, ICommand.class)) {
					try {
						commands.add((ICommand) loadedClass.getConstructor().newInstance());
					}catch(Throwable e) {
						LOG.warn(CommandStore.getCommandLoadErrorMessage(null, loadedClass, null), e);
					}
				}else if(loadedClass.isAnnotationPresent(Module.class) || CommandUtility.isInstanceOf(loadedClass, IModule.class)) {
					try {
						commands.addAll(CommandStore.loadModule(loadedClass.getConstructor().newInstance()));
					}catch(Throwable e) {
						LOG.warn(CommandStore.getCommandLoadErrorMessage(null, loadedClass, null), e);
					}
				}
			}
		}catch(Exception e) {
			LOG.warn("Failed to load commands from package " + packagePath, e);
		}
		
		return this.addCommands(commands);
	}
	
	/**
	 * Add an array of commands and modules
	 * 
	 * @param objects the commands and modules to add, these can also be classes
	 * 
	 * @return the {@link CommandStore} instance, useful for chaining
	 */
	public CommandStore addCommands(Object... objects) {
		for(Object object : objects) {
			if(object instanceof Collection) {
				this.addCommands(((Collection<?>) object).toArray(new Object[0]));
				
				continue;
			}
			
			if(object instanceof ICommand) {
				ICommand command = (ICommand) object;
				
				this.commands.add(command.getTopParent());
				
				continue;
			}else if(object.getClass().isAnnotationPresent(Module.class) || object instanceof IModule) {
				try {
					this.commands.addAll(CommandStore.loadModule(object));
				}catch(Throwable e) {
					LOG.warn(CommandStore.getCommandLoadErrorMessage(null, object.getClass(), null), e);
				}
			}
			
			if(object instanceof Class) {
				Class<?> clazz = (Class<?>) object;
				if(CommandUtility.isInstanceOf(clazz, ICommand.class)) {					
					try {
						this.commands.add((ICommand) clazz.getConstructor().newInstance());
					}catch(Throwable e) {
						LOG.warn(CommandStore.getCommandLoadErrorMessage(null, clazz, null), e);
					}
					
					continue;
				}else if(clazz.isAnnotationPresent(Module.class) || CommandUtility.isInstanceOf(clazz, IModule.class)) {
					try {
						this.commands.addAll(CommandStore.loadModule(clazz.getConstructor().newInstance()));
					}catch(Throwable e) {
						LOG.warn(CommandStore.getCommandLoadErrorMessage(null, clazz, null), e);
					}
					
					continue;
				}
			}
			
			LOG.warn(object.getClass() + " is not a command or command container (or a class of either)");
		}
		
		return this;
	}
	
	/**
	 * Add a collection of commands and modules
	 * 
	 * @param objects the commands and modules to add
	 * 
	 * @return the {@link CommandStore} instance, useful for chaining
	 */
	public CommandStore addCommands(Collection<Object> objects)  {
		return this.addCommands(objects.toArray(new Object[0]));
	}
	
	/**
	 * Remove an array of commands
	 * 
	 * @param commands the commands to be removed
	 * 
	 * @return the {@link CommandStore} instance, useful for chaining
	 */
	public CommandStore removeCommands(ICommand... commands) {
		for(ICommand command : commands) {
			this.commands.remove(command.getTopParent());
		}
		
		return this;
	}
	
	/**
	 * Remove a collection of commands
	 * 
	 * @param commands the commands to be removed
	 * 
	 * @return the {@link CommandStore} instance, useful for chaining
	 */
	public CommandStore removeCommands(Collection<ICommand> commands) {
		return this.removeCommands(commands.toArray(new ICommand[0]));
	}
	
	/**
	 * @return an unmodifiable set of all the registered commands
	 */
	public Set<ICommand> getCommands() {
		return Collections.unmodifiableSet(this.commands);
	}
}