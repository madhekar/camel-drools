package org.apache.camel.component.drools.persistence;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.camel.CamelContext;
import org.apache.camel.component.drools.dao.SessionDAO;
import org.apache.camel.component.drools.impl.DefaultSessionInitializer;
import org.apache.camel.component.drools.impl.KnowledgeSessionConfiguration;
import org.apache.camel.component.drools.impl.SessionCache;
import org.apache.camel.component.drools.impl.SessionMakerImpl;
import org.apache.camel.component.drools.impl.SessionSanitizer;
import org.apache.camel.component.drools.impl.SessionWithIdentifier;
import org.apache.camel.component.drools.utils.ResourceWrapper;
import org.apache.camel.component.drools.utils.Utils;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.h2.tools.RunScript;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PersistenceIntegrationTest {

    static ApplicationContext ctx;
    SessionSanitizer sanitizer;
    SessionMakerImpl maker;
    SessionDAO sessionDAO;
    static Log log = LogFactory.getLog(PersistenceIntegrationTest.class);

    @BeforeClass
    public static void init() throws Exception {
        System.setProperty("java.naming.factory.initial",
            "org.apache.xbean.spring.jndi.SpringInitialContextFactory");
        System.setProperty("java.naming.factory.initial",
            "org.apache.xbean.spring.jndi.SpringInitialContextFactory");
        ctx = new ClassPathXmlApplicationContext(new String[]{"classpath:persistence/dao.xml", "classpath:camel-drools-context.xml"});        
        DataSource ds = (DataSource) ctx.getBean("dataSource");
        Connection conn = ds.getConnection();
        RunScript.execute(conn, new InputStreamReader(new PersistenceIntegrationTest().getClass().getResourceAsStream("/persistence/sessioninfo-init.sql")));
        conn.commit();
    }
    
    @Before
    public void before() throws Exception {
        sanitizer = (SessionSanitizer) ctx.getBean("sessionSanitizer");
        maker = (SessionMakerImpl) ctx.getBean("sessionMaker");
        sessionDAO = (SessionDAO) ctx.getBean("sessionDAO");
        KnowledgeSessionConfiguration conf = (KnowledgeSessionConfiguration) ctx.getBean("sessionConfiguration");
        conf.setResources(Collections.singletonList(new ResourceWrapper(new ClassPathResource("stateful/task.drl"), ResourceType.DRL)));
        conf.afterPropertiesSet();
        maker.afterPropertiesSet();
        CamelContext camelCtx = new DefaultCamelContext();
        DefaultSessionInitializer initializer = (DefaultSessionInitializer) ctx.getBean("sessionInitializer");
        initializer.setCamelContext(camelCtx);
    }
    
    @Test
    public void sanitizerTest() {
        List<DatabaseObjectWrapper> l = sanitizer.getEvents(new MultiKey("123", "1"));
        Assert.assertEquals(2, l.size());
        for (DatabaseObjectWrapper wrapper : l) {
            Assert.assertNotNull(sanitizer.toObject(wrapper));
        }
    }
    
    @Test
    public void dirtySessionTest() throws Exception {
        Map<MultiKey, Boolean> m = sessionDAO.loadSessions(10000);
        Assert.assertTrue(m.get(new MultiKey("123", "2")));
        Assert.assertFalse(m.get(new MultiKey("123", "1")));
    }
    
//    @Test
//    public void migrationTest() {
//        MultiKey id = new MultiKey("123", "1");
//        SessionWithIdentifier ses = maker.makeSession(id, null);
//        Assert.assertEquals(2, ses.getSession().getFactHandles().size());
//        List<DatabaseObjectWrapper> objects = sessionDAO.loadObjectsForSession(id);
//        Assert.assertEquals(2, objects.size());  
//        ses.getSession().fireAllRules();
//        Assert.assertEquals(5, objects.size());
//        Calendar lastEventDate = Calendar.getInstance();
//        for (DatabaseObjectWrapper wrapper : objects) {
//            if (wrapper.getType().equals("EventExx") && wrapper.getTimestampAsDate().after(lastEventDate.getTime())) {
//                lastEventDate.setTimeInMillis(wrapper.getTimestamp());
//            }
//        }
//        lastEventDate.add(Calendar.DAY_OF_YEAR, 10);
//        Date nextTimer = Utils.getDateOfNextScheduled(ses.getSession());
//        Assert.assertTrue("not less", lastEventDate.getTimeInMillis() < nextTimer.getTime() + 30000);
//        Assert.assertTrue("not greater", lastEventDate.getTimeInMillis() > nextTimer.getTime() - 30000);
//    }
    
    @Test
    public void loadSessionTest() throws Exception {
        MultiKey id = new MultiKey("123", "1");
        SessionWithIdentifier ses = maker.makeSession(id, null);
        Assert.assertEquals(2, ses.getSession().getFactHandles().size());
        log.debug("fired: "+ses.getSession().fireAllRules());
        log.debug(Utils.getDateOfNextScheduled(ses.getSession()));
    }
    
}
