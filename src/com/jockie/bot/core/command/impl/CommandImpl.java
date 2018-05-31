package com.jockie.bot.core.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.argument.Endless;
import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.impl.ArgumentFactory;
import com.jockie.bot.core.command.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.utility.TriFunction;

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
	
	/* Not sure about these two, might implement it in a different way. They currently only exist for edge cases hence why they aren't well implemented */
	private Map<String, Object> customProperties = new HashMap<>();
	private Map<String, TriFunction<MessageReceivedEvent, CommandListener, Object, Boolean>> customVerification = new HashMap<>();
	
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
	
	public Object getProperty(String key) {
		return this.customProperties.get(key);
	}
	
	public TriFunction<MessageReceivedEvent, CommandListener, Object, Boolean> getVerification(String key) {
		return this.customVerification.get(key);
	}
	
	protected void setVerification(String key, TriFunction<MessageReceivedEvent, CommandListener, Object, Boolean> verificationFunction) {
		this.customVerification.put(key, verificationFunction);
	}
	
	protected void setProperty(String key, Object value) {
		this.customProperties.put(key, value);
	}
	
	protected void setProperty(String key, Object value, TriFunction<MessageReceivedEvent, CommandListener, Object, Boolean> verificationFunction) {
		this.setProperty(key, value);
		
		this.customVerification.put(key, verificationFunction);
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
				if(e instanceof IllegalArgumentException) {
					StringBuilder information = new StringBuilder();
					
					information.append("Argument type mismatch for command \"" + this.getCommand() + "\"\n");
					
					information.append("    Arguments provided:\n");
					for(Object argument : arguments) {
						information.append("        " + argument.getClass().getName() + "\n");
					}
					
					information.append("    Arguments expected:\n");
					for(Class<?> clazz : command.getParameterTypes()) {
						information.append("        " + clazz.getName() + "\n");
					}
					
					information.append("    Argument values: " + Arrays.deepToString(arguments));
					
					/* No need to throw an Exception for this, the stack trace doesn't add any additional information. I guess we should add some sort of event for this though, maybe they don't want it in the console */
					System.err.println(information);
				}else{
					e.printStackTrace();
				}
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
		
		if(event.getChannelType().isGuild()) {
			if(this.authorDiscordPermissionsNeeded.length > 0) {
				if(event.getMember() != null) {
					Permission[] permissions = this.authorDiscordPermissionsNeeded;
					if(!event.getMember().hasPermission(event.getTextChannel(), permissions) && !event.getMember().hasPermission(permissions)) {
						return false;
					}
				}
			}
		}
		
		for(String key : this.customVerification.keySet()) {
			Object property = this.customProperties.get(key);
			if(property == null) {
				return false;
			}
			
			if(!this.customVerification.get(key).apply(event, commandListener, property)) {
				return false;
			}
		}
		
		return true;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void setDefaultArguments() {
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