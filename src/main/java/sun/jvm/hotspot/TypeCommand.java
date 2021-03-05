package sun.jvm.hotspot;

import sun.jvm.hotspot.types.Field;
import sun.jvm.hotspot.types.Type;

import java.io.PrintStream;
import java.util.*;

/**
 * @author: ZQF
 * @date: 2021-02-24
 * @description: desc
 */
public class TypeCommand implements EnhanceCommand {
    @Override
    public CommandProcessor.Command getCommand(CommandProcessor commandProcessor,
                                               final PrintStream out, final PrintStream err,
                                               final CommandProcessor.DebuggerInterface debugger) {
        return commandProcessor.new Command(getName(), getName() + " typeName", true) {
            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() != 1) {
                    usage();
                    return;
                }
                HotSpotAgent agent = debugger.getAgent();
                String typeName = t.nextToken();
                Type type = agent.getTypeDataBase().lookupType(typeName);
                if(type == null){
                    err.println("Can't find Type: " + typeName);
                    return ;
                }
                dumpType(type, false);
            }

            void dumpType(Type type, boolean isSuper) {
                if (type.getSuperclass() != null) {
                    dumpType(type.getSuperclass(), true);
                }
                out.println((isSuper ? "superType: " : "type: ") + type.getName());
                out.println("size: " + type.getSize());
                out.println("fields: ");
                Iterator iterator = type.getFields();
                List<Field> fields = new ArrayList<>();
                while(iterator.hasNext()){
                    Field field = (Field)iterator.next();
                    if(!field.isStatic()){
                        fields.add(field);
                    }
                }
                Collections.sort(fields, new Comparator<Field>() {
                    @Override
                    public int compare(Field o1, Field o2) {
                        return (int)(o1.getOffset() - o2.getOffset());
                    }
                });

                for(Field field : fields) {
                    out.println(field.getType().getName() + " " + type.getName() + ":" + field.getName() +
                            ", size=" + field.getSize() + ", offset=" + field.getOffset());
                }

            }
        };


    }

    @Override
    public String getName() {
        return "typedetail";
    }
}
