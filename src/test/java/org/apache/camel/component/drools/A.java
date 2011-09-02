/**
 * 
 */
package org.apache.camel.component.drools;

import java.io.Serializable;
import java.util.Date;

import org.apache.camel.component.drools.impl.EventWithTime;

/**
 * @author mproch
 *
 */
@SuppressWarnings("serial")
public class A implements Serializable, EventWithTime {

	String a;
	
	long timestamp;
	
	public A(String a) {
		this.a = a;
		timestamp = new Date().getTime();
	}

	public String getA() {
		return a;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

    public int compareTo(EventWithTime o) {
        return new Long(timestamp).compareTo(new Long(o.getTimestamp()));
    }
	

}
