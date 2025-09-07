package rs.ac.bg.etf.pp1.semantics;

import rs.ac.bg.etf.pp1.parser.generated.ast.*;

import org.apache.log4j.Logger;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {

    Logger log = Logger.getLogger(getClass());

    private Boolean error = false;
    private Boolean inMain = false;
    private Boolean mainOccured = false;
    private Obj mainMethod;

    private Obj currProgram;
    private int currConstantVal;
    private Struct currConstantType;
    private Struct currType;

    public void printError(String message, SyntaxNode info) {
        error  = true;
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0: info.getLine();
        if (line != 0)
            msg.append (" na liniji ").append(line);
        log.error(msg.toString());
    }

    public void printInfo(String message, SyntaxNode info) {
        StringBuilder msg = new StringBuilder(message);
        int line = (info == null) ? 0: info.getLine();
        if (line != 0)
            msg.append (" na liniji ").append(line);
        log.info(msg.toString());
    }

    @Override
    public void visit(ProgramName programName) {
        currProgram = Tab.insert(Obj.Prog, programName.getI1(), Tab.noType);
        Tab.openScope();
    }

    @Override
    public void visit(Number1 numConstant) {
        currConstantVal = numConstant.getN1();
        currConstantType = Tab.intType;
    }

    @Override
    public void visit(Character1 charConstant) {
        currConstantVal = charConstant.getC1();
        currConstantType = Tab.charType;
    }

    @Override
    public void visit(Boolean1 boolConstant) {
        currConstantVal = boolConstant.getB1();
        currConstantType = Tab.find("bool").getType();
    }

    @Override
    public void visit(DeclareConst declaredConst) {
        Obj constant = Tab.find(declaredConst.getI1());
        if(constant != Tab.noObj) {
            printError("Naziv konstante je vec definisan: " + declaredConst.getI1(), declaredConst);
        } else if(currConstantType.assignableTo(currType)) {
            constant = Tab.insert(Obj.Con, declaredConst.getI1(), currType);
            constant.setAdr(currConstantVal);
        } else {
            printError("Nekompatibilna dodela tipova", declaredConst);
        }
    }

    @Override
    public void visit(Type type) {
        Obj typeObj = Tab.find(type.getI1());
        if(typeObj == Tab.noObj || typeObj.getKind() != Obj.Type) {
            printError("Nepostojeci tip podataka " + type.getI1(), type);
            currType = Tab.noType;
        }
        else
            currType = typeObj.getType();
    }

    @Override
    public void visit(Var var) {
        Obj variable;
        if(inMain) {
            variable = Tab.currentScope().findSymbol(var.getI1());
        } else {
            variable = Tab.find(var.getI1());
        }
        if(variable != Tab.noObj && variable != null) {
            printError("Ime varijable je vec definisano:" + var.getI1(), var);
        } else {
            Tab.insert(Obj.Var, var.getI1(), currType);
        }
    }

    @Override
    public void visit(Arr array) {
        Obj variable;
        if(inMain) {
            variable = Tab.currentScope().findSymbol(array.getI1());
        } else {
            variable = Tab.find(array.getI1());
        }
        if(variable != Tab.noObj && variable != null) {
            printError("Ime varijable je vec definisano:" + array.getI1(), array);
        } else {
            Tab.insert(Obj.Var, array.getI1(), new Struct(Struct.Array, currType));
        }
    }

    @Override
    public void visit (MainDeclare main) {
        mainOccured = true;
        mainMethod = Tab.insert(Obj.Meth, "main", Tab.noType);
        inMain = true;
        Tab.openScope();
    }

    @Override
    public void visit (MethodDeclaration main) {
        Tab.chainLocalSymbols(mainMethod);
        inMain = false;
        mainMethod = null;
        Tab.closeScope();
    }

    @Override
    public void visit(Program program) {
        Tab.chainLocalSymbols(currProgram);
        Tab.closeScope();
        currProgram = null;

        if(!mainOccured)
            printError("Program nije isparavan bez main metode", program);
    }

    @Override
    public void visit(FactorNumber number) {
        number.struct = Tab.intType;
    }

    @Override
    public void visit(FactorCharacter character) {
        character.struct = Tab.charType;
    }

    @Override
    public void visit(FactorBool bool) {
        bool.struct = Tab.find("bool").getType();
    }

    @Override
    public void visit(FactorDes factor) {
        factor.struct = factor.getDesignator().obj.getType();
    }
    @Override
    public void visit(TermMulOp term) {
        Struct factorType = term.getFactor().struct;
        Struct termType = term.getTerm().struct;

        if(!factorType.equals(Tab.intType) || !termType.equals(Tab.intType)) {
            printError("Pokusaj mnozenja vrednosti koje nisu tipa int", term);
            term.struct = Tab.noType;
        } else {
            term.struct = Tab.intType;
        }

    }

    @Override
    public void visit(TermF term) {
        term.struct = term.getFactor().struct;
    }

    @Override
    public void visit(Variable variable) {
        String varName = variable.getI1();
        Obj variableObj = Tab.find(varName);
        if(variableObj == Tab.noObj) {
            printError("Promenljiva nije definisana: " + varName, variable);
            variable.obj = Tab.noObj;
        } else if (variableObj.getKind() == Obj.Var || variableObj.getKind() == Obj.Con) {
            variable.obj = variableObj;
        }
        else {
            variable.obj = Tab.noObj;
            printError("Nedozvoljeno mesto za " + varName, variable);
        }
    }

    @Override
    public void visit(ArrayVarName array) {
        String arrayName = array.getI1();
        Obj arrayObj = Tab.find(arrayName);
        if(arrayObj == Tab.noObj) {
            printError("Nedefinisan niz: " + array, array);
            array.obj = Tab.noObj;
        } else if (arrayObj.getKind() == Obj.Var && arrayObj.getType().getKind() == Struct.Array) {
            array.obj = arrayObj;
        } else {
            array.obj = Tab.noObj;
            printError("Dati identifikstor ne predstavlja niz:  " + array, array);
        }
    }

    @Override
    public void visit(ArrayElem elem) {
        Obj arrayObj = elem.getArrayVarName().obj;
        if(arrayObj == Tab.noObj)
            elem.obj = Tab.noObj;
        else if(!elem.getExpr().struct.equals(Tab.intType)) {
            printError("Pokusaj nemoguceg indeksiranja sa int vrednostima.", elem);
            elem.obj = Tab.noObj;
        }
        else {
            elem.obj = new Obj(Obj.Elem, arrayObj.getName() + "[$]", arrayObj.getType().getElemType());
        }
    }

    @Override
    public void visit(MinusExprT expr) {
        if(expr.getTerm().struct.equals(Tab.intType)) {
            expr.struct = Tab.intType;
        } else {
            expr.struct = Tab.noType;
            printError("Negirati se mogu samo int vrednosti",  expr);
        }
    }

    @Override
    public void visit(ExprT expr) {
        expr.struct = expr.getTerm().struct;
    }

    @Override
    public void visit(ExprList expr) {
        if(!expr.getExpr().struct.equals(Tab.intType) || !expr.getTerm().struct.equals(Tab.intType)) {
            printError("Pokusaj sabiranja vrednosti koje nisu integeri", expr);
            expr.struct = Tab.noType;
        } else {
            expr.struct = Tab.intType;
        }
    }

    @Override
    public void visit(StetementR read) {
        Struct type = read.getDesignator().obj.getType();
        int kind = read.getDesignator().obj.getKind();

        if(kind != Obj.Elem && kind != Obj.Var) {
            printError("Read se moze raditi iskljucivo nad varijablama i elementima niza", read);
        }
        printReadExprTypeCheck(type, read);
    }

    @Override
    public void visit(StatementPrint printer) {
        Struct type = printer.getExpr().struct;
        printReadExprTypeCheck(type, printer);
    }

    @Override
    public void visit(SingleStatementPrintExprAndNumber printer) {
        Struct type = printer.getExpr().struct;
        printReadExprTypeCheck(type, printer);
    }

    @Override
    public void visit(Assign assign) {
        int kind = assign.getDesignator().obj.getKind();
        Struct typeDesignator = assign.getDesignator().obj.getType();
        Struct typeExpr = assign.getExpr().struct;
        if(kind != Obj.Elem && kind != Obj.Var) {
            printError("Dodela vrednosti se moze iskljucivo raditi nad promenljivama i elementima niza", assign);
        } else if (!typeExpr.assignableTo(typeDesignator)) {
            printError("Tipovi nisu dodeljivi", assign);
        }
    }

    @Override
    public void visit(Inc inc) {
        int kind = inc.getDesignator().obj.getKind();
        Struct type = inc.getDesignator().obj.getType();
        if(type != Tab.intType) {
            printError("Inkrementiranje je dozvoljeno iskljucivo nad int vrednostima", inc);
        }
        if(kind != Obj.Elem && kind != Obj.Var) {
            printError("Inkrementiranje iskljucivo nad promenljivama i elementima niza", inc);
        }
    }

    @Override
    public void visit(Dec dec) {
        int kind = dec.getDesignator().obj.getKind();
        Struct type = dec.getDesignator().obj.getType();
        if(type != Tab.intType) {
            printError("Dekrementiranje je dozvoljeno iskljucivo nad int vrednostima", dec);
        }
        if(kind != Obj.Elem && kind != Obj.Var) {
            printError("Dekrementiranje iskljucivo nad promenljivama i elementima niza", dec);
        }
    }

    @Override
    public void visit(SetOperation setOperation) {
        Struct set = Tab.find("set").getType();
        int kind = setOperation.getDesignator().obj.getKind();
        int kind1 = setOperation.getDesignator1().obj.getKind();
        int kind2 = setOperation.getDesignator2().obj.getKind();
        if(kind != Obj.Var || kind1 != Obj.Var || kind2 != Obj.Var) {
            printError("Skupovske operacije se rade iskljucivo nad promenljivama", setOperation);
        }
        Struct type = setOperation.getDesignator().obj.getType();
        Struct type1 = setOperation.getDesignator1().obj.getType();
        Struct type2 = setOperation.getDesignator2().obj.getType();
        Struct setType = Tab.find("set").getType();
        if(type != setType || type1 != setType || type2 != setType) {
            printError("Skupovske operacije se mogu izvravati samo nad skupovima", setOperation);
        }
    }

    @Override
    public void visit(FactorNewArrOrSet factor) {
        Struct type = factor.getExpr().struct;
        if(type != Tab.intType) {
            printError("Kapacitet mora biti int tipa", factor);
            factor.struct = Tab.noType;
        } else if(currType.equals(Tab.find("set").getType())) {
            factor.struct = Tab.find("set").getType();
        } else {
            factor.struct = new Struct(Struct.Array, currType);
        }
    }

    @Override
    public void visit(FactorExpr factor) {
        factor.struct = factor.getExpr().struct;
    }

    @Override
    public void visit(AddCall addCall) {
        Struct type1  = addCall.getExpr().struct;
        Struct type2 = addCall.getExpr1().struct;
        if(type1 != Tab.find("set").getType() || type2 != Tab.intType) {
            printError("Prvi argument metode add mora biti tipa set, a drugi tipa int", addCall);
        }
    }

    @Override
    public void visit(AddAllCall addAllCall) {
        Struct type1 = addAllCall.getExpr().struct;
        Struct type2 = addAllCall.getExpr1().struct;
        if(type1 != Tab.find("set").getType() || type2.getKind() != Struct.Array || type2.getElemType() != Tab.intType) {
            printError("Prvi argument metode addAll mora biti tipa set, a drugi niz sa elementima tipa int", addAllCall);
        }
    }

    @Override
    public void visit(ChrCall chrCall) {
        Struct type = chrCall.getExpr().struct;
        if(type != Tab.intType) {
            printError("Argument metode chr mora biti tipa int", chrCall);
            chrCall.struct =  Tab.noType;
        } else {
            chrCall.struct = Tab.charType;
        }
    }

    @Override
    public void visit(OrdCall ordCall) {
        Struct type = ordCall.getExpr().struct;
        if(type != Tab.charType) {
            printError("Argument metode ord mora biti tipa char", ordCall);
            ordCall.struct = Tab.noType;
        } else {
            ordCall.struct = Tab.intType;
        }
    }

    @Override
    public void visit(FactorMethodCall factorMethodCall) {
        factorMethodCall.struct = factorMethodCall.getNonVoidMethodsCall().struct;
    }

    private void printReadExprTypeCheck(Struct type, SyntaxNode node) {
        if(type != Tab.charType && type != Tab.intType && type != Tab.find("bool").getType() && type != Tab.find("set").getType()) {
            printError("Moguce je printati samo int, char, bool i set tipove", node);
        }
    }




}
