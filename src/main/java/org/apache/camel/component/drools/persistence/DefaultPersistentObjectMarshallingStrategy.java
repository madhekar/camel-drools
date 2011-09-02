package org.apache.camel.component.drools.persistence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.camel.component.drools.dao.SessionDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.marshalling.ObjectMarshallingStrategy;

/**
 * @author pjagielski
 */

public class DefaultPersistentObjectMarshallingStrategy implements
        ObjectMarshallingStrategy {

    protected int sessionId;
    protected SessionDAO sessionDAO;
    protected DatabaseObjectWrapperAssembler assembler;
    protected Log log = LogFactory.getLog(getClass());

    public DefaultPersistentObjectMarshallingStrategy(int sessionId, SessionDAO sessionDAO, DatabaseObjectWrapperAssembler assembler) {
        this.sessionId = sessionId;
        this.sessionDAO = sessionDAO;
        this.assembler = assembler;
    }
    
    public boolean accept(Object object) {
        return true;
    }

    public Object read(ObjectInputStream os) throws IOException, ClassNotFoundException {
        int dbId = os.readInt();
        DatabaseObjectWrapper wrapper = sessionDAO.loadObjectData(dbId);
        log.debug("Loaded: " + wrapper + ", sessionId: " + sessionId);
        return assembler.toObject(wrapper);
    }

    public void write(ObjectOutputStream os, Object object) throws IOException {
        DatabaseObjectWrapper wrapper = assembler.fromObject(object, sessionId);
        log.debug("Saving: " + wrapper.getType() + "[" + object + "], sessionId: " + sessionId);
        int dbId = sessionDAO.saveObjectData(wrapper, sessionId);
        os.writeInt(dbId);
    }

}
