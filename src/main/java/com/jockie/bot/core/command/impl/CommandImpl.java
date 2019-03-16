package com.jockie.bot.core.command.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.jockie.bot.core.Context;
import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Arguments;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.impl.ArgumentFactory;
import com.jockie.bot.core.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.impl.factory.MethodCommandFactory;
import com.jockie.bot.core.cooldown.ICooldown;
import com.jockie.bot.core.cooldown.ICooldown.Scope;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.option.impl.OptionImpl;
import com.jockie.bot.core.utility.LoaderUtility;
import com.jockie.bot.core.utility.TriFunction;

import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CommandImpl implements ICommand {
	
	@SuppressWarnings("rawtypes")
	private static Map<Class, BiFunction> contexts = new HashMap<>();
	
	public static <T> void registerContext(Class<T> type, BiFunction<CommandEvent, Parameter, T> function) {
		CommandImpl.contexts.put(Objects.requireNonNull(type), Objects.requireNonNull(function));
	}
	
	public static void unregisterContext(Class<?> type) {
		CommandImpl.contexts.remove(type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> BiFunction<CommandEvent, Parameter, T> getContextFunction(Class<T> type) {
		return CommandImpl.contexts.get(type);
	}
	
	public static Object getContextVariable(CommandEvent event, Parameter parameter) {
		final Class<?> type = parameter.getType();
		
		if(type.isAssignableFrom(MessageReceivedEvent.class)) {
			return event;
		}else if(type.isAssignableFrom(CommandEvent.class)) {
			return event;
		}else if(parameter.isAnnotationPresent(Context.class)) {
			Class<?> command = event.getCommand().getClass();
			if(type.isAssignableFrom(command)) {
				return event.getCommand();
			}
			
			if(type.isAssignableFrom(CommandListener.class)) {
				return event.getCommandListener();
			}
			
			if(type.isAssignableFrom(JDAImpl.class)) {
				return event.getJDA();
			}else if(type.isAssignableFrom(UserImpl.class)) {
				return event.getAuthor();
			}else if(type.isAssignableFrom(ChannelType.class)) {
				return event.getChannelType();
			}else if(type.isAssignableFrom(MessageChannel.class)) {
				return event.getChannel();
			}else if(type.isAssignableFrom(Message.class)) {
				return event.getMessage();
			}
			
			if(event.getChannelType().isGuild()) {
				if(type.isAssignableFrom(GuildImpl.class)) {
					return event.getGuild();
				}else if(type.isAssignableFrom(TextChannelImpl.class)) {
					return event.getTextChannel();
				}else if(type.isAssignableFrom(MemberImpl.class)) {
					return event.getMember();
				}
			}else if(event.getChannelType().equals(ChannelType.PRIVATE)) {
				if(type.isAssignableFrom(PrivateChannelImpl.class)) {
					return event.getPrivateChannel();
				}
			}else if(event.getChannelType().equals(ChannelType.GROUP)) {
				if(type.isAssignableFrom(GroupImpl.class)) {
					return event.getEvent().getGroup();
				}
			}
			
			if(CommandImpl.contexts.containsKey(type)) {
				return CommandImpl.getContextFunction(type).apply(event, parameter);
			}
			
			throw new IllegalArgumentException("There is no context avaliable for that class");
		}else if(parameter.isAnnotationPresent(Option.class)) {
			Option optionAnnotation = parameter.getAnnotation(Option.class);
			
			return event.getOptionsPresent().stream().filter(option -> {
				if(option.equalsIgnoreCase(optionAnnotation.value())) {
					return true;
				}
				
				for(String alias : optionAnnotation.aliases()) {
					if(option.equalsIgnoreCase(alias)) {
						return true;
					}
				}
				
				return false;
			}).count() > 0;
		}
		
		return null;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static IArgument<?>[] generateArguments(Method command) {
		int contextCount = 0;
		for(Parameter parameter : command.getParameters()) {
			if(parameter.getType().isAssignableFrom(MessageReceivedEvent.class) || parameter.getType().isAssignableFrom(CommandEvent.class)) {
				contextCount++;
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				contextCount++;
			}
		}
		
		Arguments argumentsInfo = command.getAnnotation(Arguments.class);
		
		IArgument<?>[] arguments = new IArgument<?>[command.getParameterCount() - contextCount];
		for(int paramCount = 0, argCount = 0, methodArgCount = 0; paramCount < command.getParameterCount(); paramCount++) {
			Parameter parameter = command.getParameters()[paramCount];
			Class<?> type = parameter.getType();
			
			if(type.isAssignableFrom(MessageReceivedEvent.class) || type.isAssignableFrom(CommandEvent.class)) {
				continue;
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				continue;
			}
			
			boolean isOptional = false;
			if(type.isAssignableFrom(Optional.class)) {
				Type parameterType = command.getGenericParameterTypes()[paramCount];
				
				try {
					ParameterizedType parameterizedType = (ParameterizedType) parameterType;
					
					Type[] typeArguments = parameterizedType.getActualTypeArguments();		
					if(typeArguments.length > 0) {
						type = (Class<?>) typeArguments[0];
						isOptional = true;
					}
				}catch(Exception e) {}
			}
			
			IArgument.Builder<?, ?, ?> builder = ArgumentFactory.of(type);
			if(builder != null) {
				Argument info = null;
				if(parameter.isAnnotationPresent(Argument.class)) {
					info = parameter.getAnnotation(Argument.class);
				}else if(argumentsInfo != null) {
					if(argumentsInfo.value().length >= methodArgCount + 1) {
						info = argumentsInfo.value()[methodArgCount++];
					}
				}
				
				if(parameter.isNamePresent()) {
					builder.setName(parameter.getName());
				}
				
				if(info != null) {
					builder.setAcceptEmpty(info.acceptEmpty())
						.setAcceptQuote(info.acceptQuote());
					
					if(info.value().length() > 0) {
						builder.setName(info.value());
					}
				
					if(info.nullDefault()) {
						builder.setDefaultAsNull();
					}
					
					if(info.endless()) {
						if(arguments.length - 1 == argCount) {
							builder.setEndless(info.endless());
						}else{
							throw new IllegalArgumentException("Only the last argument may be endless");
						}
					}
				}
				
				if(isOptional) {
					builder.setDefaultAsNull();
				}
				
				arguments[argCount++] = builder.build();
			}else{
				if(type.isArray()) {
					builder = ArgumentFactory.of(type.getComponentType());
					if(builder != null) {
						Argument info = null;
						if(parameter.isAnnotationPresent(Argument.class)) {
							info = parameter.getAnnotation(Argument.class);
						}else if(argumentsInfo != null) {
							if(argumentsInfo.value().length >= methodArgCount + 1) {
								info = argumentsInfo.value()[methodArgCount++];
							}
						}
						
						if(parameter.isNamePresent()) {
							builder.setName(parameter.getName());
						}
						
						if(info != null) {
							builder.setAcceptEmpty(info.acceptEmpty())
								.setAcceptQuote(info.acceptQuote());
							
							if(info.value().length() > 0) {
								builder.setName(info.value());
							}
						
							if(info.nullDefault()) {
								builder.setDefaultAsNull();
							}
							
							if(info.endless()) {
								throw new IllegalArgumentException("Not a valid candidate, candidate may not be endless");
							}
						}
						
						EndlessArgumentImpl.Builder<?> endlessBuilder = new EndlessArgumentImpl.Builder(type.getComponentType()).setArgument(builder.build());
						if(parameter.isAnnotationPresent(Endless.class)) {
							Endless endless = parameter.getAnnotation(Endless.class);
							
							endlessBuilder.setMinArguments(endless.minArguments())
								.setMaxArguments(endless.maxArguments())
								.setEndless(endless.endless());
						}
						
						IArgument<?> argument = endlessBuilder.build();
						
						if(argument.isEndless()) {
							if(arguments.length - 1 != argCount) {
								throw new IllegalArgumentException("Only the last argument may be endless");
							}
						}
						
						arguments[argCount++] = argument;
						
						continue;
					}else{
						throw new IllegalArgumentException("There are no default arguments for " + type.getComponentType().toString());
					}
				}
				
				throw new IllegalArgumentException("There are no default arguments for " + type.toString());
			}
		}
		
		return arguments;
	}
	
	public static IOption[] generateOptions(Method command) {
		int optionCount = 0;
		for(Parameter parameter : command.getParameters()) {
			if(parameter.isAnnotationPresent(Option.class)) {
				optionCount++;
			}
		}
		
		if(optionCount > 0) {
			IOption[] options = new IOption[optionCount];
			
			for(int i = 0, i2 = 0; i < command.getParameterCount(); i++) {
				Parameter parameter = command.getParameters()[i];
				if(parameter.isAnnotationPresent(Option.class)) {
					Option option = parameter.getAnnotation(Option.class);
					
					options[i2++] = new OptionImpl.Builder()
						.setName(option.value())
						.setDescription(option.description())
						.setAliases(option.aliases())
						.setHidden(option.hidden())
						.setDeveloper(option.developer())
						.build();
				}
			}
			
			return options;
		}else{
			return new IOption[0];
		}
	}
	
	public static List<ICommand> generateDummyCommands(ICommand command) {
		List<ICommand> dummyCommands = new ArrayList<>();
		
		if(!(command instanceof DummyCommand)) {
			List<IArgument<?>> arguments = new ArrayList<>();
			if(command.getArguments().length > 0) {
				for(int i = 0; i < command.getArguments().length; i++) {
					IArgument<?> argument = command.getArguments()[i];
					if(argument.hasDefault()) {
						arguments.add(argument);
					}
				}
				
				if(arguments.size() > 0) {
					List<IArgument<?>> args = new ArrayList<>();
			    	for(int i = 1, max = 1 << arguments.size(); i < max; ++i) {
			    	    for(int j = 0, k = 1; j < arguments.size(); ++j, k <<= 1) {
			    	        if((k & i) != 0) {
			    	        	args.add(arguments.get(j));
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
	
	protected String command;
	
	protected String description, shortDescription;
	
	protected String argumentInfo;
	
	protected String[] examples = {};
	
	protected String[] aliases = {};
	
	public static final BiFunction<CommandImpl, MessageReceivedEvent, String[]> DEFAULT_ALIASES_FUNCTION = (command, event) -> command.getAliases();
	
	protected BiFunction<CommandImpl, MessageReceivedEvent, String[]> aliasesFunction = DEFAULT_ALIASES_FUNCTION;
	
	protected IArgument<?>[] arguments = {};
	protected IOption[] options = {};
	
	protected InvalidOptionPolicy invalidOptionPolicy = InvalidOptionPolicy.INCLUDE;
	
	protected ContentOverflowPolicy overflowPolicy = ContentOverflowPolicy.FAIL;
	
	protected ArgumentParsingType[] allowedArgumentParsingTypes = { ArgumentParsingType.POSITIONAL, ArgumentParsingType.NAMED };
	
	protected Permission[] botDiscordPermissions = {};
	protected Permission[] authorDiscordPermissions = {};
	
	protected boolean guildTriggerable = true;
	protected boolean privateTriggerable;
	
	protected boolean caseSensitive;
	
	protected boolean botTriggerable;
	
	protected boolean developerCommand;
	
	protected boolean hidden;
	protected boolean nsfw = false;
	
	protected boolean executeAsync;
	
	protected Function<CommandEvent, Object> asyncOrderingKey;
	
	protected long cooldownDuration = 0;
	protected Scope cooldownScope = Scope.USER;
	
	protected ICommand parent;
	
	protected ICategory category;
	
	protected List<ICommand> subCommands = new ArrayList<>();
	
	/* Not sure about this one, might implement it in a different way. It currently only exist for edge cases hence why it isn't well implemented */
	protected Map<String, Object> customProperties = new HashMap<>();
	
	protected List<TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean>> customVerifications = new ArrayList<>();
	
	protected List<ICommand> dummyCommands;
	
	protected boolean passive = false;
	
	protected List<Method> commandMethods = this.getCommandMethods();
	
	protected boolean defaultGenerated = false;
	
	public CommandImpl(String command, boolean generateDefault, IArgument<?>... arguments) {
		this.command = command;
		
		if(generateDefault) {
			if(arguments.length == 0 && this.commandMethods.size() == 1) {
				Method commandMethod = this.commandMethods.get(0);
				
				this.arguments = CommandImpl.generateArguments(commandMethod);
				this.options = CommandImpl.generateOptions(commandMethod);
			}else{
				if(this.commandMethods.size() > 1) {
					for(int i = 0; i < this.commandMethods.size(); i++) {
						this.addSubCommand(MethodCommandFactory.getDefaultFactory().create(this.commandMethods.get(i), this));
					}
					
					if(arguments.length > 0) {
						System.err.println("Default generated commands should not have any arguments defined in the constructor");
					}
				}else{
					this.arguments = arguments;
				}
			}
			
			for(Method method : this.getClass().getDeclaredMethods()) {
				if(!method.getName().equals("onCommand")) {
					if(method.isAnnotationPresent(Command.class)) {
						this.addSubCommand(MethodCommandFactory.getDefaultFactory().create(method.getName().replace("_", " "), method, this));
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
		
		this.dummyCommands = CommandImpl.generateDummyCommands(this);
	}
	
	public CommandImpl(String command, IArgument<?>... arguments) {
		this(command, true, arguments);
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public String getShortDescription() {
		return this.shortDescription;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getArgumentInfo() {
		if(this.argumentInfo == null || this.argumentInfo.length() == 0) {
			return ICommand.super.getArgumentInfo();
		}
		
		return this.argumentInfo;
	}
	
	public String[] getExamples() {
		return this.examples;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public IArgument<?>[] getArguments() {
		return this.arguments;
	}
	
	public IOption[] getOptions() {
		return this.options;
	}
	
	public InvalidOptionPolicy getInvalidOptionPolicy() {
		return this.invalidOptionPolicy;
	}
	
	public ContentOverflowPolicy getContentOverflowPolicy() {
		return this.overflowPolicy;
	}
	
	public ArgumentParsingType[] getAllowedArgumentParsingTypes() {
		return this.allowedArgumentParsingTypes;
	}
	
	public Permission[] getBotDiscordPermissions() {
		return this.botDiscordPermissions;
	}
	
	public Permission[] getAuthorDiscordPermissions() {
		return this.authorDiscordPermissions;
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
	
	public boolean isNSFW() {
		return this.nsfw;
	}
	
	public long getCooldownDuration() {
		return this.cooldownDuration;
	}
	
	public ICooldown.Scope getCooldownScope() {
		return this.cooldownScope;
	}
	
	public boolean isExecuteAsync() {
		return this.executeAsync;
	}
	
	public Object getAsyncOrderingKey(CommandEvent event) {
		if(this.asyncOrderingKey != null) {
			return this.asyncOrderingKey.apply(event);
		}
		
		return null;
	}
	
	public ICommand getParent() {
		return this.parent;
	}
	
	public ICategory getCategory() {
		return this.category;
	}
	
	public boolean isPassive() {
		if(this.passive) {
			return true;
		}
		
		try {
			if(this.getClass().getMethod("execute", CommandEvent.class, Object[].class).getDeclaringClass().equals(CommandImpl.class)) {
				if(this.defaultGenerated && (this.commandMethods.size() == 0 || this.commandMethods.size() > 1)) {
					return true;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public List<ICommand> getSubCommands() {
		return this.subCommands;
	}
	
	/** Custom properties for the command which can be used in {@link #addVerification(TriFunction)} */
	public Object getProperty(String key) {
		return this.customProperties.get(key);
	}
	
	public BiFunction<CommandImpl, MessageReceivedEvent, String[]> getAliasesFunction() {
		return this.aliasesFunction;
	}
	
	public CommandImpl setProperty(String key, Object value) {
		this.customProperties.put(key, value);
		
		return this;
	}
	
	public CommandImpl setDeveloper(boolean developerCommand) {
		this.developerCommand = developerCommand;
		
		return this;
	}
	
	public CommandImpl setBotTriggerable(boolean botTriggerable) {
		this.botTriggerable = botTriggerable;
		
		return this;
	}
	
	public CommandImpl setBotDiscordPermissions(Permission... permissions) {
		this.botDiscordPermissions = permissions;
		
		return this;
	}
	
	public CommandImpl setAuthorDiscordPermissions(Permission... permissions) {
		this.authorDiscordPermissions = permissions;
		
		return this;
	}
	
	public CommandImpl setDescription(String description) {
		this.description = description;
		
		return this;
	}
	
	public CommandImpl setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
		
		return this;
	}
	
	public CommandImpl setArgumentInfo(String argumentInfo) {
		this.argumentInfo = argumentInfo;
		
		return this;
	}
	
	public CommandImpl setExamples(String... examples) {
		this.examples = examples;
		
		return this;
	}
	
	public CommandImpl setAliases(String... aliases) {
		/* 
		 * From the longest alias to the shortest so that if the command for instance has two aliases one being "hello" 
		 * and the other being "hello there" it would recognize that the command is "hello there" instead of it thinking that
		 * "hello" is the command and "there" being the argument.
		 */
		Arrays.sort(aliases, (a, b) -> Integer.compare(b.length(), a.length()));
		
		this.aliases = aliases;
		
		return this;
	}
	
	public CommandImpl setArguments(IArgument<?>... arguments) {
		this.arguments = arguments;
		this.dummyCommands = CommandImpl.generateDummyCommands(this);
		
		return this;
	}
	
	public CommandImpl setOptions(IOption... options) {
		this.options = options;
		
		return this;
	}
	
	public CommandImpl setInvalidOptionPolicy(InvalidOptionPolicy optionPolicy) {
		this.invalidOptionPolicy = optionPolicy;
		
		return this;
	}
	
	public CommandImpl setContentOverflowPolicy(ContentOverflowPolicy overflowPolicy) {
		this.overflowPolicy = overflowPolicy;
		
		return this;
	}
	
	public CommandImpl setAllowedArgumentParsingTypes(ArgumentParsingType... argumentParsingTypes) {
		this.allowedArgumentParsingTypes = argumentParsingTypes;
		
		return this;
	}
	
	public CommandImpl setGuildTriggerable(boolean triggerable) {
		this.guildTriggerable = triggerable;
		
		return this;
	}
	
	public CommandImpl setPrivateTriggerable(boolean triggerable) {
		this.privateTriggerable = triggerable;
		
		return this;
	}
	
	public CommandImpl setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		
		return this;
	}
	
	public CommandImpl setHidden(boolean hidden) {
		this.hidden = hidden;
		
		return this;
	}
	
	public CommandImpl setNSFW(boolean nsfw) {
		this.nsfw = nsfw;
		
		return this;
	}
	
	/**
	 * See {@link #getCooldownDuration()}
	 * @param duration milliseconds
	 */
	public CommandImpl setCooldownDuration(long duration) {
		this.cooldownDuration = duration;
		
		return this;
	}
	
	/**
	 * See {@link #getCooldownDuration()}
	 */
	public CommandImpl setCooldownDuration(long duration, TimeUnit unit) {
		return this.setCooldownDuration(unit.toMillis(duration));
	}
	
	public CommandImpl setCooldownScope(Scope scope) {
		this.cooldownScope = scope;
		
		return this;
	}
	
	public CommandImpl setExecuteAsync(boolean executeAsync) {
		this.executeAsync = executeAsync;
		
		return this;
	}
	
	public CommandImpl setAsyncOrderingKey(Function<CommandEvent, Object> function) {
		this.asyncOrderingKey = function;
		
		return this;
	}
	
	public CommandImpl setAsyncOrderingKey(Object key) {
		return this.setAsyncOrderingKey(($) -> key);
	}
	
	public CommandImpl setParent(ICommand parent) {
		this.parent = parent;
		
		return this;
	}
	
	public CommandImpl setCategory(ICategory category) {
		ICategory old = this.category;
		
		this.category = category;
		
		if(old != null) {
			this.category.removeCommand(this);
		}
		
		if(this.category != null) {
			this.category.addCommand(this);
		}
		
		return this;
	}
	
	public CommandImpl setPassive(boolean passive) {
		this.passive = passive;
		
		return this;
	}
	
	public CommandImpl addSubCommand(ICommand command) {
		this.subCommands.add(command);
		
		if(command instanceof CommandImpl) {
			((CommandImpl) command).setParent(this);
		}
		
		return this;
	}
	
	/** Custom verification which will be used in {@link #verify(MessageReceivedEvent, CommandListener)} when checking for commands */
	public CommandImpl addVerification(TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean> verification) {
		this.customVerifications.add(verification);
		
		return this;
	}
	
	public CommandImpl addVerification(BiFunction<MessageReceivedEvent, CommandImpl, Boolean> verification) {
		this.customVerifications.add((event, listener, impl) -> verification.apply(event, impl));
		
		return this;
	}
	
	public CommandImpl addVerification(Function<MessageReceivedEvent, Boolean> verification) {
		this.customVerifications.add((event, listener, impl) -> verification.apply(event));
		
		return this;
	}
	
	public CommandImpl setAliases(BiFunction<CommandImpl, MessageReceivedEvent, String[]> function) {
		if(function != null) {
			this.aliasesFunction = function;
		}else{
			this.aliasesFunction = CommandImpl.DEFAULT_ALIASES_FUNCTION;
		}
		
		return this;
	}
	
	public CommandImpl setAliases(Function<MessageReceivedEvent, String[]> function) {
		if(function != null) {
			this.aliasesFunction = (command, event) -> function.apply(event);
		}else{
			this.aliasesFunction = CommandImpl.DEFAULT_ALIASES_FUNCTION;
		}
		
		return this;
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
	
	public List<Pair<String, ICommand>> getAllCommandsRecursiveWithTriggers(MessageReceivedEvent event, String prefix) {
		List<Pair<String, ICommand>> commands = new ArrayList<>();
		
		commands.add(Pair.of((prefix + " " + this.getCommand()).trim(), this));
		
		String[] aliases = this.aliasesFunction.apply(this, event);
		for(String alias : aliases) {
			commands.add(Pair.of((prefix + " " + alias).trim(), this));
		}
		
		for(ICommand command : this.getSubCommands()) {
			commands.addAll(command.getAllCommandsRecursiveWithTriggers(event, (prefix + " " + this.getCommand()).trim()));
			
			for(String alias : aliases) {
				commands.addAll(command.getAllCommandsRecursiveWithTriggers(event, (prefix + " " + alias).trim()));
			}
		}
		
		for(ICommand command : this.dummyCommands) {
			commands.add(Pair.of((prefix + " " + command.getCommand()).trim(), command));
			
			for(String alias : aliases) {
				commands.add(Pair.of((prefix + " " + alias).trim(), command));
			}
		}
		
		return commands;
	}
	
	public void execute(CommandEvent event, Object... args) throws Throwable {
		if(!this.passive && this.commandMethods.size() == 1) {
			MethodCommand.executeMethodCommand(this, this.commandMethods.get(0), event, args);
		}
	}
	
	public boolean verify(MessageReceivedEvent event, CommandListener commandListener) {
		if(event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
			return false;
		}
		
		if(!this.botTriggerable && event.getAuthor().isBot()) {
			return false;
		}
		
		if(!this.guildTriggerable && event.getChannelType().isGuild()) {
			return false;
		}
		
		if(!this.privateTriggerable && event.getChannelType().equals(ChannelType.PRIVATE)) {
			return false;
		}
		
		if(this.developerCommand && !commandListener.isDeveloper(event.getAuthor().getIdLong())) {
			return false;
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
	
	public String toString() {
		return (this.getCommand() + " " + this.getArgumentInfo()).trim();
	}
}