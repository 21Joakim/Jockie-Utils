package com.jockie.bot.core.command.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.SubCommand;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.factory.impl.ComponentFactory;
import com.jockie.bot.core.command.factory.impl.MethodCommandFactory;
import com.jockie.bot.core.command.impl.DummyCommand.AlternativeCommand;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CommandImpl extends MethodCommandImpl {
	
	protected boolean defaultGenerated;
	
	protected List<AlternativeCommand> alternativeImplementations = new ArrayList<>();
	
	public CommandImpl(String name) {
		this(name, true);
	}
	
	public CommandImpl(String name, boolean defaultGenerated) {
		super(name);
		
		this.defaultGenerated = defaultGenerated;
		
		if(defaultGenerated) {
			List<Method> commandMethods = this.getCommandMethods();
			if(commandMethods.size() == 1) {
				/* Generate all properties for this command */
				
				this.method = commandMethods.get(0);
				this.invoker = this;
				
				IComponentFactory componentFactory = ComponentFactory.getDefault();
				
				this.setArguments(componentFactory.createArguments(this.method));
				this.setOptions(componentFactory.createOptions(this.method));
				
				this.applyAnnotations();
			}else if(commandMethods.size() > 1) {
				/* Convert all onCommand methods in to alternative implementations of the command */
				for(int i = 0; i < commandMethods.size(); i++) {
					this.alternativeImplementations.add(new AlternativeCommand(this, commandMethods.get(i), this));
				}
			}
			
			Map<String, ICommand> subCommands = new HashMap<>();
			
			/* Find all method based commands in this class */
			for(Method method : this.getClass().getDeclaredMethods()) {
				if(commandMethods.contains(method)) {
					continue;
				}
				
				if(method.isAnnotationPresent(Command.class) && !method.isAnnotationPresent(SubCommand.class)) {
					ICommand subCommand = MethodCommandFactory.getDefault().create(method, CommandUtility.getCommandName(method), this);
					subCommands.put(subCommand.getCommand(), subCommand);
				}
			}
			
			/* Find all class based commands in this class */
			for(Class<ICommand> commandClass : CommandUtility.getClassesImplementing(this.getClass().getDeclaredClasses(), ICommand.class)) {
				try {
					ICommand subCommand;
					if(Modifier.isStatic(commandClass.getModifiers())) {
						subCommand = commandClass.getDeclaredConstructor().newInstance();
					}else{
						subCommand = commandClass.getDeclaredConstructor(this.getClass()).newInstance(this);
					}
					
					subCommands.put(subCommand.getCommand(), subCommand);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			/* Find all commands with the @SubCommand annotation and add them to their parent command */
			for(Method method : this.getClass().getDeclaredMethods()) {
				if(commandMethods.contains(method)) {
					continue;
				}
				
				if(method.isAnnotationPresent(Command.class) && method.isAnnotationPresent(SubCommand.class)) {
					ICommand command = MethodCommandFactory.getDefault().create(method, CommandUtility.getCommandName(method), this);
					SubCommand subCommand = method.getAnnotation(SubCommand.class);
					
					String[] path = subCommand.value();
					if(path.length == 0) {
						System.err.println("[" + this.getClass().getSimpleName() + "] Sub command (" + command.getCommand() + ") does not have a command path");
						
						continue;
					}
					
					ICommand parent = CommandUtility.getSubCommandRecursive(subCommands.get(path[0]), Arrays.copyOfRange(path, 1, path.length));
					if(parent == null) {
						System.err.println("[" + this.getClass().getSimpleName() + "] Sub command (" + command.getCommand() + ") does not have a valid command path");
						
						continue;
					}
					
					/* TODO: Implement a proper way of handling this, commands should not have to extend AbstractCommand */
					if(!(parent instanceof AbstractCommand)) {
						System.err.println("[" + this.getClass().getSimpleName() + "] Sub command (" + command.getCommand() + ") parent does not implement AbstractCommand");
						
						continue;
					}
					
					((AbstractCommand) parent).addSubCommand(command);
				}
			}
			
			for(ICommand subCommand : subCommands.values()) {
				this.addSubCommand(subCommand);
			}
		}
	}
	
	public CommandImpl(String name, Method method, Object invoker) {
		super(name, method, invoker);
		
		this.defaultGenerated = false;
	}
	
	public boolean isDefaultGenerated() {
		return this.defaultGenerated;
	}
	
	private List<Method> getCommandMethods() {
		List<Method> methods = new ArrayList<>();
		
		for(Method method : this.getClass().getDeclaredMethods()) {
			if(method.getName().equalsIgnoreCase("onCommand") || method.getName().equalsIgnoreCase("on_command")) {
				methods.add(method);
			}
		}
		
		return methods;
	}
	
	public List<Pair<String, ICommand>> getAllCommandsRecursiveWithTriggers(Message message, String prefix) {
		List<Pair<String, ICommand>> commands = super.getAllCommandsRecursiveWithTriggers(message, prefix);
		
		for(ICommand command : this.alternativeImplementations) {
			commands.add(Pair.of((prefix + " " + command.getCommand()).trim(), command));
			
			for(String alias : this.aliases) {
				commands.add(Pair.of((prefix + " " + alias).trim(), command));
			}
		}
		
		return commands;
	}
	
	public List<AlternativeCommand> getAlternativeCommandImplementations() {
		return Collections.unmodifiableList(this.alternativeImplementations);
	}
}