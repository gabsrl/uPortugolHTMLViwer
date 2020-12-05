

import java.io.*;
import java.util.ArrayList;

/**
 * @Author: Gabriel Rocha de Lima (grl@icomp.ufam.edu.br)
 * @Author: Enzo H. Albuquerque  (ehsa@icomp.ufam.edu.br)
 * Trabalho de Compiladores - uPortugol 
 * Professor: Marco Cristo (Icomp/UFAM)
 * Icomp/UFAM - AM - Brasil
 * Saturday Dez, 5, 2020
 */

class HtmlTransformer {
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
	static int baseOffsetIdentation = 20; 

    public HtmlTransformer() {
		try {
			this.line = "";
			this.html = "<html><head>" + htmlCssRef + "<title>uPortugol</title></head><body><div class=\"header\"><span>u</span>Portugol</div><main>";
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

	public void append(String keyStr, String type) {
		String tagSpan = this.getOpeningSpan(type);
		String tagSpanClosing = this.getClosingSpan();
		this.html += tagSpan + keyStr + tagSpanClosing;
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
			this.html += "</main></body></html>";
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
	public static final int _string = 4;
	public static final int maxT = 53;

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
		if (la.kind == 5) {
			Get();
			Expect(3);
			handler.newLine();
			handler.append("algoritmo" + " ", HtmlTransformer.classType);
			handler.append(t.val);
			handler.closeNewLine();
			
		}
		while (la.kind == 41) {
			ConstantDeclaration();
		}
		while (la.kind == 28) {
			Procedure();
		}
		Expect(6);
		handler.newLine();
		handler.append("inicio", HtmlTransformer.classBlock);
		handler.closeNewLine();
		handler.indent();
		
		Cmd();
		while (StartOf(1)) {
			Cmd();
		}
		Expect(7);
		handler.exdent();
		handler.newLine();
		handler.append("fim", HtmlTransformer.classBlock);
		handler.finish(); 
		
	}

	void ConstantDeclaration() {
		String declaredConstant = ""; 
		Expect(41);
		handler.newLine();
		handler.append("constante" + " ", HtmlTransformer.classType); 
		
		declaredConstant = Constant();
		Expect(14);
		handler.append(" " + "=" + " "); 
		Expect(2);
		handler.append(t.val); 
		Expect(23);
		handler.append(";");
		handler.closeNewLine(); 
		
	}

	void Procedure() {
		String varDecl = ""; 
		ProcedureDeclaration();
		Expect(6);
		handler.identNewLine();  //creating new line with adjustments on indent
		handler.append("inicio", HtmlTransformer.classBlock);
		handler.closeNewLine(); 
		handler.indent();
		
		Cmd();
		while (StartOf(1)) {
			Cmd();
		}
		handler.exdent(); 
		Expect(7);
		handler.newLine(); // criando uma linha com a indentaÃ§Ã£o existente
		handler.append("fim", HtmlTransformer.classBlock); 
		handler.closeNewLine();
		handler.exdentNewLine(); // decrementando a indentaÃ§Ã£o. "Limpando estado"
		
	}

	void Cmd() {
		String varDeclaration = ""; 
		switch (la.kind) {
		case 35: {
			Read();
			break;
		}
		case 36: {
			Write();
			break;
		}
		case 1: case 2: case 3: case 29: case 43: {
			handler.newLine(); 
			Instruction();
			break;
		}
		case 40: {
			varDeclaration = VariableDeclaration();
			break;
		}
		case 8: {
			Get();
			handler.newLine();
			handler.append("enquanto" + " ", HtmlTransformer.classBlock);
			
			Expr();
			Expect(9);
			handler.append(" " + "faca", HtmlTransformer.classBlock);
			handler.closeNewLine();
			handler.indent(); //adding new indent
			
			while (StartOf(1)) {
				Cmd();
			}
			Expect(10);
			handler.exdent();
			handler.newLine();
			handler.append("fimenquanto", HtmlTransformer.classBlock);
			handler.closeNewLine();
			
			break;
		}
		case 11: {
			Get();
			handler.newLine();
			handler.append("repita", handler.classBlock);
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			Expect(12);
			handler.exdent();
			handler.newLine();
			handler.append("ate" +  " ", HtmlTransformer.classBlock);
			
			Instruction();
			handler.closeNewLine();
			
			break;
		}
		case 13: {
			Get();
			handler.newLine();
			handler.append("para" + " ", HtmlTransformer.classBlock);
			
			Expr();
			Expect(14);
			handler.append("=" + " "); 
			Expr();
			Expect(12);
			handler.append("ate" + " ", HtmlTransformer.classBlock); 
			Expr();
			Expect(15);
			handler.append("passo" + " ", HtmlTransformer.classBlock); 
			Expect(2);
			handler.append(t.val + " "); 
			Expect(9);
			handler.append("faca", HtmlTransformer.classBlock);
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			Expect(16);
			handler.exdent();
			handler.newLine();
			handler.append("fimpara", HtmlTransformer.classBlock);
			handler.closeNewLine();
			
			break;
		}
		case 17: {
			Get();
			handler.newLine();
			handler.append("caso" + " ", HtmlTransformer.classBlock);
			
			Expect(1);
			handler.append(t.val);
			handler.closeNewLine();
			handler.indent(); 
			
			while (la.kind == 18) {
				Get();
				handler.newLine();
				handler.append("seja" + " ", HtmlTransformer.classBlock); 
				
				Expect(2);
				handler.append(t.val + " "); 
				Expect(9);
				handler.append("faca", HtmlTransformer.classBlock);
				handler.closeNewLine();
				handler.indent();
				
				Cmd();
				while (StartOf(1)) {
					Cmd();
				}
				handler.exdent(); 
			}
			Expect(19);
			Expect(20);
			handler.newLine();
			handler.append("outrocaso:", HtmlTransformer.classBlock);
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			handler.exdent(); 
			Expect(21);
			handler.exdent();
			handler.newLine();
			handler.append("fimcaso");
			handler.closeNewLine();
			
			break;
		}
		case 22: {
			Get();
			handler.newLine();
			handler.append("retorne" + " ", HtmlTransformer.classType);
			
			Expr();
			handler.append("expr" + ";");
			handler.closeNewLine();
			
			Expect(23);
			break;
		}
		case 24: {
			Get();
			handler.newLine();
			handler.append("se" + " ", HtmlTransformer.classBlock);
			
			Expr();
			Expect(25);
			handler.append(" " + "entao", HtmlTransformer.classBlock); 
			handler.closeNewLine();
			handler.indent();
			
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			handler.exdent(); 
			if (la.kind == 26) {
				Get();
				handler.newLine();
				handler.append("senao", HtmlTransformer.classBlock);
				handler.closeNewLine();
				handler.indent();
				
				Cmd();
				while (StartOf(1)) {
					Cmd();
				}
				handler.exdent();
				
			}
			Expect(27);
			Expect(23);
			handler.newLine();
			handler.append("fimse", HtmlTransformer.classBlock);
			handler.append(";");
			handler.closeNewLine();
			
			break;
		}
		default: SynErr(54); break;
		}
	}

	void Read() {
		String readFromKeyboard = ""; 
		Expect(35);
		handler.newLine();
		handler.append("leia", HtmlTransformer.classFunc); 
		
		Expect(29);
		handler.append("("); 
		readFromKeyboard = Variable();
		Expect(31);
		Expect(23);
		handler.append(");");
		handler.closeNewLine();
		
	}

	void Write() {
		Expect(36);
		handler.newLine();
		handler.append("escreva", HtmlTransformer.classFunc);
		handler.append("(");
		
		Expect(29);
		if (la.kind == 4) {
			Get();
			handler.append(t.val); 
		} else if (StartOf(2)) {
			Expr();
		} else SynErr(55);
		while (la.kind == 30) {
			Get();
			handler.append("," + " "); 
			if (la.kind == 4) {
				Get();
				handler.append(t.val); 
			} else if (StartOf(2)) {
				Expr();
			} else SynErr(56);
		}
		Expect(31);
		handler.append(");");
		handler.closeNewLine();
		
		Expect(23);
	}

	void Instruction() {
		String newInteger = ""; 
		Expr();
		if (la.kind == 14) {
			Get();
			handler.append(" " + "=" + " "); 
			if (StartOf(2)) {
				Expr();
			} else if (la.kind == 37) {
				newInteger = NewInteger();
			} else SynErr(57);
		}
		Expect(23);
		handler.append(";");
		handler.closeNewLine(); 
		
	}

	String  VariableDeclaration() {
		String  declaration;
		String var = ""; 
		Expect(40);
		handler.newLine();
		handler.append("variavel" + " ", HtmlTransformer.classType); 
		
		var = Variable();
		while (la.kind == 30) {
			Get();
			handler.append(", "); 
			var = Variable();
		}
		Expect(20);
		Expect(32);
		handler.append(" "+ ":" + " ");
		handler.append("inteiro", HtmlTransformer.classType);
		
		if (la.kind == 33) {
			Get();
			Expect(34);
			handler.append("[]"); 
		}
		Expect(23);
		handler.append(";");
		handler.closeNewLine();
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
		Expect(37);
		Expect(32);
		handler.append("novo inteiro", HtmlTransformer.classFunc); 
		if (la.kind == 33) {
			Get();
			handler.append("["); 
			Expr();
			Expect(34);
			handler.append("]"); 
		} else if (la.kind == 38) {
			Get();
			handler.append(" " + "{"); 
			Expect(2);
			handler.append(t.val); 
			while (la.kind == 30) {
				Get();
				Expect(2);
				handler.append("," + " " + t.val); 
			}
			Expect(39);
			handler.append("}"); 
		} else SynErr(58);
		newInt = t.val; 
		return newInt;
	}

	void ProcedureDeclaration() {
		Expect(28);
		handler.newLine(); 
		Expect(1);
		handler.append("procedimento " + t.val, HtmlTransformer.classFunc); 
		Expect(29);
		handler.append("("); 
		if (la.kind == 1) {
			ProcedureParams();
			while (la.kind == 30) {
				Get();
				handler.append(", "); 
				ProcedureParams();
			}
		}
		Expect(31);
		handler.append(")"); 
		if (la.kind == 20) {
			Get();
			Expect(32);
			handler.append(":" + " ");
			handler.append("inteiro", HtmlTransformer.classType);
			
		}
		handler.closeNewLine(); 
	}

	void ProcedureParams() {
		String paramName=""; 
		paramName = Variable();
		
		Expect(20);
		Expect(32);
		handler.append(":" + " ");
		handler.append("inteiro", HtmlTransformer.classType); 
		
		if (la.kind == 33) {
			Get();
			Expect(34);
			handler.append("[]"); 
		}
	}

	String  Variable() {
		String  var;
		Expect(1);
		handler.append(t.val);
		var = t.val;
		
		return var;
	}

	String  Constant() {
		String  constant;
		Expect(3);
		handler.append(t.val); constant = t.val; 
		return constant;
	}

	void AriExpr() {
		Term();
		while (la.kind == 42 || la.kind == 43) {
			if (la.kind == 42) {
				Get();
				handler.append(" " + "+" + " "); 
			} else {
				Get();
				handler.append(" " + "-" + " "); 
			}
			Term();
		}
	}

	void RelOp() {
		switch (la.kind) {
		case 47: {
			Get();
			handler.append(" " + "==" + " "); 
			break;
		}
		case 48: {
			Get();
			handler.append(" " + "!=" + " "); 
			break;
		}
		case 49: {
			Get();
			handler.append(" " + "<" + " "); 
			break;
		}
		case 50: {
			Get();
			handler.append(" " + ">" + " "); 
			break;
		}
		case 51: {
			Get();
			handler.append(" " + "<=" + " "); 
			break;
		}
		case 52: {
			Get();
			handler.append(" " + ">=" + " "); 
			break;
		}
		default: SynErr(59); break;
		}
	}

	void Term() {
		Fator();
		while (la.kind == 44 || la.kind == 45 || la.kind == 46) {
			if (la.kind == 44) {
				Get();
				handler.append(" " + "*" + " "); 
			} else if (la.kind == 45) {
				Get();
				handler.append(" " + "/" + " "); 
			} else {
				Get();
				handler.append(" " + "%" + " "); 
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
			handler.append(t.val); 
		} else if (la.kind == 43) {
			Get();
			handler.append("-"); 
			Fator();
		} else if (la.kind == 29) {
			Get();
			handler.append("("); 
			Expr();
			Expect(31);
			handler.append(")"); 
		} else if (la.kind == 3) {
			cons = Constant();
		} else SynErr(60);
	}

	void Name() {
		Expect(1);
		if(la.val.equals("("))
		handler.append(t.val, HtmlTransformer.classFunc);
		else handler.append(t.val);
		
		if (la.kind == 29 || la.kind == 33) {
			if (la.kind == 29) {
				Get();
				handler.append("("); 
				if (StartOf(2)) {
					ArgList();
				}
				Expect(31);
				handler.append(")"); 
			} else {
				Get();
				handler.append("["); 
				Expr();
				Expect(34);
				handler.append("]"); 
			}
		}
	}

	void ArgList() {
		Expr();
		while (la.kind == 30) {
			Get();
			handler.append("," + " "); 
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
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,T,T, x,x,x,x, T,x,x,T, x,T,x,x, x,T,x,x, x,x,T,x, T,x,x,x, x,T,x,x, x,x,x,T, T,x,x,x, T,x,x,T, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,x,x}

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
			case 4: s = "string expected"; break;
			case 5: s = "\"algoritmo\" expected"; break;
			case 6: s = "\"inicio\" expected"; break;
			case 7: s = "\"fim\" expected"; break;
			case 8: s = "\"enquanto\" expected"; break;
			case 9: s = "\"faca\" expected"; break;
			case 10: s = "\"fimenquanto\" expected"; break;
			case 11: s = "\"repita\" expected"; break;
			case 12: s = "\"ate\" expected"; break;
			case 13: s = "\"para\" expected"; break;
			case 14: s = "\"=\" expected"; break;
			case 15: s = "\"passo\" expected"; break;
			case 16: s = "\"fimpara\" expected"; break;
			case 17: s = "\"caso\" expected"; break;
			case 18: s = "\"seja\" expected"; break;
			case 19: s = "\"outrocaso\" expected"; break;
			case 20: s = "\":\" expected"; break;
			case 21: s = "\"fimcaso\" expected"; break;
			case 22: s = "\"retorne\" expected"; break;
			case 23: s = "\";\" expected"; break;
			case 24: s = "\"se\" expected"; break;
			case 25: s = "\"entao\" expected"; break;
			case 26: s = "\"senao\" expected"; break;
			case 27: s = "\"fimse\" expected"; break;
			case 28: s = "\"procedimento\" expected"; break;
			case 29: s = "\"(\" expected"; break;
			case 30: s = "\",\" expected"; break;
			case 31: s = "\")\" expected"; break;
			case 32: s = "\"inteiro\" expected"; break;
			case 33: s = "\"[\" expected"; break;
			case 34: s = "\"]\" expected"; break;
			case 35: s = "\"leia\" expected"; break;
			case 36: s = "\"escreva\" expected"; break;
			case 37: s = "\"novo\" expected"; break;
			case 38: s = "\"{\" expected"; break;
			case 39: s = "\"}\" expected"; break;
			case 40: s = "\"variavel\" expected"; break;
			case 41: s = "\"constante\" expected"; break;
			case 42: s = "\"+\" expected"; break;
			case 43: s = "\"-\" expected"; break;
			case 44: s = "\"*\" expected"; break;
			case 45: s = "\"/\" expected"; break;
			case 46: s = "\"%\" expected"; break;
			case 47: s = "\"==\" expected"; break;
			case 48: s = "\"!=\" expected"; break;
			case 49: s = "\"<\" expected"; break;
			case 50: s = "\">\" expected"; break;
			case 51: s = "\"<=\" expected"; break;
			case 52: s = "\">=\" expected"; break;
			case 53: s = "??? expected"; break;
			case 54: s = "invalid Cmd"; break;
			case 55: s = "invalid Write"; break;
			case 56: s = "invalid Write"; break;
			case 57: s = "invalid Instruction"; break;
			case 58: s = "invalid NewInteger"; break;
			case 59: s = "invalid RelOp"; break;
			case 60: s = "invalid Fator"; break;
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
