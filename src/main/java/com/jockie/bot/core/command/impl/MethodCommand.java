package com.jockie.bot.core.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.jockie.bot.core.Context;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.Command.Async;
import com.jockie.bot.core.command.Command.AuthorPermissions;
import com.jockie.bot.core.command.Command.BotPermissions;
import com.jockie.bot.core.command.Command.Cooldown;
import com.jockie.bot.core.command.Command.Developer;
import com.jockie.bot.core.command.Command.Hidden;
import com.jockie.bot.core.command.Command.Nsfw;
import com.jockie.bot.core.command.Command.Policy;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.option.Option;

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
		
		command.setContentOverflowPolicy(annotation.contentOverflowPolicy());
		command.setInvalidOptionPolicy(annotation.invalidOptionPolicy());
		
		command.setAllowedArgumentParsingTypes(annotation.allowedArgumentParsingTypes());
		
		return command;
	}
	
	public static <T extends CommandImpl> T applyAnnotations(T command, Method method) {
		if(method.isAnnotationPresent(Cooldown.class)) {
			Cooldown cooldown = method.getAnnotation(Cooldown.class);
			
			command.setCooldownDuration(cooldown.value(), cooldown.cooldownUnit());
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
			
			command.setBotDiscordPermissions(botPermissions.value());
		}
		
		if(method.isAnnotationPresent(Policy.class)) {
			Policy policy = method.getAnnotation(Policy.class);
			
			command.setContentOverflowPolicy(policy.contentOverflow());
			command.setInvalidOptionPolicy(policy.invalidOption());
		}
		
		return command;
	}
	
	public static void executeMethodCommand(Object invoker, Method command, CommandEvent event, Object... args) throws Throwable {
		IContextManager contextManager = ContextManagerFactory.getDefault();
		
		Parameter[] parameters = command.getParameters();
		Type[] genericTypes = command.getGenericParameterTypes();
		
		List<Integer> contextIndexes = new ArrayList<>();
		for(int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			
			if(contextManager.isEnforcedContext(parameter.getParameterizedType())) {
				contextIndexes.add(i);
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				contextIndexes.add(i);
			}
		}
		
		Object[] arguments = new Object[args.length + contextIndexes.size()];
		
		for(int i = 0, i2 = 0; i < arguments.length; i++) {
			Parameter parameter = parameters[i];
			Class<?> type = parameter.getType();
			
			if(contextIndexes.contains(i)) {
				if(parameter.isAnnotationPresent(Option.class)) {
					Option optionAnnotation = parameter.getAnnotation(Option.class);
					
					boolean contains = false;
					for(String option : event.getOptionsPresent()) {
						if(option.equalsIgnoreCase(optionAnnotation.value())) {
							contains = true;
							
							break;
						}
						
						for(String alias : optionAnnotation.aliases()) {
							if(option.equalsIgnoreCase(alias)) {
								contains = true;
								
								break;
							}
						}
					}
					
					arguments[i] = contains;
					
					continue;
				}else{
					Object context = contextManager.getContext(event, parameter);
					if(context != null) {
						arguments[i] = context;
						
						continue;
					}else{
						throw new IllegalStateException("There is no context available for " + parameter.getType());
					}
				}
			}
			
			Object argument = args[i2++];
			
			if(type.isAssignableFrom(Optional.class)) {
				Type parameterType = genericTypes[i];
				
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
		
		try {
			if(!command.canAccess(invoker)) {
				command.setAccessible(true);
			}
			
			Object object = command.invoke(invoker, arguments);
			if(object != null) {
				IReturnManager returnManager = event.getCommandListener().getReturnManager();
				
				if(!returnManager.perform(event, object)) {
					System.err.println(object.getClass() + " is an unsupported return type for a command method");
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
				
				/* No need to throw an Exception for this, the stack trace doesn't add any additional information. 
				 * I guess we should add some sort of event for this though, maybe they don't want it in the console 
				 */
				System.err.println(information);
			}else{
				if(e instanceof InvocationTargetException) {
					if(e.getCause() != null) {
						throw e.getCause();
					}
				}
				
				throw e;
			}
		}
	}
}