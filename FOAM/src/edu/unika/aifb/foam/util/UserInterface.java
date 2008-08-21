/*
 * Created on 08.02.2005
 *
 */
package edu.unika.aifb.foam.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author meh
 * A simple API to allow for different user interfaces. Normal output is the
 * console, but in a graphical environment the information strings can be
 * directly taken and processed however wished.
 */
public class UserInterface {
	
	public static String outputBuffer = "";
	public static String errorBuffer = "";
	public static String questionBuffer = "";
	public static String inputBuffer = "";
	
	public static boolean useOutput = true;
	
	public static void print(String string) {
		if (useOutput) {
		System.out.print(string);
		}
		outputBuffer = outputBuffer + string;
	}

	public static void errorPrint(String string) {
//		if (useOutput) {
//		System.out.print("\n"+string+"\n");
		errorBuffer = errorBuffer + string;
//		}
	}
	
	public static String read(String string) {
		String input = "";
		if (useOutput) {
		System.out.print(string);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		try {
			input = br.readLine();
		} catch (Exception e) {
		}
		questionBuffer = questionBuffer + string;
/*		while (inputBuffer.equals("")) {
			sleep(1000);
		}
		input = ""+inputBuffer;
		inputBuffer = "";*/
		}
		return input;
	}
	
	public static void clear() {
		outputBuffer = "";
		errorBuffer = "";
		questionBuffer = "";
		inputBuffer = "";
	}
	
}
