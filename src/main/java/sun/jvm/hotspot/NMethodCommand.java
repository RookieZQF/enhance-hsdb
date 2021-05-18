package sun.jvm.hotspot;

import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.runtime.VM;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-05-17
 * @description: desc
 */
public class NMethodCommand implements EnhanceCommand {

    private static final String ALL = "-all";
    private static final String SCOPE_PC = "-scope_pc";
    private static final String ALL_SCOPE_PC = "-all_scope_pc";

    private static final String[] OPTIONS = new String[]{ALL, SCOPE_PC, ALL_SCOPE_PC};




    @Override
    public CommandProcessor.Command getCommand(CommandProcessor commandProcessor, final PrintStream out, final PrintStream err,
                                               final CommandProcessor.DebuggerInterface debugger) {
        return commandProcessor.new Command(getName(), getName() + getOptions() + " address", false) {


            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() > 2) {
                    this.usage();
                } else {
                    String s = t.nextToken();
                    String address;
                    String option;
                    if(isOptions(s)){
                        option = s;
                        address = t.nextToken();
                    }else{
                        option = ALL;
                        address = s;
                    }
                    if(isScopePc(option)){
                        printScopePc(option, address);
                    }else{
                        printScopePc(option, address);
                    }
                }
            }

            private void printScopePc(String option, String a){
                boolean isAll = true;
                if(SCOPE_PC.equals(option)){
                    isAll = false;
                }
                Address address = VM.getVM().getDebugger().parseAddress(a);
                CodeBlob cb = VM.getVM().getCodeCache().findBlob(address);
                if(cb == null || !cb.isNMethod()){
                    err.println("address " + a + " is not nmethod");
                    return ;
                }
                NMethod nm = (NMethod) cb;
                if(isAll) {
                    HotSpotAgent agent = debugger.getAgent();
                    long pcDescSize = agent.getTypeDataBase().lookupType("PcDesc").getSize();
                    for (Address p = nm.scopesPCsBegin(); p.lessThan(nm.scopesPCsEnd()); p = p.addOffsetTo(pcDescSize)) {
                        CustomPCDesc pcDesc = new CustomPCDesc(p);
                        pcDesc.printOn(out, nm);

                    }
                }else{
                    PCDesc pcDesc = nm.getPCDescNearDbg(address);
                    CustomPCDesc desc = new CustomPCDesc(pcDesc.getAddress());
                    desc.printOn(out, nm);
                }
            }
        };
    }



    @Override
    public String getName() {
        return "nmethod";
    }

    private static String getOptions(){
        StringBuilder builder = new StringBuilder("[");
        builder.append(OPTIONS[0]);
        for(int i = 1; i < OPTIONS.length; i ++){
            builder.append("|");
            builder.append(OPTIONS[i]);
        }
        builder.append("]");
        return builder.toString();
    }

    private static boolean isOptions(String t){
        return t.startsWith("-");
    }

    private static boolean isScopePc(String opt){
        return SCOPE_PC.equals(opt) || ALL_SCOPE_PC.equals(opt);
    }
}
