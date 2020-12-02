

import java.io.*;
import java.util.ArrayList;

/**
 * @Author: Gabriel Rocha de Lima (grl@icomp.ufam.edu.br)
 * ExercÃ­cio de Compiladores lecionado por Marco Cristo (Icomp/UFAM)
 * Icomp/UFAM - AM - Brasil
 * Sun Nov, 15, 2020
 */

class SemanticAnalyzerAndHtmlTransformer {
    ArrayList<String> inputWords;
    ArrayList<String> placeHolders;
    String line;
    String outputString;

    public SemanticAnalyzerAndHtmlTransformer() {
        this.line = "";
        this.outputString = "";
        this.inputWords = new ArrayList();
        this.placeHolders = new ArrayList();
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
	public static final int maxT = 33;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	SemanticAnalyzerAndHtmlTransformer handler = new SemanticAnalyzerAndHtmlTransformer();



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
		Expr();
		while (StartOf(1)) {
			Expr();
		}
	}

	void Expr() {
		AriExpr();
		if (StartOf(2)) {
			RelOp();
			AriExpr();
		}
	}

	void Cmd() {
		Read();
	}

	void Read() {
		String readFromKeyboard = ""; 
		Expect(4);
		handler.debug(t.val); 
		Expect(5);
		readFromKeyboard = Variable();
		handler.debug(readFromKeyboard); 
		Expect(6);
		Expect(7);
	}

	String  Variable() {
		String  var;
		Expect(1);
		handler.debug(t.val); var = t.val; 
		return var;
	}

	void Procedure() {
		String varDecl = ""; 
		ProcedureDeclaration();
		Expect(8);
		while (la.kind == 19) {
			varDecl = VariableDeclaration();
		}
		Expect(9);
	}

	void ProcedureDeclaration() {
		Expect(10);
		Expect(1);
		handler.debug(t.val); 
		Expect(5);
		if (la.kind == 1) {
			ProcedureParams();
			while (la.kind == 11) {
				Get();
				ProcedureParams();
			}
		}
		Expect(6);
		if (la.kind == 12) {
			Get();
			Expect(13);
		}
		
	}

	String  VariableDeclaration() {
		String  declaration;
		String var = ""; 
		Expect(19);
		var = Variable();
		while (la.kind == 11) {
			Get();
			var = Variable();
		}
		Expect(12);
		Expect(13);
		if (la.kind == 14) {
			Get();
			Expect(15);
		}
		Expect(7);
		declaration = t.val; 
		return declaration;
	}

	void ProcedureParams() {
		String paramName=""; 
		paramName = Variable();
		
		Expect(12);
		Expect(13);
		if (la.kind == 14) {
			Get();
			Expect(15);
		}
	}

	String  NewInteger() {
		String  newInt;
		Expect(16);
		Expect(13);
		if (la.kind == 14) {
			Get();
			Expect(2);
			Expect(15);
			Expect(7);
		} else if (la.kind == 17) {
			Get();
			Expect(2);
			while (la.kind == 11) {
				Get();
				Expect(2);
			}
			Expect(18);
			Expect(7);
		} else SynErr(34);
		newInt = t.val; 
		return newInt;
	}

	void ConstantDeclaration() {
		String declaredConstant = ""; 
		Expect(20);
		declaredConstant = Constant();
		Expect(21);
		Expect(2);
		handler.debugln(t.val); 
		Expect(7);
	}

	String  Constant() {
		String  constant;
		Expect(3);
		handler.debug(t.val); constant = t.val; 
		return constant;
	}

	void AriExpr() {
		Term();
		while (la.kind == 22 || la.kind == 23) {
			if (la.kind == 22) {
				Get();
			} else {
				Get();
			}
			Term();
		}
	}

	void RelOp() {
		switch (la.kind) {
		case 27: {
			Get();
			break;
		}
		case 28: {
			Get();
			break;
		}
		case 29: {
			Get();
			break;
		}
		case 30: {
			Get();
			break;
		}
		case 31: {
			Get();
			break;
		}
		case 32: {
			Get();
			break;
		}
		default: SynErr(35); break;
		}
	}

	void Term() {
		Fator();
		while (la.kind == 24 || la.kind == 25 || la.kind == 26) {
			if (la.kind == 24) {
				Get();
			} else if (la.kind == 25) {
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
		} else if (la.kind == 23) {
			Get();
			Fator();
		} else if (la.kind == 5) {
			Get();
			Expr();
			Expect(6);
		} else if (la.kind == 3) {
			cons = Constant();
		} else SynErr(36);
	}

	void Name() {
		Expect(1);
		if (la.kind == 5) {
			Get();
			if (StartOf(1)) {
				ArgList();
			}
			Expect(6);
		}
	}

	void ArgList() {
		Expr();
		while (la.kind == 11) {
			Get();
			Expr();
		}
	}

	void FuncCall() {
		Expect(1);
		Expect(5);
		while (la.kind == 1 || la.kind == 2) {
			if (la.kind == 1) {
				FuncCall();
				if (la.kind == 11) {
					Get();
				}
			} else {
				Get();
				if (la.kind == 11) {
					Get();
				}
			}
		}
		Expect(6);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		UPortugol();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,T,T, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,x,x}

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
			case 4: s = "\"leia\" expected"; break;
			case 5: s = "\"(\" expected"; break;
			case 6: s = "\")\" expected"; break;
			case 7: s = "\";\" expected"; break;
			case 8: s = "\"inicio\" expected"; break;
			case 9: s = "\"fim\" expected"; break;
			case 10: s = "\"procedimento\" expected"; break;
			case 11: s = "\",\" expected"; break;
			case 12: s = "\":\" expected"; break;
			case 13: s = "\"inteiro\" expected"; break;
			case 14: s = "\"[\" expected"; break;
			case 15: s = "\"]\" expected"; break;
			case 16: s = "\"novo\" expected"; break;
			case 17: s = "\"{\" expected"; break;
			case 18: s = "\"}\" expected"; break;
			case 19: s = "\"variavel\" expected"; break;
			case 20: s = "\"constante\" expected"; break;
			case 21: s = "\"=\" expected"; break;
			case 22: s = "\"+\" expected"; break;
			case 23: s = "\"-\" expected"; break;
			case 24: s = "\"*\" expected"; break;
			case 25: s = "\"/\" expected"; break;
			case 26: s = "\"%\" expected"; break;
			case 27: s = "\"==\" expected"; break;
			case 28: s = "\"!=\" expected"; break;
			case 29: s = "\"<\" expected"; break;
			case 30: s = "\">\" expected"; break;
			case 31: s = "\"<=\" expected"; break;
			case 32: s = "\">=\" expected"; break;
			case 33: s = "??? expected"; break;
			case 34: s = "invalid NewInteger"; break;
			case 35: s = "invalid RelOp"; break;
			case 36: s = "invalid Fator"; break;
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
