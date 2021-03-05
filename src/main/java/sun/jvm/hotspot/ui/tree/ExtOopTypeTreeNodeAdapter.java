package sun.jvm.hotspot.ui.tree;

import sun.jvm.hotspot.oops.FieldIdentifier;
import sun.jvm.hotspot.oops.Oop;

/**
 * @author: ZQF
 * @date: 2021-03-03
 * @description: desc
 */
public class ExtOopTypeTreeNodeAdapter extends OopTreeNodeAdapter {

    public ExtOopTypeTreeNodeAdapter(Oop oop, FieldIdentifier id) {
        super(oop, id);
    }

    public ExtOopTypeTreeNodeAdapter(Oop oop, FieldIdentifier id, boolean treeTableMode) {
        super(oop, id, treeTableMode);
    }

    @Override
    public SimpleTreeNode getChild(int index) {
        Fetcher f = new Fetcher(index);
        getOop().iterate(f, false);
        SimpleTreeNode child = f.getChild();
        if(child instanceof FieldTreeNodeAdapter){
            return new GeneralTreeNodeAdapter((FieldTreeNodeAdapter)child);
        }
        return child;
    }

    @Override
    public int getChildCount() {
        OopTreeNodeAdapter.Counter c = new OopTreeNodeAdapter.Counter();
        getOop().iterate(c, false);
        return c.getNumFields();
    }
}
