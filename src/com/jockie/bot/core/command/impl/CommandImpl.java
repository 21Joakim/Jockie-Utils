package com.jockie.bot.core.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.argument.Endless;
import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.impl.ArgumentFactory;
import com.jockie.bot.core.command.argument.impl.EndlessArgumentImpl;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandImpl implements ICommand {
	
	private String command;
	
	private String description;
	
	private String[] aliases = {};
	
	private IArgument<?>[] arguments = {};
	
	private Permission[] botDiscordPermissionsNeeded = {};
	private Permission[] authorDiscordPermissionsNeeded = {};
	
	private boolean guildTriggerable = true;
	private boolean privateTriggerable;
	
	private boolean caseSensitive;
	
	private boolean botTriggerable;
	
	private boolean developerCommand;
	
	private boolean hidden;
	
	public CommandImpl(String command, IArgument<?>... arguments) {
		this.command = command;
		
		Method commandMethod = this.getCommandMethod();
		if(arguments.length == 0 && (commandMethod != null && commandMethod.getParameterCount() > 2)) {
			this.setDefaultArguments();
		}else{
			this.arguments = arguments;
		}
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public IArgument<?>[] getArguments() {
		return this.arguments;
	}
	
	public Permission[] getBotDiscordPermissionsNeeded() {
		return this.botDiscordPermissionsNeeded;
	}
	
	public Permission[] getAuthorDiscordPermissionsNeeded() {
		return this.authorDiscordPermissionsNeeded;
	}
	
	public boolean isGuildTriggerable() {
		return this.guildTriggerable;
	}
	
	public boolean isPrivateTriggerable() {
		return this.privateTriggerable;
	}
	
	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}
	
	public boolean isBotTriggerable() {
		return this.botTriggerable;
	}
	
	public boolean isDeveloperCommand() {
		return this.developerCommand;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	protected void setDeveloperCommand(boolean developerCommand) {
		this.developerCommand = developerCommand;
	}
	
	protected void setBotTriggerable(boolean botTriggerable) {
		this.botTriggerable = botTriggerable;
	}
	
	protected void setBotDiscordPermissionsNeeded(Permission... permissions) {
		this.botDiscordPermissionsNeeded = permissions;
	}
	
	protected void setAuthorDiscordPermissionsNeeded(Permission... permissions) {
		this.authorDiscordPermissionsNeeded = permissions;
	}
	
	protected void setDescription(String description) {
		this.description = description;
	}
	
	protected void setAliases(String... aliases) {
		this.aliases = aliases;
	}
	
	protected void setArguments(IArgument<?>... arguments) {
		this.arguments = arguments;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void setDefaultArguments() {
		Method command = null;
		
		for(Method method : this.getClass().getMethods()) {
			if(method.getName().equals("onCommand")) {
				if(method.getParameterCount() >= 2) {
					if(method.getParameterTypes()[0].equals(MessageReceivedEvent.class) && method.getParameterTypes()[1].equals(CommandEvent.class)) {
						command = method;
						
						break;
					}
				}
			}
		}
		
		IArgument<?>[] arguments = new IArgument<?>[command.getParameters().length - 2];
		if(command != null) {
			for(int i = 0; i < arguments.length; i++) {
				Parameter parameter = command.getParameters()[i + 2];
				
				IArgument.Builder<?, ?, ?> builder = ArgumentFactory.of(parameter.getType());
				if(builder != null) {
					if(parameter.isAnnotationPresent(Argument.class)) {
						Argument info = parameter.getAnnotation(Argument.class);
						
						builder.setDescription(info.description())
							.setAcceptEmpty(info.acceptEmpty())
							.setAcceptQuote(info.acceptQuote());
						
						if(info.endless()) {
							if(arguments.length - 1 == i) {
								builder.setEndless(info.endless());
							}else{
								throw new IllegalArgumentException("Only the last argument may be endless");
							}
						}
					}else{
						if(parameter.isNamePresent()) {
							builder.setDescription(parameter.getName());
						}
					}
					
					arguments[i] = builder.build();
				}else{
					if(parameter.getType().isArray()) {
						if(arguments.length - 1 == i) {
							builder = ArgumentFactory.of(parameter.getType().getComponentType());
							if(builder != null) {
								if(parameter.isAnnotationPresent(Argument.class)) {
									Argument info = parameter.getAnnotation(Argument.class);
									
									builder.setDescription(info.description())
										.setAcceptEmpty(info.acceptEmpty())
										.setAcceptQuote(info.acceptQuote());
									
									if(info.endless()) {
										throw new IllegalArgumentException("An endless argument may not be endless");
									}
								}else{
									if(parameter.isNamePresent()) {
										builder.setDescription(parameter.getName());
									}
								}
								
								EndlessArgumentImpl.Builder endlessBuilder = new EndlessArgumentImpl.Builder(parameter.getType().getComponentType()).setArgument(builder.build());
								if(parameter.isAnnotationPresent(Endless.class)) {
									Endless info = parameter.getAnnotation(Endless.class);
									
									endlessBuilder.setMinArguments(info.minArguments()).setMaxArguments(info.maxArguments());
								}
								
								arguments[i] = endlessBuilder.build();
								
								break;
							}else{
								throw new IllegalArgumentException("There are no default arguments for " + parameter.getType().getComponentType().toString());
							}
						}else{
							throw new IllegalArgumentException("Only the last argument may be endless");
						}
					}
					
					throw new IllegalArgumentException("There are no default arguments for " + parameter.getType().toString());
				}
			}
		}
		
		this.arguments = arguments;
	}
	
	protected void setGuildTriggerable(boolean triggerable) {
		this.guildTriggerable = triggerable;
	}
	
	protected void setPrivateTriggerable(boolean triggerable) {
		this.privateTriggerable = triggerable;
	}
	
	protected void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
	
	protected void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... args) {
		Method command = this.getCommandMethod();
		
		if(command != null) {
			Object[] arguments = new Object[args.length + 2];
			arguments[0] = event;
			arguments[1] = commandEvent;
			
			for(int i = 0; i < args.length; i++) {
				arguments[2 + i] = args[i];
			}
			
			try {
				command.invoke(this, arguments);
			}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.out.println("Has:");
				for(Object argument : arguments) {
					System.out.println("    " + argument.getClass().getName());
				}
				
				System.out.println("Wants:");
				for(Class<?> clazz : command.getParameterTypes()) {
					System.out.println("    " + clazz.getName());
				}
				
				System.out.println("Values: " + Arrays.deepToString(arguments));
				
				e.printStackTrace();
			}
		}
	}
	
	public boolean verify(MessageReceivedEvent event, CommandListener commandListener) {
		if(!this.botTriggerable && event.getAuthor().isBot()) {
			return false;
		}
		
		if(!this.isGuildTriggerable() && event.getChannelType().isGuild()) {
			return false;
		}
		
		if(!this.isPrivateTriggerable() && event.getChannelType().equals(ChannelType.PRIVATE)) {
			return false;
		}
		
		if(this.developerCommand && !commandListener.getDevelopers().contains(event.getAuthor().getIdLong())) {
			return false;
		}
		
		if(event.isFromType(ChannelType.TEXT)) {
			if(this.authorDiscordPermissionsNeeded.length > 0) {
				if(event.getMember() != null) {
					Permission[] permissions = this.authorDiscordPermissionsNeeded;
					if(!event.getMember().hasPermission(event.getTextChannel(), permissions) && !event.getMember().hasPermission(permissions)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	private Method getCommandMethod() {
		for(Method method : this.getClass().getMethods()) {
			if(method.getName().equals("onCommand")) {
				if(method.getParameterCount() >= 2) {
					if(method.getParameterTypes()[0].equals(MessageReceivedEvent.class) && method.getParameterTypes()[1].equals(CommandEvent.class)) {
						return method;
					}
				}
			}
		}
		
		return null;
	}
}