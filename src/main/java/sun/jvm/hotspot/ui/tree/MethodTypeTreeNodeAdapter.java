package sun.jvm.hotspot.ui.tree;

import com.perfma.hotspot.util.PrintUtil;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.oops.em.VtableIndexFlag;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.AddressField;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.utilities.Assert;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-03-01
 * @description: desc
 */
public class MethodTypeTreeNodeAdapter extends AbstractFieldTreeNodeAdapter<Method> {


    public MethodTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id) {
        super(a, t, id);
    }

    private MethodTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        super(a, t, id, treeTableMode);
    }

    @Override
    public FieldTreeNodeAdapter getTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        return new MethodTypeTreeNodeAdapter(a, t, id, treeTableMode);
    }

    private static final String VTABLE_INDEX = "_vtable_index";
    private static final String CONST_METHOD = "_constMethod";
    private static final String METHOD_DATA = "_method_data";
    private static final String METHOD_COUNTERS = "_method_counters";

    @Override
    public void extraInfo(String name, Method method, PrintStream out) {
        if(VTABLE_INDEX.equals(name)){
            printVtableIndex(method.getVtableIndex(), out);
        } else if(CONST_METHOD.equals(name)){
            printConstMethod(method, out);
        } else if(METHOD_DATA.equals(name)){
            printMethodData(method, out);
        } else if(METHOD_COUNTERS.equals(name)){
            printMethodCounters(method, out);
        }
    }

    private void printMethodCounters(Method method, PrintStream out) {
        MethodCounters methodCounters = method.getMethodCounters();
        if(methodCounters != null) {
            Type type = VM.getVM().lookupType("MethodCounters");
            MethodCountersTypeTreeNodeAdapter adapter = new MethodCountersTypeTreeNodeAdapter(methodCounters.getAddress(), type, null);
            PrintUtil.printNode(1, adapter, methodCounters, out);
        }
    }

    private void printMethodData(Method method, PrintStream out) {
        Type type = VM.getVM().lookupType("Method");
        Type methodDataType = VM.getVM().lookupType("MethodData");
        AddressField methodData = type.getAddressField("_method_data");
        Address address = methodData.getValue(method.getAddress());
        if(address != null) {
            MethodDataTypeTreeNodeAdapter adapter = new MethodDataTypeTreeNodeAdapter(address,
                    methodDataType, null);
            PrintUtil.printNode(1, adapter, null, out);
        }
    }

    private void printConstMethod(Method method, PrintStream out) {
        ConstMethod constMethod = method.getConstMethod();
        if(constMethod != null) {
            Type type = VM.getVM().lookupType("ConstMethod");
            ConstMethodTypeTreeNodeAdapter adapter = new ConstMethodTypeTreeNodeAdapter(constMethod.getAddress(), type, null);
            PrintUtil.printNode(1, adapter, constMethod, out);
        }
    }


    private void printVtableIndex(long vtableIndex, PrintStream out) {
        out.print("\tnote: ");
        if(isValid(vtableIndex)){
            if(isItable(vtableIndex)){
                out.println("itable index: " + getItable(vtableIndex));
            }else{
                out.println("vtable index: " + getVtable(vtableIndex));
            }
        }else{
            out.println("Not required vtable/itable");
        }
    }

    private boolean isValid(long index){
        return isItable(index) || isVtable(index);
    }

    private boolean isItable(long index){
        return index <= VtableIndexFlag.itable_index_max.getValue();
    }

    private long getItable(long index){
        Assert.that(isItable(index), "The index is not itable index");
        return VtableIndexFlag.itable_index_max.getValue() - index;
    }

    private boolean isVtable(long index){
        return index >= 0;
    }

    private long getVtable(long index){
        Assert.that(isVtable(index), "The index is not vtable index");
        return index;
    }
}
