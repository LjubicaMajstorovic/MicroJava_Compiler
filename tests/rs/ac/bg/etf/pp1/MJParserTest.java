package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.lexer.generated.Yylex;
import rs.ac.bg.etf.pp1.parser.generated.Parser;
import rs.ac.bg.etf.pp1.semantics.SemanticAnalyzer;
import rs.ac.bg.etf.pp1.util.Log4JUtils;

import rs.ac.bg.etf.pp1.parser.generated.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class MJParserTest {

	static {
		DOMConfigurator.configure("config/log4j.xml");
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}

	public static void main(String[] args) throws Exception {

		Logger log = Logger.getLogger(MJParserTest.class);

		Reader br = null;
		try {
			File sourceCode = new File("C:\\Users\\core I7\\Desktop\\MicroJava_Compiler\\tests\\zvanicni_programi\\test301.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());

			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);

			Parser p = new Parser(lexer);
			Symbol s = p.parse();

			Program prog = (Program) (s.value);
			log.info(prog.toString(""));

			Tab.init();
			Struct boolType = new Struct(Struct.Bool);
			Obj boolObj = Tab.insert(Obj.Type, "bool", boolType);
			boolObj.setAdr(-1);
			boolObj.setLevel(-1);

			Struct setType = new Struct(8);
			setType.setElementType(Tab.intType);
			Obj setTypeSymbol = Tab.insert(Obj.Type, "set", setType);
			setTypeSymbol.setAdr(-1);
			setTypeSymbol.setLevel(-1);

			SemanticAnalyzer sa = new SemanticAnalyzer();
			prog.traverseBottomUp(sa);

			log.info("=====================================================================");
			Tab.dump();



		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e1) {
					log.error(e1.getMessage(), e1);
				}
			}
		}

	}

}