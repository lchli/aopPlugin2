package com.lch.aop.plugin;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

import javax.annotation.Nullable;

import kotlin.jvm.functions.Function3;


public class FindMethodInfoVisitor extends ClassNode {

    private final ClassVisitor nextClassVisitor;
    @Nullable
    private ApmParams params;

    public FindMethodInfoVisitor(ClassVisitor nextClassVisitor, @Nullable ApmParams params) {
        super(Opcodes.ASM7);
        this.nextClassVisitor = nextClassVisitor;
        this.params = params;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (isAnnotationClass()) {
            accept(nextClassVisitor);
            return;
        }

        for (MethodNode methodNode : methods) {
            if (isEmptyMethod(methodNode) ||
                    isSingleMethod(methodNode) ||
                    isGetSetMethod(methodNode) ||
                    isAbstractMethod(methodNode) ||
                    isNativeMethod(methodNode) ||
                    isIgnoredMethodByUser(methodNode)) {
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

    private boolean isAnnotationClass() {
        return (access & Opcodes.ACC_ANNOTATION) > 0;
    }

    private boolean isIgnoredMethodByUser(MethodNode methodNode) {
        if (params == null) {
            return false;
        }
        Function3<String, String, String, Boolean> isAopMethod = params.isAopMethod;
        if (isAopMethod == null) {
            return false;
        }

        return isAopMethod.invoke(name.replaceAll("/", "."),
                methodNode.name, methodNode.desc);
    }

    private boolean isNativeMethod(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_NATIVE) > 0;
    }

    private boolean isAbstractMethod(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_ABSTRACT) > 0;
    }

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


}