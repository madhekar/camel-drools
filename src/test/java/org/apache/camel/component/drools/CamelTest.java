/**
 * 
 */
package org.apache.camel.component.drools;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.camel.component.drools.DroolsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
/**
 * @author mproch
 * 
 */
public class CamelTest {

	DefaultCamelContext ctx;
	
	@Before
	public void makeContext() throws Exception {
		ctx = new DefaultCamelContext();
		ctx.addComponent("drools", new DroolsComponent(ctx));
		ctx.start();
	}
	
	@Test
	public void test() throws Exception {
		Object ret = ctx.createProducerTemplate().requestBody("drools:camel.drl", new A("a"));
		assertEquals("A", ret);
	}

	@Test
	public void testCol() throws Exception {
		Object ret = ctx.createProducerTemplate().requestBody("drools:camel.drl", 
				Arrays.asList(new A("b"),new A("c")));
		assertEquals("B", ret);
	}

    @Test
    public void testDep() throws Exception {
        Object ret = ctx.createProducerTemplate().requestBody("drools:camel.drl", new A("d")); 
        assertEquals("B", ret);
    }


}
