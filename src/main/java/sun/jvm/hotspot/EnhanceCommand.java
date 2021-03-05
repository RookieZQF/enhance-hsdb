package sun.jvm.hotspot;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-23
 * @description: desc
 */
public interface EnhanceCommand {

    CommandProcessor.Command getCommand(CommandProcessor commandProcessor,
                                        final PrintStream out, final PrintStream err,
                                        final CommandProcessor.DebuggerInterface debugger);

    String getName();
}
