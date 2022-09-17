package com.jockie.bot.core.utility;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

public class ArgumentUtility {
	
	private ArgumentUtility() {}
	
	public static final Pattern USER_NAME_PATTERN = Pattern.compile("(.{2,32})#([0-9]{4})");
	
	@Nullable
	private static String getGroup(@Nonnull Pattern pattern, int group, @Nonnull String value) {
		Matcher matcher = pattern.matcher(value);
		if(matcher.find()) {
			return matcher.group(group);
		}
		
		return null;
	}
	
	private static boolean isSnowflake(@Nonnull String value) {
		return value.length() <= 20 && Helpers.isNumeric(value);
	}
	
	/**
	 * Get a role by id or mention
	 * 
	 * @param guild the guild to search for the role in
	 * @param value the mention or id of the role
	 * 
	 * @return the found role, may be null
	 */
	@Nullable
	public static Role getRoleById(@Nonnull Guild guild, @Nonnull String value) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.ROLE.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return guild.getRoleById(id);
		}
		
		return null;
	}
	
	/**
	 * Get a member by id or mention
	 * 
	 * @param guild the guild to search for the member in
	 * @param value the mention or id of the member
	 * 
	 * @return the found member, may be null
	 */
	@Nullable
	public static Member getMemberById(@Nonnull Guild guild, @Nonnull String value) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return guild.getMemberById(id);
		}
		
		return null;
	}
	
	/**
	 * Get a text channel by id or mention
	 * 
	 * @param guild the guild to search for the text channel in
	 * @param value the mention or id of the text channel
	 * 
	 * @return the found text channel, may be null
	 */
	@Nullable
	public static TextChannel getTextChannelById(@Nonnull Guild guild, @Nonnull String value) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.CHANNEL.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return guild.getTextChannelById(id);
		}
		
		return null;
	}
	
	/**
	 * Get a voice channel by id or mention
	 * 
	 * @param guild the guild to search for the voice channel in
	 * @param value the mention or id of the text channel
	 * 
	 * @return the found text channel, may be null
	 */
	@Nullable
	public static VoiceChannel getVoiceChannelById(@Nonnull Guild guild, @Nonnull String value) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.CHANNEL.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return guild.getVoiceChannelById(id);
		}
		
		return null;
	}
	
	/**
	 * Get an emoji by id or mention
	 * 
	 * @param guild the guild to search for the emoji in
	 * @param value the mention or id of the emoji
	 * 
	 * @return the found emoji, may be null
	 */
	@Nullable
	public static CustomEmoji getEmojiById(@Nonnull Guild guild, @Nonnull String value, boolean rich) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		if(ArgumentUtility.isSnowflake(value)) {
			/* TODO: Should this check the entire JDA/ShardManager instance? */
			return guild.getEmojiById(Long.parseLong(value));
		}
		
		Matcher matcher = MentionType.EMOJI.getPattern().matcher(value);
		if(matcher.matches()) {
			long snowflake = Long.parseLong(matcher.group(2));
			
			RichCustomEmoji emoji = guild.getEmojiById(snowflake);
			if(emoji == null && !rich) {
				return Emoji.fromCustom(matcher.group(1), snowflake, value.startsWith("<a:"));
			}
			
			return emoji;
		}
		
		return null;
	}
	
	/**
	 * Get an emoji by id or mention
	 * 
	 * @param guild the guild to search for the emoji in
	 * @param value the mention or id of the emoji
	 * 
	 * @return the found emoji, may be null
	 */
	@Nullable
	public static CustomEmoji getEmojiById(@Nonnull Guild guild, @Nonnull String value) {
		return ArgumentUtility.getEmojiById(guild, value, false);
	}
	
	/**
	 * Get a rich emoji by id or mention
	 * 
	 * @param guild the guild to search for the emoji in
	 * @param value the mention or id of the rich emoji
	 * 
	 * @return the found rich emoji, may be null
	 */
	@Nullable
	public static RichCustomEmoji getRichEmojiById(@Nonnull Guild guild, @Nonnull String value) {
		return (RichCustomEmoji) ArgumentUtility.getEmojiById(guild, value, true);
	}
	
	/**
	 * Get an user by id or mention
	 * 
	 * @param jda the JDA instance to search for the user in
	 * @param value the mention or id of the user
	 * 
	 * @return the found user, may be null
	 */
	@Nullable
	public static User getUserById(@Nonnull JDA jda, @Nonnull String value) {
		Checks.notNull(jda, "jda");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return jda.getUserById(id);
		}
		
		return null;
	}
	
	/**
	 * Get an user by id or mention
	 * 
	 * @param shardManager the shard manager instance to search for the user in
	 * @param value the mention or id of the user
	 * 
	 * @return the found user, may be null
	 */
	@Nullable
	public static User getUserById(@Nonnull ShardManager shardManager, @Nonnull String value) {
		Checks.notNull(shardManager, "shardManager");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return shardManager.getUserById(id);
		}
		
		return null;
	}
	
	/**
	 * Retrieve an user by id or mention
	 * 
	 * @param jda the JDA instance to make the request from
	 * @param value the mention or id of the user
	 * 
	 * @return RestAction - Type: User<br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	@Nonnull
	public static RestAction<User> retrieveUserById(@Nonnull JDA jda, @Nonnull String value) {
		Checks.notNull(jda, "jda");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return jda.retrieveUserById(id);
		}
		
		return new CompletedRestAction<User>(jda, (User) null);
	}
	
	/**
	 * Retrieve an user by id or mention
	 * 
	 * @param shardManager the shard manager instance to search for the user in
	 * @param value the mention or id of the user
	 * 
	 * @return RestAction - Type: User<br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	@Nonnull
	public static RestAction<User> retrieveUserById(@Nonnull ShardManager shardManager, @Nonnull String value) {
		Checks.notNull(shardManager, "shardManager");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			return shardManager.retrieveUserById(id);
		}
		
		return new CompletedRestAction<User>(shardManager.getShardCache().iterator().next(), (User) null);
	}
	
	/**
	 * Retrieve an user by id or mention
	 * 
	 * @param shardManager the shard manager instance to search for the user in
	 * @param jda the JDA instance to make the request from if it does not exist in
	 * the shard manager
	 * @param value the mention or id of the user
	 * 
	 * @return RestAction - Type: User<br>
	 * On request, gets the User with id matching provided id from Discord.
	 */
	@Nonnull
	public static RestAction<User> retrieveUserById(@Nonnull ShardManager shardManager, @Nonnull JDA jda, @Nonnull String value) {
		Checks.notNull(shardManager, "shardManager");
		Checks.notNull(jda, "jda");
		Checks.notNull(value, "value");
		
		String id = ArgumentUtility.getGroup(MentionType.USER.getPattern(), 1, value);
		if(id != null || ArgumentUtility.isSnowflake(id = value)) {
			long snowflake = Long.parseLong(id);
			
			User user = shardManager.getUserById(snowflake);
			if(user != null) {
				return new CompletedRestAction<User>(user.getJDA(), user);
			}
			
			return jda.retrieveUserById(snowflake);
		}
		
		return new CompletedRestAction<User>(jda, (User) null);
	}
	
	/**
	 * Get members by id, mention, effective name or tag
	 * If a member was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the members in
	 * @param value the mention, id or tag of the member to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found members
	 */
	@Nonnull
	public static List<Member> getMembersByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		{
			Member member = ArgumentUtility.getMemberById(guild, value);
			if(member != null) {
				return List.of(member);
			}
		}
		
		Matcher matcher = USER_NAME_PATTERN.matcher(value);
		if(matcher.matches()) {
			String name = matcher.group(1);
			String descriminator = matcher.group(2);
			
			Member member = guild.getMemberCache().stream()
				.filter((a) -> a.getUser().getDiscriminator().equals(descriminator))
				.filter((a) -> a.getUser().getName().equals(name))
				.findFirst()
				.orElse(null);
			
			if(member != null) {
				return List.of(member);
			}
		}
		
		return guild.getMembersByEffectiveName(value, ignoreCase);
	}
	
	/**
	 * Get roles by id, mention or name
	 * If a role was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the roles in
	 * @param value the mention, id or name of the role to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found roles
	 */
	@Nonnull
	public static List<Role> getRolesByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		Role role = ArgumentUtility.getRoleById(guild, value);
		if(role != null) {
			return List.of(role);
		}
		
		return guild.getRolesByName(value, ignoreCase);
	}
	
	/**
	 * Get emojis by id, mention or name.
	 * If an emoji was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the emojis in
	 * @param value the mention, id or name of the emoji to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * @param rich whether the emoji needs to be rich or not (this effectively means it needs to be in the guild)
	 * 
	 * @return the found emojis
	 */
	@Nonnull
	public static List<? extends CustomEmoji> getEmojisByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase, boolean rich) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		CustomEmoji emoji = ArgumentUtility.getEmojiById(guild, value, rich);
		if(emoji != null) {
			return List.of(emoji);
		}
		
		return guild.getEmojisByName(value, ignoreCase);
	}
	
	/**
	 * Get emojis by id, mention or name.
	 * If an emoji was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the emojis in
	 * @param value the mention, id or name of the emoji to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found emojis
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public static List<CustomEmoji> getEmojisByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase) {
		return (List<CustomEmoji>) ArgumentUtility.getEmojisByIdOrName(guild, value, ignoreCase, false);
	}
	
	/**
	 * Get rich emojis by id, mention or name.
	 * If an emoji was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the emojis in
	 * @param value the mention, id or name of the emoji to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found emojis
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public static List<RichCustomEmoji> getRichEmojisByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase) {
		return (List<RichCustomEmoji>) ArgumentUtility.getEmojisByIdOrName(guild, value, ignoreCase, true);
	}
	
	/**
	 * Get text channels by id, mention or name
	 * If a text channel was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the text channels in
	 * @param value the mention, id or name of the text channel to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found text channels
	 */
	@Nonnull
	public static List<TextChannel> getTextChannelsByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		TextChannel channel = ArgumentUtility.getTextChannelById(guild, value);
		if(channel != null) {
			return List.of(channel);
		}
		
		return guild.getTextChannelsByName(value, ignoreCase);
	}
	
	/**
	 * Get voice channels by id or name
	 * If a voice channel was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the voice channels in
	 * @param value the id or name of the voice channel to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found voice channels
	 */
	@Nonnull
	public static List<VoiceChannel> getVoiceChannelsByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		VoiceChannel channel = ArgumentUtility.getVoiceChannelById(guild, value);
		if(channel != null) {
			return List.of(channel);
		}
		
		return guild.getVoiceChannelsByName(value, ignoreCase);
	}
	
	/**
	 * Get categories by id or name.
	 * If a category was found by id it will not check for names
	 * 
	 * @param guild the guild to search for the categories in
	 * @param value the id or name of the category to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found categories
	 */
	@Nonnull
	public static List<Category> getCategoriesByIdOrName(@Nonnull Guild guild, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(guild, "guild");
		Checks.notNull(value, "value");
		
		if(ArgumentUtility.isSnowflake(value)) {
			Category category = guild.getCategoryById(value);
			if(category != null) {
				return List.of(category);
			}
		}
		
		return guild.getCategoriesByName(value, ignoreCase);
	}
	
	/**
	 * Get users by id, mention, name or tag.
	 * If a user was found by id it will not check for names
	 * 
	 * @param jda the shard manager instance to search for the users in
	 * @param value the id or name of the user to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found user
	 */
	@Nonnull
	public static List<User> getUsersByIdOrName(@Nonnull JDA jda, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(jda, "jda");
		Checks.notNull(value, "value");
		
		{
			User user = ArgumentUtility.getUserById(jda, value);
			if(user != null) {
				return List.of(user);
			}
		}
		
		Matcher matcher = USER_NAME_PATTERN.matcher(value);
		if(matcher.matches()) {
			String name = matcher.group(1);
			String descriminator = matcher.group(2);
			
			User user = jda.getUserCache().stream()
				.filter((a) -> a.getDiscriminator().equals(descriminator))
				.filter((a) -> a.getName().equals(name))
				.findFirst()
				.orElse(null);
			
			if(user != null) {
				return List.of(user);
			}
		}
		
		return jda.getUserCache().getElementsByName(value, ignoreCase);
	}
	
	/**
	 * Get users by id, mention, name or tag. 
	 * If a user is found by id it will not check for names
	 * 
	 * @param shardManager the shard manager instance to search for the users in
	 * @param value the id or name of the user to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found users
	 */
	@Nonnull
	public static List<User> getUsersByIdOrName(@Nonnull ShardManager shardManager, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(shardManager, "shardManager");
		Checks.notNull(value, "value");
		
		{
			User user = ArgumentUtility.getUserById(shardManager, value);
			if(user != null) {
				return List.of(user);
			}
		}
		
		Matcher matcher = USER_NAME_PATTERN.matcher(value);
		if(matcher.matches()) {
			String name = matcher.group(1);
			String descriminator = matcher.group(2);
			
			User user = shardManager.getUserCache().stream()
				.filter((a) -> a.getDiscriminator().equals(descriminator))
				.filter((a) -> a.getName().equals(name))
				.findFirst()
				.orElse(null);
			
			if(user != null) {
				return List.of(user);
			}
		}
		
		return shardManager.getUserCache().getElementsByName(value, ignoreCase);
	}
	
	/**
	 * Get guilds by id or name. 
	 * If a guild is found by id it will not check for names
	 * 
	 * @param jda the JDA instance to search for the guilds in
	 * @param value the id or name of the guild to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found guilds
	 */
	@Nonnull
	public static List<Guild> getGuildsByIdOrName(@Nonnull JDA jda, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(jda, "jda");
		Checks.notNull(value, "value");
		
		if(ArgumentUtility.isSnowflake(value)) {
			Guild guild = jda.getGuildById(value);
			if(guild != null) {
				return List.of(guild);
			}
		}
		
		return jda.getGuildsByName(value, ignoreCase);
	}
	
	/**
	 * Get guilds by id or name.
	 * If a guild is found by id it will not check for names
	 * 
	 * @param shardManager the shard manager instance to search for the guilds in
	 * @param value the id or name of the guild to search for
	 * @param ignoreCase whether or not the name should be case sensitive
	 * 
	 * @return the found guilds
	 */
	@Nonnull
	public static List<Guild> getGuildsByIdOrName(@Nonnull ShardManager shardManager, @Nonnull String value, boolean ignoreCase) {
		Checks.notNull(shardManager, "shardManager");
		Checks.notNull(value, "value");
		
		if(ArgumentUtility.isSnowflake(value)) {
			Guild guild = shardManager.getGuildById(value);
			if(guild != null) {
				return List.of(guild);
			}
		}
		
		return shardManager.getGuildsByName(value, ignoreCase);
	}
}