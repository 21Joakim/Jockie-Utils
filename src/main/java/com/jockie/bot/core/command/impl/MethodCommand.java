package com.jockie.bot.core.command.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

import com.jockie.bot.core.Context;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.option.Option;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MethodCommand extends CommandImpl {
	
	private Object invoker;
	private Method method;

	public MethodCommand(String command, Object invoker, Method method) {
		super(command, false, CommandImpl.generateDefaultArguments(method));
		
		this.invoker = invoker;
		
		Objects.requireNonNull(method);
		
		this.method = method;
		
		super.setOptions(CommandImpl.generateOptions(method));
	}
	
	public static MethodCommand createFrom(Object invoker, Method method) {
		return MethodCommand.createFrom(null, invoker, method);
	}
	
	@SuppressWarnings("unchecked")
	public static MethodCommand createFrom(String name, Object invoker, Method method) {
		MethodCommand methodCommand;
		if(method.isAnnotationPresent(Command.class)) {
			Command commandAnnotation = method.getAnnotation(Command.class);
			
			methodCommand = new MethodCommand(commandAnnotation.command().length() == 0 ? (name != null ? name : "") : commandAnnotation.command(), invoker, method);
			methodCommand.setAliases(commandAnnotation.aliases());
			methodCommand.setAuthorDiscordPermissionsNeeded(commandAnnotation.authorPermissionsNeeded());
			methodCommand.setBotDiscordPermissionsNeeded(commandAnnotation.botPermissionsNeeded());
			methodCommand.setBotTriggerable(commandAnnotation.botTriggerable());
			methodCommand.setCaseSensitive(commandAnnotation.caseSensitive());
			methodCommand.setCooldownDuration(commandAnnotation.cooldown(), commandAnnotation.cooldownUnit());
			methodCommand.setCooldownScope(commandAnnotation.cooldownScope());
			methodCommand.setDescription(commandAnnotation.description());
			methodCommand.setDeveloperCommand(commandAnnotation.developerCommand());
			methodCommand.setExecuteAsync(commandAnnotation.async());
			methodCommand.setGuildTriggerable(commandAnnotation.guildTriggerable());
			methodCommand.setHidden(commandAnnotation.hidden());
			methodCommand.setPrivateTriggerable(commandAnnotation.privateTriggerable());
			methodCommand.setShortDescription(commandAnnotation.shortDescription());
			methodCommand.setExamples(commandAnnotation.examples());
			
			for(Annotation annotation : method.getAnnotations()) {
				BiFunction<CommandEvent, Annotation, Object> function = (BiFunction<CommandEvent, Annotation, Object>) CommandImpl.getBeforeExecuteFunction(annotation.getClass());
				if(function != null) {
					methodCommand.registerBeforeExecute(commandEvent -> {
						return function.apply(commandEvent, annotation);
					});
				}
			}
			
			for(Annotation annotation : method.getAnnotations()) {
				BiFunction<CommandEvent, Annotation, Object> function = (BiFunction<CommandEvent, Annotation, Object>) CommandImpl.getAfterExecuteFunction(annotation.getClass());
				if(function != null) {
					methodCommand.registerAfterExecute(commandEvent -> {
						return function.apply(commandEvent, annotation);
					});
				}
			}
		}else{
			methodCommand = new MethodCommand(name != null ? name : "", invoker, method);
		}
		
		return methodCommand;
	}
	
	public static void executeMethodCommand(Object invoker, Method command, MessageReceivedEvent event, CommandEvent commandEvent, Object... args) throws Throwable {
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
			if(parameter.getType().equals(MessageReceivedEvent.class)) {
				arguments[i] = event;
			}else if(parameter.getType().equals(CommandEvent.class)) {
				arguments[i] = commandEvent;
			}else{
				Object context = CommandImpl.getContextVariable(event, commandEvent, args, parameter);
				if(context != null) {
					arguments[i] = context;
				}else{
					arguments[i] = args[i2++];
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
				
				information.append("Argument type mismatch for command \"" + commandEvent.getCommandTrigger() + "\"\n");
				
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
							throw ((Exception) e.getCause());
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
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... args) throws Throwable {
		MethodCommand.executeMethodCommand(this.invoker, this.method, event, commandEvent, args);
	}
}