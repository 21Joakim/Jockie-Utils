package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

public class UserParser<Component> implements IParser<User, Component> {
	
	private boolean useShardManager;
	
	public UserParser(boolean useShardManager) {
		this.useShardManager = useShardManager;
	}
	
	/**
	 * @return whether or not the shard-manager (if one is present)
	 * should be used when parsing
	 */
	public boolean isUseShardManager() {
		return this.useShardManager;
	}
	
	/**
	 * @param useShardManager whether or not the shard-manager (if one is present)
	 * should be used when parsing
	 * 
	 * @return the {@link UserParser} instance, useful for chaining
	 */
	public UserParser<Component> setUseShardManager(boolean useShardManager) {
		this.useShardManager = useShardManager;
		
		return this;
	}
	
	@Override
	public ParsedResult<User> parse(ParseContext context, Component component, String content) {
		JDA jda = context.getMessage().getJDA();
		
		List<User> users = null;
		if(this.useShardManager && jda.getAccountType().equals(AccountType.BOT)) {
			ShardManager shardManager = jda.getShardManager();
			if(shardManager != null) {
				users = ArgumentUtility.getUsersByIdOrName(shardManager, content, true);
			}
		}
		
		if(users == null) {
			users = ArgumentUtility.getUsersByIdOrName(jda, content, true);
		}
		
		if(users.size() == 1) {
			return new ParsedResult<>(true, users.get(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}	
}