package com.jockie.bot.core.cooldown.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import com.jockie.bot.core.cooldown.ICooldown;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CooldownImpl implements ICooldown {
	
	private Scope scope;
	
	private String key;
	
	private Instant timeStarted;
	
	private long duration;
	private TimeUnit durationUnit;
	
	public CooldownImpl(MessageReceivedEvent event, Scope scope, long duration, TimeUnit unit) {
		this.scope = scope;
		this.duration = unit.toMillis(duration);
		this.durationUnit = TimeUnit.MILLISECONDS;
		
		this.key = this.scope.getContextKey(event);
		
		this.timeStarted = Clock.systemUTC().instant();
	}
	
	public Scope getScope() {
		return this.scope;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public Instant getTimeStarted() {
		return this.timeStarted;
	}
	
	public ZonedDateTime getTimeStarted(ZoneId zone) {
		return this.timeStarted.atZone(zone);
	}
	
	public long getDuration() {
		return this.duration;
	}
	
	public long getDuration(TimeUnit unit) {
		return this.durationUnit.convert(this.duration, unit);
	}
	
	public TimeUnit getDurationUnit() {
		return this.durationUnit;
	}
	
	public long getTimeRemainingMillis() {
		return (this.timeStarted.plusMillis(this.duration).toEpochMilli()) - Clock.systemUTC().millis();
	}
	
	public Duration getTimeRemaining() {
		return Duration.ofMillis(this.getTimeRemainingMillis());
	}
	
	public boolean hasExpired() {
		return this.getTimeRemainingMillis() <= 0;
	}
	
	public void updateDuration(long time) {
		this.duration += time;
	}
	
	public void updateDuration(long time, TimeUnit unit) {
		this.duration += unit.toMillis(time);
	}
	
	public void increase(long time) {
		this.updateDuration(time);
	}
	
	public void increase(long time, TimeUnit unit) {
		this.updateDuration(time, unit);
	}
	
	public void decrease(long time) {
		this.updateDuration(-time);
	}
	
	public void decrease(long time, TimeUnit unit) {
		this.updateDuration(-time, unit);
	}
	
	public void reset() {
		this.timeStarted = Clock.systemUTC().instant();
	}
	
	public void cancel() {
		this.timeStarted = this.timeStarted.plusMillis(this.duration);
	}
}