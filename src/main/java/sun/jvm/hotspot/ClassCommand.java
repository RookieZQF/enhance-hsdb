package sun.jvm.hotspot;

import com.perfma.hotspot.util.InstanceKlassUtil;
import com.perfma.hotspot.util.MethodUtil;
import com.perfma.hotspot.util.PrintUtil;
import com.perfma.hotspot.util.StringUtil;
import sun.jvm.hotspot.CommandProcessor.Command;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.ui.tree.*;
import sun.jvm.hotspot.utilities.MethodArray;
import sun.jvm.hotspot.utilities.SystemDictionaryHelper;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static com.perfma.hotspot.util.PrintUtil.print;


/**
 * @author: ZQF
 * @date: 2021-02-22
 * @description: desc
 */
public class ClassCommand implements EnhanceCommand {

    @Override
    public Command getCommand(CommandProcessor commandProcessor, final PrintStream out,
                              final PrintStream err, final CommandProcessor.DebuggerInterface debugger){
        return commandProcessor.new Command(getName(), getName() + " [address | fullClassName]", false) {

            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() != 1) {
                    this.usage();
                } else {
                    String name = t.nextToken();
                    InstanceKlass instanceKlass;
                    if(StringUtil.isAddress(name)){
                        Address a = VM.getVM().getDebugger().parseAddress(name);
                        if(InstanceKlassUtil.isInstanceKlass(a)){
                            instanceKlass = new InstanceKlass(a);
                        }else{
                            err.println("address @ " + name + " is not InstanceKlass Address");
                            return ;
                        }
                    }else {
                        instanceKlass = SystemDictionaryHelper.findInstanceKlass(name);
                    }
                    if(instanceKlass != null){
                        MethodArray methodArray = instanceKlass.getMethods();
                        int methodLength = methodArray.length();
                        out.println(InstanceKlassUtil.getName(instanceKlass) + " " + instanceKlass.getAddress()
                                + " method size: " + methodLength);
                        Type type = VM.getVM().getTypeDataBase().guessTypeForAddress(instanceKlass.getAddress());
                        out.println("Type is " + type.getName() + " (size of " + type.getSize() + ")");
                        out.println("TotalSize is " + InstanceKlassUtil.getSize(instanceKlass, type));
                        InstanceTypeTreeNodeAdapter node = new InstanceTypeTreeNodeAdapter(instanceKlass.getAddress(), type, null);
                        printNode(node, instanceKlass, type);
                        for(int i = 0; i < methodLength; i ++){
                            Method method = methodArray.at(i);
                            out.println(MethodUtil.getMethodSign(method) + " @ " + method.getAddress());
                        }
                        return ;
                    }

                    err.println("Couldn't find class " + name);
                }
            }

            void printNode(InstanceTypeTreeNodeAdapter node, InstanceKlass instanceKlass, Type type) {
                PrintUtil.printNode(node, instanceKlass, out);
                int heapWordSize = VM.getVM().getHeapWordSize();
                long addressSize = VM.getVM().getTypeDataBase().getAddressSize();
                Address begin = instanceKlass.getAddress();
                {
                    long vtableLen = instanceKlass.getVtableLen();
                    Address vtableAddress = InstanceKlassUtil.startOfVtable(instanceKlass, type);
                    out.println("----vtable start @ " + vtableAddress + ", size=" + vtableLen * heapWordSize + ", offset=" + type.getSize());
                    for (int i = 0; i < vtableLen; i++) {
                        printMethod(begin, vtableAddress, i, out);
                    }
                    out.println("----vtable end----");
                }

                {

                    long itableLen = instanceKlass.getItableLen();
                    Address itableAddress = InstanceKlassUtil.startOfItable(instanceKlass, type);
                    out.println("----itable start @ " + itableAddress + ", size=" + itableLen * heapWordSize + ", offset=" + itableAddress.minus(instanceKlass.getAddress()));
                    if(itableLen != 0) {
                        ITableOffsetEntry iTableOffsetEntry = new ITableOffsetEntry(itableAddress);
                        if (iTableOffsetEntry.getInterface() != null) {
                            Address methodEntry = instanceKlass.getAddress().addOffsetTo(iTableOffsetEntry.getOffset());
                            Address end = InstanceKlassUtil.endOfItable(instanceKlass, type);
                            List<ITableOffsetEntry> list = new ArrayList<>();
                            list.add(iTableOffsetEntry);
                            int index = 0;
                            long sizeOffsetTable = methodEntry.minus(itableAddress) / ITableOffsetEntry.getSize();
                            for (int i = 1; i < sizeOffsetTable; i++) {
                                list.add(new ITableOffsetEntry(itableAddress.addOffsetTo(i * ITableOffsetEntry.getSize())));
                            }
                            for (ITableOffsetEntry entry : list) {
                                entry.printf(out, begin);
                            }
                            long sizeMethodTable = end.minus(methodEntry) / VM.getVM().getTypeDataBase().getAddressSize();
                            for (int i = 0; i < sizeMethodTable; i++) {
                                Address address = methodEntry.addOffsetTo(i * addressSize);
                                if (index != sizeOffsetTable - 2 && address.greaterThanOrEqual(begin.addOffsetTo(list.get(index + 1).getOffset()))) {
                                    index++;
                                }
                                printMethod(begin, methodEntry, i, out, "interface=" + list.get(index).getInterfaceName());
                            }
                        } else {
                            iTableOffsetEntry.printf(out, begin);
                        }
                    }
                    out.println("----itable end----");
                }

                {
                    long nonstaticOopMapSize = instanceKlass.getNonstaticOopMapSize();
                    Address nonstaticOopMapsAddress = InstanceKlassUtil.startOfNonstaticOopMaps(instanceKlass, type);
                    out.println("----nonstatic_oop_maps start @ " + nonstaticOopMapsAddress + ", size=" + nonstaticOopMapSize * heapWordSize +
                            ", offset=" + nonstaticOopMapsAddress.minus(begin));
                    for(int i = 0; i < nonstaticOopMapSize; i ++){
                        OopMapBlock oopMapBlock = new OopMapBlock(nonstaticOopMapsAddress.addOffsetTo(i * OopMapBlock.getSize()));
                        oopMapBlock.printf(out, begin);
                    }
                    out.println("----nonstatic_oop_maps end----");
                }

                {
                    boolean anInterface = instanceKlass.getAccessFlagsObj().isInterface();
                    if(anInterface) {
                        Address implementorAddr = InstanceKlassUtil.endOfNonstaticOopMaps(instanceKlass, type);
                        out.println("----implementor @ " + implementorAddr + ", size=" + heapWordSize +
                                ", offset=" + implementorAddr.minus(begin));
                        Address addressAt = implementorAddr.getAddressAt(0);
                        if(addressAt == null){
                            out.println("no implementor");
                        }else if(addressAt.equals(begin)){
                            out.println("more than one implementor");
                        }else{
                            out.println("one implementor is:" + new InstanceKlass(addressAt).getName().asString());
                        }
                    }
                }

                {
                    out.println("----Anonymous TODO----");
                }
            }
        };
    }

    private void printMethod(Address begin, Address curr, int offsetWordSize, PrintStream out){
        printMethod(begin, curr, offsetWordSize, out, "");
    }

    private void printMethod(Address begin, Address curr, int offsetWordSize, PrintStream out, String extra){
        long addressSize = VM.getVM().getTypeDataBase().getAddressSize();
        Address address = curr.addOffsetTo(offsetWordSize * addressSize);
        Address a = address.getAddressAt(0);
        if(a != null) {
            Method method = new Method(a);
            print(out, "", MethodUtil.getMethodSign(method), a, 8, address.minus(begin), extra);
        }else{
            print(out, "", null, null, 8, address.minus(begin), extra);
        }
    }

    @Override
    public String getName(){
        return "classdetail";
    }
}
