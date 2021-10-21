package org.zkoss.zkspringboot.calculator;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datasource.createDatasource;
import org.datasource.readDatasource;
import org.entity.Expressions;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.wsdl.calculator.Calculator;
import org.wsdl.calculator.CalculatorLocator;
import org.wsdl.calculator.CalculatorSoap;
import org.wsdl.calculator.CalculatorSoapStub;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Selectbox;
import org.zkoss.zul.Textbox;




public class CalculatorController extends SelectorComposer<Component>{
	
	private String[] operations = { "+", "-", "*" };
	private List<Expressions> list_expressions= new ArrayList<Expressions>();
	private String pathtxt = "C:/WorkspaceSpring/data/datasource.txt";
	
	
	@Wire
	Textbox txt_result,txt_expresion,txt_result_expresion;
	
	@Wire
	Selectbox sel_operation;
	
	@Wire
	Listbox listbox_expresions;
	
	@Wire
	Intbox txt_number1,txt_number2;


    public void doAfterCompose(Component comp) throws Exception {
    	super.doAfterCompose(comp);
    	ListModelList model = new ListModelList(operations);
    	model.addToSelection(operations[0]);
    	sel_operation.setModel(model);
    	
    	
    	FillListExpressions();
		
    	
    }
    
    public void FillListExpressions() {
    	readDatasource service_read_data = new readDatasource();
		list_expressions=service_read_data.readDatasource(pathtxt);
		listbox_expresions.setModel(new ListModelList(list_expressions));
    }
	@Listen("onClick = #btn_calculate")
	public void btn_calculate() {
		try {
			if(validate_data()) {
				int number1=txt_number1.getValue() ;
				int number2=txt_number2.getValue();
				
				Calculator service = new CalculatorLocator();
				CalculatorSoap WsServices = new CalculatorSoapStub(new URL (service.getCalculatorSoapAddress()), service);
				int index=sel_operation.getSelectedIndex();
				if(index>=0) {
					String operacion=operations[sel_operation.getSelectedIndex()];	

//					Messagebox.show("Operación: "+number1+operacion+number2);
					int result=0;
					if(index==0) {
						result = WsServices.add(number1, number2);
						
					}
					if(index==1) {
						result = WsServices.subtract(number1, number2);
						
					}
					if(index==2) {
						result = WsServices.multiply(number1, number2);
						
					}
					txt_result.setValue(""+result);
					String Expresion = number1+operacion+number2+"="+result;		
					writeDatasource(pathtxt,Expresion);	
					FillListExpressions();
				}else {
					Messagebox.show("Choose an operation to perform");
				}
				
				
			}else {
				Messagebox.show("You must fill in the two fields to do the operation");
			}
		}catch(Exception e) {
			Messagebox.show("An error occurred!!!");
		}
	
		
		
	}

	
	@Listen("onClick = #btn_calculate_expresion")
	public void btn_calculate_expresion() {
		
		if(validate_data_expression()) {
			String expresion=txt_expresion.getText() ;
			try {
				int result= OperatorExpression(expresion);
				txt_result_expresion.setValue(""+result);
				String Expresion = expresion+"="+result;		
				writeDatasource(pathtxt,Expresion);	
				FillListExpressions();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Messagebox.show("The written expression could not be processed");
			}
		}else {
			Messagebox.show("You must write a correct operation to do the operation");
		}
	}
	
	
	
	
	//_____________________________________________________
	
	public boolean validate_data() {
		String number1=txt_number1.getText() ;
		String number2=txt_number2.getText();
		
		if(number1.length()==0) {
			return false;
		}
		if(number2.length()==0) {
			return false;
		}
		
		return true;
	}
	
	public boolean validate_data_expression() {
		String expresion=txt_expresion.getText() ;
		
		if(expresion.length()==0) {
			return false;
		}
		
		
		return true;
	}
	
	public String horaSistema() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String fecha=("dd/MM/yyyy HH:mm:ss-> "+dtf.format(LocalDateTime.now()));
        
        return fecha;
	}
	
	
	public void writeDatasource(String pathtxt,String  Expresion_) {
		try {
			 Date TimeExpresion = new Date();
			 String TimeExpresion_DateFormat = "hh:mm:ss.SSS"; 
		     SimpleDateFormat timeSDF = new SimpleDateFormat(TimeExpresion_DateFormat);

			 Expressions Expresion = new Expressions(Expresion_,timeSDF.format(TimeExpresion));
			 
		     createDatasource service_create_data = new createDatasource();
			 File data = new File (pathtxt);			
			 if(data.exists()) {							 
						 StringBuilder sb_ = new StringBuilder(300);
					     sb_ = new StringBuilder(300);
					     sb_.insert(0, Expresion.getExpresion() + ";"+ Expresion.getTime());
					     service_create_data.createDataSource(data,sb_,1);					 
				 }			 			
		
		} catch (Exception ex) {
			System.out.println("||writeDatasource ||"+"********** ERROR in  writeDatasource{");
            StackTraceElement[] elementRaster = ex.getStackTrace();
            for (int i = 0; i < elementRaster.length; i++) {
                StackTraceElement elementSTD = elementRaster[i];
                System.out.println("||writeDatasource ||"+"**********   " + i + "- getClassName= " + elementSTD.getClassName());
                System.out.println("||writeDatasource ||"+"**********    getMethodName=" + elementSTD.getMethodName());
                System.out.println("||writeDatasource ||"+"**********    getLineNumber=" + elementSTD.getLineNumber());
                System.out.println("||writeDatasource ||"+"**********    errorMSG=" + ex.getMessage());
            }
            System.out.println("||writeDatasource ||"+"**********}");
		}
	}
	
	
	
	public int OperatorExpression(final String str) throws RemoteException {
		try {
					
	    return new Object() {
	        int pos = -1, ch;

	        void nextChar() {
	            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
	        }

	        boolean eat(int charToEat) {
	            while (ch == ' ') nextChar();
	            if (ch == charToEat) {
	                nextChar();
	                return true;
	            }
	            return false;
	        }

	        int parse() throws RemoteException {
	            nextChar();
	            int x = parseExpression();
	            if (pos < str.length()) throw new RuntimeException("||OperatorExpression ||"+"********** Unexpected: " + (char)ch);
	            return x;
	        }

	        int parseExpression() throws RemoteException {
	        	int x = parseTerm();
	            for (;;) {
	                if      (eat('+')) x += parseTerm();// add
	                else if (eat('-')) x -= parseTerm(); // subtract
	               
	                else return x;
	            }
	        }

	        int parseTerm() throws RemoteException {
	        	int x = parseFactor();
	            for (;;) {
	                if      (eat('*')) x *= parseFactor(); // multiply
	                else return x;
	            }
	        }

	        int parseFactor() throws RemoteException {
	            if (eat('+')) return parseFactor(); 
	            if (eat('-')) return -parseFactor(); 

	            int x;
	            int startPos = this.pos;
	            if (eat('(')) { // parentheses
	                x = parseExpression();
	                eat(')');
	            } else if ((ch >= '0' && ch <= '9') || ch == '.') { 
	                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
	                x = Integer.parseInt(str.substring(startPos, this.pos));
	            } else {
	                throw new RuntimeException("||OperatorExpression ||"+"********** Unexpected: " + (char)ch);
	            }

	            

	            return x;
	        }
	    }.parse();
	    
		} catch (Exception ex) {
			System.out.println("||OperatorExpression ||"+"********** ERROR in  OperatorExpression{");
            StackTraceElement[] elementRaster = ex.getStackTrace();
            for (int i = 0; i < elementRaster.length; i++) {
                StackTraceElement elementSTD = elementRaster[i];
                System.out.println("||OperatorExpression ||"+"**********   " + i + "- getClassName= " + elementSTD.getClassName());
                System.out.println("||OperatorExpression ||"+"**********    getMethodName=" + elementSTD.getMethodName());
                System.out.println("||OperatorExpression ||"+"**********    getLineNumber=" + elementSTD.getLineNumber());
                System.out.println("||OperatorExpression ||"+"**********    errorMSG=" + ex.getMessage());
            }
            System.out.println("||OperatorExpression ||"+"**********}");
            return 0;
		}
	}
}
