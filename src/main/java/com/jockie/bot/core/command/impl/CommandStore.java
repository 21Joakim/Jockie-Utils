package com.jockie.bot.core.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.Initialize;
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
	
	public static List<ICommand> loadModule(Object module) {
		Objects.requireNonNull(module);
		
		List<ICommand> commands = new ArrayList<>();
		Class<?> moduleClass = module.getClass();
		
		Map<String, MethodCommand> methodCommands = new HashMap<>();
		
		Method[] methods = moduleClass.getDeclaredMethods();
		for(Method method : methods) {
			if(method.isAnnotationPresent(Command.class)) {
				methodCommands.put(method.getName(), MethodCommand.createFrom(method.getName().replace("_", " "), method, module));
			}
		}
		
		for(Method method : methods) {
			if(method.isAnnotationPresent(Initialize.class)) {
				Initialize initialize = method.getAnnotation(Initialize.class);
				if(initialize.all()) {
					for(MethodCommand command : methodCommands.values()) {
						try {
							method.invoke(Modifier.isStatic(method.getModifiers()) ? null : module, command);
						}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}else if(initialize.value().length == 0) {
					if(methodCommands.containsKey(method.getName())) {
						MethodCommand command = methodCommands.get(method.getName());
						
						try {
							method.invoke(Modifier.isStatic(method.getModifiers()) ? null : module, command);
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
								method.invoke(Modifier.isStatic(method.getModifiers()) ? null : module, command);
							}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		commands.addAll(methodCommands.values());
		
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