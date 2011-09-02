/**
 * 
 */
package org.apache.camel.component.drools.stateful;

import java.io.StringReader;

import org.apache.camel.component.drools.CamelDroolsHelper;
import org.apache.camel.component.drools.GenericTest;
import org.apache.camel.component.drools.stateful.model.TaskCompleted;
import org.apache.camel.component.drools.stateful.model.TaskCreated;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.io.IOUtils;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ReaderResource;
import org.junit.Test;

/**
 * @author pjagielski
 * 
 */
public class TaskEventsTest extends GenericTest {

    DefaultCamelContext ctx;

    @Test
    public void testCompleted() throws Exception {
        insertAdvanceDays(new TaskCreated("Task1"), 4);
        assertContains(0);
        insertAdvanceDays(new TaskCompleted("Task1"), 4);
        advanceDays(5);
        assertContains(0);
    }

    @Test
    public void testNotCompleted() throws Exception {
        insertAdvanceDays(new TaskCreated("Task1"), 5);
        assertContains(0);
        advanceDays(5);
        assertContains("Task1");
    }

    @Test
    public void testOneNotCompleted() throws Exception {
        ksession.insert(new TaskCreated("Task1"));
        insertAdvanceDays(new TaskCreated("Task2"), 5);
        assertContains(0);
        insertAdvanceDays(new TaskCompleted("Task1"), 4);
        assertContains(0);
        advanceDays(1);
        assertContains("Task2");
        advanceDays(10);
        assertContains("Task2");
    }
    
    @Override
    protected void setUpResources(KnowledgeBuilder kbuilder) throws Exception {
        kbuilder.add(new ReaderResource(new StringReader(
                IOUtils.toString(getClass().getResourceAsStream("/stateful/task-event.drl")))), ResourceType.DRL);
    }
    
    @Override
    public void setUpInternal() throws Exception {
        this.ctx = new DefaultCamelContext();
        CamelDroolsHelper helper = new CamelDroolsHelper(ctx, new DefaultExchange(ctx)) {
            public Object send(String uri, Object body) {
                sentStuff.add(body.toString());
                return null;
            };
        };
        ksession.setGlobal("helper", helper);
    }
}