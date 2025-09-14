package rs.ac.bg.etf.pp1.codegen;

import rs.ac.bg.etf.pp1.parser.generated.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.Iterator;

public class CodeGenerator extends VisitorAdaptor {

    private int pcMain;

    public int getPcMain() {
        return pcMain;
    }

    public CodeGenerator() {
        initChr();
        initLen();
        initOrd();
        initAdd();
        initAddAll();
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
        if(factorNewArrOrSet.struct.getKind() == Struct.Array) {
            Code.put(Code.newarray);
            if(factorNewArrOrSet.getType().struct  == Tab.charType) {
                Code.put(0);
            } else {
                Code.put(1);
            }
        } else {
            Code.loadConst(1);
            Code.put(Code.add);
            Code.put(Code.newarray);
            Code.put(1);
            Code.put(Code.dup);
            Code.loadConst(0);
            Code.loadConst(0);
            Code.put(Code.astore);
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

    private void initChr()
    {
        Obj chr = Tab.find("chr");
        chr.setAdr(Code.pc);
        Code.put(Code.return_);
    }

    private void initOrd()
    {
        Obj ord = Tab.find("ord");
        ord.setAdr(Code.pc);
        Code.put(Code.return_);
    }

    private void initLen()
    {
        Obj  len = Tab.find("len");
        len.setAdr(Code.pc);
        Code.put(Code.arraylength);
        Code.put(Code.return_);
    }

    private void initAdd()
    {
        Obj add = Tab.find("add");
        Obj set = null;
        Obj elem = null;

        Obj size = null;
        Obj index = null;
        for(Obj local: add.getLocalSymbols()) {
            if (local.getName().equals("set")) {
                set = local;
            } else if(local.getName().equals("newInt")) {
                elem = local;
            } else if(local.getName().equals("size")) {
                size = local;
            } else if(local.getName().equals("index")) {
                index = local;
            }
        }
        add.setAdr(Code.pc);

        Code.put(Code.enter);
        Code.put(2);
        Code.put(4);

        Code.loadConst(1);
        Code.store(index);

        Code.load(set);
        Code.loadConst(0);
        Code.put(Code.aload);
        Code.store(size);

        int startOfLoop = Code.pc;

        Code.load(index);
        Code.load(size);
        Code.putFalseJump(Code.le, 0);
        int exitAddressIndexLimit = Code.pc - 2;


        Code.load(index);
        Code.loadConst(1);
        Code.put(Code.add);
        Code.store(index);

        Code.load(set);
        Code.load(index);
        Code.put(Code.aload);
        Code.load(elem);
        Code.putFalseJump(Code.ne, 0);
        int exitAddresElemFound = Code.pc - 2;

        Code.putJump(startOfLoop);
        Code.fixup(exitAddressIndexLimit);
        // ADDING ELEM
        Code.load(size);
        Code.loadConst(1);
        Code.put(Code.add);
        Code.store(size);
        Code.load(set);
        Code.load(size);
        Code.load(elem);
        Code.put(Code.astore);
        Code.load(set);
        Code.loadConst(0);
        Code.load(size);
        Code.put(Code.astore);
        //FINISH ADDING ELEM
        Code.fixup(exitAddresElemFound);

        Code.put(Code.exit);
        Code.put(Code.return_);
    }

    public void initAddAll() {
        Obj addAll = Tab.find("addAll");
        Obj set = null;
        Obj array = null;
        Obj arrLength = null;
        Obj index = null;
        for(Obj local: addAll.getLocalSymbols()) {
            if (local.getName().equals("set")) {
                set = local;
            } else if (local.getName().equals("arrayOfNewInts")) {
                array = local;
            } else if (local.getName().equals("index")) {
                index = local;
            } else if(local.getName().equals("arrayLength")) {
                arrLength = local;
            }
        }

        addAll.setAdr(Code.pc);
        Code.put(Code.enter);
        Code.put(2);
        Code.put(4);

        Code.loadConst(0);
        Code.store(index);

        Code.load(array);
        Obj len = Tab.find("len");
        int offset = len.getAdr() - Code.pc;
        Code.put(Code.call);
        Code.put2(offset);
        Code.store(arrLength);

        int loopStart = Code.pc;
        Code.load(index);
        Code.load(arrLength);
        Code.putFalseJump(Code.lt, 0);
        int jumpExitArrayVisited = Code.pc - 2;

        Code.load(set);
        Code.load(array);
        Code.load(index);
        Code.put(Code.aload);
        Obj add = Tab.find("add");
        int offsetAL = add.getAdr() - Code.pc;
        Code.put(Code.call);
        Code.put2(offsetAL);

        Code.load(index);
        Code.loadConst(1);
        Code.put(Code.add);
        Code.store(index);

        Code.putJump(loopStart);
        Code.fixup(jumpExitArrayVisited);

        Code.put(Code.exit);
        Code.put(Code.return_);


    }

    @Override
    public void visit(LenCall lenCall) {
        Obj len = Tab.find("len");
        int offset = len.getAdr() - Code.pc;
        Code.put(Code.call);
        Code.put2(offset);
    }

    @Override
    public void visit(AddCall addCall) {
        Obj add = Tab.find("add");
        int offset = add.getAdr() - Code.pc;
        Code.put(Code.call);
        Code.put2(offset);

    }

    @Override
    public void visit(AddAllCall addAllCall) {
        Obj addAll = Tab.find("addAll");
        int offset = addAll.getAdr() - Code.pc;
        Code.put(Code.call);
        Code.put2(offset);

    }

}
