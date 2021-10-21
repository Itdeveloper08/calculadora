package org.datasource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.entity.*;

public class readDatasource {
	
	public List<Expressions> readDatasource(String pathDataSource) {
		boolean flag = false;
		List<Expressions> list_expressions= new ArrayList<Expressions>();
		try {
			File file = new File(pathDataSource);
			String aline = "";
			
			if(file.exists()) {
				flag= true;
				BufferedReader bfreader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
				
				 while (bfreader.ready()) {
			            aline = bfreader.readLine();
			            try {
			            	  String datos []= aline.split(";");
			            	  Expressions expressions;
			            	  if(datos.length>0) {
			            		  expressions= new Expressions(datos[0],datos[1]);
			            		  list_expressions.add(expressions);
			            	  }
					          
			            	  
					            
					          System.out.println(aline);
			            }catch(Exception e) {
			            	
			            }
			          
				 }
				 
				 bfreader.close();
			}
			
			return list_expressions;
		} catch (Exception ex) {
			flag = false;
			// TODO: handle exception
			System.out.println("||readDatasource ||"+"********** ERROR in  readDatasource{");
            StackTraceElement[] elementRaster = ex.getStackTrace();
            for (int i = 0; i < elementRaster.length; i++) {
                StackTraceElement elementSTD = elementRaster[i];
                System.out.println("||readDatasource ||"+"**********   " + i + "- getClassName= " + elementSTD.getClassName());
                System.out.println("||readDatasource ||"+"**********    getMethodName=" + elementSTD.getMethodName());
                System.out.println("||readDatasource ||"+"**********    getLineNumber=" + elementSTD.getLineNumber());
                System.out.println("||readDatasource ||"+"**********    errorMSG=" + ex.getMessage());
            }
            System.out.println("||readDatasource ||"+"**********}");
            return list_expressions;
		}
		
	}
	

}
