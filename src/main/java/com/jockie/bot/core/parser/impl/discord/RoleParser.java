package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.Role;

public class RoleParser<Component> implements IParser<Role, Component> {
	
	@Override
	@Nonnull
	public ParsedResult<Role> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		List<Role> roles = ArgumentUtility.getRolesByIdOrName(context.getMessage().getGuild(), content, true);
		if(roles.size() == 1) {
			return ParsedResult.valid(roles.get(0));
		}
		
		return ParsedResult.invalid();
	}
}