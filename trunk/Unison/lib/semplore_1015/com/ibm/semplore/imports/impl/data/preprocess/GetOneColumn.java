package com.ibm.semplore.imports.impl.data.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GetOneColumn {
public void getOneColumn(String inname, String outname, int columnNum){ 
		try{
		BufferedReader reader = new BufferedReader(new FileReader(inname));
		PrintWriter writer = new PrintWriter(new FileWriter(outname));
		String temp,lasttemp = "";
		while ((temp = reader.readLine())!=null){
			temp = (temp.split("\t"))[columnNum-1];
			if (!temp.equals(lasttemp)) writer.println(temp);
			lasttemp = temp;
		}
		writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
