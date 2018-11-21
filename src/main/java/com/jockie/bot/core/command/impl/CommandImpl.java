package com.jockie.bot.core.command.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.jockie.bot.core.Context;
import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.impl.ArgumentFactory;
import com.jockie.bot.core.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.command.Command;
import com.jockie.bot.core.command.ICommand;
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
	private static Map<Class, BiFunction> contextes = new HashMap<>();
	
	public static <T> void registerContext(Class<T> type, BiFunction<CommandEvent, Parameter, T> function) {
		CommandImpl.contextes.put(Objects.requireNonNull(type), Objects.requireNonNull(function));
	}
	
	public static void unregisterContext(Class<?> type) {
		CommandImpl.contextes.remove(type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> BiFunction<CommandEvent, Parameter, T> getContextFunction(Class<T> type) {
		return CommandImpl.contextes.get(type);
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<Class, BiFunction> beforeExecuteAnnotation = new HashMap<>();
	
	public static <T extends Annotation> void registerBeforeExecuteAnnotation(Class<T> type, BiFunction<CommandEvent, T, Object> function) {
		CommandImpl.beforeExecuteAnnotation.put(type, function);
	}
	
	public static void unregisterBeforeExecuteAnnotation(Class<? extends Annotation> type) {
		CommandImpl.beforeExecuteAnnotation.remove(type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> BiFunction<CommandEvent, T, Object> getBeforeExecuteFunction(Class<T> annotationType) {
		return CommandImpl.beforeExecuteAnnotation.get(annotationType);
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<Class, BiFunction> afterExecuteAnnotation = new HashMap<>();
	
	public static <T extends Annotation> void registerAfterExecuteAnnotation(Class<T> type, BiFunction<CommandEvent, T, Object> function) {
		CommandImpl.afterExecuteAnnotation.put(type, function);
	}
	
	public static void unregisterAfterExecuteAnnotation(Class<? extends Annotation> type) {
		CommandImpl.afterExecuteAnnotation.remove(type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> BiFunction<CommandEvent, T, Object> getAfterExecuteFunction(Class<T> annotationType) {
		return CommandImpl.afterExecuteAnnotation.get(annotationType);
	}
	
	public static Object getContextVariable(MessageReceivedEvent event, CommandEvent commandEvent, Object[] arguments, Parameter parameter) {
		if(parameter.getType().isAssignableFrom(MessageReceivedEvent.class)) {
			return event;
		}else if(parameter.getType().isAssignableFrom(CommandEvent.class)) {
			return commandEvent;
		}else if(parameter.isAnnotationPresent(Context.class)) {
			Class<?> command = commandEvent.getCommand().getClass();
			if(parameter.getType().isAssignableFrom(command)) {
				return commandEvent.getCommand();
			}
			
			if(parameter.getType().isAssignableFrom(CommandListener.class)) {
				return commandEvent.getCommandListener();
			}
			
			if(parameter.getType().isAssignableFrom(JDAImpl.class)) {
				return event.getJDA();
			}else if(parameter.getType().isAssignableFrom(UserImpl.class)) {
				return event.getAuthor();
			}else if(parameter.getType().isAssignableFrom(ChannelType.class)) {
				return event.getChannelType();
			}else if(parameter.getType().isAssignableFrom(MessageChannel.class)) {
				return event.getChannel();
			}else if(parameter.getType().isAssignableFrom(Message.class)) {
				return event.getMessage();
			}
			
			if(event.getChannelType().isGuild()) {
				if(parameter.getType().isAssignableFrom(GuildImpl.class)) {
					return event.getGuild();
				}else if(parameter.getType().isAssignableFrom(TextChannelImpl.class)) {
					return event.getTextChannel();
				}else if(parameter.getType().isAssignableFrom(MemberImpl.class)) {
					return event.getMember();
				}
			}else if(event.getChannelType().equals(ChannelType.PRIVATE)) {
				if(parameter.getType().isAssignableFrom(PrivateChannelImpl.class)) {
					return event.getPrivateChannel();
				}
			}else if(event.getChannelType().equals(ChannelType.GROUP)) {
				if(parameter.getType().isAssignableFrom(GroupImpl.class)) {
					return event.getGroup();
				}
			}
			
			if(CommandImpl.contextes.containsKey(parameter.getType())) {
				return CommandImpl.getContextFunction(parameter.getType()).apply(commandEvent, parameter);
			}
			
			throw new IllegalArgumentException("There is no context avaliable for that class");
		}else if(parameter.isAnnotationPresent(Option.class)) {
			Option option = parameter.getAnnotation(Option.class);
			
			return commandEvent.getOptionsPresent().stream().filter(opt -> {
				if(opt.equalsIgnoreCase(option.option())) {
					return true;
				}
				
				for(String str : option.aliases()) {
					if(opt.equalsIgnoreCase(str)) {
						return true;
					}
				}
				
				return false;
			}).count() > 0;
		}
		
		return null;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static IArgument<?>[] generateDefaultArguments(Method command) {
		int contextCount = 0;
		for(Parameter parameter : command.getParameters()) {
			if(parameter.getType().isAssignableFrom(MessageReceivedEvent.class) || parameter.getType().isAssignableFrom(CommandEvent.class)) {
				contextCount++;
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				contextCount++;
			}
		}
		
		IArgument<?>[] arguments = new IArgument<?>[command.getParameterCount() - contextCount];
		for(int i = 0, i2 = 0; i < command.getParameterCount(); i++) {
			Parameter parameter = command.getParameters()[i];
			if(parameter.getType().isAssignableFrom(MessageReceivedEvent.class) || parameter.getType().isAssignableFrom(CommandEvent.class)) {
				continue;
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				continue;
			}
			
			IArgument.Builder<?, ?, ?> builder = ArgumentFactory.of(parameter.getType());
			if(builder != null) {
				if(parameter.isAnnotationPresent(Argument.class)) {
					Argument info = parameter.getAnnotation(Argument.class);
					
					builder.setName(info.name())
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
								.setAcceptQuote(info.acceptQuote());
							
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
						.setName(option.option())
						.setDescription(option.description())
						.setAliases(option.aliases())
						.setHidden(option.hidden())
						.setDeveloperOption(option.developer())
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
	
	private String command;
	
	private String description, shortDescription;
	
	private String[] examples = {};
	
	private String[] aliases = {};
	
	public static final BiFunction<CommandImpl, MessageReceivedEvent, String[]> DEFAULT_ALIASES_FUNCTION = (command, event) -> command.getAliases();
	
	private BiFunction<CommandImpl, MessageReceivedEvent, String[]> aliasesFunction = DEFAULT_ALIASES_FUNCTION;
	
	private List<Function<CommandEvent, Object>> beforeExecute = new ArrayList<>();
	private List<Function<CommandEvent, Object>> afterExecute = new ArrayList<>();
	
	private IArgument<?>[] arguments = {};
	private IOption[] options = {};
	
	private InvalidOptionPolicy optionPolicy = InvalidOptionPolicy.INCLUDE;
	
	private ContentOverflowPolicy overflowPolicy = ContentOverflowPolicy.FAIL;
	
	private Permission[] botDiscordPermissionsNeeded = {};
	private Permission[] authorDiscordPermissionsNeeded = {};
	
	private boolean guildTriggerable = true;
	private boolean privateTriggerable;
	
	private boolean caseSensitive;
	
	private boolean botTriggerable;
	
	private boolean developerCommand;
	
	private boolean hidden;
	
	private boolean executeAsync;
	
	private long cooldownDuration = 0;
	private Scope cooldownScope = Scope.USER;
	
	private ICommand parent;
	
	private List<ICommand> subCommands = new ArrayList<>();
	
	/* Not sure about this one, might implement it in a different way. It currently only exist for edge cases hence why it isn't well implemented */
	private Map<String, Object> customProperties = new HashMap<>();
	
	private List<TriFunction<MessageReceivedEvent, CommandListener, CommandImpl, Boolean>> customVerifications = new ArrayList<>();
	
	private List<ICommand> dummyCommands;
	
	private boolean passive = false;
	
	private List<Method> commandMethods = this.getCommandMethods();
	
	private boolean defaultGenerated = false;
	
	@SuppressWarnings("unchecked")
	public CommandImpl(String command, boolean generateDefault, IArgument<?>... arguments) {
		this.command = command;
		
		if(generateDefault) {
			if(arguments.length == 0 && this.commandMethods.size() == 1) {
				Method commandMethod = this.commandMethods.get(0);
				
				this.arguments = CommandImpl.generateDefaultArguments(commandMethod);
				this.options = CommandImpl.generateOptions(commandMethod);
				
				for(Annotation annotation : commandMethod.getAnnotations()) {
					BiFunction<CommandEvent, Annotation, Object> function = CommandImpl.beforeExecuteAnnotation.get(annotation.getClass());
					if(function != null) {
						this.registerBeforeExecute(commandEvent -> {
							return function.apply(commandEvent, annotation);
						});
					}
				}
				
				for(Annotation annotation : commandMethod.getAnnotations()) {
					BiFunction<CommandEvent, Annotation, Object> function = CommandImpl.afterExecuteAnnotation.get(annotation.getClass());
					if(function != null) {
						this.registerAfterExecute(commandEvent -> {
							return function.apply(commandEvent, annotation);
						});
					}
				}
			}else{
				if(this.commandMethods.size() > 1) {
					for(int i = 0; i < this.commandMethods.size(); i++) {
						this.addSubCommand(MethodCommand.createFrom(this.commandMethods.get(i).getName().replace("_", " "), this, this.commandMethods.get(i)));
					}
					
					if(arguments.length > 0) {
						/* Won't make any difference */
					}
				}else{
					this.arguments = arguments;
				}
			}
			
			for(Method method : this.getClass().getDeclaredMethods()) {
				if(!method.getName().equals("onCommand")) {
					if(method.isAnnotationPresent(Command.class)) {
						this.addSubCommand(MethodCommand.createFrom(method.getName().replace("_", " "), this, method));
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
	
	public String[] getExamples() {
		return this.examples;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public List<Function<CommandEvent, Object>> getBeforeExecuteFunctions() {
		return this.beforeExecute;
	}
	
	public List<Function<CommandEvent, Object>> getAfterExecuteFunctions() {
		return this.afterExecute;
	}
	
	public IArgument<?>[] getArguments() {
		return this.arguments;
	}
	
	public IOption[] getOptions() {
		return this.options;
	}
	
	public InvalidOptionPolicy getInvalidOptionPolicy() {
		return this.optionPolicy;
	}
	
	public ContentOverflowPolicy getContentOverflowPolicy() {
		return this.overflowPolicy;
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
	
	public long getCooldownDuration() {
		return this.cooldownDuration;
	}
	
	public Scope getCooldownScope() {
		return this.cooldownScope;
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
	
	public BiFunction<CommandImpl, MessageReceivedEvent, String[]> getAliasesFunction() {
		return this.aliasesFunction;
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
	
	protected CommandImpl setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
		
		return this;
	}
	
	protected CommandImpl setExamples(String... examples) {
		this.examples = examples;
		
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
		this.dummyCommands = CommandImpl.generateDummyCommands(this);
		
		return this;
	}
	
	protected CommandImpl setOptions(IOption... options) {
		this.options = options;
		
		return this;
	}
	
	protected CommandImpl setOptionPolicy(InvalidOptionPolicy optionPolicy) {
		this.optionPolicy = optionPolicy;
		
		return this;
	}
	
	protected CommandImpl setContentOverflowPolicy(ContentOverflowPolicy overflowPolicy) {
		this.overflowPolicy = overflowPolicy;
		
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
	
	protected CommandImpl setCooldownScope(Scope scope) {
		this.cooldownScope = scope;
		
		return this;
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
	
	protected CommandImpl registerBeforeExecute(Function<CommandEvent, Object> beforeExecute) {
		this.beforeExecute.add(beforeExecute);
		
		return this;
	}
	
	protected CommandImpl registerAfterExecute(Function<CommandEvent, Object> afterExecute) {
		this.afterExecute.add(afterExecute);
		
		return this;
	}
	
	protected CommandImpl setAliases(BiFunction<CommandImpl, MessageReceivedEvent, String[]> function) {
		if(function != null) {
			this.aliasesFunction = function;
		}else{
			this.aliasesFunction = CommandImpl.DEFAULT_ALIASES_FUNCTION;
		}
		
		return this;
	}
	
	protected CommandImpl setAliases(Function<MessageReceivedEvent, String[]> function) {
		if(function != null) {
			this.aliasesFunction = (command, event) -> function.apply(event);
		}else{
			this.aliasesFunction = CommandImpl.DEFAULT_ALIASES_FUNCTION;
		}
		
		return this;
	}
	
	public List<Pair<String, ICommand>> getAllCommandsRecursive(MessageReceivedEvent event, String prefix) {
		List<Pair<String, ICommand>> commands = new ArrayList<>();
		
		commands.add(Pair.of((prefix + " " + this.getCommand()).trim(), this));
		
		String[] aliases = this.aliasesFunction.apply(this, event);
		for(String alias : aliases) {
			commands.add(Pair.of((prefix + " " + alias).trim(), this));
		}
		
		for(ICommand command : this.getSubCommands()) {
			commands.addAll(command.getAllCommandsRecursive(event, (prefix + " " + this.getCommand()).trim()));
			
			for(String alias : aliases) {
				commands.addAll(command.getAllCommandsRecursive(event, (prefix + " " + alias).trim()));
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
	
	public void execute(MessageReceivedEvent event, CommandEvent commandEvent, Object... args) throws Throwable {
		if(!this.passive && this.commandMethods.size() == 1) {
			MethodCommand.executeMethodCommand(this, this.commandMethods.get(0), event, commandEvent, args);
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
		
		if(this.developerCommand && !commandListener.isDeveloper(event.getAuthor().getIdLong())) {
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
	
	public String toString() {
		return this.getCommand() + " " + this.getArgumentInfo();
	}
}