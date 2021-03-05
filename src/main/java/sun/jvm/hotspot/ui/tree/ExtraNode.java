package sun.jvm.hotspot.ui.tree;

import java.io.PrintStream;

/**
 * @author: ZQF
 * @date: 2021-02-24
 * @description: desc
 */
public interface ExtraNode<T> {
    void extraInfo(FieldTreeNodeAdapter adapter, T t, PrintStream out);
}
