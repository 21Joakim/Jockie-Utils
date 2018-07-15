package com.jockie.bot.core.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.argument.Argument;
import com.jockie.bot.core.command.argument.Endless;
import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.impl.ArgumentFactory;
import com.jockie.bot.core.command.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.utility.LoaderUtility;
import com.jockie.bot.core.utility.TriFunction;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CommandImpl implements ICommand {
	
	public static void executeMethodCommand(Object invoker, Method command, MessageReceivedEvent event, CommandEvent commandEvent, Object... args) throws Exception {
		int contextCount = 0;
		for(Parameter parameter : command.getParameters()) {
			if(parameter.getType().equals(MessageReceivedEvent.class) || parameter.getType().equals(CommandEvent.class)) {
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
				arguments[i] = args[i2++];
			}
		}
		
		try {
			if(command.canAccess(invoker)) {
				command.invoke(invoker, arguments);
			}else{
				command.setAccessible(true);
				
				command.invoke(invoker, arguments);
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
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static IArgument<?>[] generateDefaultArguments(Method command) {
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
						
						builder.setName(info.name())
							.setAcceptEmpty(info.acceptEmpty())
							.setAcceptQuote(info.acceptQuote())
							.setError(info.error().length() > 0 ? info.error() : null);
						
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
							builder.setName(parameter.getName());
						}
					}
					
					arguments[i2++] = builder.build();
				}else{
					if(parameter.getType().isArray()) {
						builder = ArgumentFactory.of(parameter.getType().getComponentType());
						if(builder != null) {
							if(parameter.isAnnotationPresent(Argument.class)) {
								Argument info = parameter.getAnnotation(Argument.class);
								
								builder.setName(info.name())
									.setAcceptEmpty(info.acceptEmpty())
									.setAcceptQuote(info.acceptQuote())
									.setError(info.error().length() > 0 ? info.error() : null);
								
								if(info.nullDefault()) {
									builder.setDefaultAsNull();
								}
								
								if(info.endless()) {
									throw new IllegalArgumentException("Not a valid candidate, candidate may not be endless");
								}
							}else{
								if(parameter.isNamePresent()) {
									builder.setName(parameter.getName());
								}
							}
							
							EndlessArgumentImpl.Builder<?> endlessBuilder = new EndlessArgumentImpl.Builder(parameter.getType().getComponentType()).setArgument(builder.build());
							if(parameter.isAnnotationPresent(Endless.class)) {
								Endless info = parameter.getAnnotation(Endless.class);
								
								endlessBuilder.setMinArguments(info.minArguments()).setMaxArguments(info.maxArguments()).setEndless(info.endless());
							}
							
							IArgument<?> argument = endlessBuilder.build();
							
							if(argument.isEndless()) {
								if(arguments.length - 1 != i2) {
									throw new IllegalArgumentException("Only the last argument may be endless");
								}
							}
							
							arguments[i2++] = argument;
							
							continue;
						}else{
							throw new IllegalArgumentException("There are no default arguments for " + parameter.getType().getComponentType().toString());
						}
					}
					
					throw new IllegalArgumentException("There are no default arguments for " + parameter.getType().toString());
				}
			}
		}
		
		return arguments;
	}
	
	public MethodCommand createFrom(String name, Method method) {
		MethodCommand methodCommand;
		if(method.isAnnotationPresent(Command.class)) {
			Command annotation = method.getAnnotation(Command.class);
			
			methodCommand = new MethodCommand(annotation.command().length() == 0 ? (name != null ? name : "") : annotation.command(), this, method);
			methodCommand.setAliases(annotation.aliases());
			methodCommand.setAuthorDiscordPermissionsNeeded(annotation.authorPermissionsNeeded());
			methodCommand.setBotDiscordPermissionsNeeded(annotation.botPermissionsNeeded());
			methodCommand.setBotTriggerable(annotation.botTriggerable());
			methodCommand.setCaseSensitive(annotation.caseSensitive());
			methodCommand.setCooldownDuration(annotation.cooldown());
			methodCommand.setDescription(annotation.description());
			methodCommand.setDeveloperCommand(annotation.developerCommand());
			methodCommand.setExecuteAsync(annotation.async());
			methodCommand.setGuildTriggerable(annotation.guildTriggerable());
			methodCommand.setHidden(annotation.hidden());
			methodCommand.setPrivateTriggerable(annotation.privateTriggerable());
		}else{
			methodCommand = new MethodCommand(name != null ? name : "", this, method);
		}
		
		return methodCommand;
	}
	
	public MethodCommand createFrom(Method method) {
		return this.createFrom(null, method);
	}
	
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
	
	private boolean executeAsync;
	
	private Category category;
	
	private long cooldownDuration = 0;
	
	private ICommand parent;
	
	private List<ICommand> subCommands = new ArrayList<>();
	
	/* Not sure about this one, might implement it in a different way. It currently only exist for edge cases hence why it isn't well implemented */
	private Map<String, Object> customProperties = new HashMap<>();
	
	private List<TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean>> customVerifications = new ArrayList<>();
	
	private List<ICommand> dummyCommands;
	
	private boolean passive = false;
	
	private List<Method> commandMethods = this.getCommandMethods();
	
	private boolean defaultGenerated = false;
	
	public CommandImpl(String command, boolean generateDefault, IArgument<?>... arguments) {
		this.command = command;
		
		if(generateDefault) {
			if(arguments.length == 0 && this.commandMethods.size() == 1) {
				this.arguments = CommandImpl.generateDefaultArguments(this.commandMethods.get(0));
			}else{
				if(this.commandMethods.size() > 1) {
					for(int i = 0; i < this.commandMethods.size(); i++) {
						this.addSubCommand(this.createFrom(this.commandMethods.get(i)));
					}
					
					if(arguments.length > 0) {
						/* Won't make any difference */
					}
				}else{
					this.arguments = arguments;
				}
			}
			
			for(Method method :this.getClass().getDeclaredMethods()) {
				if(!method.getName().equals("onCommand")) {
					if(method.isAnnotationPresent(Command.class)) {
						this.addSubCommand(this.createFrom(method.getName().replace("_", " "), method));
					}
				}
			}
			
			for(Class<?> clazz : this.getClass().getClasses()) {
				if(LoaderUtility.isDeepImplementation(clazz, ICommand.class)) {
					try {
						try {
							Constructor<?> constructor = clazz.getDeclaredConstructor();
							
							this.addSubCommand((ICommand) constructor.newInstance());
							
							continue;
						}catch(NoSuchMethodException | SecurityException e) {}
						
						try {
							Constructor<?> constructor = clazz.getDeclaredConstructor(this.getClass());
							
							this.addSubCommand((ICommand) constructor.newInstance(this));
							
							continue;
						}catch(NoSuchMethodException | SecurityException e) {}
						
					}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			this.arguments = arguments;
		}
		
		this.defaultGenerated = generateDefault;
		
		this.dummyCommands = LoaderUtility.generateDummyCommands(this);
	}
	
	public CommandImpl(String command, IArgument<?>... arguments) {
		this(command, true, arguments);
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
	
	public long getCooldownDuration() {
		return this.cooldownDuration;
	}
	
	public boolean isExecuteAsync() {
		return this.executeAsync;
	}
	
	public ICommand getParent() {
		return this.parent;
	}
	
	public boolean isPassive() {
		if(this.passive) {
			return true;
		}
		
		try {
			if(this.getClass().getMethod("execute", MessageReceivedEvent.class, CommandEvent.class, Object[].class).getDeclaringClass().equals(CommandImpl.class)) {
				if(this.defaultGenerated && (this.commandMethods.size() == 0 || this.commandMethods.size() > 1)) {
					return true;
				}
			}
		}catch(Exception e) {}
		
		return this.passive;
	}
	
	public List<ICommand> getSubCommands() {
		return this.subCommands;
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
		/* 
		 * From the longest alias to the shortest so that if the command for instance has two aliases one being "hello" 
		 * and the other being "hello there" it would recognize that the command is "hello there" instead of it thinking that
		 * "hello" is the command and "there" being the argument.
		 */
		Arrays.sort(aliases, (a, b) -> Integer.compare(b.length(), a.length()));
		
		this.aliases = aliases;
		
		return this;
	}
	
	protected CommandImpl setArguments(IArgument<?>... arguments) {
		this.arguments = arguments;
		this.dummyCommands = LoaderUtility.generateDummyCommands(this);
		
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
	
	/**
	 * See {@link #getCooldownDuration()}
	 * @param duration milliseconds
	 */
	protected CommandImpl setCooldownDuration(long duration) {
		this.cooldownDuration = duration;
		
		return this;
	}
	
	/**
	 * See {@link #getCooldownDuration()}
	 */
	protected CommandImpl setCooldownDuration(long duration, TimeUnit unit) {
		return this.setCooldownDuration(unit.toMillis(duration));
	}
	
	protected CommandImpl setExecuteAsync(boolean executeAsync) {
		this.executeAsync = executeAsync;
		
		return this;
	}
	
	protected CommandImpl setParent(ICommand parent) {
		this.parent = parent;
		
		return this;
	}
	
	protected CommandImpl setPassive(boolean passive) {
		this.passive = passive;
		
		return this;
	}
	
	protected CommandImpl addSubCommand(ICommand command) {
		this.subCommands.add(command);
		
		if(command instanceof CommandImpl) {
			((CommandImpl) command).setParent(this);
		}
		
		return this;
	}
	
	/** Custom verification which will be used in {@link #verify(MessageReceivedEvent, CommandListener)} when checking for commands */
	protected CommandImpl addVerification(TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean> verification) {
		this.customVerifications.add(verification);
		
		return this;
	}
	
	protected CommandImpl addVerification(BiFunction<MessageReceivedEvent, CommandImpl, Boolean> verification) {
		this.customVerifications.add((event, listener, impl) -> verification.apply(event, impl));
		
		return this;
	}
	
	protected CommandImpl addVerification(Function<MessageReceivedEvent, Boolean> verification) {
		this.customVerifications.add((event, listener, impl) -> verification.apply(event));
		
		return this;
	}
	
	public List<Pair<ICommand, List<?>>> getAllCommandsRecursive(String prefix) {
		List<Pair<ICommand, List<?>>> commands = new ArrayList<>();
		List<Object> commandTriggers = new ArrayList<>();
		
		commandTriggers.add((prefix + " " + this.getCommand()).trim());
		
		for(String alias : this.aliases) {
			commandTriggers.add((prefix + " " + alias).trim());
		}
		
		for(ICommand command : this.getSubCommands()) {
			commands.addAll(command.getAllCommandsRecursive((prefix + " " + this.getCommand()).trim()));
			
			for(String alias : this.aliases) {
				commands.addAll(command.getAllCommandsRecursive((prefix + " " + alias).trim()));
			}
		}
		
		for(ICommand command : this.dummyCommands) {
			List<String> triggers = new ArrayList<>();
			
			triggers.add((prefix + " " + command.getCommand()).trim());
			
			for(String alias : this.aliases) {
				triggers.add((prefix + " " + alias).trim());
			}
			
			commandTriggers.add(Pair.of(command, triggers));
		}
		
		commands.add(Pair.of(this, commandTriggers));
		
		return commands;
	}
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... args) throws Exception {
		if(!this.passive && this.commandMethods.size() == 1) {
			CommandImpl.executeMethodCommand(this, this.commandMethods.get(0), event, commandEvent, args);
		}
	}
	
	public boolean verify(MessageReceivedEvent event, CommandListener commandListener) {
		if(event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
			return false;
		}
		
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
	
	private List<Method> getCommandMethods() {
		List<Method> methods = new ArrayList<>();
		
		for(Method method : this.getClass().getDeclaredMethods()) {
			if(method.getName().equals("onCommand")) {
				methods.add(method);
			}
		}
		
		return methods;
	}
}