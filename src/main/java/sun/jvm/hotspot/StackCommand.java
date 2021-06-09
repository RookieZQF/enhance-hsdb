package sun.jvm.hotspot;

import com.perfma.hotspot.util.MethodUtil;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.interpreter.OopMapCacheEntry;
import sun.jvm.hotspot.oops.AccessFlags;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.AddressOps;
import sun.jvm.hotspot.utilities.Assert;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZQF
 * @date: 2021-03-03
 * @description: desc
 */
public class StackCommand implements EnhanceCommand {

    private Map<String, JavaThread> cache;

    private static final String DETAIL_OPTION = "-d";

    private JavaThread getJavaThread(String threadName){
        if(cache == null){
            cache = new HashMap<>(16);
            Threads threads = VM.getVM().getThreads();
            for (JavaThread t = threads.first(); t != null; t = t.next()) {
                if (t.isJavaThread()) {
                    cache.put(t.getThreadName(), t);
                }
            }
        }
        return cache.get(threadName);
    }

    @Override
    public CommandProcessor.Command getCommand(CommandProcessor commandProcessor, final PrintStream out, final PrintStream err,
                                               CommandProcessor.DebuggerInterface debugger) {
        return commandProcessor.new Command(getName(), getName() + " [" + DETAIL_OPTION + "] threadName", false) {

            @Override
            void doit(CommandProcessor.Tokens t) {
                if (t.countTokens() < 1) {
                    this.usage();
                } else {
                    String threadName = t.nextToken();
                    boolean isDetail = false;
                    if(DETAIL_OPTION.equals(threadName)){
                        isDetail = true;
                        threadName = t.nextToken();
                    }
                    while(t.hasMoreTokens()){
                        threadName = threadName + " " + t.nextToken();
                    }
                    JavaThread thread = getJavaThread(threadName);
                    if(thread == null){
                        err.println("thread " + threadName + " is not exist");
                        return ;
                    }
                    out.println("JavaThread:" + thread.getAddress());
                    sun.jvm.hotspot.runtime.Frame tmpFrame = thread.getCurrentFrameGuess();
                    if (tmpFrame != null ) {
                        Address sp = tmpFrame.getSP();
                        Address maxSp = sp;
                        Address minSp = sp;
                        RegisterMap tmpMap = thread.newRegisterMap(false);
                        while ((tmpFrame != null) && (!tmpFrame.isFirstFrame())) {
                            tmpFrame = tmpFrame.sender(tmpMap);
                            if (tmpFrame != null) {
                                sp = tmpFrame.getSP();
                                maxSp = AddressOps.max(maxSp, sp);
                                minSp = AddressOps.min(minSp, sp);
                            }
                        }
                        out.println("Stack in use by Java: " + minSp + " .. " + maxSp);
                    } else {
                        out.println("No Java frames present");
                    }
                    out.println("Base of Stack: " + thread.getStackBase());
                    out.println("Last_Java_SP: " + thread.getLastJavaSP());
                    out.println("Last_Java_FP: " + thread.getLastJavaFP());
                    out.println("Last_Java_PC: " + thread.getLastJavaPC());

                    for (JavaVFrame vf = thread.getLastJavaVFrameDbg(); vf != null; vf = vf.javaSender()) {
                        StringBuilder buf = new StringBuilder();
                        Method method = vf.getMethod();
                        InstanceKlass instanceKlass = method.getMethodHolder();
                        buf.append(instanceKlass.getName().asString()).append(" @ ").append(instanceKlass.getAddress()).append(" ");
                        buf.append(MethodUtil.getMethodSign(method));
                        buf.append(' ');
                        buf.append('@');
                        buf.append(method.getAddress().toString());
                        buf.append(" @bci = ").append(vf.getBCI());

                        int lineNumber = method.getLineNumberFromBCI(vf.getBCI());
                        if (lineNumber != -1) {
                            buf.append(", line = ");
                            buf.append(lineNumber);
                        }

                        sun.jvm.hotspot.debugger.Address pc = vf.getFrame().getPC();
                        if (pc != null) {
                            buf.append(", pc = ");
                            buf.append(pc);
                        }

                        if (vf.isCompiledFrame()) {
                            buf.append(" (Compiled");
                        }
                        else if (vf.isInterpretedFrame()) {
                            buf.append(" (Interpreted");
                        }

                        if (vf.mayBeImpreciseDbg()) {
                            buf.append("; information may be imprecise");
                        }
                        buf.append(")");
                        out.println(buf);
                        if(isDetail) {
                            StackValueCollection locals;
                            StackValueCollection expressions = null;
                            if (vf.isCompiledFrame()) {
                                locals = getCompiledLocals(new ExtCompiledVFrame((CompiledVFrame) vf));
                            } else {
                                locals = getInterpretedLocals((InterpretedVFrame) vf);
                                expressions = getInterpretedExpressions((InterpretedVFrame) vf);
                            }
                            print(locals, out, true);
                            print(expressions, out, false);
                        }
                    }
                }
            }
        };
    }

    private void print(StackValueCollection collection, PrintStream out, boolean isLocals){
        if(collection == null || collection.isEmpty()){
            return ;
        }
        out.println(isLocals ? "Locals: " : "Expressions: ");
        for(int i = 0; i < collection.size(); i ++){
            out.print("slot[" + i + "]: ");
            StackValue stackValue = collection.get(i);
            stackValue.printOn(out);
        }
    }

    private StackValueCollection getInterpretedExpressions(InterpretedVFrame vFrame){
        int length = getInterpreterFrameExpressionStackSize(vFrame);

        if (vFrame.getMethod().isNative()) {
            // If the method is native, there is no expression stack
            length = 0;
        }

        StackValueCollection result = new StackValueCollection(length);

        for(int i = 0; i < length; i++) {
            // Find stack location
            Address addr = vFrame.getFrame().addressOfInterpreterFrameExpressionStackSlot(i);

            StackValue sv = new ExtStackValue(addr.getCIntegerAt(0, VM.getVM().getAddressSize(), false));
            result.add(sv);
        }

        return result;
    }

    private int getInterpreterFrameExpressionStackSize(InterpretedVFrame vFrame) {
        return (int)vFrame.getMethod().getMaxStack();
    }

    private StackValueCollection getInterpretedLocals(InterpretedVFrame vFrame){
        Method m = vFrame.getMethod();
        int length = (int)m.getMaxLocals();
        if (m.isNative()) {
            length = (int)m.getSizeOfParameters();
        }

        StackValueCollection result = new StackValueCollection(length);
        OopMapCacheEntry oopMask = vFrame.getMethod().getMaskFor(vFrame.getBCI());

        for(int i = 0; i < length; ++i) {
            Address addr = vFrame.getFrame().addressOfInterpreterFrameLocal(i);
            StackValue sv;
            if (oopMask.isOop(i)) {
                sv = new ExtStackValue(addr.getOopHandleAt(0L), 0L);
            } else if(isClass(m, i)) {
                sv = new ExtStackValue(null, 0L);
            } else {

                sv = new ExtStackValue(addr.getCIntegerAt(0L, VM.getVM().getAddressSize(), false));
            }

            result.add(sv);
        }

        return result;
    }

    private boolean isClass(Method m, int index) {
        if(index == 0 && m.isNative()){
            AccessFlags accessFlagsObj = m.getAccessFlagsObj();
            return accessFlagsObj.isNative();
        }
        return false;
    }

    private StackValueCollection getCompiledLocals(ExtCompiledVFrame vFrame){
        ScopeDesc scope = vFrame.getScope();
        if (scope == null) {
            return new StackValueCollection();
        } else {
            List scvList = scope.getLocals();
            if (scvList == null) {
                return new StackValueCollection();
            } else {
                int length = scvList.size();
                StackValueCollection result = new StackValueCollection(length);

                for (Object o : scvList) {
                    result.add(this.createStackValue((ScopeValue) o, vFrame));
                }

                return result;
            }
        }
    }

    private StackValue createStackValue(ScopeValue sv, ExtCompiledVFrame vFrame) {
        if (sv.isLocation()) {
            Location loc = ((LocationValue)sv).getLocation();
            if (loc.isIllegal()) {
                return new ExtStackValue();
            } else {
                Address valueAddr = loc.isRegister() ? vFrame.getRegisterMap().getLocation(new VMReg(loc.getRegisterNumber())) : vFrame.getFrame().getUnextendedSP().addOffsetTo((long)loc.getStackOffset());
                if (loc.holdsFloat()) {
                    if (Assert.ASSERTS_ENABLED) {
                        Assert.that(loc.isRegister(), "floats always saved to stack in 1 word");
                    }

                    float value = (float)valueAddr.getJDoubleAt(0L);
                    return new ExtStackValue((long)(Float.floatToIntBits(value)), BasicType.getTFloat());
                } else if (loc.holdsInt()) {
                    if (Assert.ASSERTS_ENABLED) {
                        Assert.that(loc.isRegister(), "ints always saved to stack in 1 word");
                    }

                    return new ExtStackValue(valueAddr.getJLongAt(0L));
                } else if (loc.holdsNarrowOop()) {
                    return loc.isRegister() && VM.getVM().isBigEndian() ? new ExtStackValue(valueAddr.getCompOopHandleAt(VM.getVM().getIntSize()), 0L) : new ExtStackValue(valueAddr.getCompOopHandleAt(0L), 0L);
                } else if (loc.holdsOop()) {
                    return new ExtStackValue(valueAddr.getOopHandleAt(0L), 0L);
                } else if (loc.holdsDouble()) {
                    return new ExtStackValue(valueAddr.getJLongAt(0L), BasicType.getTDouble());
                } else if (loc.holdsAddr()) {
                    if (Assert.ASSERTS_ENABLED) {
                        Assert.that(!VM.getVM().isServerCompiler(), "No address type for locations with C2 (jsr-s are inlined)");
                    }

                    return new ExtStackValue(0L);
                } else if (VM.getVM().isLP64() && loc.holdsLong()) {
                    return loc.isRegister() ? new ExtStackValue((valueAddr.getJLongAt(0L)) << 32 | valueAddr.getJLongAt(8L)) : new ExtStackValue(valueAddr.getJLongAt(0L));
                } else {
                    return loc.isRegister() ? new ExtStackValue((long)(valueAddr.getJIntAt(0L))) : new ExtStackValue((long)(valueAddr.getJIntAt(0L)));
                }
            }
        } else if (sv.isConstantInt()) {
            return new ExtStackValue((long)(((ConstantIntValue) sv).getValue()));
        } else if (sv.isConstantOop()) {
            return new ExtStackValue(((ConstantOopReadValue)sv).getValue(), 0L);
        } else if (sv.isConstantDouble()) {
            double d = ((ConstantDoubleValue)sv).getValue();
            return new ExtStackValue(Double.doubleToLongBits(d), BasicType.getTDouble());
        } else if (VM.getVM().isLP64() && sv.isConstantLong()) {
            return new ExtStackValue(((ConstantLongValue) sv).getValue());
        } else if (sv.isObject()) {
            return new ExtStackValue(((ObjectValue)sv).getValue(), 1L);
        } else {
            Assert.that(false, "Should not reach here");
            return new ExtStackValue(0L);
        }
    }

    @Override
    public String getName() {
        return "stack";
    }
}
