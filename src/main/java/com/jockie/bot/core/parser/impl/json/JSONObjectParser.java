package com.jockie.bot.core.parser.impl.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class JSONObjectParser<Component> implements IParser<JSONObject, Component> {
	
	public int getIndex(JSONTokener tokener) {
		String string = tokener.toString().substring(4);
		string = string.substring(0, string.indexOf(" "));
		
		return Integer.parseInt(string);
	}
	
	/* Code from org.json.JSONObject */
	public ParsedResult<JSONObject> parse(ParseContext context, Component component, String value) {
		JSONTokener tokener = new JSONTokener(value);
		JSONObject object = new JSONObject();
		
		char character;
		String key;

		if(tokener.nextClean() != '{') {
			return new ParsedResult<>(false, null);
		}
		
		for(;;) {
			character = tokener.nextClean();
			switch(character) {
				case 0: {
					return new ParsedResult<>(false, null);
				}
				case '}': {
					return new ParsedResult<>(true, object, value.substring(this.getIndex(tokener)));
				}
				default: {
					tokener.back();
					key = tokener.nextValue().toString();
				}
			}
			
			character = tokener.nextClean();
			if(character != ':') {
				return new ParsedResult<>(false, null);
			}
			
			try {
				object.putOnce(key, tokener.nextValue());
			}catch(JSONException e) {
				return new ParsedResult<>(false, null);
			}
			
			switch(tokener.nextClean()) {
				case ';': 
				case ',': {
					if(tokener.nextClean() == '}') {
						return new ParsedResult<>(true, object, value.substring(this.getIndex(tokener)));
					}
					
					tokener.back();
					
					break;
				}
				case '}': {
					return new ParsedResult<>(true, object, value.substring(this.getIndex(tokener)));
				}
				default: {
					return new ParsedResult<>(false, null);
				}
			}
		}
	}
	
	public boolean isHandleAll() {
		return true;
	}
}