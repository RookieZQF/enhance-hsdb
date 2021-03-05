package sun.jvm.hotspot.oops;

import com.perfma.hotspot.util.PrintUtil;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.runtime.VM;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-25
 * @description: desc
 */
public class OopMapBlock {
    private static long offsetOffset;
    private static long countOffset;

    private Address addr;

    static {
        offsetOffset = 0;
        countOffset = 4;
    }

    public OopMapBlock(Address addr){
        this.addr = addr;
    }

    public static int getSize(){
        return VM.getVM().getHeapWordSize();
    }

    public int getOffset(){
        return addr.getJIntAt(offsetOffset);
    }

    public int getCount(){
        return (int)addr.getCIntegerAt(countOffset, 4, true);
    }

    public void printf(PrintStream out, Address begin){
        PrintUtil.print(out, "", "int OopMapBlock::_offset: " + getOffset(),
                null, 4, addr.minus(begin), "");
        PrintUtil.print(out, "", "uint OopMapBlock::_count: " + getCount(),
                null, 4, addr.minus(begin) + countOffset, "");
    }
}
