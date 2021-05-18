package sun.jvm.hotspot.code;

import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.utilities.Assert;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: ZQF
 * @date: 2021-05-18
 * @description: desc
 */
public class CustomScopeDesc {
    /** NMethod information */
    private NMethod code;
    private Method method;
    private int     bci;
    private boolean reexecute;
    /** Decoding offsets */
    private int     decodeOffset;
    private int     senderDecodeOffset;
    private int     localsDecodeOffset;
    private int     expressionsDecodeOffset;
    private int     monitorsDecodeOffset;
    /** Scalar replaced objects pool */
    private List<ScopeValue> objects;

    private CustomScopeDesc(NMethod code, int decodeOffset, List<ScopeValue> objects, boolean reexecute) {
        this.code = code;
        this.decodeOffset = decodeOffset;
        this.objects      = objects;
        this.reexecute    = reexecute;

        // Decode header
        DebugInfoReadStream stream  = streamAt(decodeOffset);

        senderDecodeOffset = stream.readInt();
        method = stream.readMethod();
        bci    = stream.readBCI();
        // Decode offsets for body and sender
        localsDecodeOffset      = stream.readInt();
        expressionsDecodeOffset = stream.readInt();
        monitorsDecodeOffset    = stream.readInt();
    }

    public CustomScopeDesc(NMethod code, int decodeOffset, int objectDecodeOffset, boolean reexecute) {
        this.code = code;
        this.decodeOffset = decodeOffset;
        this.objects      = decodeObjectValues(objectDecodeOffset);
        this.reexecute    = reexecute;

        // Decode header
        DebugInfoReadStream stream  = streamAt(decodeOffset);

        senderDecodeOffset = stream.readInt();
        method = stream.readMethod();
        bci    = stream.readBCI();
        // Decode offsets for body and sender
        localsDecodeOffset      = stream.readInt();
        expressionsDecodeOffset = stream.readInt();
        monitorsDecodeOffset    = stream.readInt();
    }

    public NMethod getNMethod()   { return code; }
    public Method getMethod()     { return method; }
    public int    getBCI()        { return bci;    }
    public boolean getReexecute() { return reexecute;}

    /** Returns a List&lt;ScopeValue&gt; */
    public List<ScopeValue> getLocals() {
        return decodeScopeValues(localsDecodeOffset);
    }

    /** Returns a List&lt;ScopeValue&gt; */
    public List<ScopeValue> getExpressions() {
        return decodeScopeValues(expressionsDecodeOffset);
    }

    /** Returns a List&lt;MonitorValue&gt; */
    public List<MonitorValue> getMonitors() {
        return decodeMonitorValues(monitorsDecodeOffset);
    }

    /** Returns a List&lt;ObjectValue&gt; */
    public List<ScopeValue> getObjects() {
        return objects;
    }

    /** Stack walking. Returns null if this is the outermost scope. */
    public CustomScopeDesc sender() {
        if (isTop()) {
            return null;
        }

        return new CustomScopeDesc(code, senderDecodeOffset, objects, false);
    }

    /** Returns where the scope was decoded */
    public int getDecodeOffset() {
        return decodeOffset;
    }

    /** Tells whether sender() returns null */
    public boolean isTop() {
        return (senderDecodeOffset == DebugInformationRecorder.SERIALIZED_NULL);
    }

    @Override
    public boolean equals(Object arg) {
        if (arg == null) {
            return false;
        }

        if (!(arg instanceof CustomScopeDesc)) {
            return false;
        }

        CustomScopeDesc sd = (CustomScopeDesc) arg;

        return (sd.method.equals(method) && (sd.bci == bci));
    }


    public void printValueOn(PrintStream tty) {
        DebugInfoReadStream stream = streamAt(decodeOffset);
        tty.print("CustomScopeDesc for " );
        if(method != null) {
            method.printValueOn(tty);
        }
        tty.print(" base=" + stream.buffer);
        tty.print(" decodeOffset=" + decodeOffset);
        tty.print(" senderDecodeOffset=" + senderDecodeOffset);
        tty.print(" localsDecodeOffset=" + localsDecodeOffset);
        tty.print(" expressionsDecodeOffset=" + expressionsDecodeOffset);
        tty.print(" monitorsDecodeOffset=" + monitorsDecodeOffset);
        tty.print(" @bci " + bci);

        tty.println(" reexecute=" + reexecute);
        printScopeValue(tty, getLocals(), "Locals", localsDecodeOffset);

        printScopeValue(tty, getExpressions(), "Expressions", expressionsDecodeOffset);

        printScopeValue(tty, objects, "Objects", -1);


        tty.print("Monitors:\n");
        for(MonitorValue monitorValue : getMonitors()){
            monitorValue.printOn(tty);
        }
        tty.print("Monitors raw:\n");
        printRaw(tty, monitorsDecodeOffset, getMonitorEndOffset(monitorsDecodeOffset));
    }

    private void printScopeValue(PrintStream tty, List<ScopeValue> list, String desc, int begin){
        tty.print(desc + ":\n");
        for(ScopeValue scopeValue : list){
            scopeValue.printOn(tty);
        }
        if(begin >= 0) {
            tty.print(desc + " raw:\n");
            printRaw(tty, begin, getEndOffset(begin));
        }
    }

    private void printRaw(PrintStream tty, int begin, int end){
        DebugInfoReadStream stream = streamAt(begin);
        tty.println("length:" + (end - begin));
        while(stream.position < end){
            tty.print((short) stream.buffer.getCIntegerAt(stream.position, 1, true));
            tty.print(" ");
            stream.position ++;
        }
        tty.println();
    }

    // FIXME: add more accessors

    //--------------------------------------------------------------------------------
    // Internals only below this point
    //
    private DebugInfoReadStream streamAt(int decodeOffset) {
        return new DebugInfoReadStream(code, decodeOffset, objects);
    }

    /** Returns a List&lt;ScopeValue&gt; or null if no values were present */
    private List<ScopeValue> decodeScopeValues(int decodeOffset) {
        if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
            return new ArrayList<>();
        }
        DebugInfoReadStream stream = streamAt(decodeOffset);
        int length = stream.readInt();
        List<ScopeValue> res = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            res.add(ScopeValue.readFrom(stream));
        }
        return res;
    }


    private int getEndOffset(int decodeOffset) {
        if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
            return decodeOffset;
        }
        DebugInfoReadStream stream = streamAt(decodeOffset);
        int length = stream.readInt();
        for (int i = 0; i < length; i++) {
            ScopeValue.readFrom(stream);
        }
        return stream.position;
    }

    private int getMonitorEndOffset(int decodeOffset) {
        if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
            return decodeOffset;
        }
        DebugInfoReadStream stream = streamAt(decodeOffset);
        int length = stream.readInt();
        for (int i = 0; i < length; i++) {
            new MonitorValue(stream);
        }
        return stream.position;
    }

    /** Returns a List&lt;MonitorValue&gt; or null if no values were present */
    private List<MonitorValue> decodeMonitorValues(int decodeOffset) {
        if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
            return new ArrayList<>();
        }
        DebugInfoReadStream stream = streamAt(decodeOffset);
        int length = stream.readInt();
        List<MonitorValue> res = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            res.add(new MonitorValue(stream));
        }
        return res;
    }

    /** Returns a List&lt;ObjectValue&gt; or null if no values were present */
    private List<ScopeValue> decodeObjectValues(int decodeOffset) {
        if (decodeOffset == DebugInformationRecorder.SERIALIZED_NULL) {
            return new ArrayList<>();
        }
        List<ScopeValue> res = new ArrayList<>();
        DebugInfoReadStream stream = new DebugInfoReadStream(code, decodeOffset, res);
        int length = stream.readInt();
        for (int i = 0; i < length; i++) {
            // Objects values are pushed to 'res' array during read so that
            // object's fields could reference it (OBJECT_ID_CODE).
            ScopeValue.readFrom(stream);
            // res.add(ScopeValue.readFrom(stream));
        }
        Assert.that(res.size() == length, "inconsistent debug information");
        return res;
    }
}
