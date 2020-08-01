package com.jockie.bot.core.command.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;

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
import com.jockie.bot.core.command.CommandTrigger;
import com.jockie.bot.core.command.Context;
import com.jockie.bot.core.command.ICommand;
import com.jockie.bot.core.command.IMethodCommand;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.factory.impl.ComponentFactory;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.IReturnManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.utility.CommandUtility;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;

public class MethodCommandImpl extends AbstractCommand implements IMethodCommand {
	
	public static final Logger LOG = JDALogger.getLog(MethodCommandImpl.class);
	
	protected Method method;
	protected Object invoker;
	
	protected List<DummyCommand> dummyCommands = Collections.emptyList();
	
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
	
	@Override
	public Method getCommandMethod() {
		return this.method;
	}
	
	@Override
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
	
	public MethodCommandImpl setArguments(IArgument<?>... arguments) {
		super.setArguments(arguments);
		
		this.dummyCommands = MethodCommandImpl.generateDummyCommands(this);
		
		return this;
	}
	
	@Override
	public boolean isPassive() {
		if(this.passive) {
			return true;
		}
		
		if(this.method == null) {
			return true;
		}
		
		return false;
	}
	
	@Override
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
	
	@Override
	public List<CommandTrigger> getAllCommandsRecursiveWithTriggers(Message message, String prefix) {
		List<CommandTrigger> commands = super.getAllCommandsRecursiveWithTriggers(message, prefix);
		
		for(ICommand command : this.dummyCommands) {
			commands.add(new CommandTrigger((prefix + " " + command.getCommand()).trim(), command));
			
			for(String alias : this.aliases) {
				commands.add(new CommandTrigger((prefix + " " + alias).trim(), command));
			}
		}
		
		return commands;
	}
	
	@Override
	public void execute(CommandEvent event, Object... arguments) throws Throwable {
		if(!this.isPassive()) {
			MethodCommandImpl.executeMethodCommand(this, this.invoker, this.method, event, arguments);
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
		
		this.setBotTriggerable(annotation.botTriggerable());
		this.setCaseSensitive(annotation.caseSensitive());
		this.setGuildTriggerable(annotation.guildTriggerable());
		this.setPrivateTriggerable(annotation.privateTriggerable());
		
		this.setContentOverflowPolicy(annotation.contentOverflowPolicy());
		this.setUnknownOptionPolicy(annotation.unknownOptionPolicy());
		
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
			this.setUnknownOptionPolicy(policy.unknownOption());
		}
	}
	
	private static Set<Parameter> getContextParameters(IContextManager contextManager, Parameter[] parameters) {
		Set<Parameter> contextParameters = new HashSet<>();
		for(Parameter parameter : parameters) {
			if(contextManager.isEnforcedContext(parameter.getParameterizedType())) {
				contextParameters.add(parameter);
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				contextParameters.add(parameter);
			}
		}
		
		return contextParameters;
	}
	
	private static Object getContextArgument(CommandEvent event, IContextManager contextManager, List<IOption<?>> options, Parameter parameter) {
		Option annotation = parameter.getAnnotation(Option.class);
		if(annotation == null) {
			Object context = contextManager.getContext(event, parameter);
			if(context == null) {
				throw new IllegalStateException("There is no context available for " + parameter.getType());
			}
			
			return context;
		}
		
		IOption<?> option = options.stream()
			.filter(opt -> opt.getName().equals(annotation.value()))
			.findFirst()
			.orElse(null);
		
		if(option == null) {
			throw new IllegalStateException("The option, " + annotation.value() + ", specified in the annotation does not exist in the command");
		}
		
		Object value = event.getOption(annotation.value());
		
		DEFAULT:
		if(value == null) {
			if(option.hasDefault()) {
				value = option.getDefault(event);
			}
			
			if(value != null) {
				break DEFAULT;
			}
			
			return CommandUtility.getDefaultValue(option.getType());
		}
		
		return value;
	}
	
	private static void handleExecutionFailure(CommandEvent event, Object[] arguments, Method method,Throwable throwable) throws Throwable {
		if(throwable instanceof IllegalArgumentException) {
			StringBuilder information = new StringBuilder();
			information.append("Argument type mismatch for command \"" + event.getCommandTrigger() + "\"\n");
			
			information.append("	Arguments provided:\n");
			for(Object argument : arguments) {
				if(argument != null) {
					information.append("		" + argument.getClass().getName() + "\n");
				}else{
					information.append("		null\n");
				}
			}
			
			information.append("	Arguments expected:\n");
			for(Class<?> type : method.getParameterTypes()) {
				information.append("		" + type.getName() + "\n");
			}
			
			information.append("	Argument values: " + Arrays.deepToString(arguments));
			
			throw new IllegalStateException(information.toString());
		}
		
		if(throwable instanceof InvocationTargetException) {
			Throwable cause = throwable.getCause();
			if(cause == null) {
				return;
			}
			
			if(event.getCommandListener().isFilterStackTrace()) {
				List<StackTraceElement> elements = List.of(cause.getStackTrace());
				
				int index = -1;
				for(int i = 0; i < elements.size(); i++) {
					StackTraceElement element = elements.get(i);
					if(element.getClassName().equals(method.getDeclaringClass().getName()) && element.getMethodName().equals(method.getName())) {
						index = i;
					}
				}
				
				if(index != -1) {
					cause.setStackTrace(elements.subList(0, index + 1).toArray(new StackTraceElement[0]));
				}
			}
			
			throw cause;
		}
		
		throw throwable;
	}
	
	/**
	 * Execute a command from the provided method
	 * 
	 * @param command the command to execute
	 * @param invoker the command method's invoker, if commandMethod is static this should be null
	 * @param commandMethod the command method to invoke
	 * @param event the context to execute the command with
	 * @param args the arguments to execute the command with
	 * 
	 * @throws Throwable if the execution of the command fails
	 */
	public static void executeMethodCommand(@Nonnull ICommand command, @Nullable Object invoker, 
			@Nonnull Method commandMethod, @Nonnull CommandEvent event, @Nonnull Object... args) throws Throwable {
		
		Checks.notNull(command, "command");
		Checks.notNull(commandMethod, "commandMethod");
		Checks.notNull(event, "event");
		Checks.notNull(args, "args");
		
		IContextManager contextManager = ContextManagerFactory.getDefault();
		
		Parameter[] parameters = commandMethod.getParameters();
		Type[] genericTypes = commandMethod.getGenericParameterTypes();
		
		Set<Parameter> contextParameters = MethodCommandImpl.getContextParameters(contextManager, parameters);
		Object[] arguments = new Object[args.length + contextParameters.size()];
		
		List<IOption<?>> options = command.getOptions();
		
		for(int i = 0, i2 = 0; i < arguments.length; i++) {
			Parameter parameter = parameters[i];
			if(contextParameters.contains(parameter)) {
				arguments[i] = MethodCommandImpl.getContextArgument(event, contextManager, options, parameter);
				
				continue;
			}
			
			Object argument = args[i2++];
			
			/* TODO: Move this to some sort of implementation which will allow anyone to extend upon this idea */
			Class<?> type = parameter.getType();
			if(type.isAssignableFrom(Optional.class)) {
				Type parameterType = genericTypes[i];
				
				try {
					Type[] typeArguments = ((ParameterizedType) parameterType).getActualTypeArguments();
					if(typeArguments.length > 0) {
						arguments[i] = Optional.ofNullable(argument);
					}
				}catch(Throwable e) {
					LOG.error(e.getMessage(), e);
				}
			}
			
			if(arguments[i] == null) {
				arguments[i] = argument;
			}
		}
		
		try {
			if(!commandMethod.canAccess(invoker)) {
				commandMethod.setAccessible(true);
			}
			
			Object object = commandMethod.invoke(invoker, arguments);
			if(object != null) {
				IReturnManager returnManager = event.getCommandListener().getReturnManager();
				
				if(!returnManager.perform(event, object)) {
					LOG.warn(object.getClass() + " is an unsupported return type for a command method");
				}
			}
		}catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			MethodCommandImpl.handleExecutionFailure(event, arguments, commandMethod, e);
		}
	}
	
	/**
	 * Generate {@link DummyCommand DummyCommands} from the provided command,
	 * this uses the arguments of the provided command to check
	 * for optional arguments which are then used to create the dummy commands.
	 * 
	 * @param command the command to create {@link DummyCommand DummyCommands} from
	 * 
	 * @return the generated {@link DummyCommand DummyCommands}
	 */
	@Nonnull
	public static List<DummyCommand> generateDummyCommands(@Nonnull ICommand command) {
		List<DummyCommand> dummyCommands = new ArrayList<>();
		if(command instanceof DummyCommand) {
			return dummyCommands;
		}
		
		List<IArgument<?>> arguments = command.getArguments();
		if(arguments.isEmpty()) {
			return dummyCommands;
		}
		
		List<IArgument<?>> optionalArguments = new ArrayList<>();
		for(int i = 0; i < arguments.size(); i++) {
			IArgument<?> argument = arguments.get(i);
			if(argument.hasDefault()) {
				optionalArguments.add(argument);
			}
		}
		
		if(optionalArguments.isEmpty()) {
			return dummyCommands;
		}
		
		List<IArgument<?>> dummyArguments = new ArrayList<>();
		for(int i = 1, max = 1 << optionalArguments.size(); i < max; ++i) {
			for(int j = 0, k = 1; j < optionalArguments.size(); ++j, k <<= 1) {
				if((k & i) != 0) {
					dummyArguments.add(optionalArguments.get(j));
				}
			}
			
			dummyCommands.add(new DummyCommand(command, dummyArguments.toArray(new IArgument[0])));
			dummyArguments.clear();
		}
		
		return dummyCommands;
	}
}