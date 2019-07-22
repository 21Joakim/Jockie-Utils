package example;

import com.jockie.bot.core.category.ICategory;
import com.jockie.bot.core.category.impl.CategoryImpl;

public enum CommandCategory {
	MATH(new CategoryImpl("Math", "The ultimate math commands just for you")),
	TESTING(new CategoryImpl("Testing", "Category filled with commands used for testing"))
	;
	
	private ICategory category;
	
	private CommandCategory(ICategory category) {
		this.category = category;
	}
	
	public ICategory getCategory() {
		return this.category;
	}
}