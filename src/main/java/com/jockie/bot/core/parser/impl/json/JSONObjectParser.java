package com.jockie.bot.core.parser.impl.json;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import javax.annotation.Nonnull;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class JSONObjectParser<Component> implements IParser<JSONObject, Component> {
	
	protected static final VarHandle JSON_TOKENER_INDEX_FIELD;
	
	static {
		try {
			JSON_TOKENER_INDEX_FIELD = MethodHandles.privateLookupIn(JSONTokener.class, MethodHandles.lookup())
				.findVarHandle(JSONTokener.class, "index", long.class);
		}catch(NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getIndex(JSONTokener tokener) {
		return (int) (long) JSONArrayParser.JSON_TOKENER_INDEX_FIELD.get(tokener);
	}
	
	/* Code from org.json.JSONObject */
	@Nonnull
	public ParsedResult<JSONObject> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String value) {
		JSONTokener tokener = new JSONTokener(value);
		JSONObject object = new JSONObject();
		
		char character;
		String key;

		if(tokener.nextClean() != '{') {
			return ParsedResult.invalid();
		}
		
		for(;;) {
			character = tokener.nextClean();
			switch(character) {
				case 0: {
					return ParsedResult.invalid();
				}
				case '}': {
					return ParsedResult.valid(object, value.substring(this.getIndex(tokener)));
				}
				default: {
					tokener.back();
					key = tokener.nextValue().toString();
				}
			}
			
			character = tokener.nextClean();
			if(character != ':') {
				return ParsedResult.invalid();
			}
			
			try {
				object.putOnce(key, tokener.nextValue());
			}catch(JSONException e) {
				return ParsedResult.invalid();
			}
			
			switch(tokener.nextClean()) {
				case ';': 
				case ',': {
					if(tokener.nextClean() == '}') {
						return ParsedResult.valid(object, value.substring(this.getIndex(tokener)));
					}
					
					tokener.back();
					
					break;
				}
				case '}': {
					return ParsedResult.valid(object, value.substring(this.getIndex(tokener)));
				}
				default: {
					return ParsedResult.invalid();
				}
			}
		}
	}
	
	public boolean isHandleAll() {
		return true;
	}
}