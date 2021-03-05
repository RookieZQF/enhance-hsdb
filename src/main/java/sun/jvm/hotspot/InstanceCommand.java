package sun.jvm.hotspot;

import com.perfma.hotspot.util.PrintUtil;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.ui.tree.ExtOopTypeTreeNodeAdapter;
import sun.jvm.hotspot.utilities.RobustOopDeterminator;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-03-03
 * @description: desc
 */
public class InstanceCommand implements EnhanceCommand {
    @Override
    public CommandProcessor.Command getCommand(CommandProcessor commandProcessor, final PrintStream out, PrintStream err, CommandProcessor.DebuggerInterface debugger) {
        return commandProcessor.new Command(getName(), getName() + " address", false) {

            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() != 1) {
                    this.usage();
                } else {
                    Address a = VM.getVM().getDebugger().parseAddress(t.nextToken());
                    if(a != null){
                        OopHandle handle = a.addOffsetToAsOopHandle(0);

                        if (RobustOopDeterminator.oopLooksValid(handle)) {
                            Oop oop = VM.getVM().getObjectHeap().newOop(handle);
                            PrintUtil.printNode(new ExtOopTypeTreeNodeAdapter(oop, null), out);
                        }
                    }
                }
            }
        };
    }

    @Override
    public String getName() {
        return "instance";
    }
}
