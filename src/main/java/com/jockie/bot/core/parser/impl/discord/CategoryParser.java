package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.Category;

public class CategoryParser<Component> implements IParser<Category, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Category> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		List<Category> categories = ArgumentUtility.getCategoriesByIdOrName(context.getMessage().getGuild(), content, true);
		if(categories.size() == 1) {
			return new ParsedResult<>(true, categories.get(0));
		}
		
		return new ParsedResult<>(false, null);
	}
}