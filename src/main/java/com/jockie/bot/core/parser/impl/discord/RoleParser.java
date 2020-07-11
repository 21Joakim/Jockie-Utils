package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.Role;

public class RoleParser<Component> implements IParser<Role, Component> {

	@Override
	public ParsedResult<Role> parse(ParseContext context, Component component, String content) {
		List<Role> roles = ArgumentUtility.getRolesByIdOrName(context.getMessage().getGuild(), content, true);
		
		if(roles.size() == 1) {
			return new ParsedResult<>(true, roles.get(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}	
}