package sun.jvm.hotspot.ui.tree;

import sun.jvm.hotspot.types.Field;

/**
 * @author: ZQF
 * @date: 2021-02-23
 * @description: desc
 */
public class GeneralTreeNodeAdapter implements SimpleTreeNode {

    private FieldTreeNodeAdapter proxy;

    GeneralTreeNodeAdapter(FieldTreeNodeAdapter proxy){
        this.proxy = proxy;
    }

    @Override
    public int getChildCount() {
        return proxy.getChildCount();
    }

    @Override
    public SimpleTreeNode getChild(int i) {
        return proxy.getChild(i);
    }

    @Override
    public boolean isLeaf() {
        return proxy.isLeaf();
    }

    @Override
    public int getIndexOfChild(SimpleTreeNode simpleTreeNode) {
        return proxy.getIndexOfChild(simpleTreeNode);
    }

    @Override
    public String getName() {
        return proxy.getName();
    }

    @Override
    public String getValue() {
        String res = proxy.getValue();
        if(proxy.getID() == null || !(proxy.getID() instanceof CTypeTreeNodeAdapter.CTypeFieldIdentifier)){
            return res;
        }
        Field field = ((CTypeTreeNodeAdapter.CTypeFieldIdentifier) proxy.getID()).getField();
        return res + ", size=" + field.getType().getSize() + ", offset=" + field.getOffset();
    }

    @Override
    public String toString() {
        if (proxy.getTreeTableMode()) {
            return this.getName();
        } else {
            return proxy.getID() != null ? this.getName() + ": " + this.getValue() : this.getValue();
        }
    }

    public FieldTreeNodeAdapter getProxy() {
        return proxy;
    }
}
