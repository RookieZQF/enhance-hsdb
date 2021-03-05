package sun.jvm.hotspot.print;

import sun.jvm.hotspot.oops.*;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-25
 * @description: desc
 */
public class ExtOopPrinter extends OopPrinter {
    private PrintStream tty;
    public ExtOopPrinter(PrintStream tty) {
        super(tty);
        this.tty = tty;
    }

    @Override
    public void doMetadata(MetadataField field, boolean isVmField) {
        field.printOn(this.tty);
        Metadata metadata = field.getValue(getObj());
        metadata.printValueOn(tty);
        tty.print(" @ " + metadata.getAddress());
        tty.println();
    }
}
