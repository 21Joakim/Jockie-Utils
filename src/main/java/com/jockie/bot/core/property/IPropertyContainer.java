package com.jockie.bot.core.property;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPropertyContainer {
	
	/**
	 * Get a custom property
	 * 
	 * @param name the property name
	 * 
	 * @return the property value or null if it does not exist
	 */
	@Nullable
	public default <T> T getProperty(@Nonnull String name) {
		return this.getProperty(name, (T) null);
	}
	
	/**
	 * Get a custom property
	 * 
	 * @param name the property name
	 * @param type the type of the value
	 * 
	 * @return the property value or null if it does not exist
	 */
	@Nullable
	public default <T> T getProperty(@Nonnull String name, @Nonnull Class<T> type) {
		return this.getProperty(name, (T) null);
	}
	
	/**
	 * Get a custom property
	 * 
	 * @param name the property name
	 * @param defaultValue the default value if the property does not exist
	 * 
	 * @return the property value or the provided default value if it does not exist
	 */
	@Nullable
	public <T> T getProperty(@Nonnull String name, @Nullable T defaultValue);
	
	/**
	 * Get all of the custom properties
	 * 
	 * @return an unmodifiable map of all the custom propreties
	 */
	@Nonnull
	public Map<String, Object> getProperties();
	
}