package com.perfma.hotspot.util;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.Symbol;
import sun.jvm.hotspot.ui.tree.*;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-24
 * @description: desc
 */
public class PrintUtil {
    public static void print(PrintStream out, String prefix, String body, Address address, long size, long offset, String extra){
        out.println(prefix + body + (address != null ? " @ " + address : "" ) + ", size=" + size + ", offset=" + offset +
                (StringUtil.isEmpty(extra) ? "" : ", " + extra));
    }

    public static <T> void printNode(AbstractFieldTreeNodeAdapter<T> node, T t, PrintStream out){
        printNode(0, node, t, out);
    }

    public static <T> void printNode(int tab, AbstractFieldTreeNodeAdapter<T> node, T t, PrintStream out){
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            for(int j = 0; j < tab; j ++){
                out.print("\t");
            }
            try {
                GeneralTreeNodeAdapter field = node.getChild(i);
                FieldTreeNodeAdapter proxy = field.getProxy();
                printSimpleTreeNode(field, out);
                node.extraInfo(proxy, t, out);
            } catch (Exception e) {
                out.println();
                out.println("Error: " + e);
                e.printStackTrace(out);
            }
        }
    }

    public static void printNode(SimpleTreeNode node, PrintStream out){
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            try {
                SimpleTreeNode field = node.getChild(i);
                printSimpleTreeNode(field, out);
            } catch (Exception e) {
                out.println();
                out.println("Error: " + e);
                e.printStackTrace(out);
            }
        }
    }

    private static void printSimpleTreeNode(SimpleTreeNode field, PrintStream out){
        if (field instanceof OopTreeNodeAdapter) {
            out.print(field);
            out.print(" ");
            printOopValue(((OopTreeNodeAdapter)field).getOop(), out);
            out.println();
        } else {
            out.println(field);
        }
    }

    private static void printOopValue(Oop oop, PrintStream out) {
        if (oop != null) {
            Klass k = oop.getKlass();
            Symbol s = k.getName();
            if (s != null) {
                out.print("Oop for " + s.asString());
            }
        }

    }
}
