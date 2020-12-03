

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
	public static final int maxT = 46;

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
		if (la.kind == 4) {
			Get();
			Expect(1);
		}
		while (la.kind == 34) {
			ConstantDeclaration();
		}
		while (la.kind == 25) {
			Procedure();
		}
		Expect(5);
		while (StartOf(1)) {
			Cmd();
		}
		Expect(6);
	}

	void ConstantDeclaration() {
		String declaredConstant = ""; 
		Expect(34);
		declaredConstant = Constant();
		Expect(13);
		Expect(2);
		handler.debugln(t.val); 
		Expect(21);
	}

	void Procedure() {
		String varDecl = ""; 
		ProcedureDeclaration();
		Expect(5);
		while (la.kind == 33) {
			varDecl = VariableDeclaration();
		}
		Expect(6);
	}

	void Cmd() {
		String varDeclaration = ""; 
		switch (la.kind) {
		case 22: {
			Read();
			break;
		}
		case 1: case 2: case 3: case 23: case 36: {
			Instruction();
			break;
		}
		case 33: {
			varDeclaration = VariableDeclaration();
			break;
		}
		case 7: {
			Get();
			Expr();
			Expect(8);
			while (StartOf(1)) {
				Cmd();
			}
			Expect(9);
			break;
		}
		case 10: {
			Get();
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			Expect(11);
			Instruction();
			break;
		}
		case 12: {
			Get();
			Expr();
			Expect(13);
			Expr();
			Expect(11);
			Expr();
			Expect(14);
			Expect(2);
			Expect(8);
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			Expect(15);
			break;
		}
		case 16: {
			Get();
			Expect(1);
			while (la.kind == 17) {
				Get();
				Expect(2);
				Expect(8);
				Cmd();
				while (StartOf(1)) {
					Cmd();
				}
			}
			Expect(18);
			Expect(19);
			Cmd();
			while (StartOf(1)) {
				Cmd();
			}
			Expect(20);
			break;
		}
		default: SynErr(47); break;
		}
	}

	void Read() {
		String readFromKeyboard = ""; 
		Expect(22);
		handler.debug(t.val); 
		Expect(23);
		readFromKeyboard = Variable();
		handler.debug(readFromKeyboard); 
		Expect(24);
		Expect(21);
	}

	void Instruction() {
		Expr();
		if (la.kind == 13) {
			Get();
			Expr();
		}
		Expect(21);
	}

	String  VariableDeclaration() {
		String  declaration;
		String var = ""; 
		Expect(33);
		var = Variable();
		while (la.kind == 26) {
			Get();
			var = Variable();
		}
		Expect(19);
		Expect(27);
		if (la.kind == 28) {
			Get();
			Expect(29);
		}
		Expect(21);
		declaration = t.val; 
		return declaration;
	}

	void Expr() {
		AriExpr();
		if (StartOf(2)) {
			RelOp();
			AriExpr();
		}
	}

	String  Variable() {
		String  var;
		Expect(1);
		handler.debug(t.val); var = t.val; 
		return var;
	}

	void ProcedureDeclaration() {
		Expect(25);
		Expect(1);
		handler.debug(t.val); 
		Expect(23);
		if (la.kind == 1) {
			ProcedureParams();
			while (la.kind == 26) {
				Get();
				ProcedureParams();
			}
		}
		Expect(24);
		if (la.kind == 19) {
			Get();
			Expect(27);
		}
		
	}

	void ProcedureParams() {
		String paramName=""; 
		paramName = Variable();
		
		Expect(19);
		Expect(27);
		if (la.kind == 28) {
			Get();
			Expect(29);
		}
	}

	String  NewInteger() {
		String  newInt;
		Expect(30);
		Expect(27);
		if (la.kind == 28) {
			Get();
			Expect(2);
			Expect(29);
			Expect(21);
		} else if (la.kind == 31) {
			Get();
			Expect(2);
			while (la.kind == 26) {
				Get();
				Expect(2);
			}
			Expect(32);
			Expect(21);
		} else SynErr(48);
		newInt = t.val; 
		return newInt;
	}

	String  Constant() {
		String  constant;
		Expect(3);
		handler.debug(t.val); constant = t.val; 
		return constant;
	}

	void AriExpr() {
		Term();
		while (la.kind == 35 || la.kind == 36) {
			if (la.kind == 35) {
				Get();
			} else {
				Get();
			}
			Term();
		}
	}

	void RelOp() {
		switch (la.kind) {
		case 40: {
			Get();
			break;
		}
		case 41: {
			Get();
			break;
		}
		case 42: {
			Get();
			break;
		}
		case 43: {
			Get();
			break;
		}
		case 44: {
			Get();
			break;
		}
		case 45: {
			Get();
			break;
		}
		default: SynErr(49); break;
		}
	}

	void Term() {
		Fator();
		while (la.kind == 37 || la.kind == 38 || la.kind == 39) {
			if (la.kind == 37) {
				Get();
			} else if (la.kind == 38) {
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
		} else if (la.kind == 36) {
			Get();
			Fator();
		} else if (la.kind == 23) {
			Get();
			Expr();
			Expect(24);
		} else if (la.kind == 3) {
			cons = Constant();
		} else SynErr(50);
	}

	void Name() {
		Expect(1);
		if (la.kind == 23 || la.kind == 28) {
			if (la.kind == 23) {
				Get();
				if (StartOf(3)) {
					ArgList();
				}
				Expect(24);
			} else {
				Get();
				Expr();
				Expect(29);
			}
		}
	}

	void ArgList() {
		Expr();
		while (la.kind == 26) {
			Get();
			Expr();
		}
	}

	void FuncCall() {
		Expect(1);
		Expect(23);
		while (la.kind == 1 || la.kind == 2) {
			if (la.kind == 1) {
				FuncCall();
				if (la.kind == 26) {
					Get();
				}
			} else {
				Get();
				if (la.kind == 26) {
					Get();
				}
			}
		}
		Expect(24);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		UPortugol();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,T,T, x,x,x,T, x,x,T,x, T,x,x,x, T,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,T,x,x, T,x,x,x, x,x,x,x, x,x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,T,T, T,T,x,x},
		{x,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,x,x, x,x,x,x}

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
			case 21: s = "\";\" expected"; break;
			case 22: s = "\"leia\" expected"; break;
			case 23: s = "\"(\" expected"; break;
			case 24: s = "\")\" expected"; break;
			case 25: s = "\"procedimento\" expected"; break;
			case 26: s = "\",\" expected"; break;
			case 27: s = "\"inteiro\" expected"; break;
			case 28: s = "\"[\" expected"; break;
			case 29: s = "\"]\" expected"; break;
			case 30: s = "\"novo\" expected"; break;
			case 31: s = "\"{\" expected"; break;
			case 32: s = "\"}\" expected"; break;
			case 33: s = "\"variavel\" expected"; break;
			case 34: s = "\"constante\" expected"; break;
			case 35: s = "\"+\" expected"; break;
			case 36: s = "\"-\" expected"; break;
			case 37: s = "\"*\" expected"; break;
			case 38: s = "\"/\" expected"; break;
			case 39: s = "\"%\" expected"; break;
			case 40: s = "\"==\" expected"; break;
			case 41: s = "\"!=\" expected"; break;
			case 42: s = "\"<\" expected"; break;
			case 43: s = "\">\" expected"; break;
			case 44: s = "\"<=\" expected"; break;
			case 45: s = "\">=\" expected"; break;
			case 46: s = "??? expected"; break;
			case 47: s = "invalid Cmd"; break;
			case 48: s = "invalid NewInteger"; break;
			case 49: s = "invalid RelOp"; break;
			case 50: s = "invalid Fator"; break;
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
