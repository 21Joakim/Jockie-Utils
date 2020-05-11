package com.jockie.bot.core.command.factory.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.factory.IArgumentFactory;
import com.jockie.bot.core.argument.factory.impl.ArgumentFactory;
import com.jockie.bot.core.command.Context;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.option.impl.OptionImpl;

public class ComponentFactoryImpl implements IComponentFactory {
	
	/* TODO: Temporary until a custom parser implementation is added */
	private static Set<Class<?>> supportedOptionTypes = new HashSet<>();
	
	static {
		ComponentFactoryImpl.supportedOptionTypes.add(boolean.class);
		ComponentFactoryImpl.supportedOptionTypes.add(Boolean.class);
		ComponentFactoryImpl.supportedOptionTypes.add(String.class);
	}
	
	public IArgument<?>[] createArguments(Method commandMethod) {
		IContextManager contextManager = ContextManagerFactory.getDefault();
		IArgumentFactory argumentFactory = ArgumentFactory.getDefault();
		
		Parameter[] parameters = commandMethod.getParameters();
		
		List<Integer> contextIndexes = new ArrayList<>();
		for(int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			
			if(contextManager.isEnforcedContext(parameter.getParameterizedType())) {
				contextIndexes.add(i);
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				contextIndexes.add(i);
			}
		}
		
		IArgument<?>[] arguments = new IArgument<?>[parameters.length - contextIndexes.size()];
		for(int paramCount = 0, argCount = 0; paramCount < parameters.length; paramCount++) {
			/* Ignore if it is a context variable */
			if(contextIndexes.contains(paramCount)) {
				continue;
			}
			
			Parameter parameter = parameters[paramCount];
			IArgument<?> argument = argumentFactory.createArgument(parameter);
			
			if(arguments.length - 1 != argCount) {
				if(argument.isEndless()) {
					throw new IllegalArgumentException("Only the last argument may be endless");
				}
			}
			
			arguments[argCount++] = argument;
		}
		
		return arguments;
	}
	
	public IOption<?>[] createOptions(Method commandMethod) {
		Set<String> optionTriggers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		
		int optionCount = 0;
		
		Parameter[] parameters = commandMethod.getParameters();
		for(int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			
			if(!parameter.isAnnotationPresent(Option.class)) {
				continue;
			}
			
			optionCount++;
			
			Option option = parameter.getAnnotation(Option.class);
			if(!optionTriggers.add(option.value())) {
				throw new IllegalArgumentException("Option at parameter " + (i + 1) + " has a name, " + option.value() + ", which already exists in another option");
			}
			
			for(String alias : option.aliases()) {
				if(!optionTriggers.add(alias)) {
					throw new IllegalArgumentException("Option at parameter " + (i + 1) + " has an alias, " + alias + ", which already exists in another option");
				}
			}
		}
		
		if(optionCount == 0) {
			return new IOption[0];
		}
		
		IOption<?>[] options = new IOption[optionCount];
		
		for(int i = 0, i2 = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			Class<?> type = parameter.getType();
			
			if(!parameter.isAnnotationPresent(Option.class)) {
				continue;
			}
			
			if(!ComponentFactoryImpl.supportedOptionTypes.contains(type)) {
				throw new IllegalArgumentException("The type, " +  type.getName() + ", of the option at parameter " + (i + 1) + " is not supported");
			}
			
			Option option = parameter.getAnnotation(Option.class);
			
			options[i2++] = new OptionImpl.Builder<>(type)
				.setName(option.value())
				.setDescription(option.description())
				.setAliases(option.aliases())
				.setHidden(option.hidden())
				.setDeveloper(option.developer())
				.build();
		}
		
		return options;
	}
}