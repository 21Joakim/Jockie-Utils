package example.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import example.CommandCategory;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Category {
	
	public CommandCategory value();
	
}