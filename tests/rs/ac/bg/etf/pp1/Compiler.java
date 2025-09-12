package rs.ac.bg.etf.pp1;

import java.io.*;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.codegen.CodeGenerator;
import rs.ac.bg.etf.pp1.lexer.generated.Yylex;
import rs.ac.bg.etf.pp1.parser.generated.Parser;
import rs.ac.bg.etf.pp1.semantics.SemanticAnalyzer;
import rs.ac.bg.etf.pp1.util.Log4JUtils;

import rs.ac.bg.etf.pp1.parser.generated.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class Compiler {

	static {
		DOMConfigurator.configure("config/log4j.xml");
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}

	public static void main(String[] args) throws Exception {

		Logger log = Logger.getLogger(Compiler.class);

		Reader br = null;
		try {
			File sourceCode = new File("tests/zvanicni_programi/test301.mj");
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

			addMissingInitMethods();

			SemanticAnalyzer sa = new SemanticAnalyzer();
			prog.traverseBottomUp(sa);

			log.info("=====================================================================");
			Tab.dump();

			if(p.errorDetected || sa.getError() == true) {
				log.info("Program nije uspesno zavrsen");

			} else {
				log.info("Uspesno parsiranje i semanticko analiziranje");

				File obj = new File("tests/program.obj");
				if(obj.exists()) {
					obj.delete();
				}
				CodeGenerator cg = new CodeGenerator();
				prog.traverseBottomUp(cg);
				Code.dataSize = sa.numberOfVars();
				Code.mainPc = cg.getPcMain();
				Code.write(new FileOutputStream(obj));
			}



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

	private static void addMissingInitMethods() {
		Obj add = new Obj(Obj.Meth, "add", Tab.noType, 0, 2);
		Tab.currentScope().addToLocals(add);
		{
			Tab.openScope();
			Obj set = new Obj(Obj.Var, "set", Tab.find("set").getType(), 0, 1);
			Obj newInt = new Obj(Obj.Var, "newInt", Tab.intType, 1, 1);
			Tab.currentScope().addToLocals(set);
			Tab.currentScope().addToLocals(newInt);
			add.setLocals(Tab.currentScope().getLocals());
			Tab.closeScope();
		}

		Obj addAll = new Obj(Obj.Meth, "addAll", Tab.noType, 0, 2);
		Tab.currentScope().addToLocals(addAll);
		{
			Tab.openScope();
			Obj set = new Obj(Obj.Var, "set", Tab.find("set").getType(), 0, 1);
			Struct intArray = new Struct(Struct.Array, Tab.intType);
			Obj arrayOfNewInts = new Obj(Obj.Var, "arrayOfNewInts", intArray, 1, 1);

			Tab.currentScope().addToLocals(set);
			Tab.currentScope().addToLocals(arrayOfNewInts);
			addAll.setLocals(Tab.currentScope().getLocals());
			Tab.closeScope();
		}
	}

}