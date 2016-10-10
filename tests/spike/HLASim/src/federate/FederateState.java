package federate;

import hla.rti1516e.*;
import model.Entity;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andrew on 10/8/2016.
 */
public class FederateState {

    private FederateHandle federateHandle;
    private ConcurrentHashMap<String, ObjectClassHandle> classNameToObjectHandleMap;

    //  Entity data
    private ConcurrentHashMap<String, ObjectInstanceHandle> entityIdToInstanceHandleMap;
    private ConcurrentHashMap<String, Entity> entityIdToInstanceMap;

    public FederateHandle getFederateHandle() {
        return federateHandle;
    }

    /**
     *
     * Federate state encapsulates information needed by a federate during execution
     * which can be accessed in a thread-safe way.  This makes the FederateState
     * useful for asynchronous functionality such as a UI thread, which would
     * read the federateState concurrently with the federate thread and draw the UI.
     *
     * This pattern was used in the MAK examples frequently, may not be necessary.
     * Making data thread-safe and encapsulating within the Federate class
     * would also work for the purposes described.
     *
     */
    public FederateState() {
        this.classNameToObjectHandleMap = new ConcurrentHashMap<String, ObjectClassHandle>();
        this.entityIdToInstanceHandleMap = new ConcurrentHashMap<String, ObjectInstanceHandle>();
        this.entityIdToInstanceMap = new ConcurrentHashMap<String, Entity>();
    }

    public void setFederateHandle(FederateHandle federateHandle) {
        this.federateHandle = federateHandle;
    }

    public void addEntity(Entity entity) {
        assert entity.getInstanceHandle() != null;
        this.entityIdToInstanceHandleMap.put(entity.getId(), entity.getInstanceHandle());
        this.entityIdToInstanceMap.put(entity.getId(), entity);
    }

    public Collection<Entity> getEntityCollection() {
        return this.entityIdToInstanceMap.values();
    }
}
