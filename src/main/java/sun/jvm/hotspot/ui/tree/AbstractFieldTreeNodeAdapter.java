package sun.jvm.hotspot.ui.tree;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.AddressException;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.oops.FieldIdentifier;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.UnknownOopException;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.CStringUtilities;

import java.io.PrintStream;
import java.util.*;

/**
 * @author: ZQF
 * @date: 2021-03-01
 * @description: desc
 */
public abstract class AbstractFieldTreeNodeAdapter<T> extends FieldTreeNodeAdapter implements ExtraNode<T> {

    private Address addr;
    private Type type;

    private List<CTypeTreeNodeAdapter.CTypeFieldIdentifier> fieldList = new ArrayList<>();

    private CTypeTreeNodeAdapter.CTypeFieldIdentifier[] fields;


    AbstractFieldTreeNodeAdapter(Address a, Type t, FieldIdentifier id) {
        super(id, false);
        this.addr = a;
        this.type = t;
    }

    AbstractFieldTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode) {
        super(id, treeTableMode);
        this.addr = a;
        this.type = t;
    }


    private static final String ADDRESS = "address";

    private FieldTreeNodeAdapter getChild(CTypeTreeNodeAdapter.CTypeFieldIdentifier cf){
        Field f = cf.getField();
        Type t = f.getType();
        try {
            if (t.isOopType()) {
                OopHandle handle;
                if (f.isStatic()) {
                    handle = f.getOopHandle();
                } else {
                    handle = f.getOopHandle(addr);
                }
                try {
                    Oop oop = VM.getVM().getObjectHeap().newOop(handle);
                    return new OopTreeNodeAdapter(oop, cf, getTreeTableMode());
                } catch (AddressException | UnknownOopException e) {
                    return new BadAddressTreeNodeAdapter(handle,
                            new CTypeTreeNodeAdapter.CTypeFieldIdentifier(type, f),
                            getTreeTableMode());
                }
            } else if (t.isCIntegerType()) {
                long value;
                if (f.isStatic()) {
                    value = f.getCInteger((CIntegerType)t);
                } else {
                    value = f.getCInteger(addr, (CIntegerType)t);
                }
                return new LongTreeNodeAdapter(value, cf, getTreeTableMode());
            } else if (t.isJavaPrimitiveType()) {
                boolean isStatic = f.isStatic();
                if (f instanceof JByteField) {
                    long value = isStatic? f.getJByte() : f.getJByte(addr);
                    return new LongTreeNodeAdapter(value, cf, getTreeTableMode());
                } else if (f instanceof JShortField) {
                    long value = isStatic? f.getJShort() : f.getJShort(addr);
                    return new LongTreeNodeAdapter(value, cf, getTreeTableMode());
                } else if (f instanceof JIntField) {
                    long value = isStatic? f.getJInt() : f.getJInt(addr);
                    return new LongTreeNodeAdapter(value, cf, getTreeTableMode());
                } else if (f instanceof JLongField) {
                    long value = isStatic? f.getJLong() : f.getJLong(addr);
                    return new LongTreeNodeAdapter(value, cf, getTreeTableMode());
                } else if (f instanceof JCharField) {
                    char value = isStatic? f.getJChar() : f.getJChar(addr);
                    return new CharTreeNodeAdapter(value, cf, getTreeTableMode());
                } else if (f instanceof JBooleanField) {
                    boolean value = isStatic? f.getJBoolean() : f.getJBoolean(addr);
                    return new BooleanTreeNodeAdapter(value, cf, getTreeTableMode());
                } else if (f instanceof JFloatField) {
                    float value = isStatic? f.getJFloat() : f.getJFloat(addr);
                    return new DoubleTreeNodeAdapter(value, cf, getTreeTableMode());
                } else if (f instanceof JDoubleField) {
                    double value = isStatic? f.getJDouble() : f.getJDouble(addr);
                    return new DoubleTreeNodeAdapter(value, cf, getTreeTableMode());
                } else {
                    throw new RuntimeException("unhandled type: " + t.getName());
                }
            } else if (t.isPointerType()) {
                Address ptr;
                if (f.isStatic()) {
                    ptr = f.getAddress();
                } else {
                    ptr = f.getAddress(addr);
                }

                if (t.isCStringType()) {
                    return new CStringTreeNodeAdapter(CStringUtilities.getString(ptr), cf);
                }

                return getTypeTreeNodeAdapter(ptr, ((PointerType) t).getTargetType(), cf, getTreeTableMode());
            } else if (t.getName().equals(ADDRESS)){
                Address ptr;
                if (f.isStatic()) {
                    ptr = f.getAddress();
                } else {
                    ptr = f.getAddress(addr);
                }
                return getTypeTreeNodeAdapter(ptr, f.getType(), cf, getTreeTableMode());
            } else {
                if (f.isStatic()) {
                    return getTypeTreeNodeAdapter(f.getStaticFieldAddress(), f.getType(),
                            cf, getTreeTableMode());
                } else {
                    return getTypeTreeNodeAdapter(addr.addOffsetTo(f.getOffset()), f.getType(),
                            cf, getTreeTableMode());
                }
            }
        } catch (AddressException e) {
            return new BadAddressTreeNodeAdapter(e.getAddress(),
                    new CTypeTreeNodeAdapter.CTypeFieldIdentifier(type, f),
                    getTreeTableMode());
        }
    }

    private void collectFields(Type type) {
        Type supertype = type.getSuperclass();
        if (supertype != null) {
            collectFields(supertype);
        }
        Iterator i = type.getFields();
        while (i.hasNext()) {
            Field f = (Field) i.next();
            if (!f.isStatic()) {
                fieldList.add(new CTypeTreeNodeAdapter.CTypeFieldIdentifier(type, f));
            }
        }
    }

    private CTypeTreeNodeAdapter.CTypeFieldIdentifier[] getFields() {
        if (this.fields == null) {
            this.collectFields(this.type);
            Collections.sort(fieldList, new Comparator<CTypeTreeNodeAdapter.CTypeFieldIdentifier>() {
                @Override
                public int compare(CTypeTreeNodeAdapter.CTypeFieldIdentifier a, CTypeTreeNodeAdapter.CTypeFieldIdentifier b) {
                    return (int)(a.getField().getOffset() - b.getField().getOffset());
                }
            });
            fields = fieldList.toArray(new CTypeTreeNodeAdapter.CTypeFieldIdentifier[0]);
        }

        return this.fields;
    }

    @Override
    public int getChildCount() {
        return getFields().length;
    }

    @Override
    public GeneralTreeNodeAdapter getChild(int index) {
        FieldTreeNodeAdapter child = getChild(getFields()[index]);
        return new GeneralTreeNodeAdapter(child);
    }


    public abstract FieldTreeNodeAdapter getTypeTreeNodeAdapter(Address a, Type t, FieldIdentifier id, boolean treeTableMode);

    @Override
    public boolean isLeaf() {
        return getFields().length == 0;
    }

    @Override
    public int getIndexOfChild(SimpleTreeNode child) {
        CTypeTreeNodeAdapter.CTypeFieldIdentifier id = (CTypeTreeNodeAdapter.CTypeFieldIdentifier)((FieldTreeNodeAdapter) child).getID();
        CTypeTreeNodeAdapter.CTypeFieldIdentifier[] f = getFields();
        for (int i = 0; i < f.length; i++) {
            if (id == f[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getValue() {
        if (type != null) {
            return type.getName() + " @ " + addr;
        } else {
            return "<statics>";
        }
    }

    @Override
    public void extraInfo(FieldTreeNodeAdapter adapter, T t, PrintStream out) {
        FieldIdentifier id = adapter.getID();
        if(id instanceof CTypeTreeNodeAdapter.CTypeFieldIdentifier) {
            CTypeTreeNodeAdapter.CTypeFieldIdentifier typeFieldIdentifier = (CTypeTreeNodeAdapter.CTypeFieldIdentifier) id;
            String name = typeFieldIdentifier.getField().getName();
            extraInfo(name, t, out);
        }
    }

    abstract void extraInfo(String name, T t, PrintStream out);
}
