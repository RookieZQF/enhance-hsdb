package com.perfma.hotspot.enhance;

import com.perfma.xbox.asm.api.AroundInterceptor;
import sun.jvm.hotspot.CommandProcessor;
import sun.jvm.hotspot.EnhanceCommand;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author: ZQF
 * @date: 2021-02-22
 * @description: desc
 */
public class CommandProcessorConstructorInterceptor implements AroundInterceptor {

    private static boolean init;
    private static boolean error;

    private static Field commandField;
    private static Field outField;
    private static Field errField;
    private static Field debugerField;

    static {
        try{
            commandField = CommandProcessor.class.getDeclaredField("commands");
            commandField.setAccessible(true);
            outField = CommandProcessor.class.getDeclaredField("out");
            outField.setAccessible(true);
            errField = CommandProcessor.class.getDeclaredField("err");
            errField.setAccessible(true);
            debugerField = CommandProcessor.class.getDeclaredField("debugger");
            debugerField.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Get field CommandProcessor.commands exception");
            error = true;
        }
    }

    @Override
    public void before(Object o, Object[] objects) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void after(Object o, Object[] args, Object result, Throwable throwable) {
        if(init || error){
            return;
        }
        init = true;
        CommandProcessor commandProcessor = (CommandProcessor)o;
        Map commands;
        PrintStream out;
        PrintStream err;
        CommandProcessor.DebuggerInterface debugger;
        try{
            commands = (Map)commandField.get(commandProcessor);
            out = (PrintStream)outField.get(commandProcessor);
            err = (PrintStream)errField.get(commandProcessor);
            debugger = (CommandProcessor.DebuggerInterface)debugerField.get(commandProcessor);
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Get CommandProcessor.commands value exception");
            error = true;
            return ;
        }

        out.println("notice: ??????????????????sizeof???C++????????????1;" +
                "\n\t????????????????????????[0]?????????????????????????????????0,?????????????????????????????????;" +
                "\n\t????????????class???????????????,????????????????????????????????????vmStruct,??????????????????????????????;");

        ServiceLoader<EnhanceCommand> serviceLoader = ServiceLoader.load(EnhanceCommand.class, this.getClass().getClassLoader());
        for(EnhanceCommand command : serviceLoader){
            try {
                EnhanceCommand instance = command.getClass().newInstance();
                commands.put(instance.getName(), instance.getCommand(commandProcessor, out, err, debugger));
            }catch (Exception e){
                System.err.println(command.getClass().getName() + "newInstance() exception");
            }
        }

    }
}
