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
    public void visit(Program program) {

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
        Obj variable = null;
        if(inMain) {
            variable = Tab.currentScope().findSymbol(var.getI1());
        } else {
            variable = Tab.find(var.getI1());
        }
        if(variable != Tab.noObj) {
            printError("Ime varijable je vec definisano:" + var.getI1(), var);
        } else {
            variable = Tab.insert(Obj.Var, var.getI1(), currType);
        }
    }

    @Override
    public void visit(Arr array) {
        Obj variable = null;
        if(inMain) {
            variable = Tab.currentScope().findSymbol(array.getI1());
        } else {
            variable = Tab.find(array.getI1());
        }
        if(variable != Tab.noObj) {
            printError("Ime varijable je vec definisano:" + array.getI1(), array);
        } else {
            variable = Tab.insert(Obj.Var, array.getI1(), currType);
        }
    }

}
