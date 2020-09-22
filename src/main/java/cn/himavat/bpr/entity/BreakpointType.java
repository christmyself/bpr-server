package cn.himavat.bpr.entity;

public enum BreakpointType {
    RESTORE(-1),BROKEN(1);
    private int value;
    private BreakpointType(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
