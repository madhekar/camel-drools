package org.apache.camel.component.drools.stateful.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.collections.keyvalue.MultiKey;

public class TaskRouteBuilder extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        from("direct:drools")
            .setHeader("drools.key", constant(new MultiKey(new String[]{"process-1"})))
            .to("drools:stateful/task.drl?stateful=true");
        from("direct:completed").to("log:test");
    }
}
