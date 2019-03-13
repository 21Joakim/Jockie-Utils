package com.jockie.bot.core.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.jockie.bot.core.Context;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.annotation.Async;
import com.jockie.bot.core.command.annotation.AuthorPermissions;
import com.jockie.bot.core.command.annotation.BotPermissions;
import com.jockie.bot.core.command.annotation.Cooldown;
import com.jockie.bot.core.command.annotation.Developer;
import com.jockie.bot.core.command.annotation.Hidden;
import com.jockie.bot.core.command.annotation.Nsfw;
import com.jockie.bot.core.option.Option;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MethodCommand extends CommandImpl {
	
	protected final Object invoker;
	protected final Method method;
	
	public MethodCommand(String command, Method method, Object invoker) {
		super(command, false, CommandImpl.generateArguments(Objects.requireNonNull(method)));
		
		this.method = method;
		
		if(invoker == null && !Modifier.isStatic(method.getModifiers())) {
			throw new IllegalArgumentException("Non-static method can not have a null invoker");
		}
		
		this.invoker = invoker;
		
		super.setOptions(CommandImpl.generateOptions(method));
	}
	
	public Object getCommandInvoker() {
		return this.invoker;
	}
	
	public Method getCommandImplementation() {
		return this.method;
	}
	
	public void execute(CommandEvent event, Object... args) throws Throwable {
		MethodCommand.executeMethodCommand(this.invoker, this.method, event, args);
	}
	
	public static MethodCommand createFrom(Method method, Object invoker) {
		return MethodCommand.createFrom(null, method, invoker);
	}
	
	public static MethodCommand createFrom(String name, Method method, Object invoker) {
		MethodCommand methodCommand;
		if(method.isAnnotationPresent(Command.class)) {
			Command commandAnnotation = method.getAnnotation(Command.class);
			
			methodCommand = new MethodCommand(commandAnnotation.value().length() == 0 ? (name != null ? name : "") : commandAnnotation.value(), method, invoker);
			methodCommand = MethodCommand.applyCommandAnnotation(methodCommand, commandAnnotation);
		}else{
			methodCommand = new MethodCommand(name != null ? name : "", method, invoker);
		}
		
		return MethodCommand.applyAnnotations(methodCommand, method);
	}
	
	public static <T extends CommandImpl> T applyCommandAnnotation(T command, Command annotation) {
		command.setCooldownDuration(annotation.cooldown(), annotation.cooldownUnit());
		command.setCooldownScope(annotation.cooldownScope());
		
		command.setExecuteAsync(annotation.async());
		command.setAsyncOrderingKey(annotation.orderingKey().length() > 0 ? annotation.orderingKey() : null);

		command.setHidden(annotation.hidden());
		command.setDeveloper(annotation.developer());
		command.setNSFW(annotation.nsfw());
		
		command.setAuthorDiscordPermissions(annotation.authorPermissions());
		command.setBotDiscordPermissions(annotation.botPermissions());
		
		command.setDescription(annotation.description());
		command.setShortDescription(annotation.shortDescription());
		command.setArgumentInfo(annotation.argumentInfo());
		command.setAliases(annotation.aliases());
		command.setExamples(annotation.examples());
		
		command.setBotTriggerable(annotation.botTriggerable());
		command.setCaseSensitive(annotation.caseSensitive());
		command.setGuildTriggerable(annotation.guildTriggerable());
		command.setPrivateTriggerable(annotation.privateTriggerable());
		
		return command;
	}
	
	public static <T extends CommandImpl> T applyAnnotations(T command, Method method) {
		if(method.isAnnotationPresent(Cooldown.class)) {
			Cooldown cooldown = method.getAnnotation(Cooldown.class);
			
			command.setCooldownDuration(cooldown.cooldown(), cooldown.cooldownUnit());
			command.setCooldownScope(cooldown.cooldownScope());
		}
		
		if(method.isAnnotationPresent(Async.class)) {
			Async async = method.getAnnotation(Async.class);
			
			command.setExecuteAsync(async.value());
			command.setAsyncOrderingKey(async.orderingKey().length() > 0 ? async.orderingKey() : null);
		}
		
		if(method.isAnnotationPresent(Hidden.class)) {
			Hidden hidden = method.getAnnotation(Hidden.class);
			
			command.setHidden(hidden.value());
		}
		
		if(method.isAnnotationPresent(Developer.class)) {
			Developer developer = method.getAnnotation(Developer.class);
			
			command.setDeveloper(developer.value());
		}
		
		if(method.isAnnotationPresent(Nsfw.class)) {
			Nsfw nsfw = method.getAnnotation(Nsfw.class);
			
			command.setNSFW(nsfw.value());
		}
		
		if(method.isAnnotationPresent(AuthorPermissions.class)) {
			AuthorPermissions authorPermissions = method.getAnnotation(AuthorPermissions.class);
			
			command.setAuthorDiscordPermissions(authorPermissions.value());
		}
		
		if(method.isAnnotationPresent(BotPermissions.class)) {
			BotPermissions botPermissions = method.getAnnotation(BotPermissions.class);
			
			command.setAuthorDiscordPermissions(botPermissions.value());
		}
		
		return command;
	}
	
	public static void executeMethodCommand(Object invoker, Method command, CommandEvent event, Object... args) throws Throwable {
		int contextCount = 0;
		for(Parameter parameter : command.getParameters()) {
			if(parameter.getType().isAssignableFrom(MessageReceivedEvent.class) || parameter.getType().isAssignableFrom(CommandEvent.class)) {
				contextCount++;
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				contextCount++;
			}
		}
		
		Object[] arguments = new Object[args.length + contextCount];
		
		for(int i = 0, i2 = 0; i < arguments.length; i++) {
			Parameter parameter = command.getParameters()[i];
			Class<?> type = parameter.getType();
			
			if(type.equals(MessageReceivedEvent.class)) {
				arguments[i] = event.getEvent();
			}else if(type.equals(CommandEvent.class)) {
				arguments[i] = event;
			}else{
				Object context = CommandImpl.getContextVariable(event, parameter);
				if(context != null) {
					arguments[i] = context;
				}else{
					Object argument = args[i2++];
					
					if(type.isAssignableFrom(Optional.class)) {
						Type parameterType = command.getGenericParameterTypes()[i];
						
						try {
							ParameterizedType parameterizedType = (ParameterizedType) parameterType;
							
							Type[] typeArguments = parameterizedType.getActualTypeArguments();
							if(typeArguments.length > 0) {
								arguments[i] = Optional.ofNullable(argument);
							}
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
					
					if(arguments[i] == null) {
						arguments[i] = argument;
					}
				}
			}
		}
		
		try {
			if(!command.canAccess(invoker)) {
				command.setAccessible(true);
			}
			
			Object obj = command.invoke(invoker, arguments);
			if(obj != null) {
				if(obj instanceof Message) {
					event.getChannel().sendMessage((Message) obj).queue();
				}else if(obj instanceof MessageEmbed) {
					event.getChannel().sendMessage((MessageEmbed) obj).queue();
				}else if(obj instanceof CharSequence) {
					event.getChannel().sendMessage((CharSequence) obj).queue();
				}else{
					System.err.println(obj.getClass() + " is an unsupported return type for a command method");
				}
			}
		}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			if(e instanceof IllegalArgumentException) {
				StringBuilder information = new StringBuilder();
				
				information.append("Argument type mismatch for command \"" + event.getCommandTrigger() + "\"\n");
				
				information.append("    Arguments provided:\n");
				for(Object argument : arguments) {
					if(argument != null) {
						information.append("        " + argument.getClass().getName() + "\n");
					}else{
						information.append("        null\n");
					}
				}
				
				information.append("    Arguments expected:\n");
				for(Class<?> clazz : command.getParameterTypes()) {
					information.append("        " + clazz.getName() + "\n");
				}
				
				information.append("    Argument values: " + Arrays.deepToString(arguments));
				
				/* No need to throw an Exception for this, the stack trace doesn't add any additional information. I guess we should add some sort of event for this though, maybe they don't want it in the console */
				System.err.println(information);
			}else{
				if(e instanceof InvocationTargetException) {
					if(e instanceof Exception) {
						try {
							throw e.getCause();
						}catch(ClassCastException e2) {
							try {
								throw e.getCause();
							}catch(Throwable e1) {
								e1.printStackTrace();
							}
						}
					}
				}
				
				throw e;
			}
		}
	}
}