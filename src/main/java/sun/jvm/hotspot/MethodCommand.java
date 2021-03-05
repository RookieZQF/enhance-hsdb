package sun.jvm.hotspot;

import com.perfma.hotspot.util.MethodUtil;
import com.perfma.hotspot.util.PrintUtil;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.ui.tree.MethodTypeTreeNodeAdapter;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-24
 * @description: desc
 */
public class MethodCommand implements EnhanceCommand {
    @Override
    public CommandProcessor.Command getCommand(CommandProcessor commandProcessor, final PrintStream out, final PrintStream err,
                                               CommandProcessor.DebuggerInterface debugger) {
        return commandProcessor.new Command(getName(), getName() + " address", false) {
            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() != 1) {
                    this.usage();
                } else {
                    Address a = VM.getVM().getDebugger().parseAddress(t.nextToken());
                    Type type = VM.getVM().getTypeDataBase().guessTypeForAddress(a);
                    Type methodType = VM.getVM().lookupType("Method");
                    if(type == null || type != methodType){
                        err.println("Address " + a + " is not Method, " + (type != null ? "actual type: " + type.getName() : "unknown type"));
                        return ;
                    }
                    Method method = new Method(a);
                    out.println(MethodUtil.getMethodSign(method) + " @ " + a);

                    MethodTypeTreeNodeAdapter node = new MethodTypeTreeNodeAdapter(method.getAddress(), type, null);
                    PrintUtil.printNode(node, method, out);
                }
            }
        };
    }

    @Override
    public String getName() {
        return "method";
    }
}
