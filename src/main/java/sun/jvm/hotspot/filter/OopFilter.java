package sun.jvm.hotspot.filter;

import sun.jvm.hotspot.oops.*;

import static sun.jvm.hotspot.constant.Constant.JAVA_LANG_STRING;

/**
 * @author: ZQF
 * @date: 2021-02-26
 * @description: desc
 */
public class OopFilter implements OopVisitor {
    private Oop obj;

    private boolean ok;

    private String fieldName;
    private String filterValue;

    public OopFilter(String fieldName, String filterValue){
        this.fieldName = fieldName;
        this.filterValue = filterValue;
    }
    @Override
    public void prologue() {

    }

    @Override
    public void epilogue() {

    }

    @Override
    public void setObj(Oop oop) {
        this.obj = oop;
    }

    @Override
    public Oop getObj() {
        return obj;
    }

    @Override
    public void doMetadata(MetadataField metadataField, boolean b) {

    }

    @Override
    public void doOop(OopField oopField, boolean b) {
        filter(oopField);
    }

    @Override
    public void doOop(NarrowOopField narrowOopField, boolean b) {
        filter(narrowOopField);
    }

    private void filter(OopField field){
        if(field.getID().getName().equals(fieldName)){
            Oop value = field.getValue(getObj());
            if(value != null && value.getKlass().getName().asString().equals(JAVA_LANG_STRING)) {
                if(filterValue.equals(OopUtilities.stringOopToString(value))){
                    ok = true;
                }
            }
        }
    }

    @Override
    public void doByte(ByteField byteField, boolean b) {

    }

    @Override
    public void doChar(CharField charField, boolean b) {

    }

    @Override
    public void doBoolean(BooleanField booleanField, boolean b) {

    }

    @Override
    public void doShort(ShortField shortField, boolean b) {

    }

    @Override
    public void doInt(IntField intField, boolean b) {

    }

    @Override
    public void doLong(LongField longField, boolean b) {

    }

    @Override
    public void doFloat(FloatField floatField, boolean b) {

    }

    @Override
    public void doDouble(DoubleField doubleField, boolean b) {

    }

    @Override
    public void doCInt(CIntField cIntField, boolean b) {

    }

    public boolean isOk() {
        return ok;
    }
}
