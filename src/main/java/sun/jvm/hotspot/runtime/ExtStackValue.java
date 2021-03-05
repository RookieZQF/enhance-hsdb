package sun.jvm.hotspot.runtime;

import com.perfma.hotspot.util.PrintUtil;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.ui.tree.ExtOopTypeTreeNodeAdapter;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-03-04
 * @description: desc
 */
public class ExtStackValue extends StackValue {

    private int type;
    public ExtStackValue(){
        super();
    }

    public ExtStackValue(long l){
        super(l);
    }

    public ExtStackValue(OopHandle h, long scalarReplaced) {
        super(h, scalarReplaced);
    }

    public ExtStackValue(long l, int type) {
        super(l);
        this.type = type;
    }

    @Override
    public void printOn(PrintStream tty) {
        if (this.getType() == BasicType.getTInt()) {
            if(type == BasicType.getTDouble()){
                tty.println(Double.longBitsToDouble(this.getInteger()));
            }else if(type == BasicType.getTFloat()){
                tty.println(Float.intBitsToFloat((int)this.getInteger()));
            }else {
                tty.println(this.getInteger());
            }
        } else if (this.getType() == BasicType.getTObject()) {
            OopHandle object = getObject();
            if(object == null){
                tty.println("null");
                return ;
            }
            Oop oop = VM.getVM().getObjectHeap().newOop(object);
            Klass klass = oop.getKlass();
            tty.println(klass.getName().asString());
            PrintUtil.printNode(new ExtOopTypeTreeNodeAdapter(oop, null), tty);
        } else {
            if (this.getType() != BasicType.getTConflict()) {
                throw new RuntimeException("should not reach here");
            }

            tty.println("<no reach>");
        }
    }
}
