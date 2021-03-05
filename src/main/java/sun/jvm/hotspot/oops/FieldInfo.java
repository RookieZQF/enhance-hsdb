package sun.jvm.hotspot.oops;

import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.TypeDataBase;
import sun.jvm.hotspot.types.WrongTypeException;
import sun.jvm.hotspot.utilities.U2Array;

import java.util.Observable;
import java.util.Observer;

/**
 * @author: ZQF
 * @date: 2021-02-25
 * @description: desc
 */
public class FieldInfo {
    private static int ACCESS_FLAGS_OFFSET;
    private static int NAME_INDEX_OFFSET;
    private static int SIGNATURE_INDEX_OFFSET;
    private static int INITVAL_INDEX_OFFSET;
    private static int LOW_OFFSET;
    private static int HIGH_OFFSET;
    private static int FIELD_SLOTS;

    private static short FIELDINFO_TAG_SIZE;
    private static short FIELDINFO_TAG_MASK;
    private static short FIELDINFO_TAG_OFFSET;

    private U2Array fields;

    private InstanceKlass instanceKlass;
    static {
        VM.registerVMInitializedObserver(new Observer() {
            @Override
            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }
    public FieldInfo(InstanceKlass instanceKlass){
        this.instanceKlass = instanceKlass;
        this.fields = instanceKlass.getFields();
    }

    private static void initialize(TypeDataBase db) throws WrongTypeException {
        ACCESS_FLAGS_OFFSET            = db.lookupIntConstant("FieldInfo::access_flags_offset");
        NAME_INDEX_OFFSET              = db.lookupIntConstant("FieldInfo::name_index_offset");
        SIGNATURE_INDEX_OFFSET         = db.lookupIntConstant("FieldInfo::signature_index_offset");
        INITVAL_INDEX_OFFSET           = db.lookupIntConstant("FieldInfo::initval_index_offset");
        LOW_OFFSET                     = db.lookupIntConstant("FieldInfo::low_packed_offset");
        HIGH_OFFSET                    = db.lookupIntConstant("FieldInfo::high_packed_offset");
        FIELD_SLOTS                    = db.lookupIntConstant("FieldInfo::field_slots");

        FIELDINFO_TAG_SIZE             = db.lookupIntConstant("FIELDINFO_TAG_SIZE").shortValue();
        FIELDINFO_TAG_MASK             = db.lookupIntConstant("FIELDINFO_TAG_MASK").shortValue();
        FIELDINFO_TAG_OFFSET           = db.lookupIntConstant("FIELDINFO_TAG_OFFSET").shortValue();
    }

    public AccessFlags getFieldAccessFlags(int index){
        return new AccessFlags(fields.at(index * FIELD_SLOTS + ACCESS_FLAGS_OFFSET));
    }


    public String getFieldName(int index){
        int nameIndex = fields.at(index * FIELD_SLOTS + NAME_INDEX_OFFSET);
        return instanceKlass.getConstants().getSymbolAt(nameIndex).asString();
    }

    public Symbol getFieldSignature(int index) {
        int signatureIndex = getFields().at(index * FIELD_SLOTS + SIGNATURE_INDEX_OFFSET);
        return instanceKlass.getConstants().getSymbolAt(signatureIndex);
    }

    public String getFieldInitialValue(int index) {
        int initValIndex = getFields().at(index * FIELD_SLOTS + INITVAL_INDEX_OFFSET);
        if(initValIndex == 0){
            return "0";
        }
        FieldType type = new FieldType(getFieldSignature(index));
        ConstantPool constants = instanceKlass.getConstants();
        if (type.isByte()) {
            return constants.getIntAt(initValIndex) + "";
        }
        if (type.isChar()) {
            return constants.getIntAt(initValIndex) + "";
        }
        if (type.isDouble()) {
            return constants.getDoubleAt(initValIndex) + "";
        }
        if (type.isFloat()) {
            return constants.getFloatAt(initValIndex) + "";
        }
        if (type.isInt()) {
            return constants.getIntAt(initValIndex) + "";
        }
        if (type.isLong()) {
            return constants.getLongAt(initValIndex) + "";
        }
        if (type.isShort()) {
            return constants.getIntAt(initValIndex) + "";
        }
        if (type.isBoolean()) {
            return constants.getIntAt(initValIndex) + "";
        }
        return constants.getSymbolAt(initValIndex).asString();
    }

    public int getFieldOffset(int index) {
        U2Array fields = getFields();
        short lo = fields.at(index * FIELD_SLOTS + LOW_OFFSET);
        short hi = fields.at(index * FIELD_SLOTS + HIGH_OFFSET);
        if ((lo & FIELDINFO_TAG_MASK) == FIELDINFO_TAG_OFFSET) {
            return VM.getVM().buildIntFromShorts(lo, hi) >> FIELDINFO_TAG_SIZE;
        }
        throw new RuntimeException("should not reach here");
    }

    private U2Array getFields() {
        return fields;
    }
}
