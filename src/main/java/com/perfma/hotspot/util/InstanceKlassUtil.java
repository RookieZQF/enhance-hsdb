package com.perfma.hotspot.util;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.AccessFlags;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.Type;

import static sun.jvm.hotspot.util.GlobalUtil.alignObjectOffset;
import static sun.jvm.hotspot.util.GlobalUtil.alignObjectSize;

/**
 * @author: ZQF
 * @date: 2021-02-20
 * @description: desc
 */
public class InstanceKlassUtil {
    public static String getName(InstanceKlass instanceKlass){
        return KlassUtil.getName(instanceKlass);
    }

    public static boolean isInstanceKlass(Address a){
        Type instanceKlass = VM.getVM().lookupType("InstanceKlass");
        Type type = VM.getVM().getTypeDataBase().guessTypeForAddress(a);
        return type != null && type == instanceKlass;
    }

    public static long getSize(InstanceKlass instanceKlass, Type type){
        long vtableLen = instanceKlass.getVtableLen();
        long itableLen = instanceKlass.getItableLen();
        AccessFlags flags = instanceKlass.getAccessFlagsObj();
        boolean isInterface = flags.isInterface();
        boolean isAnonymous = false;
        long nonstaticOopMapSize = instanceKlass.getNonstaticOopMapSize();
        return alignObjectSize(alignObjectOffset(type.getSize()/ VM.getVM().getHeapWordSize()) +
                alignObjectOffset(vtableLen) +
                alignObjectOffset(itableLen) +
                ((isInterface || isAnonymous) ?
                        alignObjectOffset(nonstaticOopMapSize) :
                        nonstaticOopMapSize) +
                (isInterface ? 1 : 0) +
                (isAnonymous ? 1 : 0)) * VM.getVM().getHeapWordSize();
    }

    public static Address startOfVtable(InstanceKlass instanceKlass, Type type){
        return instanceKlass.getAddress().addOffsetTo(type.getSize());
    }

    public static Address startOfItable(InstanceKlass instanceKlass, Type type){
        return startOfVtable(instanceKlass, type).addOffsetTo(alignObjectOffset(instanceKlass.getVtableLen()) * VM.getVM().getHeapWordSize());
    }

    public static Address endOfItable(InstanceKlass instanceKlass, Type type){
        return startOfItable(instanceKlass, type).addOffsetTo(instanceKlass.getItableLen() * VM.getVM().getHeapWordSize());
    }

    public static Address startOfNonstaticOopMaps(InstanceKlass instanceKlass, Type type){
        return startOfItable(instanceKlass, type).addOffsetTo(alignObjectOffset(instanceKlass.getItableLen()) * VM.getVM().getHeapWordSize());
    }

    public static Address endOfNonstaticOopMaps(InstanceKlass instanceKlass, Type type){
        long nonstaticOopMapSize = instanceKlass.getNonstaticOopMapSize();
        return startOfNonstaticOopMaps(instanceKlass, type).addOffsetTo(
                nonstaticOopMapSize * VM.getVM().getHeapWordSize());
    }
}
