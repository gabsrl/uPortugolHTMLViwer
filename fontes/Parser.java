

import java.io.*;
import java.util.ArrayList;

/**
 * @Author: Gabriel Rocha de Lima (grl@icomp.ufam.edu.br)
 * ExercÃ­cio de Compiladores lecionado por Marco Cristo (Icomp/UFAM)
 * Icomp/UFAM - AM - Brasil
 * Sun Nov, 15, 2020
 */

class HtmlTransformer {
    //ArrayList<String> inputWords;
    String line;
	String html;
	FileWriter writer;
	static String htmlCssRef = "<link href=\"style.css\" rel=\"stylesheet\">";
	static String marginLeftProp = "margin-left:";

	static String classComments = "comments";
	static String classType = "types";
	static String classFunc = "funcs";
	static String classBlock = "blocks";
	static int identation = 0;
	static int baseOffsetIdentation = 10; // 10px

    public HtmlTransformer() {
		try {
			this.line = "";
			this.html = "<html><head>" + htmlCssRef + "<title>uPortugol</title></head><body>";
			this.writer = new FileWriter("uPortugol.html");
		} catch(IOException e) {
			System.out.println("Falha ao abrir/criar o arquivo html " + e.getMessage());
		}
	}

	public void indent() {
		identation += baseOffsetIdentation;
	}

	public void exdent() {
		identation-= baseOffsetIdentation;
	}


	public void newLine() { 
		int i = 0;

		while( i < identation) {
			i += 5;
		}
		this.html += "<p style=" + "\"" + marginLeftProp + i + "px;\">";
	}

	public void identNewLine() {
		identation += baseOffsetIdentation;
		this.newLine();
	}

	public void closeNewLine() { this.html += "</p>"; }

	public void exdentNewLine() {
		identation-= baseOffsetIdentation;
		this.newLine();
	}

	
	public void append(String genericStrToappend) {
		this.html += genericStrToappend;
	}
	/**
	 * Stylize "procedimento $procedureName"
	 */
	public void procedureName(String procedureName) {
		String tagSpan = this.getOpeningSpan(classFunc);
		String tagSpanClosing = this.getClosingSpan();
		this.html += tagSpan + "procedimento " +  procedureName + tagSpanClosing;
	}

	public String getOpeningSpan(String styleClass) {
		return "<span " + "class=\"" + styleClass + "\">";
	}
	
	public String getClosingSpan() {
		return "</span>";
	}
	
	public void finish() {
		try {
			this.html += "</body></html>";
			this.writer.write(this.html);
			this.writer.close();
			System.out.println("O arquivo foi gravado com sucesso!");
		} catch(IOException e) {
			System.out.println("Falha ao salvar o arquivo html " + e.getMessage());
		}

	}

	public void debugHtml() {
		this.newLine(); 
		this.append("teste-comando"); 
		this.closeNewLine();
	}

	public void debugln(String s) {
        System.out.println(s);
    }

	public void debug(String s) {
        System.out.print(s + " ");
	}
}



public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _number = 2;
	public static final int _constantNumber = 3;
	public static final int maxT = 51;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	HtmlTransformer handler = new HtmlTransformer();



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void UPortugol() {
		String declaration = ""; 
		if (la.kind == 4) {
			Get();
			Expect(1);
		}
		while (la.kind == 39) {
			ConstantDeclaration();
		}
		while (la.kind == 27) {
			Procedure();
		}
		Expect(5);
		Cmd();
		while (StartOf(1)) {
			Cmd();
		}
		Expect(6);
		handler.finish(); 
	}

	void ConstantDeclaration() {
		String declaredConstant = ""; 
		Expect(39);
		declaredConstant = Constant();
		Expect(13);
		Expect(2);
		handler.debugln(t.val); 
		Expect(22);
	}

	void Procedure() {
		String varDecl = ""; 
		ProcedureDeclaration();
		Expect(5);
		handler.identNewLine();  //creating new line with adjustments on indent
		handler.append("inicio");
		handler.closeNewLine(); 
		
		while (StartOf(1)) {
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
		}
		Expect(6);
		handler.newLine(); // criando uma linha com a indentaÃ§Ã£o existente
		handler.append("fim"); handler.closeNewLine();
		handler.exdentNewLine(); // decrementando a indentaÃ§Ã£o. "Limpando estado"
		
	}

	void Cmd() {
		String varDeclaration = ""; 
		switch (la.kind) {
		case 34: {
			Read();
			break;
		}
		case 1: case 2: case 3: case 28: case 41: {
			Instruction();
			break;
		}
		case 38: {
			varDeclaration = VariableDeclaration();
			break;
		}
		case 7: {
			Get();
			handler.newLine();
			handler.append("enquanto");
			
			Expr();
			Expect(8);
			handler.append(" " + "faca");
			handler.closeNewLine();
			handler.indent(); //adding new indent
			
			while (StartOf(1)) {
				Cmd();
				handler.newLine(); handler.append("teste-enquanto"); handler.closeNewLine(); 
			}
			Expect(9);
			handler.exdent();
			handler.newLine();
			handler.append("fimenquanto");
			handler.closeNewLine();
			
			break;
		}
		case 10: {
			Get();
			handler.newLine();
			handler.append("repita");
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			handler.newLine(); handler.append("teste-repita"); handler.closeNewLine(); 
			while (StartOf(1)) {
				Cmd();
			}
			Expect(11);
			handler.exdent();
			handler.newLine();
			handler.append("ate" +  " ");
			
			Instruction();
			handler.closeNewLine();
			
			break;
		}
		case 12: {
			Get();
			handler.newLine();
			handler.append("para" + " ");
			
			Expr();
			Expect(13);
			Expr();
			handler.append("expr" + " "); 
			Expect(11);
			handler.append("ate" + " "); 
			Expr();
			handler.append("expr" + " "); 
			Expect(14);
			handler.append("passo" + " "); 
			Expect(2);
			handler.append(t.val + " "); 
			Expect(8);
			handler.append("faca");
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			handler.newLine(); handler.append("para-teste-cmd"); handler.closeNewLine(); 
			while (StartOf(1)) {
				Cmd();
			}
			Expect(15);
			handler.exdent();
			handler.newLine();
			handler.append("fimpara");
			handler.closeNewLine();
			
			break;
		}
		case 16: {
			Get();
			handler.newLine();
			handler.append("caso" + " ");
			
			Expect(1);
			handler.append(t.val);
			handler.closeNewLine();
			handler.indent(); 
			
			while (la.kind == 17) {
				Get();
				handler.newLine();
				handler.append("seja" + " "); 
				
				Expect(2);
				handler.append(t.val + " "); 
				Expect(8);
				handler.append("faca");
				handler.closeNewLine();
				handler.indent();
				
				Cmd();
				while (StartOf(1)) {
					Cmd();
				}
				handler.exdent(); 
			}
			Expect(18);
			Expect(19);
			handler.newLine();
			handler.append("outrocaso:");
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			handler.exdent(); 
			Expect(20);
			handler.exdent();
			handler.newLine();
			handler.append("fimcaso");
			handler.closeNewLine();
			
			break;
		}
		case 21: {
			Get();
			handler.newLine();
			handler.append("retorne" + " ");
			
			Expr();
			handler.append("expr" + ";");
			handler.closeNewLine();
			
			Expect(22);
			break;
		}
		case 23: {
			Get();
			handler.newLine();
			handler.append("se" + " ");
			
			Expr();
			handler.append("expr"+ " "); 
			Expect(24);
			handler.append("entao"); 
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			handler.debugHtml(); 
			while (StartOf(1)) {
				Cmd();
			}
			handler.exdent(); 
			if (la.kind == 25) {
				Get();
				handler.newLine();
				handler.append("senao");
				handler.closeNewLine();
				handler.indent();
				
				Cmd();
				handler.debugHtml(); 
				while (StartOf(1)) {
					Cmd();
				}
				handler.exdent();
				
			}
			Expect(26);
			Expect(22);
			handler.newLine();
			handler.append("fimse;");
			handler.closeNewLine();
			
			break;
		}
		default: SynErr(52); break;
		}
	}

	void Read() {
		String readFromKeyboard = ""; 
		Expect(34);
		handler.debug(t.val); 
		Expect(28);
		readFromKeyboard = Variable();
		handler.debug(readFromKeyboard); 
		Expect(30);
		Expect(22);
	}

	void Instruction() {
		String newInteger = ""; 
		Expr();
		if (la.kind == 13) {
			Get();
			if (StartOf(2)) {
				Expr();
			} else if (la.kind == 35) {
				newInteger = NewInteger();
			} else SynErr(53);
		}
		Expect(22);
	}

	String  VariableDeclaration() {
		String  declaration;
		String var = ""; 
		Expect(38);
		var = Variable();
		while (la.kind == 29) {
			Get();
			var = Variable();
		}
		Expect(19);
		Expect(31);
		if (la.kind == 32) {
			Get();
			Expect(33);
		}
		Expect(22);
		declaration = t.val; 
		return declaration;
	}

	void Expr() {
		AriExpr();
		if (StartOf(3)) {
			RelOp();
			AriExpr();
		}
	}

	String  NewInteger() {
		String  newInt;
		Expect(35);
		Expect(31);
		if (la.kind == 32) {
			Get();
			Expr();
			Expect(33);
		} else if (la.kind == 36) {
			Get();
			Expect(2);
			while (la.kind == 29) {
				Get();
				Expect(2);
			}
			Expect(37);
		} else SynErr(54);
		newInt = t.val; 
		return newInt;
	}

	void ProcedureDeclaration() {
		Expect(27);
		handler.newLine(); 
		Expect(1);
		handler.procedureName(t.val); 
		Expect(28);
		handler.append("("); 
		if (la.kind == 1) {
			ProcedureParams();
			while (la.kind == 29) {
				Get();
				ProcedureParams();
			}
		}
		Expect(30);
		handler.append(")"); 
		if (la.kind == 19) {
			Get();
			Expect(31);
			handler.append(": inteiro"); 
		}
		handler.closeNewLine(); 
	}

	void ProcedureParams() {
		String paramName=""; 
		paramName = Variable();
		
		Expect(19);
		Expect(31);
		if (la.kind == 32) {
			Get();
			Expect(33);
		}
	}

	String  Variable() {
		String  var;
		Expect(1);
		handler.debug(t.val); var = t.val; 
		return var;
	}

	String  Constant() {
		String  constant;
		Expect(3);
		handler.debug(t.val); constant = t.val; 
		return constant;
	}

	void AriExpr() {
		Term();
		while (la.kind == 40 || la.kind == 41) {
			if (la.kind == 40) {
				Get();
			} else {
				Get();
			}
			Term();
		}
	}

	void RelOp() {
		switch (la.kind) {
		case 45: {
			Get();
			break;
		}
		case 46: {
			Get();
			break;
		}
		case 47: {
			Get();
			break;
		}
		case 48: {
			Get();
			break;
		}
		case 49: {
			Get();
			break;
		}
		case 50: {
			Get();
			break;
		}
		default: SynErr(55); break;
		}
	}

	void Term() {
		Fator();
		while (la.kind == 42 || la.kind == 43 || la.kind == 44) {
			if (la.kind == 42) {
				Get();
			} else if (la.kind == 43) {
				Get();
			} else {
				Get();
			}
			Fator();
		}
	}

	void Fator() {
		String cons = ""; 
		if (la.kind == 1) {
			Name();
		} else if (la.kind == 2) {
			Get();
		} else if (la.kind == 41) {
			Get();
			Fator();
		} else if (la.kind == 28) {
			Get();
			Expr();
			Expect(30);
		} else if (la.kind == 3) {
			cons = Constant();
		} else SynErr(56);
	}

	void Name() {
		Expect(1);
		if (la.kind == 28 || la.kind == 32) {
			if (la.kind == 28) {
				Get();
				if (StartOf(2)) {
					ArgList();
				}
				Expect(30);
			} else {
				Get();
				Expr();
				Expect(33);
			}
		}
	}

	void ArgList() {
		Expr();
		while (la.kind == 29) {
			Get();
			Expr();
		}
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		UPortugol();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x},
		{x,T,T,T, x,x,x,T, x,x,T,x, T,x,x,x, T,x,x,x, x,T,x,T, x,x,x,x, T,x,x,x, x,x,T,x, x,x,T,x, x,T,x,x, x,x,x,x, x,x,x,x, x},
		{x,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,x,x, x,x,x,x, x,T,x,x, x,x,x,x, x,x,x,x, x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,T,T, T,T,T,x, x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "ident expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "constantNumber expected"; break;
			case 4: s = "\"algoritmo\" expected"; break;
			case 5: s = "\"inicio\" expected"; break;
			case 6: s = "\"fim\" expected"; break;
			case 7: s = "\"enquanto\" expected"; break;
			case 8: s = "\"faca\" expected"; break;
			case 9: s = "\"fimenquanto\" expected"; break;
			case 10: s = "\"repita\" expected"; break;
			case 11: s = "\"ate\" expected"; break;
			case 12: s = "\"para\" expected"; break;
			case 13: s = "\"=\" expected"; break;
			case 14: s = "\"passo\" expected"; break;
			case 15: s = "\"fimpara\" expected"; break;
			case 16: s = "\"caso\" expected"; break;
			case 17: s = "\"seja\" expected"; break;
			case 18: s = "\"outrocaso\" expected"; break;
			case 19: s = "\":\" expected"; break;
			case 20: s = "\"fimcaso\" expected"; break;
			case 21: s = "\"retorne\" expected"; break;
			case 22: s = "\";\" expected"; break;
			case 23: s = "\"se\" expected"; break;
			case 24: s = "\"entao\" expected"; break;
			case 25: s = "\"senao\" expected"; break;
			case 26: s = "\"fimse\" expected"; break;
			case 27: s = "\"procedimento\" expected"; break;
			case 28: s = "\"(\" expected"; break;
			case 29: s = "\",\" expected"; break;
			case 30: s = "\")\" expected"; break;
			case 31: s = "\"inteiro\" expected"; break;
			case 32: s = "\"[\" expected"; break;
			case 33: s = "\"]\" expected"; break;
			case 34: s = "\"leia\" expected"; break;
			case 35: s = "\"novo\" expected"; break;
			case 36: s = "\"{\" expected"; break;
			case 37: s = "\"}\" expected"; break;
			case 38: s = "\"variavel\" expected"; break;
			case 39: s = "\"constante\" expected"; break;
			case 40: s = "\"+\" expected"; break;
			case 41: s = "\"-\" expected"; break;
			case 42: s = "\"*\" expected"; break;
			case 43: s = "\"/\" expected"; break;
			case 44: s = "\"%\" expected"; break;
			case 45: s = "\"==\" expected"; break;
			case 46: s = "\"!=\" expected"; break;
			case 47: s = "\"<\" expected"; break;
			case 48: s = "\">\" expected"; break;
			case 49: s = "\"<=\" expected"; break;
			case 50: s = "\">=\" expected"; break;
			case 51: s = "??? expected"; break;
			case 52: s = "invalid Cmd"; break;
			case 53: s = "invalid Instruction"; break;
			case 54: s = "invalid NewInteger"; break;
			case 55: s = "invalid RelOp"; break;
			case 56: s = "invalid Fator"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
