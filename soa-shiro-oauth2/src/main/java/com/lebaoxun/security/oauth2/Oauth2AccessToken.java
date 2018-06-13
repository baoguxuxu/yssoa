package com.lebaoxun.security.oauth2;

public class Oauth2AccessToken {
	
	private static ThreadLocal<String> token = new ThreadLocal<String>();
	
	public static String getToken(){
		return token.get();
	}
	
	public static void setToken(String value){
		token.set(value);
	}
	
	public static void remove(){
		token.remove();
	}
}
