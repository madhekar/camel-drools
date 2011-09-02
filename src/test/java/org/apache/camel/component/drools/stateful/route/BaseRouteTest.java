package org.apache.camel.component.drools.stateful.route;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.drools.DroolsComponent;
import org.apache.camel.component.drools.mock.MockSessionDAO;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class BaseRouteTest {

    protected DefaultCamelContext ctx;
    protected ProducerTemplate<Exchange> tpl;
    protected MockSessionDAO dao;

    @Before
    public void makeContext() throws Exception {
        ctx = new DefaultCamelContext();
        ctx.addComponent("drools", new DroolsComponent(ctx));
        ApplicationContext appCtx = new ClassPathXmlApplicationContext(
                new String[] { "classpath:camel-drools-context.xml", "classpath:stateful/mock-dao-context.xml" });
        dao = (MockSessionDAO) appCtx.getBean("sessionDAO");
        ctx.setRegistry(new ApplicationContextRegistry(appCtx));
        addRoutes();
        ctx.start();
        tpl = ctx.createProducerTemplate();
    }

    protected abstract void addRoutes() throws Exception;
}
