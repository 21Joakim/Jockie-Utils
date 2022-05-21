package com.jockie.bot.core.utility.function;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<A, B, C> {
	
	public void accept(A a, B b, C c);
	
	public default TriConsumer<A, B, C> andThen(TriConsumer<? super A, B, C> after) {
		Objects.requireNonNull(after);
		
		return (A a, B b, C c) -> {
			this.accept(a, b, c);
			after.accept(a, b, c);
		};
	}
}