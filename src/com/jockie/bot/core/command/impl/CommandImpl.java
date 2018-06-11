package com.jockie.bot.core.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	
	private Category category;
	
	/* Not sure about this one, might implement it in a different way. It currently only exist for edge cases hence why it isn't well implemented */
	private Map<String, Object> customProperties = new HashMap<>();
	
	private List<TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean>> customVerifications = new ArrayList<>();
	
	public CommandImpl(String command, IArgument<?>... arguments) {
		this.command = command;
		
		Method commandMethod = this.getCommandMethod();
		if(arguments.length == 0 && commandMethod != null) {
			this.generateDefaultArguments();
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
	
	public Category getCategory() {
		return this.category;
	}
	
	/** Custom properties for the command which can be used in {@link #addVerification(TriFunction)} */
	public Object getProperty(String key) {
		return this.customProperties.get(key);
	}
	
	protected CommandImpl setProperty(String key, Object value) {
		this.customProperties.put(key, value);
		
		return this;
	}
	
	protected CommandImpl setDeveloperCommand(boolean developerCommand) {
		this.developerCommand = developerCommand;
		
		return this;
	}
	
	protected CommandImpl setBotTriggerable(boolean botTriggerable) {
		this.botTriggerable = botTriggerable;
		
		return this;
	}
	
	protected CommandImpl setBotDiscordPermissionsNeeded(Permission... permissions) {
		this.botDiscordPermissionsNeeded = permissions;
		
		return this;
	}
	
	protected CommandImpl setAuthorDiscordPermissionsNeeded(Permission... permissions) {
		this.authorDiscordPermissionsNeeded = permissions;
		
		return this;
	}
	
	protected CommandImpl setDescription(String description) {
		this.description = description;
		
		return this;
	}
	
	protected CommandImpl setAliases(String... aliases) {
		this.aliases = aliases;
		
		return this;
	}
	
	protected CommandImpl setArguments(IArgument<?>... arguments) {
		this.arguments = arguments;
		
		return this;
	}
	
	protected CommandImpl setGuildTriggerable(boolean triggerable) {
		this.guildTriggerable = triggerable;
		
		return this;
	}
	
	protected CommandImpl setPrivateTriggerable(boolean triggerable) {
		this.privateTriggerable = triggerable;
		
		return this;
	}
	
	protected CommandImpl setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		
		return this;
	}
	
	protected CommandImpl setHidden(boolean hidden) {
		this.hidden = hidden;
		
		return this;
	}
	
	protected CommandImpl setCategory(Category category) {
		if(this.category != null) {
			this.category.removeCommands(this);
		}
		
		this.category = category.addCommands(this);
		
		return this;
	}
	
	/** Custom verification which will be used in {@link #verify(MessageReceivedEvent, CommandListener)} when checking for commands */
	protected CommandImpl addVerification(TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean> verification) {
		this.customVerifications.add(verification);
		
		return this;
	}
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... args) {
		Method command = this.getCommandMethod();
		
		int contextCount = 0;
		for(Parameter parameter : command.getParameters()) {
			if(parameter.getType().equals(MessageReceivedEvent.class) || parameter.getType().equals(CommandEvent.class)) {
				contextCount++;
			}
		}
		
		if(command != null) {
			Object[] arguments = new Object[args.length + contextCount];
			
			for(int i = 0, i2 = 0; i < arguments.length; i++) {
				Parameter parameter = command.getParameters()[i];
				if(parameter.getType().equals(MessageReceivedEvent.class)) {
					arguments[i] = event;
				}else if(parameter.getType().equals(CommandEvent.class)) {
					arguments[i] = commandEvent;
				}else{
					arguments[i] = args[i2++];
				}
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
		
		for(TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean> function : this.customVerifications) {
			if(!function.apply(event, commandListener, this)) {
				return false;
			}
		}
		
		return true;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void generateDefaultArguments() {
		Method command = this.getCommandMethod();
		
		int contextCount = 0;
		for(Parameter parameter : command.getParameters()) {
			if(parameter.getType().equals(MessageReceivedEvent.class) || parameter.getType().equals(CommandEvent.class)) {
				contextCount++;
			}
		}
		
		IArgument<?>[] arguments = new IArgument<?>[command.getParameterCount() - contextCount];
		if(command != null) {
			for(int i = 0, i2 = 0; i < command.getParameterCount(); i++) {
				Parameter parameter = command.getParameters()[i];
				if(parameter.getType().equals(MessageReceivedEvent.class) || parameter.getType().equals(CommandEvent.class)) {
					continue;
				}
				
				IArgument.Builder<?, ?, ?> builder = ArgumentFactory.of(parameter.getType());
				if(builder != null) {
					if(parameter.isAnnotationPresent(Argument.class)) {
						Argument info = parameter.getAnnotation(Argument.class);
						
						builder.setDescription(info.description())
							.setAcceptEmpty(info.acceptEmpty())
							.setAcceptQuote(info.acceptQuote());
						
						if(info.nullDefault()) {
							builder.setDefaultAsNull();
						}
						
						if(info.endless()) {
							if(arguments.length - 1 == i2) {
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
					
					arguments[i2++] = builder.build();
				}else{
					if(parameter.getType().isArray()) {
						if(arguments.length - 1 == i2) {
							builder = ArgumentFactory.of(parameter.getType().getComponentType());
							if(builder != null) {
								if(parameter.isAnnotationPresent(Argument.class)) {
									Argument info = parameter.getAnnotation(Argument.class);
									
									builder.setDescription(info.description())
										.setAcceptEmpty(info.acceptEmpty())
										.setAcceptQuote(info.acceptQuote());
									
									if(info.nullDefault()) {
										builder.setDefaultAsNull();
									}
									
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
								
								arguments[i2++] = endlessBuilder.build();
								
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
				return method;
			}
		}
		
		return null;
	}
}