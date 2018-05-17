package com.jockie.bot.core.command.argument.impl;

import com.jockie.bot.core.command.argument.IArgument;
import com.jockie.bot.core.command.argument.IArgument.VerifiedArgument;
import com.jockie.bot.core.command.argument.IArgument.VerifiedArgument.VerifiedType;

public class ArgumentFactory {
	
	@SuppressWarnings("unchecked")
	public static <ReturnType> IArgument.Builder<ReturnType, ?, ?> of(Class<ReturnType> type) {
		IArgument.Builder<ReturnType, ?, ?> builder = null;
		
		if(type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				try {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) Byte.parseByte(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				try {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) Short.parseShort(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				try {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) Integer.parseInt(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				try {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) Long.parseLong(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				try {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) Float.parseFloat(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				try {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) Double.parseDouble(value));
				}catch(NumberFormatException e) {
					return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) Boolean.parseBoolean(value));
				}
				
				return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
			});
		}else if(type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				if(value.length() == 1) {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) (Object) value.charAt(0));
				}else{
					return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
				}
			});
		}else if(type.isAssignableFrom(String.class)) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				if(argument.isEndless()) {
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID_END_NOW, (ReturnType) value);
				}else{
					return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) value);
				}
			});
		}else if(type.isEnum()) {
			builder = new SimpleArgument.Builder<ReturnType>().setFunction((argument, value) -> {
				Enum<?>[] enums = (Enum[]) type.getEnumConstants();
				
				for(Enum<?> enumEntry : enums) {
					if(enumEntry.name().equalsIgnoreCase(value)) {
						return new VerifiedArgument<ReturnType>(VerifiedType.VALID, (ReturnType) enumEntry);
					}
				}
				
				return new VerifiedArgument<ReturnType>(VerifiedType.INVALID, null);
			});
		}
		
		return builder;
	}
}