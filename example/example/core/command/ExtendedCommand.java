package example.core.command;

import java.lang.reflect.Method;

import com.jockie.bot.core.command.impl.CommandImpl;

import example.CommandCategory;
import example.core.annotation.Category;

public class ExtendedCommand extends CommandImpl {
	
	public ExtendedCommand(String name) {
		super(name, true);
		
		this.applyExtendedAnnotations();
	}
	
	public ExtendedCommand(String name, Method method, Object invoker) {
		super(name, method, invoker);
		
		this.applyExtendedAnnotations();
	}
	
	public ExtendedCommand setCategory(CommandCategory category) {
		super.setCategory(category.getCategory());
		
		return this;
	}
	
	private final void applyExtendedAnnotations() {
		if(this.method == null) {
			return;
		}
		
		if(this.method.isAnnotationPresent(Category.class)) {
			Category categoryAnnotation = this.method.getAnnotation(Category.class);
			this.setCategory(categoryAnnotation.value());
		}
	}
}