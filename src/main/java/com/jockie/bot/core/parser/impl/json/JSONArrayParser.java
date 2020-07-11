package com.jockie.bot.core.parser.impl.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;

public class JSONArrayParser<Component> implements IParser<JSONArray, Component> {
	
	public int getIndex(JSONTokener tokener) {
		String string = tokener.toString().substring(4);
		string = string.substring(0, string.indexOf(" "));
		
		return Integer.parseInt(string);
	}
	
	/* Code from org.json.JSONArray */
	public ParsedResult<JSONArray> parse(ParseContext context, Component component, String value) {
		JSONTokener tokener = new JSONTokener(value);
		JSONArray array = new JSONArray();
		
		if(tokener.nextClean() != '[') {
			return new ParsedResult<>(false, null);
		}
		
		if(tokener.nextClean() == ']') {
			return new ParsedResult<>(true, array, value.substring(this.getIndex(tokener)));
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
					return new ParsedResult<>(false, null);
				}
			}
			
			switch (tokener.nextClean()) {
				case ',': {
					if(tokener.nextClean() == ']') {
						return new ParsedResult<>(true, array, value.substring(this.getIndex(tokener)));
					}
					
					tokener.back();
					
					break;
				}
				case ']': {
					return new ParsedResult<>(true, array, value.substring(this.getIndex(tokener)));
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