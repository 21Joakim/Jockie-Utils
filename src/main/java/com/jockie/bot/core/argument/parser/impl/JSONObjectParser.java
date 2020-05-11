package com.jockie.bot.core.argument.parser.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.parser.IArgumentParser;
import com.jockie.bot.core.argument.parser.ParsedArgument;
import com.jockie.bot.core.command.parser.ParseContext;

public class JSONObjectParser implements IArgumentParser<JSONObject> {
	
	public int getIndex(JSONTokener tokener) {
		String string = tokener.toString().substring(4);
		string = string.substring(0, string.indexOf(" "));
		
		return Integer.parseInt(string);
	}
	
	/* Code from org.json.JSONObject */
	public ParsedArgument<JSONObject> parse(ParseContext context, IArgument<JSONObject> argument, String value) {
		JSONTokener tokener = new JSONTokener(value);
		JSONObject object = new JSONObject();
		
		char character;
		String key;

		if(tokener.nextClean() != '{') {
			return new ParsedArgument<>(false, null);
		}
		
		for(;;) {
			character = tokener.nextClean();
			switch(character) {
				case 0: {
					return new ParsedArgument<>(false, null);
				}
				case '}': {
					return new ParsedArgument<>(true, object, value.substring(this.getIndex(tokener)));
				}
				default: {
					tokener.back();
					key = tokener.nextValue().toString();
				}
			}
			
			character = tokener.nextClean();
			if(character != ':') {
				return new ParsedArgument<>(false, null);
			}
			
			try {
				object.putOnce(key, tokener.nextValue());
			}catch(JSONException e) {
				return new ParsedArgument<>(false, null);
			}
			
			switch(tokener.nextClean()) {
				case ';': 
				case ',': {
					if(tokener.nextClean() == '}') {
						return new ParsedArgument<>(true, object, value.substring(this.getIndex(tokener)));
					}
					
					tokener.back();
					
					break;
				}
				case '}': {
					return new ParsedArgument<>(true, object, value.substring(this.getIndex(tokener)));
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