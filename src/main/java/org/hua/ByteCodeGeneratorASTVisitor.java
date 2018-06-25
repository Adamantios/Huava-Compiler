package org.hua;

import java.io.PrintWriter;
import java.util.*;

import org.hua.ast.*;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.hua.symbol.LocalIndexPool;
import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;
import org.hua.types.TypeUtils;

class ByteCodeGeneratorASTVisitor implements ASTVisitor {

    /**
     * The name of the class which contains the main function.
     */
    private static String classWithMain;

    /**
     * Contains all the class bytes with their ids, in the order that they have been parsed.
     */
    private TreeMap<String, byte[]> classBytesWithIds;

    /**
     * A class node.
     */
    private ClassNode cn;

    /**
     * An instruction list for the current method.
     */
    private InsnList methodInstructionList;

    /**
     * The name of the file that we want to create the Byte Code for.
     */
    private String filenameWithoutExtension;

    /**
     * The current function's return type.
     */
    private Type functionReturnType;

    /**
     * Flag which shows whether the current method has returned or not.
     */
    private boolean returnedFlag;

    /**
     * Flag which shows whether a call is static or not.
     */
    private boolean staticCall;

    /**
     * The max stack that is going to be needed for the current method.
     */
    private int maxStack;

    /**
     * The index of the variable to be assigned.
     */
    private int assignIndex;

    /**
     * Flag which shows if a SimpleIdentifierExpression gets visited after an AssignmentStatement.
     */
    private boolean afterAssignment;

    /**
     * Flag which shows if the fields are class fields.
     */
    private boolean classFields;

    /**
     * Flag which shows if a SimpleIdentifierExpression is being visited after a function definition.
     */
    private boolean afterFunctionDefinition;

    /**
     * Flag which shows if a dot has been found and it is the first of the expression.
     */
    private boolean firstDot;

    /**
     * Flag which shows if it is the first visit of an Identifier Expression.
     */
    private boolean firstVisit;

    /**
     * The name of the identifier after the last dot of an expression.
     */
    private String lastIdentifier;

    /**
     * Flag which shows if an Identifier Expression is being assigned with a value.
     */
    private boolean afterConstructorCall;

    /**
     * Flag which shows if a PUT Instruction last happened.
     */
    private boolean afterPut;

    /**
     * The literal after
     */
    private String literal;

    /**
     * Flag which shows if a literal got interrupted because of an Identifier Expression in progress.
     */
    private boolean interrupted;

    ByteCodeGeneratorASTVisitor(String filenameWithoutExtension) {
        classBytesWithIds = new TreeMap<String, byte[]>();
        this.cn = new ClassNode();
        this.methodInstructionList = new InsnList();
        this.filenameWithoutExtension = filenameWithoutExtension;
        this.returnedFlag = false;
        this.staticCall = false;
        this.afterAssignment = false;
        this.classFields = false;
        this.afterFunctionDefinition = false;
        this.firstDot = false;
        this.firstVisit = true;
        this.afterConstructorCall = false;
        this.afterPut = false;
        this.interrupted = false;
    }

    /**
     * Gets the name of the class which contains the main function.
     *
     * @return String
     */
    static String getClassWithMain() {
        return classWithMain;
    }

    /**
     * Returns a TreeMap containing all the generated classes with their ids.
     *
     * @return the TreeMap with the generated classes and their ids.
     */
    TreeMap<String, byte[]> getClassBytes() {
        return classBytesWithIds;
    }

    /**
     * Generates the class, prints its content and inserts it in a HashMap with it's identifier as a key.
     *
     * @param id the identifier of the class.
     */
    private void generateClass(String id) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
        cn.accept(cv);
        classBytesWithIds.put(id, cw.toByteArray());
    }

    /**
     * Filters a String and removes unwanted characters.
     * <br>
     * It is used in order to filter the types for the owner field of the Method and Field Instructions.
     *
     * @param input the String to be filtered.
     * @return String
     * @see MethodInsnNode
     * @see FieldInsnNode
     */
    private String fullyFilter(String input) {
        return input.replace("Lorg/hua/", "").replace(";", "");
    }

    /**
     * Filters a String and removes unwanted characters.
     * <br>
     * It is used in order to remove the org/hua/ prefix.
     *
     * @param input the String to be filtered.
     * @return String
     */
    private String huaFilter(String input) {
        return input.replace("org/hua/", "");
    }

    /**
     * Initializes all the flags.
     * <br>
     * Useful after visiting a label node.
     */
    private void initializeFlags() {
        returnedFlag = false;
        staticCall = false;
        afterAssignment = false;
        classFields = false;
        afterFunctionDefinition = false;
        firstDot = false;
        firstVisit = true;
        afterConstructorCall = false;
        afterPut = false;
        interrupted = false;
    }

    /**
     * Adds a label to all the Jump Nodes.
     *
     * @param list      the list containing all the Jump Instruction Nodes.
     * @param labelNode the Label Node.
     */
    private void backpatch(List<JumpInsnNode> list, LabelNode labelNode) {
        if (list == null)
            return;

        for (JumpInsnNode instr : list)
            instr.label = labelNode;
    }

    /**
     * Casts the top of the stack's variable type,
     * which is the source type,
     * to the target type.
     *
     * @param target the target type.
     * @param source the source type to be casted.
     */
    private void widen(Type target, Type source) {
        if (!source.equals(target))
            if (source.equals(Type.BOOLEAN_TYPE)) {
                if (target.equals(Type.FLOAT_TYPE))
                    methodInstructionList.add(new InsnNode(Opcodes.I2F));
                else if (target.equals(TypeUtils.STRING_TYPE))
                    methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;"));

            } else if (source.equals(Type.INT_TYPE)) {
                if (target.equals(Type.FLOAT_TYPE))
                    methodInstructionList.add(new InsnNode(Opcodes.I2F));
                else if (target.equals(TypeUtils.STRING_TYPE))
                    methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;"));

            } else if (source.equals(Type.FLOAT_TYPE))
                if (target.equals(TypeUtils.STRING_TYPE))
                    methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;"));
    }

    /**
     * Handles a boolean operation.
     *
     * @param node the expression's node.
     * @param op   the operator.
     * @param type the max type of the operands.
     * @throws ASTVisitorException
     */
    private void handleBooleanOperator(Expression node, Operator op, Type type) throws ASTVisitorException {
        // create a trueList.
        List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();

        // add a JumpInsnNode with null label based on the operation.
        // add the jmp instruction into trueList.
        if (type.equals(TypeUtils.STRING_TYPE)) {
            methodInstructionList.add(new InsnNode(Opcodes.SWAP));
            JumpInsnNode jmp = null;
            methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFNE, null);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings!");
                    break;
            }
            trueList.add(jmp);
        }

        // add DCMPG instruction.
        // add a JumpInsnNode with null label based on the operation.
        // add the jmp instruction into trueList.
        else if (type.equals(Type.FLOAT_TYPE)) {
            methodInstructionList.add(new InsnNode(Opcodes.DCMPG));
            JumpInsnNode jmp = null;
            switch (op) {
                case AND:
                    ASTUtils.error(node, "Operator && cannot be applied to float type!");
                    break;
                case OR:
                    ASTUtils.error(node, "Operator || cannot be applied to float type!");
                    break;
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                    methodInstructionList.add(jmp);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                    methodInstructionList.add(jmp);
                    break;
                case GREATER:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                    methodInstructionList.add(jmp);
                    break;
                case GREATER_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    methodInstructionList.add(jmp);
                    break;
                case LESS:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                    methodInstructionList.add(jmp);
                    break;
                case LESS_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                    methodInstructionList.add(jmp);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported!");
                    break;
            }
            trueList.add(jmp);
        }

        // add a JumpInsnNode with null label based on the operation.
        // add the jmp instruction into trueList.
        else {
            JumpInsnNode jmp = null;
            switch (op) {
                case AND:
                    jmp = new JumpInsnNode(Opcodes.IAND, null);
                    methodInstructionList.add(jmp);
                    break;
                case OR:
                    jmp = new JumpInsnNode(Opcodes.IOR, null);
                    methodInstructionList.add(jmp);
                    break;
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                    methodInstructionList.add(jmp);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                    methodInstructionList.add(jmp);
                    break;
                case GREATER:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                    methodInstructionList.add(jmp);
                    break;
                case GREATER_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    methodInstructionList.add(jmp);
                    break;
                case LESS:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                    methodInstructionList.add(jmp);
                    break;
                case LESS_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                    methodInstructionList.add(jmp);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported!");
                    break;
            }
            trueList.add(jmp);
        }
        ASTUtils.setTrueList(node, trueList);
    }

    /**
     * Handles an operation between two strings.
     * <br>
     * Assumes that the top of the stack contains two strings.
     *
     * @param node the operation node.
     * @param op   the operator.
     * @throws ASTVisitorException
     */
    private void handleStringOperator(ASTNode node, Operator op) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            methodInstructionList.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));

            methodInstructionList.add(new InsnNode(Opcodes.DUP));
            maxStack++;

            methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
            methodInstructionList.add(new InsnNode(Opcodes.SWAP));
            methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            methodInstructionList.add(new InsnNode(Opcodes.SWAP));
            methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        } else if (op.isRelational()) {
            LabelNode trueLabelNode = new LabelNode(new Label());
            switch (op) {
                case EQUAL:
                    methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    methodInstructionList.add(new JumpInsnNode(Opcodes.IFNE, trueLabelNode));
                    break;
                case NOT_EQUAL:
                    methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    methodInstructionList.add(new JumpInsnNode(Opcodes.IFEQ, trueLabelNode));
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings!");
                    break;
            }
            methodInstructionList.add(new InsnNode(Opcodes.ICONST_0));
            LabelNode endLabelNode = new LabelNode(new Label());
            methodInstructionList.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
            methodInstructionList.add(trueLabelNode);
            initializeFlags();
            methodInstructionList.add(new InsnNode(Opcodes.ICONST_1));
            methodInstructionList.add(endLabelNode);
            initializeFlags();
        } else
            ASTUtils.error(node, "Operator not recognized!");
    }

    /**
     * Handles an operation between numbers.
     *
     * @param node the operation's node.
     * @param op   the operator.
     * @param type the max type of the operands.
     * @throws ASTVisitorException
     */
    private void handleNumberOperator(ASTNode node, Operator op, Type type) throws ASTVisitorException {
        if (op.equals(Operator.PLUS))
            methodInstructionList.add(new InsnNode(type.getOpcode(Opcodes.IADD)));
        else if (op.equals(Operator.MINUS))
            methodInstructionList.add(new InsnNode(type.getOpcode(Opcodes.ISUB)));
        else if (op.equals(Operator.MULTIPLY))
            methodInstructionList.add(new InsnNode(type.getOpcode(Opcodes.IMUL)));
        else if (op.equals(Operator.DIVISION))
            methodInstructionList.add(new InsnNode(type.getOpcode(Opcodes.IDIV)));
        else if (op.equals(Operator.MOD))
            methodInstructionList.add(new InsnNode(type.getOpcode(Opcodes.IREM)));
        else if (op.isRelational()) {
            if (type.equals(Type.FLOAT_TYPE)) {
                methodInstructionList.add(new InsnNode(Opcodes.DCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        methodInstructionList.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        methodInstructionList.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        methodInstructionList.add(jmp);
                        break;
                    case GREATER_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        methodInstructionList.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        methodInstructionList.add(jmp);
                        break;
                    case LESS_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        methodInstructionList.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported!");
                        break;
                }
                methodInstructionList.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode(new Label());
                methodInstructionList.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                LabelNode trueLabelNode = new LabelNode(new Label());
                jmp.label = trueLabelNode;
                methodInstructionList.add(trueLabelNode);
                initializeFlags();
                methodInstructionList.add(new InsnNode(Opcodes.ICONST_1));
                methodInstructionList.add(endLabelNode);
                initializeFlags();
            } else if (type.equals(Type.INT_TYPE)) {
                LabelNode trueLabelNode = new LabelNode(new Label());
                switch (op) {
                    case EQUAL:
                        methodInstructionList.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, trueLabelNode));
                        break;
                    case NOT_EQUAL:
                        methodInstructionList.add(new JumpInsnNode(Opcodes.IF_ICMPNE, trueLabelNode));
                        break;
                    case GREATER:
                        methodInstructionList.add(new JumpInsnNode(Opcodes.IF_ICMPGT, trueLabelNode));
                        break;
                    case GREATER_EQUAL:
                        methodInstructionList.add(new JumpInsnNode(Opcodes.IF_ICMPGE, trueLabelNode));
                        break;
                    case LESS:
                        methodInstructionList.add(new JumpInsnNode(Opcodes.IF_ICMPLT, trueLabelNode));
                        break;
                    case LESS_EQUAL:
                        methodInstructionList.add(new JumpInsnNode(Opcodes.IF_ICMPLE, trueLabelNode));
                        break;
                    default:
                        break;
                }
                methodInstructionList.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode(new Label());
                methodInstructionList.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                methodInstructionList.add(trueLabelNode);
                initializeFlags();
                methodInstructionList.add(new InsnNode(Opcodes.ICONST_1));
                methodInstructionList.add(endLabelNode);
                initializeFlags();
            } else
                ASTUtils.error(node, "Cannot compare such types!");
        } else
            ASTUtils.error(node, "Operator not recognized!");
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for (ClassDefinition c : node.getClassDefinitionList())
            c.accept(this);
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        afterConstructorCall = false;
        node.getExpression2().accept(this);
        afterAssignment = true;

        Type t1 = ASTUtils.getSafeType(node.getExpression1());
        Type t2 = ASTUtils.getSafeType(node.getExpression2());

        node.getExpression1().accept(this);
        afterAssignment = false;

        widen(t1, t2);
        if (!afterPut) {
            Type maxType = TypeUtils.maxType(t1, t2);
            int storeOP = maxType.getOpcode(Opcodes.ISTORE);
            methodInstructionList.add(new VarInsnNode(storeOP, assignIndex));
            afterPut = false;
        }

        if (afterConstructorCall) {
            int loadOP = t2.getOpcode(Opcodes.ILOAD);
            methodInstructionList.add(new VarInsnNode(loadOP, assignIndex));
            maxStack++;
            afterConstructorCall = false;
        }
    }

    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type type = ASTUtils.getSafeType(node.getExpression());
        LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
        int index = pool.getLocalIndex(type);

        if (!afterPut) {
            int storeOP = type.getOpcode(Opcodes.ISTORE);
            methodInstructionList.add(new VarInsnNode(storeOP, index));
            afterPut = false;
        }

        FieldInsnNode fieldInsnNode =
                new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodInstructionList.add(fieldInsnNode);

        int loadOP = type.getOpcode(Opcodes.ILOAD);
        methodInstructionList.add(new VarInsnNode(loadOP, index));
        maxStack++;

        String methodType = Type.getMethodType(Type.VOID_TYPE, type).toString();
        MethodInsnNode methodCall = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", methodType);
        methodInstructionList.add(methodCall);

        pool.freeLocalIndex(index);
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        List<JumpInsnNode> breakList = new ArrayList<JumpInsnNode>();
        List<JumpInsnNode> continueList = new ArrayList<JumpInsnNode>();

        Statement s = null, ps;
        for (Statement statement : node.getStatementList()) {
            ps = s;
            s = statement;
            if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
                LabelNode labelNode = new LabelNode(new Label());
                methodInstructionList.add(labelNode);
                initializeFlags();
                backpatch(ASTUtils.getNextList(ps), labelNode);
            }

            s.accept(this);
            breakList.addAll(ASTUtils.getBreakList(s));
            continueList.addAll(ASTUtils.getContinueList(s));
        }

        if (s != null)
            ASTUtils.setNextList(node, ASTUtils.getNextList(s));

        ASTUtils.setBreakList(node, breakList);
        ASTUtils.setContinueList(node, continueList);
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        Type expr1Type = ASTUtils.getSafeType(node.getExpression1());
        Type expr2Type = ASTUtils.getSafeType(node.getExpression2());

        node.getExpression1().accept(this);
        node.getExpression2().accept(this);

        Type maxType = TypeUtils.maxType(expr1Type, expr2Type);

        // make maxType the lowest Type.
        if (!maxType.equals(expr2Type))
            widen(maxType, expr2Type);

        // cast second from top to max
        if (!maxType.equals(expr1Type)) {
            LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
            int localIndex = -1;
            if (expr2Type.equals(Type.FLOAT_TYPE) || expr1Type.equals(Type.FLOAT_TYPE)) {
                localIndex = pool.getLocalIndex(expr2Type);
                methodInstructionList.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ISTORE), localIndex));
            } else
                methodInstructionList.add(new InsnNode(Opcodes.SWAP));

            widen(maxType, expr1Type);
            if (expr2Type.equals(Type.FLOAT_TYPE) || expr1Type.equals(Type.FLOAT_TYPE)) {
                methodInstructionList.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ILOAD), localIndex));
                maxStack++;
                pool.freeLocalIndex(localIndex, expr2Type);
            } else
                methodInstructionList.add(new InsnNode(Opcodes.SWAP));
        }

        // handle operator, depending on the type of the expression.
        if (ASTUtils.isBooleanExpression(node))
            handleBooleanOperator(node, node.getOperator(), maxType);
        else if (maxType.equals(TypeUtils.STRING_TYPE)) {
            methodInstructionList.add(new InsnNode(Opcodes.SWAP));
            handleStringOperator(node, node.getOperator());
        } else
            handleNumberOperator(node, node.getOperator(), maxType);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type type = ASTUtils.getSafeType(node.getExpression());

        if (node.getOperator().equals(Operator.MINUS))
            methodInstructionList.add(new InsnNode(type.getOpcode(Opcodes.INEG)));
        else if (node.getOperator().equals(Operator.NOT)) {
            // Create jump instruction.
            JumpInsnNode jmp = new JumpInsnNode(Opcodes.IFEQ, null);
            methodInstructionList.add(jmp);

            // Fix true list.
            List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();
            trueList.add(jmp);
            ASTUtils.setTrueList(node, trueList);
        } else
            ASTUtils.error(node, "Operator not recognized!");
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        if (firstVisit)
            firstDot = true;

        // Find opcode.
        int opcode;
        if (firstDot) {
            opcode = staticCall ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
            lastIdentifier = node.getIdentifier();
            firstDot = false;
            firstVisit = false;
            afterPut = true;
        } else
            opcode = staticCall ? Opcodes.GETSTATIC : Opcodes.GETFIELD;

        // Find expression type.
        Type exprType = ASTUtils.getSafeType(node.getExpression());

        // Find Symbol Table Entry of the expression.
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node.getExpression());
        SymTableEntry s = st.lookup(node.getIdentifier() + "var");

        // Find var type.
        Type varType;
        if (s != null)
            varType = s.getType();
        else {
            SymTable<SymTableEntry> classSt = Registry.getInstance().getClasses().get(exprType);
            SymTableEntry classEntry = classSt.lookup(node.getIdentifier() + "var");
            varType = classEntry.getType();
        }

        String filteredExprType = fullyFilter(exprType.toString());

        node.getExpression().accept(this);

        // If this is the last identifier of the dots change the opcode.
        if (node.getIdentifier().equals(lastIdentifier)) {
            opcode = staticCall ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
            firstDot = true;
            firstVisit = true;
            afterPut = true;
        }

        String finalVarType = huaFilter(varType.toString());

        if (interrupted && opcode != Opcodes.GETFIELD && opcode != Opcodes.GETSTATIC) {
            methodInstructionList.add(new LdcInsnNode(literal));
            interrupted = false;
        }

        methodInstructionList.add(new FieldInsnNode(opcode, filteredExprType, node.getIdentifier(), finalVarType));
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode jumpInsnNode = new JumpInsnNode(Opcodes.GOTO, null);
            methodInstructionList.add(jumpInsnNode);
            if (node.getLiteral() != 0)
                ASTUtils.getTrueList(node).add(jumpInsnNode);
            else
                ASTUtils.getFalseList(node).add(jumpInsnNode);

        } else {
            @SuppressWarnings("BoxingBoxedValue") Float f = Float.valueOf(node.getLiteral());
            methodInstructionList.add(new LdcInsnNode(f));
        }
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode jumpInsnNode = new JumpInsnNode(Opcodes.GOTO, null);
            methodInstructionList.add(jumpInsnNode);
            if (node.getLiteral() != 0)
                ASTUtils.getTrueList(node).add(jumpInsnNode);
            else
                ASTUtils.getFalseList(node).add(jumpInsnNode);

        } else {
            @SuppressWarnings("BoxingBoxedValue") Integer i = Integer.valueOf(node.getLiteral());
            methodInstructionList.add(new LdcInsnNode(i));
        }
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        if (firstDot) {
            interrupted = true;
            literal = node.getLiteral();
        } else
            methodInstructionList.add(new LdcInsnNode(node.getLiteral()));
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelNode beginLabelNode = new LabelNode(new Label());
        methodInstructionList.add(beginLabelNode);
        initializeFlags();

        node.getExpression().accept(this);

        LabelNode trueLabelNode = new LabelNode(new Label());

        backpatch(ASTUtils.getTrueList(node.getExpression()), trueLabelNode);

        Statement statement = node.getStatement();
        if (statement != null) {
            statement.accept(this);
            backpatch(ASTUtils.getNextList(statement), beginLabelNode);
            backpatch(ASTUtils.getContinueList(statement), beginLabelNode);
        }

        methodInstructionList.add(new JumpInsnNode(Opcodes.GOTO, beginLabelNode));
        methodInstructionList.add(trueLabelNode);
        initializeFlags();

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));

        if (statement != null)
            ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(statement));
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode labelNode = new LabelNode(new Label());
        backpatch(ASTUtils.getTrueList(node.getExpression()), labelNode);

        if (node.getStatement() != null)
            node.getStatement().accept(this);

        methodInstructionList.add(labelNode);
        initializeFlags();

        if (node.getStatement() != null) {
            ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
            ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement()));
        }

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));

        if (node.getStatement() != null)
            ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode labelNode1 = new LabelNode(new Label());
        LabelNode labelNode2 = new LabelNode(new Label());

        if (node.getStatement1() != null)
            node.getStatement1().accept(this);

        List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
        JumpInsnNode jmp1 = new JumpInsnNode(Opcodes.GOTO, null);
        methodInstructionList.add(jmp1);
        falseList.add(jmp1);
        ASTUtils.setFalseList(node.getExpression(), falseList);

        methodInstructionList.add(labelNode1);
        initializeFlags();

        if (node.getStatement2() != null)
            node.getStatement2().accept(this);

        methodInstructionList.add(labelNode2);
        initializeFlags();

        backpatch(ASTUtils.getTrueList(node.getExpression()), labelNode1);
        backpatch(ASTUtils.getFalseList(node.getExpression()), labelNode2);

        if (node.getStatement1() != null) {
            ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement1()));
            ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement1()));
            ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement1()));
        }
        if (node.getStatement2() != null) {
            ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement2()));
            ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement2()));
            ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement2()));
        }

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        methodInstructionList.add(jmp);
        ASTUtils.getBreakList(node).add(jmp);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        methodInstructionList.add(jmp);
        ASTUtils.getContinueList(node).add(jmp);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        // visit the expression.
        node.getExpression().accept(this);

        // Find the expression's type.
        Type exprType = ASTUtils.getSafeType(node.getExpression());
        // Find the max type between the function's and the expression's return type.
        Type maxType = TypeUtils.maxType(exprType, functionReturnType);

        // If the expression's type is lower than the function's,
        // cast the top of the stack(the expression) to the function's return type.
        if (!maxType.equals(exprType))
            widen(maxType, exprType);

        // find opcode.
        int opcode = maxType.getOpcode(Opcodes.IRETURN);

        // add return instruction.
        methodInstructionList.add(new InsnNode(opcode));

        // set returned flag to true.
        returnedFlag = true;
    }

    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        methodInstructionList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        maxStack++;
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        methodInstructionList.add(new InsnNode(Opcodes.ACONST_NULL));
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        staticCall = false;
        node.getExpression().accept(this);

        int opcode = Opcodes.INVOKEVIRTUAL;
        if (staticCall)
            opcode = Opcodes.INVOKESPECIAL;

        // Find expression type
        Type exprType = ASTUtils.getSafeType(node.getExpression());

        // Find method type.
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "function");

        Type methodType;
        //noinspection Duplicates
        if (s != null)
            methodType = s.getType();
        else {
            SymTable<SymTableEntry> classSt = Registry.getInstance().getClasses().get(exprType);
            SymTableEntry classEntry = classSt.lookup(node.getIdentifier() + "function");
            methodType = classEntry.getType();
        }

        for (Expression e : node.getExpressionList())
            e.accept(this);

        String finalExprType = fullyFilter(exprType.toString());

        methodInstructionList.add(new MethodInsnNode
                (opcode, finalExprType, node.getIdentifier(), methodType.toString()));
    }

    @Override
    public void visit(FieldDefinition node) throws ASTVisitorException {
        if (classFields) {
            String filteredType = huaFilter(node.getType().toString());
            FieldNode fn = new FieldNode(Opcodes.ACC_PUBLIC, node.getIdentifier(), filteredType, null, null);
            //noinspection unchecked
            cn.fields.add(fn);
        }
    }

    @Override
    public void visit(StorageSpecifier node) throws ASTVisitorException {
        // nothing here.
    }

    @Override
    public void visit(SimpleFunctionCallExpression node) throws ASTVisitorException {
        staticCall = false;
        node.getExpression().accept(this);

        int opcode = Opcodes.INVOKEVIRTUAL;
        if (staticCall)
            opcode = Opcodes.INVOKESPECIAL;

        // Find expression type
        Type exprType = ASTUtils.getSafeType(node.getExpression());

        // Find method type.
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "function");

        Type methodType;

        //noinspection Duplicates
        if (s != null)
            methodType = s.getType();
        else {
            SymTable<SymTableEntry> classSt = Registry.getInstance().getClasses().get(exprType);
            SymTableEntry classEntry = classSt.lookup(node.getIdentifier() + "function");
            methodType = classEntry.getType();
        }

        String finalExprType = fullyFilter(exprType.toString());

        methodInstructionList.add(new MethodInsnNode
                (opcode, finalExprType, node.getIdentifier(), methodType.toString()));
    }

    @Override
    public void visit(MethodIdentifier node) throws ASTVisitorException {
        // Find method type.
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "function");
        Type methodType = s.getType();

        // load this on stack.
        methodInstructionList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        maxStack++;

        for (Expression e : node.getExpressionList())
            e.accept(this);

        int opcode = s.isStatic() ? Opcodes.INVOKESPECIAL : Opcodes.INVOKEVIRTUAL;

        if (s.isStatic())
            opcode = Opcodes.INVOKESTATIC;

        methodInstructionList.add(new MethodInsnNode(opcode, cn.name, node.getIdentifier(), methodType.toString()));
    }

    @Override
    public void visit(SimpleMethodIdentifier node) throws ASTVisitorException {
        // Find method type.
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry s = st.lookup(node.getIdentifier() + "function");
        Type methodType = s.getType();

        // load this on stack.
        methodInstructionList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        maxStack++;

        int opcode = s.isStatic() ? Opcodes.INVOKESPECIAL : Opcodes.INVOKEVIRTUAL;

        if (s.isStatic())
            opcode = Opcodes.INVOKESTATIC;

        methodInstructionList.add(new MethodInsnNode(opcode, cn.name, node.getIdentifier(), methodType.toString()));
    }

    @Override
    public void visit(SimpleConstructorIdentifier node) throws ASTVisitorException {
        afterConstructorCall = true;

        methodInstructionList.add(new TypeInsnNode(Opcodes.NEW, node.getIdentifier()));

        methodInstructionList.add(new InsnNode(Opcodes.DUP));
        maxStack++;

        methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.getIdentifier(), "<init>", "()V"));
    }

    @Override
    public void visit(ConstructorIdentifier node) throws ASTVisitorException {
        afterConstructorCall = true;

        methodInstructionList.add(new TypeInsnNode(Opcodes.NEW, node.getIdentifier()));

        methodInstructionList.add(new InsnNode(Opcodes.DUP));
        maxStack++;

        String parameterTypes = "";
        for (Expression e : node.getExpressionList()) {
            e.accept(this);
            Type type = ASTUtils.getSafeType(e);
            parameterTypes += type.toString();
        }

        methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.getIdentifier(),
                "<init>", "(" + parameterTypes + ")V"));
    }

    @Override
    public void visit(SimpleIdentifierExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeEnv(node);
        SymTableEntry symTableEntry = st.lookup(node.getIdentifier() + "var");
        assignIndex = symTableEntry.getIndex();

        FieldNode fieldNode;
        for (Object field : cn.fields) {
            fieldNode = (FieldNode) field;
            if (fieldNode.name.equals(node.getIdentifier())) {
                // Load this.
                methodInstructionList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                maxStack++;

                int opcode = staticCall ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
                String filteredType = huaFilter(symTableEntry.getType().toString());
                methodInstructionList.add(new FieldInsnNode(opcode, cn.name, node.getIdentifier(), filteredType));

                if (symTableEntry.isStatic())
                    staticCall = true;
                return;

            }
        }

        if (!afterAssignment && !afterFunctionDefinition) {
            int loadOP = symTableEntry.getType().getOpcode(Opcodes.ILOAD);
            methodInstructionList.add(new VarInsnNode(loadOP, assignIndex));
            maxStack++;
        }

        if (symTableEntry.isStatic())
            staticCall = true;
    }

    @Override
    public void visit(SimpleReturnStatement node) throws ASTVisitorException {
        methodInstructionList.add(new InsnNode(Opcodes.RETURN));
        returnedFlag = true;
    }

    @Override
    public void visit(SimpleStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        // nothing here.
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        classFields = false;
        // initialize the function's maxStack and instruction list.
        maxStack = 0;
        methodInstructionList.clear();

        // If constructor: remove default and set name correspondingly!
        String methodName;
        if (node.getIdentifier().equals(cn.name)) {
            cn.methods.remove(node.getIdentifier());
            methodName = "<init>";
        } else
            methodName = node.getIdentifier();

        // Fix method's access.
        int methodAccess = Opcodes.ACC_PUBLIC;
        if (node.isStatic())
            methodAccess += Opcodes.ACC_STATIC;

        // Find all the parameter types.
        List<Type> parameterTypes = new ArrayList<Type>();
        afterFunctionDefinition = true;
        for (ParameterDeclaration p : node.getParameterDeclarationList()) {
            p.accept(this);
            parameterTypes.add(p.getType());
        }
        afterFunctionDefinition = false;

        // Put the parameter types to an array.
        Type[] a = new Type[parameterTypes.size()];
        int i = 0;
        for (Type t : parameterTypes)
            a[i++] = t;

        // Add the number of the variables to the methods max stack number.
        maxStack += a.length;

        // Find the function's type.
        Type functionType = Type.getMethodType(node.getReturnType(), a);
        functionReturnType = functionType.getReturnType();

        // If main: Set class with main name and fix the function's type.
        if (methodName.equals("main")) {
            functionType = Type.getType("([Ljava/lang/String;)V");
            classWithMain = cn.name;
        }

        // Create method.
        MethodNode methodNode = new MethodNode(methodAccess, methodName, functionType.toString(), null, null);

        if (methodName.equals("<init>"))
            methodInstructionList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));

        returnedFlag = false;

        // Visit Compound Statement.
        if (node.getCompoundStatement() != null)
            node.getCompoundStatement().accept(this);

        Iterator iterator = methodInstructionList.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode instruction = (AbstractInsnNode) iterator.next();
            methodNode.instructions.add(instruction);
        }

        // Return something.Set max locals and stack. Add method to the class.
        if (!returnedFlag)
            methodNode.instructions.add(new InsnNode(Opcodes.RETURN));

        methodNode.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 1;
        methodNode.maxStack = maxStack;

        // noinspection unchecked
        cn.methods.add(methodNode);
    }

    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        cn.access = Opcodes.ACC_PUBLIC;
        cn.version = Opcodes.V1_5;
        cn.name = node.getIdentifier();
        cn.sourceFile = filenameWithoutExtension + ".huava";
        cn.superName = "java/lang/Object";

        // Remove old methods and fields.
        if (cn.methods != null)
            cn.methods.clear();
        if (cn.fields != null)
            cn.fields.clear();

        // create default constructor
        MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;
        //noinspection unchecked
        cn.methods.add(methodNode);

        classFields = true;

        for (FieldOrFunctionDefinition fieldOrFunctionDefinition : node.getFieldOrFunctionDefinitionList())
            fieldOrFunctionDefinition.accept(this);

        generateClass(node.getIdentifier());
    }
}
