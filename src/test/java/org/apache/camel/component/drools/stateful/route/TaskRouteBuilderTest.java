/**
 * 
 */
package org.apache.camel.component.drools.stateful.route;

import junit.framework.Assert;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.drools.impl.SessionWithIdentifier;
import org.apache.camel.component.drools.stateful.model.State;
import org.apache.camel.component.drools.stateful.model.Task;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * @author pjagielski
 * 
 */
public class TaskRouteBuilderTest extends BaseRouteTest {

    protected void addRoutes() throws Exception {
        ctx.addRoutes(new TaskRouteBuilder());
        ctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:completed").to("mock:test");
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void tasksCreated() throws Exception {
        Endpoint<Exchange> endpoint = ctx.getEndpoint("direct:drools");

        tpl.requestBody(endpoint, new State("start"));
        SessionWithIdentifier session = dao.getSession();
        Assert.assertEquals(2, session.getSession().getFactHandles().size());
        
        tpl.requestBody(endpoint, new Task("Task1", true));
        tpl.requestBody(endpoint, new Task("Task2", true));
        Assert.assertEquals(3, session.getSession().getFactHandles().size());
        tpl.requestBody(endpoint, new Task("Task3", true));

        MockEndpoint mock = MockEndpoint.resolve(ctx, "mock:test");
        mock.expectedMessageCount(1);
        mock.setResultWaitTime(5000L);
        mock.assertIsSatisfied();
    }

}