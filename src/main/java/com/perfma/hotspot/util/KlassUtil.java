package com.perfma.hotspot.util;

import sun.jvm.hotspot.oops.AccessFlags;
import sun.jvm.hotspot.oops.Klass;

/**
 * @author: ZQF
 * @date: 2021-03-01
 * @description: desc
 */
public class KlassUtil {
    public static String getName(Klass klass){
        return getAccess(klass.getAccessFlagsObj()) + klass.getName().asString();
    }

    private static String getAccess(AccessFlags accessFlags){
        StringBuilder builder = new StringBuilder();
        if (accessFlags.isPublic()) {
            builder.append("public ");
        }

        if (accessFlags.isPrivate()) {
            builder.append("private ");
        }

        if (accessFlags.isProtected()) {
            builder.append("protected ");
        }

        if (accessFlags.isStatic()) {
            builder.append("static ");
        }

        if (accessFlags.isFinal()) {
            builder.append("final ");
        }

        if (accessFlags.isSynchronized()) {
            builder.append("synchronized ");
        }

        if (accessFlags.isVolatile()) {
            builder.append("volatile ");
        }

        if (accessFlags.isBridge()) {
            builder.append("bridge ");
        }

        if (accessFlags.isTransient()) {
            builder.append("transient ");
        }

        if (accessFlags.isVarArgs()) {
            builder.append("varargs ");
        }

        if (accessFlags.isNative()) {
            builder.append("native ");
        }

        if (accessFlags.isEnum()) {
            builder.append("enum ");
        }

        if (accessFlags.isInterface()) {
            builder.append("interface ");
        }

        if (accessFlags.isAbstract()) {
            builder.append("abstract ");
        }

        if (accessFlags.isStrict()) {
            builder.append("strict ");
        }

        if (accessFlags.isSynthetic()) {
            builder.append("synthetic ");
        }
        return builder.toString();
    }
}
