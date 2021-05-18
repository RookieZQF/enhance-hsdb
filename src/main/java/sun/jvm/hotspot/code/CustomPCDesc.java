package sun.jvm.hotspot.code;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.runtime.VMObject;
import sun.jvm.hotspot.types.CIntegerField;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.types.TypeDataBase;

import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;

/**
 * @author: ZQF
 * @date: 2021-05-17
 * @description: desc
 */
public class CustomPCDesc extends VMObject {
    private static CIntegerField pcOffsetField;
    private static CIntegerField scopeDecodeOffsetField;
    private static CIntegerField objDecodeOffsetField;
    private static CIntegerField pcFlagsField;
    private static int reexecuteMask;
    private static int isMethodHandleInvokeMask;
    private static int returnOopMask;

    static {
        VM.registerVMInitializedObserver(new Observer() {
            @Override
            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        Type type = db.lookupType("PcDesc");

        pcOffsetField          = type.getCIntegerField("_pc_offset");
        scopeDecodeOffsetField = type.getCIntegerField("_scope_decode_offset");
        objDecodeOffsetField   = type.getCIntegerField("_obj_decode_offset");
        pcFlagsField           = type.getCIntegerField("_flags");

        reexecuteMask            = db.lookupIntConstant("PcDesc::PCDESC_reexecute");
        isMethodHandleInvokeMask = db.lookupIntConstant("PcDesc::PCDESC_is_method_handle_invoke");
        returnOopMask            = db.lookupIntConstant("PcDesc::PCDESC_return_oop");
    }

    public CustomPCDesc(Address addr) {
        super(addr);
    }

    // FIXME: add additional constructor probably needed for ScopeDesc::sender()

    public int getPCOffset() {
        return (int) pcOffsetField.getValue(addr);
    }

    public int getScopeDecodeOffset() {
        return ((int) scopeDecodeOffsetField.getValue(addr));
    }

    public int getObjDecodeOffset() {
        return ((int) objDecodeOffsetField.getValue(addr));
    }

    public Address getRealPC(NMethod code) {
        return code.codeBegin().addOffsetTo(getPCOffset());
    }

    public int getPcFlags(){
        return (int)pcFlagsField.getValue(addr);
    }

    public boolean getReexecute() {
        int flags = getPcFlags();
        return (flags & reexecuteMask) != 0;
    }

    public boolean isMethodHandleInvoke() {
        int flags = getPcFlags();
        return (flags & isMethodHandleInvokeMask) != 0;
    }

    public boolean isReturnOop() {
        int flags = getPcFlags();
        return (flags & returnOopMask) != 0;
    }

    public void print(NMethod code) {
        printOn(System.out, code);
    }

    public void printOn(PrintStream tty, NMethod code) {
        tty.println("CustomPCDesc(pc=" + getRealPC(code) + ",_scopes_data_begin=" + code.scopesDataBegin() + "):");
        if(getScopeDecodeOffset() == DebugInformationRecorder.SERIALIZED_NULL){
            return ;
        }
        for (CustomScopeDesc sd = new CustomScopeDesc(code, getScopeDecodeOffset(), getObjDecodeOffset(), getReexecute());
             sd != null;
             sd = sd.sender()) {
            tty.print(" ");
            tty.print("  @" + sd.getBCI());
            tty.print("  _pc_offset=" + getPCOffset());
            tty.print("  _scope_decode_offset=" + getScopeDecodeOffset());
            tty.print("  _obj_decode_offset=" + getObjDecodeOffset());
            tty.print("  _flags=" + getPcFlags());
            tty.print("  reexecute=" + sd.getReexecute());
            tty.print("  methodHandleInvoke=" + isMethodHandleInvoke());
            tty.print("  returnOop=" + isReturnOop());
            tty.println();

            sd.printValueOn(tty);
        }
    }
}
