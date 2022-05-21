package com.jockie.bot.core.command.manager.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jockie.bot.core.command.manager.IContextManager;

/**
 * A factory to get to get the global ContextManager, 
 * the provided ContextManager is used in the creation and execution of commands.
 * <br><br>
 * <b>NOTE:</b> Changing the default {@link IContextManager} after commands have been created may have unintended consequences. 
 */

/* 
 * TODO: Figure out a way to move this to the CommandListener.
 * 
 * This can simply not be moved to the CommandListener due to the fact
 * that commands have no awareness of the CommandListener or CommandStore they are 
 * registered to and I find it hard to change that behaviour due to how 
 * the whole system is setup.
 * 
 * Making commands aware of the CommandListener and CommandStore they are registered to
 * would make it so that you need different instances depending on where they were registered
 * and because different CommandStores can be hot-swapped and switched between different CommandListeners
 * this might be a problem.
 */
public class ContextManagerFactory {
	
	private ContextManagerFactory() {};
	
	/**
	 * The default context manager, {@link ContextManagerImpl}
	 */
	public static final ContextManagerImpl DEFAULT = new ContextManagerImpl();
	
	private static IContextManager defaultContextManager = DEFAULT;
	
	/**
	 * Set the default context manager
	 * 
	 * @param manager the manager to set the default to, if null {@link #DEFAULT}
	 */
	public static void setDefault(@Nullable IContextManager manager) {
		ContextManagerFactory.defaultContextManager = Objects.requireNonNullElse(manager, DEFAULT);
	}
	
	/**
	 * @return the default context manager, if this has not been set
	 * it will be {@link #DEFAULT}
	 */
	@Nonnull
	public static IContextManager getDefault() {
		return ContextManagerFactory.defaultContextManager;
	}
}