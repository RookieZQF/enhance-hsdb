package com.perfma.hotspot.proxy;

import com.perfma.hotspot.enhance.CommandProcessorConstructorInterceptor;
import com.perfma.xbox.asm.api.InstrumentClass;
import com.perfma.xbox.asm.api.InstrumentMethod;
import com.perfma.xbox.sdk.util.AsmUtil;
import com.perfma.xbox.sdk.util.ClassUtil;
import sun.jvm.hotspot.CLHSDB;
import sun.jvm.hotspot.CommandProcessor;

import java.util.List;

/**
 * @author: ZQF
 * @date: 2021-02-22
 * @description: desc
 */
public class CLHSDBProxy {

    public static void run(String[] args){
        enhance();
        CLHSDB.main(args);
    }

    private static void enhance(){
        byte[] originalByteCode = AsmUtil.getOriginalByteCode(CommandProcessor.class);
        InstrumentClass instrumentClass = AsmUtil.getInstrumentClass(originalByteCode);
        List<InstrumentMethod> allConstructor = instrumentClass.getAllConstructor();
        for(InstrumentMethod method : allConstructor){
            method.addAroundInterceptor(CommandProcessorConstructorInterceptor.class);
        }
        byte[] bytes = instrumentClass.toBytecode();
        ClassUtil.redefine(CommandProcessor.class, bytes);
    }
}
