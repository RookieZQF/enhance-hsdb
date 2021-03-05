package sun.jvm.hotspot.oops;

import com.perfma.hotspot.util.PrintUtil;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.runtime.VM;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-24
 * @description: desc
 */
public class ITableOffsetEntry {

    private static long interfaceOffset;
    private static long offsetOffset;

    private Address addr;

    static {
        interfaceOffset = 0;
        offsetOffset = 8;
    }

    public ITableOffsetEntry(Address addr){
        this.addr = addr;
    }

    public static long getSize(){
        return 2 * VM.getVM().getHeapWordSize();
    }

    public Klass getInterface(){
        Address address = addr.getAddressAt(interfaceOffset);
        if(address == null){
            return null;
        }
        return new Klass(address);
    }

    public int getOffset(){
        return addr.getJIntAt(offsetOffset);
    }

    public String getInterfaceName(){
        Klass anInterface = getInterface();
        return anInterface != null ? anInterface.getName().asString() : "";
    }

    public void printf(PrintStream out, Address begin){
        PrintUtil.print(out, "", "Klass* itableOffsetEntry::_interface: " + getInterfaceName(),
                addr.getAddressAt(interfaceOffset), 8, addr.minus(begin), "");
        PrintUtil.print(out, "", "int itableOffsetEntry::_offset: " + getOffset(),
                null, 4, addr.minus(begin) + offsetOffset, "");
    }
}
