package com.jockie.bot.core.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.module.IModule;
import com.jockie.bot.core.module.Module;
import com.jockie.bot.core.utility.LoaderUtility;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandStore {
	
	public static CommandStore of(String packagePath) {
		return new CommandStore().loadFrom(packagePath);
	}
	
	public static CommandStore of(String packagePath, boolean subPackages) {
		return new CommandStore().loadFrom(packagePath, subPackages);
	}
	
	public static List<ICommand> loadModule(Object module) {
		List<ICommand> commands = new ArrayList<>();
		Class<?> moduleClass = module.getClass();
		
		for(Method method : moduleClass.getDeclaredMethods()) {
			if(method.isAnnotationPresent(Command.class)) {
				commands.add(CommandImpl.createFrom(method.getName().replace("_", " "), module, method));
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
		
		return commands;
	}
	
	private List<ICommand> commands = new ArrayList<ICommand>();
	
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
						ICommand command = (ICommand) loadedClass.getConstructor().newInstance();
						
						commands.add(command);
					}catch(Exception e1) {
						throw new Exception("Failed to load class " + loadedClass, e1);
					}
				}else if(loadedClass.isAnnotationPresent(Module.class) || LoaderUtility.isDeepImplementation(loadedClass, IModule.class)) {
					commands.addAll(CommandStore.loadModule(loadedClass.getConstructor().newInstance()));
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
				
				ICommand topParent = command.getTopParent();
				if(!this.commands.contains(topParent)) {
					this.commands.add(topParent);
				}
				
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
	
	public List<ICommand> getCommands() {
		return Collections.unmodifiableList(this.commands);
	}
	
	public List<ICommand> getCommandsAuthorized(MessageReceivedEvent event, CommandListener commandListener) {
		return Collections.unmodifiableList(this.commands.stream().filter(c -> c.verify(event, commandListener)).collect(Collectors.toList()));
	}
}