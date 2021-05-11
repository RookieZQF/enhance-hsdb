package sun.jvm.hotspot;

import sun.jvm.hotspot.code.CodeBlob;
import sun.jvm.hotspot.code.CodeCacheVisitor;
import sun.jvm.hotspot.code.NMethod;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.runtime.VM;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-05-10
 * @description: desc
 */
public class CodeCacheCommand implements EnhanceCommand {
    @Override
    public CommandProcessor.Command getCommand(CommandProcessor commandProcessor, PrintStream out, PrintStream err, CommandProcessor.DebuggerInterface debugger) {
        return commandProcessor.new Command(getName(), getName() + " [name]", false) {

            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() > 1) {
                    this.usage();
                    return ;
                }
                final String name;
                if(t.countTokens() == 1){
                    name = t.nextToken();
                }else{
                    name = null;
                }
                VM.getVM().getCodeCache().iterate(new CodeCacheVisitor() {
                    @Override
                    public void prologue(Address address, Address address1) {

                    }

                    @Override
                    public void visit(CodeBlob codeBlob) {
                        if(codeBlob instanceof NMethod){
                            if(((NMethod)codeBlob).getMethod() == null){
                                return ;
                            }
                        }
                        String blobName = codeBlob.getName();
                        if(name == null || (blobName != null && blobName.contains(name))) {
                            System.out.println("@ " + codeBlob.codeBegin() + " " + blobName);
                        }
                    }

                    @Override
                    public void epilogue() {

                    }
                });
            }
        };
    }

    @Override
    public String getName() {
        return "codecache";
    }
}
