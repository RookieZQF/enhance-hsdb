package sun.jvm.hotspot.visitor;

import sun.jvm.hotspot.oops.HeapVisitor;
import sun.jvm.hotspot.oops.Oop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZQF
 * @date: 2021-02-25
 * @description: desc
 */
public class ObjectVisitor implements HeapVisitor {

    private final Map<String, List<Oop>> map = new HashMap<>();

    @Override
    public void prologue(long l) {

    }

    @Override
    public boolean doObj(Oop oop) {
        String name = oop.getKlass().getName().asString();
        List<Oop> oops = map.get(name);
        if(oops == null){
            oops = new ArrayList<>();
            map.put(name, oops);
        }
        oops.add(oop);
        return false;
    }

    @Override
    public void epilogue() {

    }

    public List<Oop> getOops(String name){
        return map.get(name.replace(".", "/"));
    }
}
