package com.flatironschool.javacs;

/* USAGE:
 * java -classpath . Interface
 */
public class Interface
{
	public static void main(String[] args){
		System.out.println("Hello!");
		ArgParser parser = new ArgParser("java -classpath . Interface");
	}
}	
