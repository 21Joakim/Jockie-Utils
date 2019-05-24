package com.jockie.bot.core.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.jockie.bot.core.Context;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.Command.Async;
import com.jockie.bot.core.command.Command.AuthorPermissions;
import com.jockie.bot.core.command.Command.BotPermissions;
import com.jockie.bot.core.command.Command.Cooldown;
import com.jockie.bot.core.command.Command.Developer;
import com.jockie.bot.core.command.Command.Hidden;
import com.jockie.bot.core.command.Command.Nsfw;
import com.jockie.bot.core.command.Command.Policy;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.factory.impl.ComponentFactory;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.option.Option;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class MethodCommandImpl extends AbstractCommand implements IMethodCommand {
	
	protected Method method;
	protected Object invoker;
	
	private List<DummyCommand> dummyCommands = Collections.emptyList();
	
	public MethodCommandImpl(String name) {
		super(name);
	}
	
	public MethodCommandImpl(String name, Method method, Object invoker) {
		super(name);
		
		this.method = method;
		this.invoker = invoker;
		
		IComponentFactory componentFactory = ComponentFactory.getDefault();
		
		this.setArguments(componentFactory.createArguments(this.method));
		this.setOptions(componentFactory.createOptions(this.method));
		
		this.applyAnnotations();
	}
	
	public Method getCommandMethod() {
		return this.method;
	}
	
	public Object getCommandInvoker() {
		return this.invoker;
	}
	
	public MethodCommandImpl setCommandMethod(Method method) {
		this.method = method;
		
		return this;
	}
	
	public MethodCommandImpl setCommandInvoker(Object invoker) {
		this.invoker = invoker;
		
		return this;
	}
	
	public AbstractCommand setArguments(IArgument<?>... arguments) {
		super.setArguments(arguments);
		
		this.dummyCommands = MethodCommandImpl.generateDummyCommands(this);
		
		return this;
	}
	
	public boolean isPassive() {
		if(this.passive) {
			return true;
		}
		
		if(this.method == null) {
			return true;
		}
		
		return false;
	}
	
	public List<ICommand> getAllCommandsRecursive(boolean includeDummyCommands) {
		List<ICommand> commands = new ArrayList<>();
		commands.add(this);
		
		for(ICommand command : this.subCommands) {
			commands.addAll(command.getAllCommandsRecursive(includeDummyCommands));
		}
		
		if(includeDummyCommands) {
			commands.addAll(this.dummyCommands);
		}
		
		return commands;
	}
	
	public List<Pair<String, ICommand>> getAllCommandsRecursiveWithTriggers(Message message, String prefix) {
		List<Pair<String, ICommand>> commands = super.getAllCommandsRecursiveWithTriggers(message, prefix);
		
		for(ICommand command : this.dummyCommands) {
			commands.add(Pair.of((prefix + " " + command.getCommand()).trim(), command));
			
			for(String alias : this.aliases) {
				commands.add(Pair.of((prefix + " " + alias).trim(), command));
			}
		}
		
		return commands;
	}
	
	public void execute(CommandEvent event, Object... args) throws Throwable {
		if(!this.passive && this.method != null) {
			MethodCommandImpl.executeMethodCommand(this.invoker, this.method, event, args);
		}
	}
	
	protected void applyCommandAnnotation(Command annotation) {
		this.setCooldownDuration(annotation.cooldown(), annotation.cooldownUnit());
		this.setCooldownScope(annotation.cooldownScope());
		
		this.setExecuteAsync(annotation.async());
		this.setAsyncOrderingKey(annotation.orderingKey().length() > 0 ? annotation.orderingKey() : null);

		this.setHidden(annotation.hidden());
		this.setDeveloper(annotation.developer());
		this.setNSFW(annotation.nsfw());
		
		this.setAuthorDiscordPermissions(annotation.authorPermissions());
		this.setBotDiscordPermissions(annotation.botPermissions());
		
		this.setDescription(annotation.description());
		this.setShortDescription(annotation.shortDescription());
		this.setArgumentInfo(annotation.argumentInfo());
		this.setAliases(annotation.aliases());
		this.setExamples(annotation.examples());
		
		this.setBotTriggerable(annotation.botTriggerable());
		this.setCaseSensitive(annotation.caseSensitive());
		this.setGuildTriggerable(annotation.guildTriggerable());
		this.setPrivateTriggerable(annotation.privateTriggerable());
		
		this.setContentOverflowPolicy(annotation.contentOverflowPolicy());
		this.setInvalidOptionPolicy(annotation.invalidOptionPolicy());
		
		this.setAllowedArgumentParsingTypes(annotation.allowedArgumentParsingTypes());
		this.setArgumentTrimType(annotation.argumentTrimType());
	}
	
	
	protected void applyAnnotations() {
		if(this.method.isAnnotationPresent(Command.class)) {
			this.applyCommandAnnotation(this.method.getAnnotation(Command.class));
		}
		
		if(this.method.isAnnotationPresent(Cooldown.class)) {
			Cooldown cooldown = this.method.getAnnotation(Cooldown.class);
			
			this.setCooldownDuration(cooldown.value(), cooldown.cooldownUnit());
			this.setCooldownScope(cooldown.cooldownScope());
		}
		
		if(this.method.isAnnotationPresent(Async.class)) {
			Async async = this.method.getAnnotation(Async.class);
			
			this.setExecuteAsync(async.value());
			this.setAsyncOrderingKey(async.orderingKey().length() > 0 ? async.orderingKey() : null);
		}
		
		if(this.method.isAnnotationPresent(Hidden.class)) {
			Hidden hidden = this.method.getAnnotation(Hidden.class);
			
			this.setHidden(hidden.value());
		}
		
		if(this.method.isAnnotationPresent(Developer.class)) {
			Developer developer = this.method.getAnnotation(Developer.class);
			
			this.setDeveloper(developer.value());
		}
		
		if(this.method.isAnnotationPresent(Nsfw.class)) {
			Nsfw nsfw = this.method.getAnnotation(Nsfw.class);
			
			this.setNSFW(nsfw.value());
		}
		
		if(this.method.isAnnotationPresent(AuthorPermissions.class)) {
			AuthorPermissions authorPermissions = this.method.getAnnotation(AuthorPermissions.class);
			
			this.setAuthorDiscordPermissions(authorPermissions.value());
		}
		
		if(this.method.isAnnotationPresent(BotPermissions.class)) {
			BotPermissions botPermissions = this.method.getAnnotation(BotPermissions.class);
			
			this.setBotDiscordPermissions(botPermissions.value());
		}
		
		if(this.method.isAnnotationPresent(Policy.class)) {
			Policy policy = this.method.getAnnotation(Policy.class);
			
			this.setContentOverflowPolicy(policy.contentOverflow());
			this.setInvalidOptionPolicy(policy.invalidOption());
		}
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
	
	
	public static List<DummyCommand> generateDummyCommands(ICommand command) {
		List<DummyCommand> dummyCommands = new ArrayList<>();
		
		if(!(command instanceof DummyCommand)) {
			List<IArgument<?>> arguments = command.getArguments();
			List<IArgument<?>> dummyArguments = new ArrayList<>();
			if(arguments.size() > 0) {
				for(int i = 0; i < arguments.size(); i++) {
					IArgument<?> argument = arguments.get(i);
					if(argument.hasDefault()) {
						dummyArguments.add(argument);
					}
				}
				
				if(dummyArguments.size() > 0) {
					List<IArgument<?>> args = new ArrayList<>();
			    	for(int i = 1, max = 1 << dummyArguments.size(); i < max; ++i) {
			    	    for(int j = 0, k = 1; j < dummyArguments.size(); ++j, k <<= 1) {
			    	        if((k & i) != 0) {
			    	        	args.add(dummyArguments.get(j));
			    	        }
			    	    }
			    	    
			    	    dummyCommands.add(new DummyCommand(command, args.toArray(new IArgument[0])));
						
						args.clear();
			    	}
				}
			}
		}
		
		return dummyCommands;
	}
}
