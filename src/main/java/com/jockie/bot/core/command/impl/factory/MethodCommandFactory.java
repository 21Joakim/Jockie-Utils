package com.jockie.bot.core.command.impl.factory;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.factory.IMethodCommandFactory;
import com.jockie.bot.core.command.impl.MethodCommand;

public class MethodCommandFactory {
	
	private MethodCommandFactory() {};
	
	public static class DefaultMethodCommandFactory implements IMethodCommandFactory<MethodCommand> {
		
		private DefaultMethodCommandFactory() {}
		
		public MethodCommand create(String name, Method method, Object invoker) {
			return MethodCommand.createFrom(name, method, invoker);
		}
		
		public MethodCommand create(Method method, Object invoker) {
			return MethodCommand.createFrom(method, invoker);
		}
	}
	
	public static final DefaultMethodCommandFactory DEFAULT = new DefaultMethodCommandFactory();
	
	private static IMethodCommandFactory<?> defaultCommandFactory = DEFAULT;
	
	public static void setDefaultFactory(IMethodCommandFactory<?> factory) {
		if(factory != null) {
			defaultCommandFactory = factory;
		}else{
			defaultCommandFactory = DEFAULT;
		}
	}
	
	public static IMethodCommandFactory<?> getDefaultFactory() {
		return defaultCommandFactory;
	}
}