package com.lch.aop.plugin;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class FindMethodInfoVisitor extends ClassNode {

    private final ClassVisitor nextClassVisitor;

    public FindMethodInfoVisitor(ClassVisitor nextClassVisitor) {
        super(Opcodes.ASM7);
        this.nextClassVisitor = nextClassVisitor;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
//        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
//            this.isABSClass = true;
//        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        Iterator<MethodNode> methodsIter = methods.iterator();

        while (methodsIter.hasNext()) {
            MethodNode methodNode = methodsIter.next();
            if (isEmptyMethod(methodNode) || isSingleMethod(methodNode) || isGetSetMethod(methodNode)) {
                continue;
            }
            AbstractInsnNode callSuperOrThisIns = null;
            if (methodNode.name.equals("<init>")) {
                AbstractInsnNode first = methodNode.instructions.getFirst();
                if (first instanceof MethodInsnNode) {//call super/this
                    MethodInsnNode firstIns = (MethodInsnNode) first;
                    if (firstIns.getOpcode() == Opcodes.INVOKESPECIAL && firstIns.name.equals("<init>")) {
                        callSuperOrThisIns = firstIns;
                    }
                }
            }

            InsnList startlist = new InsnList();
            MethodInsnNode nullCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
                    TraceBuildConstants.MATRIX_TRACE_CLASS, "i",
                    "()V", false);
            startlist.add(nullCheck);
            if (callSuperOrThisIns != null) {
                methodNode.instructions.insert(callSuperOrThisIns, startlist);
            } else {
                methodNode.instructions.insert(startlist);
            }

            ListIterator<AbstractInsnNode> instructionsIter = methodNode.instructions.iterator();
            while (instructionsIter.hasNext()) {
                AbstractInsnNode abstractInsnNode = instructionsIter.next();
                switch (abstractInsnNode.getOpcode()) {
                    case Opcodes.RETURN:
                    case Opcodes.IRETURN:
                    case Opcodes.LRETURN:
                    case Opcodes.FRETURN:
                    case Opcodes.ARETURN:
                    case Opcodes.DRETURN:
                    case Opcodes.ATHROW: {
                        InsnList endlist = new InsnList();
                        MethodInsnNode nullCheck2 = new MethodInsnNode(Opcodes.INVOKESTATIC,
                                TraceBuildConstants.MATRIX_TRACE_CLASS, "o",
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
                        endlist.add(new LdcInsnNode(name));
                        endlist.add(new LdcInsnNode(methodNode.name));
                        endlist.add(new LdcInsnNode(methodNode.desc));
                        endlist.add(nullCheck2);
                        methodNode.instructions.insertBefore(abstractInsnNode, endlist);
                    }
                }
            }


        }


        accept(nextClassVisitor);
    }

//    @Override
//    public MethodVisitor visitMethod(int access, String name, String desc,
//                                     String signature, String[] exceptions) {
//
////        if (isIgnoreClass(className)) {
////            return super.visitMethod(access, name, desc, signature, exceptions);
////        }
//        if (isABSClass) {
//            return super.visitMethod(access, name, desc, signature, exceptions);
//        }
//        return new CollectMethodNode(className, access, name, desc, signature, exceptions);
//
//    }

    private boolean isEmptyMethod(MethodNode methodNode) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode insnNode = iterator.next();
            int opcode = insnNode.getOpcode();
            if (-1 == opcode) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isGetSetMethod(MethodNode methodNode) {
        int ignoreCount = 0;
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode insnNode = iterator.next();
            int opcode = insnNode.getOpcode();
            if (-1 == opcode) {
                continue;
            }
            if (opcode != Opcodes.GETFIELD
                    && opcode != Opcodes.GETSTATIC
                    && opcode != Opcodes.H_GETFIELD
                    && opcode != Opcodes.H_GETSTATIC

                    && opcode != Opcodes.RETURN
                    && opcode != Opcodes.ARETURN
                    && opcode != Opcodes.DRETURN
                    && opcode != Opcodes.FRETURN
                    && opcode != Opcodes.LRETURN
                    && opcode != Opcodes.IRETURN

                    && opcode != Opcodes.PUTFIELD
                    && opcode != Opcodes.PUTSTATIC
                    && opcode != Opcodes.H_PUTFIELD
                    && opcode != Opcodes.H_PUTSTATIC
                    && opcode > Opcodes.SALOAD) {
                if (methodNode.name.equals("<init>") && opcode == Opcodes.INVOKESPECIAL) {
                    ignoreCount++;
                    if (ignoreCount > 1) {
                        return false;
                    }
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private boolean isSingleMethod(MethodNode methodNode) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode insnNode = iterator.next();
            int opcode = insnNode.getOpcode();
            if (-1 == opcode) {
                continue;
            } else if (Opcodes.INVOKEVIRTUAL <= opcode && opcode <= Opcodes.INVOKEDYNAMIC) {
                return false;
            }
        }
        return true;
    }


    private class CollectMethodNode extends MethodNode {
        private String className;
        private boolean isConstructor;


        CollectMethodNode(String className, int access, String name, String desc,
                          String signature, String[] exceptions) {
            super(Opcodes.ASM5, access, name, desc, signature, exceptions);
            this.className = className;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();

            if ("<init>".equals(name)) {
                isConstructor = true;
            }
            // filter simple methods
//            if ((isEmptyMethod() || isGetSetMethod() || isSingleMethod())) {
//                collectedIgnoreMethod.add(name + "-" + desc);
//            }

            /////////
//            AbstractInsnNode enterInsnNode=instructions.getFirst();;
//            if (isConstructor) {
//                AbstractInsnNode  initConstructorInstructionNode = findInitConstructorInstruction();
//                if(initConstructorInstructionNode!=null){
//                    enterInsnNode=initConstructorInstructionNode;
//                }
//            }
            InsnList startlist = new InsnList();
            MethodInsnNode nullCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
                    TraceBuildConstants.MATRIX_TRACE_CLASS, "i",
                    "()V", false);
            startlist.add(nullCheck);


            instructions.insert(startlist);

            addTraceReturn();
        }

        private void addTraceReturn() {

            InsnList il = instructions;

            Iterator<AbstractInsnNode> it = il.iterator();
            while (it.hasNext()) {
                AbstractInsnNode abstractInsnNode = it.next();

                switch (abstractInsnNode.getOpcode()) {
                    case Opcodes.RETURN: {
                        InsnList endlist = new InsnList();
                        MethodInsnNode nullCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
                                TraceBuildConstants.MATRIX_TRACE_CLASS, "o",
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
                        endlist.add(new LdcInsnNode(className));
                        endlist.add(new LdcInsnNode(name));
                        endlist.add(new LdcInsnNode(desc));
                        endlist.add(nullCheck);
                        il.insertBefore(abstractInsnNode, endlist);
                    }
                    break;
                    case Opcodes.IRETURN:
                    case Opcodes.LRETURN:
                    case Opcodes.FRETURN:
                    case Opcodes.ARETURN:
                    case Opcodes.DRETURN: {
                        InsnList endlist = new InsnList();
                        MethodInsnNode nullCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
                                TraceBuildConstants.MATRIX_TRACE_CLASS, "o",
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
                        endlist.add(new LdcInsnNode(className));
                        endlist.add(new LdcInsnNode(name));
                        endlist.add(new LdcInsnNode(desc));
                        endlist.add(nullCheck);
                        il.insertBefore(abstractInsnNode, endlist);
                    }
                }
            }
        }


        AbstractInsnNode findInitConstructorInstruction() {
            int nested = 0;
            for (AbstractInsnNode insnNode = instructions.getFirst(); insnNode != null; insnNode = insnNode
                    .getNext()) {
                if (insnNode instanceof TypeInsnNode) {
                    if (insnNode.getOpcode() == Opcodes.NEW) {
                        // new object().
                        nested++;
                    }
                } else if (insnNode instanceof MethodInsnNode) {
                    final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL && methodInsnNode.name.equals("<init>")) {
                        if (--nested < 0) {
                            // find this() or super().
                            return insnNode.getNext();
                        }
                    }
                }
            }

            return null;
        }

        private boolean isGetSetMethod() {
            int ignoreCount = 0;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insnNode = iterator.next();
                int opcode = insnNode.getOpcode();
                if (-1 == opcode) {
                    continue;
                }
                if (opcode != Opcodes.GETFIELD
                        && opcode != Opcodes.GETSTATIC
                        && opcode != Opcodes.H_GETFIELD
                        && opcode != Opcodes.H_GETSTATIC

                        && opcode != Opcodes.RETURN
                        && opcode != Opcodes.ARETURN
                        && opcode != Opcodes.DRETURN
                        && opcode != Opcodes.FRETURN
                        && opcode != Opcodes.LRETURN
                        && opcode != Opcodes.IRETURN

                        && opcode != Opcodes.PUTFIELD
                        && opcode != Opcodes.PUTSTATIC
                        && opcode != Opcodes.H_PUTFIELD
                        && opcode != Opcodes.H_PUTSTATIC
                        && opcode > Opcodes.SALOAD) {
                    if (isConstructor && opcode == Opcodes.INVOKESPECIAL) {
                        ignoreCount++;
                        if (ignoreCount > 1) {
                            return false;
                        }
                        continue;
                    }
                    return false;
                }
            }
            return true;
        }

        private boolean isSingleMethod() {
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insnNode = iterator.next();
                int opcode = insnNode.getOpcode();
                if (-1 == opcode) {
                    continue;
                } else if (Opcodes.INVOKEVIRTUAL <= opcode && opcode <= Opcodes.INVOKEDYNAMIC) {
                    return false;
                }
            }
            return true;
        }


        private boolean isEmptyMethod() {
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insnNode = iterator.next();
                int opcode = insnNode.getOpcode();
                if (-1 == opcode) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }

    }
}