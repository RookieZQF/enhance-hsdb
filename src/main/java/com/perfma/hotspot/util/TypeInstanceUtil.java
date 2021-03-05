package com.perfma.hotspot.util;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.Type;

/**
 * @author: ZQF
 * @date: 2021-02-20
 * @description: desc
 */
public class TypeInstanceUtil {
    public static String getName(Type type, Address address){
        Type method = VM.getVM().lookupType("Method");
        Type instanceKlass = VM.getVM().lookupType("InstanceKlass");
        if(type == method){
            return MethodUtil.getMethodSign(new Method(address));
        }else if(type == instanceKlass){
            return InstanceKlassUtil.getName(new InstanceKlass(address));
        }
        return null;
    }
}
