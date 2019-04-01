package test.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TestRuns.class)
public @interface TestRun {
	
	public String argument() default "";
	
	public String result() default "";
	
	public boolean pass();
	
}