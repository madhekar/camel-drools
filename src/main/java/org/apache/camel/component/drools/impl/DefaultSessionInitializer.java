package org.apache.camel.component.drools.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.component.drools.CamelDroolsHelper;
import org.apache.camel.component.drools.RecoveryDroolsHelper;
import org.springframework.beans.factory.annotation.Required;

/**
 * author pjagielski
 */
public class DefaultSessionInitializer implements CamelContextAware, SessionInitializer {

    private CamelContext ctx;

    public void initSession(SessionWithIdentifier session, boolean recoveryMode) {
        CamelDroolsHelper helper;
        if (recoveryMode) {
            helper = new RecoveryDroolsHelper();
        } else {
            helper = new CamelDroolsHelper(ctx, null);
        }
        session.getSession().setGlobal("helper", helper);
    }

    public boolean shouldInitializeNewSession(SessionWithIdentifier session, Object initiator) {
        return false;
    }

    @Required
    public void setCamelContext(CamelContext camelContext) {
        this.ctx = camelContext;
    }

}

