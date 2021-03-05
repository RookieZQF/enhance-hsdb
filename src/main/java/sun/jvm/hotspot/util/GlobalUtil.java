package sun.jvm.hotspot.util;

import sun.jvm.hotspot.runtime.VM;

/**
 * @author: ZQF
 * @date: 2021-02-24
 * @description: desc
 */
public class GlobalUtil {
    public static long alignObjectOffset(long offset) {
        return VM.getVM().alignUp(offset, VM.getVM().getBytesPerLong() / VM.getVM().getHeapWordSize());
    }

    public static long alignObjectSize(long size) {
        return VM.getVM().alignUp(size, VM.getVM().getMinObjAlignmentInBytes() / VM.getVM().getHeapWordSize());
    }
}
