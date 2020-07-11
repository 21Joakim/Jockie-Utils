package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

public class GuildParser<Component> implements IParser<Guild, Component> {
	
	private boolean useShardManager;
	
	public GuildParser(boolean useShardManager) {
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
	 * @return the {@link GuildParser} instance, useful for chaining
	 */
	public GuildParser<Component> setUseShardManager(boolean useShardManager) {
		this.useShardManager = useShardManager;
		
		return this;
	}
	
	@Override
	public ParsedResult<Guild> parse(ParseContext context, Component component, String content) {
		JDA jda = context.getMessage().getJDA();
		
		List<Guild> guilds = null;
		if(this.useShardManager && jda.getAccountType().equals(AccountType.BOT)) {
			ShardManager shardManager = jda.getShardManager();
			if(shardManager != null) {
				guilds = ArgumentUtility.getGuildsByIdOrName(shardManager, content, true);
			}
		}
		
		if(guilds == null) {
			guilds = ArgumentUtility.getGuildsByIdOrName(jda, content, true);
		}
		
		if(guilds.size() == 1) {
			return new ParsedResult<>(true, guilds.get(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}	
}