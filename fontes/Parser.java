

import java.io.*;
import java.util.ArrayList;

/**
 * @Author: Gabriel Rocha de Lima (grl@icomp.ufam.edu.br)
 * ExercÃ­cio de Compiladores lecionado por Marco Cristo (Icomp/UFAM)
 * Icomp/UFAM - AM - Brasil
 * Sun Nov, 15, 2020
 */

class ChangeWords {
    ArrayList<String> inputWords;
    ArrayList<String> placeHolders;
    String line;
    String outputString;

    public ChangeWords() {
        this.line = "";
        this.outputString = "";
        this.inputWords = new ArrayList();
        this.placeHolders = new ArrayList();
    }

    public void clearBuffers() {
        this.line += "\n";
        this.inputWords.clear();
        this.placeHolders.clear();
    }

    public void add(String text) {
        this.line += text + " ";
    }

    public void addInputWord(String input) {
        this.inputWords.add(input);
    }

    public void showInputWords() {
        System.out.println(this.inputWords);
    }

    public void handlePlaceHolder(String digits) {
        int readedIndex = Integer.parseInt(digits);
        String searchedPlaceHolder = this.inputWords.get(readedIndex-1);        
        this.line+= searchedPlaceHolder + " ";
    }

    public void showPlaceHolders() {
        System.out.println(this.placeHolders);
    }    

    public void show() {
        System.out.println(this.line);
    }

}



public class Parser {
	public static final int _EOF = 0;
	public static final int _palavra = 1;
	public static final int _placeHolderWord = 2;
	public static final int maxT = 8;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	ChangeWords handler;



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
		Variavel();
	}

	void Variavel() {
		Expect(3);
		Expect(1);
		while (la.kind == 4) {
			Get();
			Expect(1);
		}
		Expect(5);
		Expect(6);
		Expect(7);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		UPortugol();
		Expect(0);

	}

	private static final boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x}

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
			case 1: s = "palavra expected"; break;
			case 2: s = "placeHolderWord expected"; break;
			case 3: s = "\"variavel\" expected"; break;
			case 4: s = "\",\" expected"; break;
			case 5: s = "\":\" expected"; break;
			case 6: s = "\"inteiro\" expected"; break;
			case 7: s = "\";\" expected"; break;
			case 8: s = "??? expected"; break;
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