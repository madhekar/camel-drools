/**
 * 
 */
package org.apache.camel.component.drools;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

/**
 * @author mproch
 * @author pjagielski
 *
 */
@SuppressWarnings("unchecked")
public class CamelDroolsHelper {

	Exchange ex;
    ProducerTemplate tpl;
    CamelContext ctx;

    public CamelDroolsHelper(CamelContext ctx, Exchange ex) {
		this.ex = ex;
        this.ctx = ctx;
		this.tpl = ctx.createProducerTemplate();
	}
	
	public Exchange getEx() {
		return ex;
	}

	public void set(Object o) {
	    if (ex == null) {
	        return;
	    }
		ex.getOut().setBody(o);
	}
	
	public Object send(String uri, Object body) {
	    return tpl.requestBody(uri, body);
	}
	
	public <T> T lookup(String name, Class<T> type) {
	    return ctx.getRegistry().lookup(name, type);
	}
	
}
