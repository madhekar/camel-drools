package org.apache.camel.component.drools;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.spi.Registry;

/**
 * @author mproch
 *
 */
public class MapRegistry implements Registry {

	Map<String, Object> map = new HashMap<String, Object>();
	
	public Object lookup(String name) {
		return map.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T lookup(String name, Class<T> type) {
		return (T) map.get(name);
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void add(String name, Object value) {
		map.put(name, value);
	}

    public <T> Map<String, T> lookupByType(Class<T> type) {
        return Collections.emptyMap();
    }

}
