package sun.jvm.hotspot.runtime;

/**
 * @author: ZQF
 * @date: 2021-03-04
 * @description: desc
 */
public class ExtCompiledVFrame extends CompiledVFrame {
    public ExtCompiledVFrame(CompiledVFrame vFrame) {
        super(vFrame.fr, vFrame.regMap, vFrame.thread, vFrame.getScope(), vFrame.mayBeImpreciseDbg());
    }

    @Override
    public Frame getFrame() {
        return super.getFrame();
    }
}
