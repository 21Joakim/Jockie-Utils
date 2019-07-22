package example.core.argument.regex;

import java.util.regex.Pattern;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.impl.ArgumentImpl;

public class RegexArgument extends ArgumentImpl<String> {
	
	public static class Builder extends IArgument.Builder<String, RegexArgument, Builder> {
		
		private Pattern pattern;
		
		public Builder() {
			super(String.class);
		}
		
		public Builder setPattern(Pattern pattern) {
			this.pattern = pattern;
			
			return this;
		}
		
		public Builder setPattern(String pattern) {
			this.pattern = Pattern.compile(pattern);
			
			return this;
		}
		
		public Pattern getPattern() {
			return this.pattern;
		}
		
		public Builder self() {
			return this;
		}
		
		public RegexArgument build() {
			return new RegexArgument(this);
		}
	}
	
	private final Pattern pattern;
	
	private RegexArgument(Builder builder) {
		super(builder);
		
		this.pattern = builder.getPattern();
	}
	
	public Pattern getPattern() {
		return this.pattern;
	}
}