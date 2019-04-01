package com.jockie.bot.core.option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
/* TODO: Might be worth copying the documentation from IOption here */
public @interface Option {
	
	/**
	 * @see {@link IOption#getName()}
	 */
	public String value();
	
	/**
	 * @see {@link IOption#getDescription()}
	 */
	public String description() default "";
	
	/**
	 * @see {@link IOption#getAliases()}
	 */
	public String[] aliases() default {};
	
	/**
	 * @see {@link IOption#isHidden()}
	 */
	public boolean hidden() default false;
	
	/**
	 * @see {@link IOption#isDeveloper()}
	 */
	public boolean developer() default false;
	
}