package sun.jvm.hotspot.ui.tree;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.FieldIdentifier;
import sun.jvm.hotspot.oops.MethodData;
import sun.jvm.hotspot.types.Type;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-03-01
 * @description: desc
 */
public class MethodDataTypeTreeNodeAdapter extends AbstractFieldTreeNodeAdapter<MethodData> {
    public MethodDataTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id) {
        super(a, t, id);
    }

    private MethodDataTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        super(a, t, id, treeTableMode);
    }

    @Override
    public FieldTreeNodeAdapter getTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        return new MethodDataTypeTreeNodeAdapter(a, t, id, treeTableMode);
    }

    @Override
    void extraInfo(String name, MethodData methodData, PrintStream out) {

    }
}
