package sun.jvm.hotspot.ui.tree;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.FieldIdentifier;
import sun.jvm.hotspot.oops.MethodCounters;
import sun.jvm.hotspot.types.Type;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-03-01
 * @description: desc
 */
public class MethodCountersTypeTreeNodeAdapter extends AbstractFieldTreeNodeAdapter<MethodCounters> {
    public MethodCountersTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id) {
        super(a, t, id);
    }

    private MethodCountersTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        super(a, t, id, treeTableMode);
    }

    @Override
    public FieldTreeNodeAdapter getTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        return new MethodCountersTypeTreeNodeAdapter(a, t, id, treeTableMode);
    }

    @Override
    void extraInfo(String name, MethodCounters methodCounters, PrintStream out) {

    }
}
