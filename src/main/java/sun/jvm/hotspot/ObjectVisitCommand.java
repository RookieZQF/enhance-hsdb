package sun.jvm.hotspot;

import sun.jvm.hotspot.constant.Constant;
import sun.jvm.hotspot.filter.OopFilter;
import sun.jvm.hotspot.oops.Instance;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.print.ExtOopPrinter;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.visitor.ObjectVisitor;

import java.io.PrintStream;
import java.util.List;

/**
 * @author: ZQF
 * @date: 2021-02-25
 * @description: desc
 */
public class ObjectVisitCommand implements EnhanceCommand {

    private static final String DETAIL_OPTION = "-d";

    private ObjectVisitor objectVisitor;

    private ObjectVisitor getObjectVisitor(){
        if(objectVisitor == null){
            objectVisitor = new ObjectVisitor();
            VM.getVM().getObjectHeap().iterate(objectVisitor);
        }
        return objectVisitor;
    }

    @Override
    public CommandProcessor.Command getCommand(CommandProcessor commandProcessor, final PrintStream out, final PrintStream err,
                                               final CommandProcessor.DebuggerInterface debugger) {
        return commandProcessor.new Command(getName(), getName() + " [" + DETAIL_OPTION + "] fullClassName[.class]", false) {
            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() != 1 && t.countTokens() != 2) {
                    usage();
                    return;
                }
                boolean isDetail = false;
                if(t.countTokens() == 2){
                    String s = t.nextToken();
                    if(s.equals(DETAIL_OPTION)){
                        isDetail = true;
                    }else{
                        usage();
                        return;
                    }
                }
                out.println("Iterating over heap. Please wait...");
                long curr = System.currentTimeMillis();
                String name = t.nextToken();
                boolean filter = false;
                String filterName = "";
                if(name.endsWith(Constant.CLASS_SUFFIX)){
                    filter = true;
                    filterName = name.substring(0, name.length()-Constant.CLASS_SUFFIX.length());
                    name = "java/lang/Class";
                }
                ObjectVisitor objectVisitor = getObjectVisitor();
                List<Oop> oops = objectVisitor.getOops(name);
                if(oops == null || oops.size() == 0){
                    out.println("Can't find " + name + " instances");
                    return ;
                }
                for(Oop oop : oops){
                    if(filter){
                        OopFilter oopFilter = new OopFilter("name", filterName);
                        oop.iterate(oopFilter, false);
                        if(!oopFilter.isOk()){
                            continue;
                        }
                    }
                    out.println(" @ " + oop.getHandle().toString());
                    if(isDetail) {
                        print(oop, out);
                    }


                }

                out.println("Heap traversal took " + (System.currentTimeMillis()-curr) * 1.0 / 1000 + " seconds.");
            }
        };
    }

    private void print(Oop oop, PrintStream out){
        if(oop.isInstance()){
            printInstance((Instance)oop, out);
        }else {
            oop.printOn(out);
        }
    }

    private void printInstance(Instance oop, PrintStream out){
        ExtOopPrinter oopPrinter = new ExtOopPrinter(out);
        oop.iterate(oopPrinter, true);
    }

    @Override
    public String getName() {
        return "objectvisit";
    }
}
