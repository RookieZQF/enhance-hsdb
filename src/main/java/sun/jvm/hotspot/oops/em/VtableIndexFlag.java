package sun.jvm.hotspot.oops.em;

/**
 * @author: ZQF
 * @date: 2021-03-01
 * @description: desc
 */
public enum VtableIndexFlag {
    //
    itable_index_max(-10, "itable最大index"),
    pending_itable_index(-9, "itable index等待分配"),
    invalid_vtable_index(-4, "无效vtable index"),
    garbage_vtable_index(-3, "vtable index初始状态"),
    nonvirtual_vtable_index(-2, "不需要vtable分发"),
    ;

    private long value;

    private String desc;

    VtableIndexFlag(long value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static String getDesc(long value){
        for(VtableIndexFlag vtableIndexFlag : values()){
            if(value == vtableIndexFlag.value){
                return vtableIndexFlag.desc;
            }
        }
        return null;
    }

    public long getValue() {
        return value;
    }
}
