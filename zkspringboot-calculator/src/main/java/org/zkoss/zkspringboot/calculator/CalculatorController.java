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
import java.util.Stack;
import java.util.StringTokenizer;

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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Selectbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Button;

import ch.qos.logback.core.net.server.Client;

public class CalculatorController extends SelectorComposer<Component> {

	private String[] operations = { "+", "-", "*" };
	private List<Expressions> list_expressions = new ArrayList<Expressions>();
	private String pathtxt = "C:/WorkspaceSpring/data/datasource.txt";
	private static Stack<Character> queue = new Stack<Character>();

	Calculator service = null;
	CalculatorSoap WsServices = null;
	
	@Wire 
	Button btn_calculate_expresion;
	
	@Wire
	Textbox txt_result, txt_expresion, txt_result_expresion;

	@Wire
	Selectbox sel_operation;

	@Wire
	Listbox listbox_expresions;

	@Wire
	Intbox txt_number1, txt_number2;

	/*
	 * init Compose Front
	 * 
	 */
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		ListModelList model = new ListModelList(operations);
		model.addToSelection(operations[0]);
		sel_operation.setModel(model);

		FillListExpressions();

		service = new CalculatorLocator();
		WsServices = new CalculatorSoapStub(new URL(service.getCalculatorSoapAddress()), service);

	}

	/*
	 * fill the list expresion read data source
	 * 
	 */
	public void FillListExpressions() {
		readDatasource service_read_data = new readDatasource();
		list_expressions = service_read_data.readDatasource(pathtxt);
		listbox_expresions.setModel(new ListModelList(list_expressions));
	}

	/*
	 * calculate two number operation
	 * 
	 */
	@Listen("onClick = #btn_calculate")
	public void btn_calculate() {
		try {
			if (validate_data()) {
				int number1 = txt_number1.getValue();
				int number2 = txt_number2.getValue();

				int index = sel_operation.getSelectedIndex();
				if (index >= 0) {
					String operacion = operations[sel_operation.getSelectedIndex()];

//					Messagebox.show("Operación: "+number1+operacion+number2);
					int result = 0;
					if (index == 0) {
						result = WsServices.add(number1, number2);

					}
					if (index == 1) {
						result = WsServices.subtract(number1, number2);

					}
					if (index == 2) {
						result = WsServices.multiply(number1, number2);

					}
					txt_result.setValue("" + result);
					String Expresion = number1 + operacion + number2 + "=" + result;
					writeDatasource(pathtxt, Expresion);
					FillListExpressions();
				} else {
					Messagebox.show("Choose an operation to perform");
				}

			} else {
				Messagebox.show("You must fill in the two fields to do the operation");
			}
		} catch (Exception e) {
			Clients.log("An error occurred!!!");
		}

	}

	/*
	 * calculate expresion
	 * 
	 */
	@Listen("onClick = #btn_calculate_expresion")
	public void btn_calculate_expresion() {

		if (validate_data_expression()) {
			String expresion = txt_expresion.getText();
			try {
				boolean flag_parenthese = validateParentheses(expresion);
				boolean flag_startoperator = StartWithOperator(expresion);
				boolean flag_endoperator = EndWithOperator(expresion);
				boolean flag_alternationexpresion = evaluateAlternationsExpresion(expresion);

//			        Clients.log("flag_parenthese; "+flag_parenthese +"\n" +
//			        		"flag_startoperator; "+flag_startoperator +"\n" +
//			        		"flag_endoperator; "+flag_endoperator +"\n" +
//			        		"flag_alternationexpresion; "+flag_alternationexpresion +"\n" 
//			        		);

				
				int result = 0;
				if (flag_parenthese && !flag_startoperator && !flag_endoperator && flag_alternationexpresion) {
					String posfix = toPosfix(expresion);
					if (posfix != null) {
						result = evaluatePostfix(posfix);
						txt_result_expresion.setValue("" + result);
						String Expresion = expresion + "=" + result;
						writeDatasource(pathtxt, Expresion);
					}
				}

				FillListExpressions();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Clients.log("The written expression could not be processed");
			}
		} else {
//			Messagebox.show("You must write a correct operation to do the operation");
			Notification.show("You must write a correct operation!!", "error",
					txt_expresion  , "after_start", 5000, false);
		}
	}

	/*
	 * validate data
	 * 
	 */
	public boolean validate_data() {
		String number1 = txt_number1.getText();
		String number2 = txt_number2.getText();

		if (number1.length() == 0) {
			return false;
		}
		if (number2.length() == 0) {
			return false;
		}

		return true;
	}

	/*
	 * validate data expresion
	 * 
	 */
	public boolean validate_data_expression() {
		String expresion = txt_expresion.getText();

		if (expresion.length() == 0) {
			return false;
		}

		return true;
	}

	/*
	 * date System calculate
	 * 
	 */

	public String hoursystem() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String date = ("dd/MM/yyyy HH:mm:ss-> " + dtf.format(LocalDateTime.now()));

		return date;
	}

	/*
	 * write datasource in file
	 * 
	 */
	public void writeDatasource(String pathtxt, String Expresion_) {
		try {
			Date TimeExpresion = new Date();
			String TimeExpresion_DateFormat = "hh:mm:ss.SSS";
			SimpleDateFormat timeSDF = new SimpleDateFormat(TimeExpresion_DateFormat);

			Expressions Expresion = new Expressions(Expresion_, timeSDF.format(TimeExpresion));

			createDatasource service_create_data = new createDatasource();
			File data = new File(pathtxt);
			if (data.exists()) {
				StringBuilder sb_ = new StringBuilder(300);
				sb_ = new StringBuilder(300);
				sb_.insert(0, Expresion.getExpresion() + ";" + Expresion.getTime());
				service_create_data.createDataSource(data, sb_, 1);
			}

		} catch (Exception ex) {
			Clients.log("||writeDatasource ||" + "********** ERROR in  writeDatasource{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log(
						"||writeDatasource ||" + "**********   " + i + "- getClassName= " + elementSTD.getClassName());
				Clients.log("||writeDatasource ||" + "**********    getMethodName=" + elementSTD.getMethodName());
				Clients.log("||writeDatasource ||" + "**********    getLineNumber=" + elementSTD.getLineNumber());
				Clients.log("||writeDatasource ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||writeDatasource ||" + "**********}");
		}
	}

	/*
	 * validate expresion parentheses
	 * 
	 */

	public boolean validateParentheses(String operation) {
		try {
			Stack<Character> queue = new Stack<Character>();

			char[] equation = operation.toCharArray();
			for (int c = 0; c < equation.length; c++) {
				char character = equation[c];
				if (character == '(') {
					queue.push(character);
				} else if (character == ')') {
					if (queue.empty()) {
						String out = generateError(operation, c);
						out += "Validate number parentheses of the expression";
//						Messagebox.show(out);
						Notification.show(out, "error",
								txt_expresion  , "after_start", 5000, false);
						return false;
					} else {
						queue.pop();
					}
				}
			}
			if (!queue.empty()) {
				String out = generateError(operation, equation.length - 1);
				out += "Validate expression parentheses";
//				Messagebox.show(out);
				Notification.show(out, "error",
						txt_expresion  , "after_start", 5000, false);
				return false;

			}
			return true;

		} catch (Exception ex) {

			Clients.log("||validateParentheses ||" + "********** ERROR in  validateParentheses{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log("||validateParentheses ||" + "**********   " + i + "- getClassName= "
						+ elementSTD.getClassName());
				Clients.log("||validateParentheses ||" + "**********    getMethodName=" + elementSTD.getMethodName());
				Clients.log("||validateParentheses ||" + "**********    getLineNumber=" + elementSTD.getLineNumber());
				Clients.log("||validateParentheses ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||validateParentheses ||" + "**********}");
			return true;
		}
	}

	/*
	 * validate expresion start operator
	 * 
	 */

	public boolean StartWithOperator(String infix) {
		try {
			char[] chain = infix.toCharArray();
			for (int c = 0; c < chain.length; c++) {
				char character = chain[c];
				if (Character.isDigit(character)) {
					return false;
				}
				if (Character.isLetter(character)) {
					return false;
				} else if (character == '+' || character == '-' || character == '*' || character == '/'
						|| character == '^') {
					String out = generateError(infix, c);
					out += "The expression can't start with operator";
//					Messagebox.show(out);
					Notification.show(out, "error",
							txt_expresion  , "after_start", 5000, false);
					return true;
				}
			}
			return false;

		} catch (Exception ex) {

			Clients.log("||StartWithOperator ||" + "********** ERROR in  StartWithOperator{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log("||StartWithOperator ||" + "**********   " + i + "- getClassName= "
						+ elementSTD.getClassName());
				Clients.log("||StartWithOperator ||" + "**********    getMethodName=" + elementSTD.getMethodName());
				Clients.log("||StartWithOperator ||" + "**********    getLineNumber=" + elementSTD.getLineNumber());
				Clients.log("||StartWithOperator ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||StartWithOperator ||" + "**********}");
			return false;
		}
	}

	/*
	 * validate expresion end operator
	 * 
	 */
	public boolean EndWithOperator(String infix) {
		try {
			char[] chain = infix.toCharArray();
			for (int c = chain.length - 1; c > 0; c--) {
				char character = chain[c];
				if (Character.isDigit(character)) {
					return false;
				}
				if (Character.isLetter(character)) {
					return false;
				} else if (character == '+' || character == '-' || character == '*' || character == '/'
						|| character == '^') {
					String out = generateError(infix, c);
					out += "The expression can't end with operatedr";
//					Messagebox.show(out);
					Notification.show(out, "error",
							txt_expresion  , "after_start", 5000, false);
					return true;
				}
			}
			return false;
		} catch (Exception ex) {

			Clients.log("||EndWithOperator ||" + "********** ERROR in  EndWithOperator{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log(
						"||EndWithOperator ||" + "**********   " + i + "- getClassName= " + elementSTD.getClassName());
				Clients.log("||EndWithOperator ||" + "**********    getMethodName=" + elementSTD.getMethodName());
				Clients.log("||EndWithOperator ||" + "**********    getLineNumber=" + elementSTD.getLineNumber());
				Clients.log("||EndWithOperator ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||EndWithOperator ||" + "**********}");
			return false;
		}
	}

	/*
	 * validate expresion alternation
	 * 
	 */
	public boolean evaluateAlternationsExpresion(String infix) {
		boolean validacion = true;
		try {
			char[] chain = infix.toCharArray();
			char lastElement = chain[0];

			for (int c = 1; c < chain.length; c++) {
				char character = chain[c];
				if (Character.isDigit(character)) {
					if (lastElement == ')') {
						String out = generateError(infix, c);
						out += "You cannot put a number after a ')'\n";
//						Messagebox.show(out);
						Notification.show(out, "error",
								txt_expresion  , "after_start", 5000, false);
						validacion = false;
					} else {
						lastElement = character;
					}
				} else if (character == '(') {
					if (lastElement == '(') {
						lastElement = character;
					} else if (lastElement == '+' || lastElement == '-' || lastElement == '*' || lastElement == '/'
							|| lastElement == '^') {
						lastElement = character;
					} else {
						String out = generateError(infix, c);
						out += "After an  '" + lastElement + "' cannot come a '('\n";
//						Messagebox.show(out);
						Notification.show(out, "error",
								txt_expresion  , "after_start", 5000, false);
						validacion = false;
					}
				} else if (character == ')') {
					if (lastElement == ')') {
						lastElement = character;
					} else if (Character.isDigit(lastElement)) {
						lastElement = character;
					} else {
						String out = generateError(infix, c);
						out += "After an '" + lastElement + "' cannot come a ')'\n";
//						Messagebox.show(out);
						Notification.show(out, "error",
								txt_expresion  , "after_start", 5000, false);
						validacion = false;
					}
				} else if (character == '+' || character == '-' || character == '*' || character == '/'
						|| character == '^') {
					if (lastElement == ')') {
						lastElement = character;
					} else if (Character.isDigit(lastElement)) {
						lastElement = character;
					} else {
						String out = generateError(infix, c);
						out += "After an  '" + lastElement + "' cannot come a  Operator '" + character + "'\n";
//						Messagebox.show(out);
						Notification.show(out, "error",
								txt_expresion  , "after_start", 5000, false);
						validacion = false;
					}
				}
			}

			return validacion;
		} catch (Exception ex) {

			Clients.log("||evaluateAlternationsExpresion ||" + "********** ERROR in  evaluateAlternationsExpresion{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log("||evaluateAlternationsExpresion ||" + "**********   " + i + "- getClassName= "
						+ elementSTD.getClassName());
				Clients.log("||evaluateAlternationsExpresion ||" + "**********    getMethodName="
						+ elementSTD.getMethodName());
				Clients.log("||evaluateAlternationsExpresion ||" + "**********    getLineNumber="
						+ elementSTD.getLineNumber());
				Clients.log("||evaluateAlternationsExpresion ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||evaluateAlternationsExpresion ||" + "**********}");
			return validacion;
		}
	}
	/*
	 * validate expresion posfix
	 * 
	 */

	public String toPosfix(String infix) {
		String out = "";
		try {
			char[] chain = infix.toCharArray();

			for (int c = 0; c < chain.length; c++) {
				char character = chain[c];
				if (character == '(') {
					queue.push(character);
				} else if (character == ')') {
					while (true) {
						if (queue.empty()) {
							String return_ = generateError(infix, c);
							return_ += "Invalid operation number of odd parentheses";
//							Clients.log(return_);
							Notification.show(return_, "error",
									txt_expresion  , "after_start", 5000, false);
							return null;
						}
						char temp = queue.pop().charValue();
						if (temp == '(') {
							break;
						} else {
							out += " " + temp;
						}
					} // fin del wile
				} else if (Character.isDigit(character)) {
					out += " " + character;
					c++;
					search: for (; c < chain.length; c++) {
						if (Character.isDigit(chain[c])) {
							out += chain[c];
						} else {
							c--;
							break search;
						}
					}
				} else if (character == '+' || character == '-' || character == '/' || character == '*'
						|| character == '^') {
					if (queue.empty()) {
						queue.push(character);
					} else {
						while (true) {
							if (isOfGreaterPresence(character)) {
								queue.push(character);
								break;
							} else {
								out += " " + queue.pop();
							}
						}
					}
				} else {
					String return_ = generateError(infix, c);
					return_ += "character not valid for the expression : '" + character + "'";
//					Clients.log(return_);
					Notification.show(return_, "error",
							txt_expresion  , "after_start", 5000, false);
					return null;
				}
			} // fin del for
			if (!queue.empty()) {
				do {
					char temp = queue.pop().charValue();
					out += " " + temp;
				} while (!queue.empty());
			}

			return out.trim();
		} catch (Exception ex) {

			Clients.log("||toPosfix ||" + "********** ERROR in  toPosfix{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log("||toPosfix ||" + "**********   " + i + "- getClassName= " + elementSTD.getClassName());
				Clients.log("||toPosfix ||" + "**********    getMethodName=" + elementSTD.getMethodName());
				Clients.log("||toPosfix ||" + "**********    getLineNumber=" + elementSTD.getLineNumber());
				Clients.log("||toPosfix ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||toPosfix ||" + "**********}");
			return out.trim();
		}
	}

	/*
	 * validate expresion caracter is of greater presence
	 * 
	 */
	private boolean isOfGreaterPresence(char character) {
		try {

			if (queue.empty()) {
				return true;
			}
			if (character == queue.peek().charValue()) {
				return false;
			}
			if (character == '^') {
				return true;
			}
			if ((character == '*' && queue.peek().charValue() == '/')
					|| (character == '/' && queue.peek().charValue() == '*')) {
				return false;
			}
			if ((character == '+' && queue.peek().charValue() == '-')
					|| (character == '-' && queue.peek().charValue() == '+')) {
				return false;
			} else if (character == '-' || character == '+') {
				char temp = queue.peek().charValue();
				if (temp == '*' || temp == '/') {
					return false;
				}
			}
			return true;
		} catch (Exception ex) {

			Clients.log("||isOfGreaterPresence ||" + "********** ERROR in  isOfGreaterPresence{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log(
						"||EndWithOperator ||" + "**********   " + i + "- getClassName= " + elementSTD.getClassName());
				Clients.log("||isOfGreaterPresence ||" + "**********    getMethodName=" + elementSTD.getMethodName());
				Clients.log("||isOfGreaterPresence ||" + "**********    getLineNumber=" + elementSTD.getLineNumber());
				Clients.log("||isOfGreaterPresence ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||isOfGreaterPresence ||" + "**********}");
			return true;
		}
	}

	/*
	 * execute expresion
	 * 
	 */

	public int evaluatePostfix(String posfix) {
		ArrayList<String> token = new ArrayList<String>();

		StringTokenizer st = new StringTokenizer(posfix, " ");
		while (st.hasMoreTokens()) {
			token.add(st.nextToken());
		}

		if (token.size() == 1) {
			return Integer.parseInt(token.get(0));
		}
		int c = 0;

		while (token.size() != 1) {

			String operator = token.get(c);
			if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")
					|| operator.equals("^")) {
				String operando1 = token.get(c - 1);
				String operando2 = token.get(c - 2);

				token.remove(c);
				token.remove(c - 1);
				token.remove(c - 2);
				if (operator.equals("+")) {
					try {
						String add = WsServices.add(Integer.parseInt(operando2), Integer.parseInt(operando1)) + "";
						token.add(c - 2, add);
						c = 0;
					} catch (Exception e) {
						Messagebox.show("Error converting an operand\n" + e);
						return 0;
					}
				} else if (operator.equals("-")) {
					try {
						String subtract = WsServices.subtract(Integer.parseInt(operando2), Integer.parseInt(operando1))
								+ "";
						token.add(c - 2, subtract);
						c = 0;
					} catch (Exception e) {
						Messagebox.show("Error converting an operand\n" + e);
						return 0;
					}
				} else if (operator.equals("*")) {
					try {
						String multiply = WsServices.multiply(Integer.parseInt(operando2), Integer.parseInt(operando1))
								+ "";
						token.add(c - 2, multiply);
						c = 0;
					} catch (Exception e) {
						Messagebox.show("Error converting an operand\n" + e);
						return 0;
					}
				} else if (operator.equals("/")) {
					try {
						Messagebox.show("Error the division is not defined in this calculator\n");
					} catch (Exception e) {
						return 0;
					}
				}
			} else {
				c++;
			}
		}

		try {
			return Integer.parseInt(token.get(0));
		} catch (Exception ex) {
			Clients.log("Error parsing the result\n" + ex);
			Clients.log("||evaluatePostfix ||" + "********** ERROR in  evaluatePostfix{");
			StackTraceElement[] elementRaster = ex.getStackTrace();
			for (int i = 0; i < elementRaster.length; i++) {
				StackTraceElement elementSTD = elementRaster[i];
				Clients.log(
						"||evaluatePostfix ||" + "**********   " + i + "- getClassName= " + elementSTD.getClassName());
				Clients.log("||evaluatePostfix ||" + "**********    getMethodName=" + elementSTD.getMethodName());
				Clients.log("||evaluatePostfix ||" + "**********    getLineNumber=" + elementSTD.getLineNumber());
				Clients.log("||evaluatePostfix ||" + "**********    errorMSG=" + ex.getMessage());
			}
			Clients.log("||evaluatePostfix ||" + "**********}");
			return 0;
		}

	}

	/*
	 * validate expresion ERRROR
	 * 
	 */
	private String generateError(String infix, int index) {
		String error[] = new String[infix.length()];
		for (int c = 0; c < error.length; c++) {
			error[c] = "  ";
		}
		error[index] = "^";

		String error2 = "";
		for (int c = 0; c < error.length; c++) {
			error2 += error[c];
		}
		return infix + "\n" + error2 + "\n";
	}

}
