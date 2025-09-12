package rs.ac.bg.etf.pp1.codegen;

import rs.ac.bg.etf.pp1.parser.generated.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {

    private int pcMain;

    public int getPcMain() {
        return pcMain;
    }

    @Override
    public void visit(MainDeclare mainDeclare) {
        mainDeclare.obj.setAdr(Code.pc);
        this.pcMain = Code.pc;

        Code.put(Code.enter);
        Code.put(mainDeclare.obj.getLevel()); //b1
        Code.put(mainDeclare.obj.getLocalSymbols().size());
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        Code.put(Code.exit);
        Code.put(Code.return_);
    }

    @Override
    public void visit(StatementPrint print){
        Code.loadConst(0);
        Struct exprType = print.getExpr().struct;
        if(exprType == Tab.charType) {
            Code.put(Code.bprint);
        } else {
            Code.put(Code.print);
        }
    }

    @Override
    public void visit(StatementPrintExprAndNumber print) {
        Struct type = print.getExpr().struct;
        Code.loadConst(print.getN2());
        if(type == Tab.charType) {
            Code.put(Code.bprint);
        } else {
            Code.put(Code.print);
        }
    }

    @Override
    public void visit(StetementR read) {
        Struct type = read.getDesignator().obj.getType();
        if(type == Tab.charType) {
            Code.put(Code.bprint);
        } else {
            Code.put(Code.print);
        }

        Code.store(read.getDesignator().obj);
    }

    @Override
    public void visit(Number1 number) {
        Code.loadConst(number.getN1());
    }

    @Override
    public void visit(Character1 character) {
        Code.loadConst(character.getC1());
    }

    @Override
    public void visit(Boolean1 bool) {
        Code.loadConst(bool.getB1());
    }

    @Override
    public void visit(FactorNumber number) {
        Code.loadConst(number.getN1());
    }

    @Override
    public void visit(FactorBool bool) {
        Code.loadConst(bool.getB1());
    }

    @Override
    public void visit(FactorCharacter character) {
        Code.loadConst(character.getC1());
    }

    @Override
    public void visit(FactorDes designator) {
        Code.load(designator.getDesignator().obj);
    }

    @Override
    public void visit(ExprList exprList) {
        AddOp addOp = exprList.getAddOp();
        if(addOp instanceof AddOpPlus) {
            Code.put(Code.add);
        } else if (addOp instanceof AddOpMinus) {
            Code.put(Code.sub);
        }
    }

    @Override
    public void visit(TermMulOp termMulOp) {
        MulOp mulOp = termMulOp.getMulOp();
        if (mulOp instanceof MulOpMul) {
            Code.put(Code.mul);
        } else if (mulOp instanceof MulOpDiv) {
            Code.put(Code.div);
        } else if (mulOp instanceof MulOpMod) {
            Code.put(Code.rem);
        }
    }

    @Override
    public void visit(MinusExprT minusExprT) {
        Code.put(Code.neg);
    }

    @Override
    public void visit(Assign assign) {
        Code.store(assign.getDesignator().obj);
    }

    @Override
    public void visit(ArrayVarName arrayVarName) {
        Code.load(arrayVarName.obj);
    }

    @Override
    public void visit(FactorNewArrOrSet factorNewArrOrSet) {
        Code.put(Code.newarray);
        if(factorNewArrOrSet.getType().struct  == Tab.charType) {
            Code.put(0);
        } else {
            Code.put(1);
        }
    }

    @Override
    public void visit(Inc inc) {
        Designator designator = inc.getDesignator();
        if (designator.obj.getKind() == Obj.Elem){
            Code.put(Code.dup2);
        }
        Code.load(designator.obj);
        Code.loadConst(1);
        Code.put(Code.add);
        Code.store(designator.obj);
    }

    @Override
    public void visit(Dec dec) {
        Designator designator = dec.getDesignator();
        if (designator.obj.getKind() == Obj.Elem){
            Code.put(Code.dup2);
        }
        Code.load(designator.obj);
        Code.loadConst(1);
        Code.put(Code.sub);
        Code.store(designator.obj);
    }
}
