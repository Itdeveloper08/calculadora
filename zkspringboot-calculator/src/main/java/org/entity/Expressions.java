package org.entity;

import java.util.Date;

public class Expressions {
	
	private String expresion;
	private String  time;
	

	
	public Expressions(String expresion, String time) {
		this.expresion = expresion; 
		this.time = time; 
	}
	public String getExpresion() {
		return expresion;
	}
	public void setExpresion(String expresion) {
		this.expresion = expresion;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	

}
