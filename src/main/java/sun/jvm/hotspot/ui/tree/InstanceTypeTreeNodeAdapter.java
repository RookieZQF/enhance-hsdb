package sun.jvm.hotspot.ui.tree;

import com.perfma.hotspot.util.KlassUtil;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.FieldIdentifier;
import sun.jvm.hotspot.oops.FieldInfo;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.utilities.IntArray;
import sun.jvm.hotspot.utilities.KlassArray;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-23
 * @description: desc
 */
public class InstanceTypeTreeNodeAdapter extends AbstractFieldTreeNodeAdapter<InstanceKlass> {

    public InstanceTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id) {
        super(a, t, id);
    }

    private InstanceTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        super(a, t, id, treeTableMode);
    }

    @Override
    public FieldTreeNodeAdapter getTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        return new InstanceTypeTreeNodeAdapter(a, t, id, treeTableMode);
    }


    private static final String FIELDS = "_fields";
    private static final String LOCAL_INTERFACES = "_local_interfaces";
    private static final String TRANSITIVE_INTERFACES = "_transitive_interfaces";
    private static final String METHOD_ORDERING = "_method_ordering";
    @Override
    public void extraInfo(String name, InstanceKlass instanceKlass, PrintStream out) {
        if(FIELDS.equals(name)){
            printFields(instanceKlass, out);
        } else if(LOCAL_INTERFACES.equals(name)){
            printKlassArray(instanceKlass.getLocalInterfaces(), out);
        } else if(TRANSITIVE_INTERFACES.equals(name)){
            printKlassArray(instanceKlass.getTransitiveInterfaces(), out);
        } else if(METHOD_ORDERING.equals(name)){
            //enable jvmti 'can_maintain_original_method_order' the length don't for 0
            printIntArray(instanceKlass.getMethodOrdering(), out);
        }
    }

    private void printIntArray(IntArray intArray, PrintStream out) {
        if(intArray != null){
            int length = intArray.getLength();
            out.println("\t" + "array length: " + length);
            if(length > 0) {
                out.print("\t");
                for (int i = 0; i < length; i++) {
                    out.print(intArray.at(i) + " ");
                }
                out.println();
            }
        }
    }

    private void printKlassArray(KlassArray klassArray, PrintStream out) {
        if(klassArray != null){
            int length = klassArray.getLength();
            out.println("\t" + "array length: " + length);
            for(int i = 0; i < length; i ++){
                Klass klass = klassArray.getAt(i);
                out.println("\t" + KlassUtil.getName(klass) + " @ " + klass.getAddress());
            }
        }
    }


    private void printFields(InstanceKlass instanceKlass, PrintStream out){
        if(instanceKlass.getSuper() != null) {
            printFields((InstanceKlass) instanceKlass.getSuper(), out);
        }
        //@Contended -XX:-RestrictContended
        int count = instanceKlass.getJavaFieldsCount();
        out.println("\t" + instanceKlass.getName().asString() +  " fields array length: " + instanceKlass.getFields().length());
        out.println("\t" + instanceKlass.getName().asString() +  " Field count: " + count);
        FieldInfo fieldInfo = new FieldInfo(instanceKlass);
        for(int i = 0; i < count; i ++){
            out.print("\t\t");
            fieldInfo.getFieldAccessFlags(i).printOn(out);
            out.println(" " + fieldInfo.getFieldSignature(i).asString() +
                    " " + fieldInfo.getFieldName(i) + ", initValue=" + fieldInfo.getFieldInitialValue(i)
                    + ", fieldOffset=" + fieldInfo.getFieldOffset(i));

        }
    }

}
