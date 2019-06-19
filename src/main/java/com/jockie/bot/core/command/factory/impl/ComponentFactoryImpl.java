package com.jockie.bot.core.command.factory.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.argument.Arguments;
import com.jockie.bot.core.argument.Endless;
import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.impl.ArgumentFactory;
import com.jockie.bot.core.argument.impl.EndlessArgumentImpl;
import com.jockie.bot.core.command.Context;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.option.impl.OptionImpl;

public class ComponentFactoryImpl implements IComponentFactory {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IArgument<?>[] createArguments(Method commandMethod) {
		IContextManager manager = ContextManagerFactory.getDefault();
		
		Parameter[] parameters = commandMethod.getParameters();
		Type[] genericTypes = commandMethod.getGenericParameterTypes();
		
		List<Integer> contextIndexes = new ArrayList<>();
		for(int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			
			if(manager.isEnforcedContext(parameter.getParameterizedType())) {
				contextIndexes.add(i);
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				contextIndexes.add(i);
			}
		}
		
		Arguments argumentsInfo = commandMethod.getAnnotation(Arguments.class);
		
		IArgument<?>[] arguments = new IArgument<?>[parameters.length - contextIndexes.size()];
		for(int paramCount = 0, argCount = 0, methodArgCount = 0; paramCount < parameters.length; paramCount++) {
			/* Ignore if it is a context variable */
			if(contextIndexes.contains(paramCount)) {
				continue;
			}
			
			Parameter parameter = parameters[paramCount];
			Class<?> type = parameter.getType();
			
			boolean isOptional = false;
			if(type.isAssignableFrom(Optional.class)) {
				Type parameterType = genericTypes[paramCount];
				
				try {
					ParameterizedType parameterizedType = (ParameterizedType) parameterType;
					
					Type[] typeArguments = parameterizedType.getActualTypeArguments();		
					if(typeArguments.length > 0) {
						type = (Class<?>) typeArguments[0];
						isOptional = true;
					}
				}catch(Exception e) {}
			}
			
			IArgument.Builder<?, ?, ?> builder = ArgumentFactory.builder(type);
			if(builder != null) {
				Argument info = parameter.getAnnotation(Argument.class);
				if(info == null && argumentsInfo != null) {
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
					builder = ArgumentFactory.builder(type.getComponentType());
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
	
	public IOption[] createOptions(Method commandMethod) {
		int optionCount = 0;
		for(Parameter parameter : commandMethod.getParameters()) {
			if(parameter.isAnnotationPresent(Option.class)) {
				optionCount++;
			}
		}
		
		if(optionCount > 0) {
			IOption[] options = new IOption[optionCount];
			
			for(int i = 0, i2 = 0; i < commandMethod.getParameterCount(); i++) {
				Parameter parameter = commandMethod.getParameters()[i];
				
				if(parameter.isAnnotationPresent(Option.class)) {
					if(!parameter.getType().equals(boolean.class) && !parameter.getType().equals(Boolean.class)) {
						throw new IllegalArgumentException("Option at parameter " + (i + 1) + " is not a boolean, options with values are currently not supported");
					}
					
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
}