package org.apache.camel.component.drools;

public class B extends A {

    private static final long serialVersionUID = -4406740289189260518L;
    public long duration = 1000 * 100;

    public B(String a) {
        super(a);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
    
}
