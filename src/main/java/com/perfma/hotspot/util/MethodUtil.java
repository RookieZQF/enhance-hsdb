package com.perfma.hotspot.util;

import sun.jvm.hotspot.oops.AccessFlags;
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.oops.Symbol;
import sun.jvm.hotspot.runtime.SignatureConverter;

/**
 * @author: ZQF
 * @date: 2021-02-20
 * @description: desc
 */
public class MethodUtil {
    public static String getMethodSign(Method method){
        StringBuffer buf = new StringBuffer();
        buf.append(genMethodModifierString(method.getAccessFlagsObj()));
        Symbol sig = method.getSignature();
        new SignatureConverter(sig, buf).iterateReturntype();
        buf.append(" ");
        String methodName = method.getName().asString();
        buf.append(methodName);
        buf.append('(');
        new SignatureConverter(sig, buf).iterateParameters();
        buf.append(')');
        // is it generic?
        Symbol genSig = method.getGenericSignature();
        if (genSig != null) {
            buf.append(" [signature ");
            buf.append(genSig.asString());
            buf.append("] ");
        }
        return buf.toString().replace('/', '.');
    }

    public static String genMethodModifierString(AccessFlags acc) {
        StringBuilder buf = new StringBuilder();
        if (acc.isPrivate()) {
            buf.append("private ");
        } else if (acc.isProtected()) {
            buf.append("protected ");
        } else if (acc.isPublic()) {
            buf.append("public ");
        }

        if (acc.isStatic()) {
            buf.append("static ");
        } else if (acc.isAbstract() ) {
            buf.append("abstract ");
        } else if (acc.isFinal()) {
            buf.append("final ");
        }

        if (acc.isNative()) {
            buf.append("native ");
        }

        if (acc.isStrict()) {
            buf.append("strict ");
        }

        if (acc.isSynchronized()) {
            buf.append("synchronized ");
        }

        // javac generated flags
        if (acc.isBridge()) {
            buf.append("[bridge] ");
        }

        if (acc.isSynthetic()) {
            buf.append("[synthetic] ");
        }

        if (acc.isVarArgs()) {
            buf.append("[varargs] ");
        }

        return buf.toString();
    }
}
