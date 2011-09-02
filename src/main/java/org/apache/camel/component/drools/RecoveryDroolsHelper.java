package org.apache.camel.component.drools;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecoveryDroolsHelper extends CamelDroolsHelper {

    protected static Log log = LogFactory.getLog(RecoveryDroolsHelper.class);
    
    public RecoveryDroolsHelper() {
        super(new DefaultCamelContext(), null);
    }
    
    @Override
    public Object send(String uri, Object body) {
        log.debug("Omitting: " + uri + ", body: " + body);
        return null;
    }
}
