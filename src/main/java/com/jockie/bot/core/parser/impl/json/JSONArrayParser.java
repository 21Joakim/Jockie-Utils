package com.jockie.bot.core.parser.impl.json;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import javax.annotation.Nonnull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class JSONArrayParser<Component> implements IParser<JSONArray, Component> {
	
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
	
	/* Code from org.json.JSONArray */
	@Nonnull
	public ParsedResult<JSONArray> parse(@Nonnull ParseContext context, @Nonnull Component component, @Nonnull String value) {
		JSONTokener tokener = new JSONTokener(value);
		JSONArray array = new JSONArray();
		
		if(tokener.nextClean() != '[') {
			return ParsedResult.invalid();
		}
		
		if(tokener.nextClean() == ']') {
			return ParsedResult.valid(array, value.substring(this.getIndex(tokener)));
		}
		
		tokener.back();
		
		for(;;) {
			if(tokener.nextClean() == ',') {
				tokener.back();
				
				array.put(JSONObject.NULL);
			}else{
				tokener.back();
				
				try {
					array.put(tokener.nextValue());
				}catch(JSONException e) {
					return ParsedResult.invalid();
				}
			}
			
			switch (tokener.nextClean()) {
				case ',': {
					if(tokener.nextClean() == ']') {
						return ParsedResult.valid(array, value.substring(this.getIndex(tokener)));
					}
					
					tokener.back();
					
					break;
				}
				case ']': {
					return ParsedResult.valid(array, value.substring(this.getIndex(tokener)));
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