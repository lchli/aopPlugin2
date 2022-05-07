package com.lch.aop.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;


public class TraceClassAdapter extends ClassVisitor {

    private String className;
    private String superName;
    private boolean isABSClass = false;
    List<String> collectedIgnoreMethod;

   public TraceClassAdapter(int i, ClassVisitor classVisitor,List<String> collectedIgnoreMethod) {
        super(i, classVisitor);
        this.collectedIgnoreMethod=collectedIgnoreMethod;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
       // System.err.println("className:"+name);
        this.className = name;
        this.superName = superName;
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.isABSClass = true;
        }

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {

//            if (isIgnoreClass(className)) {
//                return super.visitMethod(access, name, desc, signature, exceptions);
//            }
        System.err.println("visitMethod:"+name+ "-" + desc);
        if (isABSClass) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }


        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        return new TraceMethodAdapter(api, methodVisitor, access, name, desc, className);

    }


    class TraceMethodAdapter extends AdviceAdapter {


        private final String classname;

        protected TraceMethodAdapter(int api, MethodVisitor mv, int access, String name, String desc, String classname) {
            super(api, mv, access, name, desc);
            this.classname = classname;

        }

        @Override
        protected void onMethodEnter() {
            if (collectedIgnoreMethod.contains(getName() + "-" + methodDesc)) {
                System.err.println("ignore:"+getName() + "-" + methodDesc);
                return;
            }
            System.err.println("onMethodEnter:"+getName() + "-" + methodDesc);
            mv.visitLdcInsn(0);
            mv.visitMethodInsn(INVOKESTATIC, TraceBuildConstants.MATRIX_TRACE_CLASS, "i", "(I)V", false);
        }


        @Override
        protected void onMethodExit(int opcode) {
            if (collectedIgnoreMethod.contains(getName() + "-" + methodDesc)) {
                return;
            }

            mv.visitLdcInsn(classname);
            mv.visitLdcInsn(getName());
            mv.visitLdcInsn(methodDesc);
            mv.visitMethodInsn(INVOKESTATIC, TraceBuildConstants.MATRIX_TRACE_CLASS, "o", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
        }


    }

}

