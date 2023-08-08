package com.jockie.bot.core.parser.impl.discord;

import java.util.List;

import javax.annotation.Nonnull;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

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
	
	public List<Guild> getGuilds(ParseContext context, String content) {
		JDA jda = context.getMessage().getJDA();
		
		if(this.useShardManager) {
			ShardManager shardManager = jda.getShardManager();
			if(shardManager != null) {
				return ArgumentUtility.getGuildsByIdOrName(shardManager, content, true);
			}
		}
		
		return ArgumentUtility.getGuildsByIdOrName(jda, content, true);
	}
	
	@Override
	@Nonnull
	public ParsedResult<Guild> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String content) {
		List<Guild> guilds = this.getGuilds(context, content);
		if(guilds.size() == 1) {
			return ParsedResult.valid(guilds.get(0));
		}
		
		return ParsedResult.invalid();
	}
}