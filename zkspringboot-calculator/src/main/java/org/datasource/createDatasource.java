package org.datasource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class createDatasource {

    public void createDataSource(File rootDataSource, StringBuilder sb, Integer interline) throws IOException{
        try {
                        
           
            String LS = System.getProperty("line.separator");
            FileWriter fw = new FileWriter(rootDataSource, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter writeFile = new PrintWriter(bw);
            
            writeFile.write(sb.toString());  
                      
            for (int i = 0; i < interline; i++) {
                writeFile.write(LS);
            }

            writeFile.close();
            
        } catch (Exception ex) {
        	System.out.println("||createDataSource ||"+"********** ERROR in  createDataSource{");
            StackTraceElement[] elementRaster = ex.getStackTrace();
            for (int i = 0; i < elementRaster.length; i++) {
                StackTraceElement elementSTD = elementRaster[i];
                System.out.println("||createDataSource ||"+"**********   " + i + "- getClassName= " + elementSTD.getClassName());
                System.out.println("||createDataSource ||"+"**********    getMethodName=" + elementSTD.getMethodName());
                System.out.println("||createDataSource ||"+"**********    getLineNumber=" + elementSTD.getLineNumber());
                System.out.println("||createDataSource ||"+"**********    errorMSG=" + ex.getMessage());
            }
            System.out.println("||createDataSource ||"+"**********}");
        }
    }
    
    
    
    
    public String padLeftZeros(String inputString, int length) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < length; i++) {
	    sb.append(" ");
	}
	 
	return sb.substring(inputString.length()) + inputString;
    }
    
    
    
    
    
}
