package com.jockie.bot.core.argument.parser.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.command.parser.ParseContext;

public class JSONArrayParser implements IArgumentParser<JSONArray> {
	
	public int getIndex(JSONTokener tokener) {
		String string = tokener.toString().substring(4);
		string = string.substring(0, string.indexOf(" "));
		
		return Integer.parseInt(string);
	}
	
	/* Code from org.json.JSONArray */
	public ParsedArgument<JSONArray> parse(ParseContext context, IArgument<JSONArray> argument, String value) {
		JSONTokener tokener = new JSONTokener(value);
		JSONArray array = new JSONArray();
		
		if(tokener.nextClean() != '[') {
			return new ParsedArgument<>(false, null);
		}
		
		if(tokener.nextClean() == ']') {
			return new ParsedArgument<>(true, array, value.substring(this.getIndex(tokener)));
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
					return new ParsedArgument<>(false, null);
				}
			}
			
			switch (tokener.nextClean()) {
				case ',': {
					if(tokener.nextClean() == ']') {
						return new ParsedArgument<>(true, array, value.substring(this.getIndex(tokener)));
					}
					
					tokener.back();
					
					break;
				}
				case ']': {
					return new ParsedArgument<>(true, array, value.substring(this.getIndex(tokener)));
				}
				default: {
					return new ParsedArgument<>(false, null);
				}
			}
		}
	}
	
	public boolean isHandleAll() {
		return true;
	}
}