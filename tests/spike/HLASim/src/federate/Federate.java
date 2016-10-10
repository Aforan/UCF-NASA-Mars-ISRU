package federate;

import hla.rti1516e.*;
import model.Entity;
import model.EntityDef;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by Andrew on 10/8/2016.
 */
public class Federate {

    private static final String federateType = "JavaFederate";
    private static final CallbackModel CALLBACK_MODEL = CallbackModel.HLA_IMMEDIATE;

    private boolean connected;

    private List<EntityDef> entityDefinitions;

    private String federationName, federateName;
    private FederateAmbassador federateAmbassador;
    private FederateHandle federateHandle;
    private FederateState federateState;
    private RTIambassador rtiAmbassador;
    private URL fddPath;

    private HashMap<String, EntityDef> classNameToEntityDefMap;

    /**
     * The federate class encapsulates all data and functionality required of
     * an HLA federate.
     *
     *
     * @param federationName
     * @param federateName
     * @param fddPath
     */
    public Federate(String federationName, String federateName, URL fddPath) {
        this.federationName = federationName;
        this.federateName = federateName;
        this.fddPath = fddPath;
        this.connected = false;

        this.classNameToEntityDefMap = new HashMap<String, EntityDef>();
        this.entityDefinitions = new LinkedList<>();
    }

    /**
     *
     * Initialize must be called on the federate before starting sim execution.
     * Initialize connects to the HLA, creates and connects to the Federation
     * Execution and initializes all object classes and interactions.
     *
     * TODO: Interactions
     *
     * @return
     */
    public boolean initialize() {
        this.connected = false;
        this.connect();

        if (!this.connected) {
            System.out.println("ERROR: Could not connect");
            return false;
        }

        if(!this.createFedEx()) {
            System.out.println("ERROR: Could not create FedEx");
            return false;
        }

        if(!this.joinFedEx()) {
            System.out.println("ERROR: Could not join FedEx");
            return false;
        }

        if(!this.initializeEntityDefinitions()) {
            System.out.println("ERROR: Could initialize entity Definitions");
            return false;
        }

        System.out.println("SUCCESS: Federate initialized!");
        return true;

    }

    /**
     * Add an entity definition, make sure that all entity definitions
     * are added before beginning the simulation!
     *
     *
     * @param entityDef
     */
    public void addEntityDefinition(EntityDef entityDef) {
        this.entityDefinitions.add(entityDef);
    }

    /**
     *
     * Initialize all the entity definitions that have been added.
     * Works as follows:
     * 1.   First initialize all federation attributes on the definition
     *      IE: get and set the ObjectClassHandle for the entity
     * 2.   Initialize all the attributes (get the attribute handles)
     *      and publish the set of attributes for each class.
     * 3.   Set up the ClassName -> EntityDef mappings
     *
     * @return
     */
    public boolean initializeEntityDefinitions() {
        for(EntityDef entityDef : this.entityDefinitions) {
            try {
                entityDef.initializeFederationAttributes();

                this.rtiAmbassador.publishObjectClassAttributes(
                                    entityDef.getClassHandle(),
                                    entityDef.getAttributeHandleSet());

                this.evokeCallbacks(0.1, 0.2);

                this.classNameToEntityDefMap.put(entityDef.getClassName(), entityDef);
                System.out.println("SUCCESS: Initialized entityDef: " + entityDef.getClassName());

            } catch (Exception e) {
                String msg = "ERROR: Could not initialize entityDef " + entityDef.getClassName()
                        + "\n\tGot Exception " +e;
                System.out.println(msg);
                e.printStackTrace();

                return false;
            }
        }

        System.out.println("SUCCESS: All entityDefs initialized!");
        return true;
    }

    /**
     *
     * Create the Federation Execution (Call to rtiAmbassador) must succeed.
     *
     * @return
     */
    public boolean createFedEx() {
        if(this.federationName == null
        || this.federateName == null
        || this.fddPath == null
        || this.rtiAmbassador == null) {
            String msg = "ERROR: Could not create FedEx something is null: " +
                         "\n\tfederationName=" + this.federationName +
                         "\n\trtiAmbassador=" + this.rtiAmbassador +
                         "\n\tfederateName=" + this.federateName +
                         "\n\tfddPath=" + this.fddPath;

            System.out.println(msg);
            return false;
        }

        try {
            this.rtiAmbassador.createFederationExecution(this.federationName, this.fddPath);
            System.out.println("SUCCESS: Created FedEx!");

            return true;
        } catch (Exception e) {
            System.out.println("ERROR: Unhandled exception creating FedEx : " + e);
            e.printStackTrace();

            return false;
        }
    }


    /**
     *
     * Join the federation execution, must succeed
     *
     * @return
     */
    public boolean joinFedEx() {
        if(this.federationName == null
                || this.federateName == null
                || federateType == null
                || this.federateState == null) {
            String msg = "ERROR: Could not join FedEx something is null: " +
                    "\n\tfederationName=" + this.federationName +
                    "\n\tfederateName=" + this.federateName +
                    "\n\tfederateState=" + this.federateState +
                    "\n\tfederateType=" + federateType;

            System.out.println(msg);
            return false;
        }

        boolean joined = false;
        int maxTries = 10;
        int tries = 0;

        while(!joined && (tries++ < maxTries)) {
            try {
                this.federateHandle = rtiAmbassador.joinFederationExecution(
                        this.federateName, federateType, this.federationName);

                this.federateState.setFederateHandle(this.federateHandle);

                joined = true;
            } catch(Exception e) {
                String msg = "WARNING: Could not join federation on attempt " + tries
                            + "\n\tGot Exception : " + e;

                System.out.println(msg);
            }
        }

        if(!joined) {
            System.out.println("ERROR: Could not join FedEx, timed out after " + tries + " attempts");
            return false;
        } else {
            this.evokeCallbacks(0.1, 0.2);
            System.out.println("SUCCESS: Joined FedEx! (" + this.federateHandle + ")");

            return true;
        }
    }

    /**
     *
     * Initialize the FederateState/Ambassador and Connect to the HLA
     *
     */
    public void connect() {
        try {
            RtiFactory factory = RtiFactoryFactory.getRtiFactory();

            this.rtiAmbassador = factory.getRtiAmbassador();

            this.federateState = new FederateState();
            this.federateAmbassador = new SimFederateAmbassador(this.federateState);

            this.rtiAmbassador.connect(this.federateAmbassador, CALLBACK_MODEL);
            this.connected = true;

            System.out.println("SUCCESS: Connected to HLA successfully!");
        } catch(Exception e) {
            this.connected = false;

            System.out.println("ERROR: Unhandled exception connecting to federation : " + e);
            System.out.println(this);
            e.printStackTrace();
        }
    }

    /**
     *
     * Add an entity to this federate by storing it in federateState and
     * registering a new instance with the HLA.
     *
     * @param entity
     */
    public void addEntity(Entity entity) {
        if(entity.getInstanceHandle() == null) {
            try {
                ObjectClassHandle classHandle = entity.getEntityDefinition().getClassHandle();
                this.evokeCallbacks(0.1, 0.2);

                //  This "instantiates" the instance within the HLA and returns
                //  A handle to access the object later on
                ObjectInstanceHandle instanceHandle =
                    this.rtiAmbassador.registerObjectInstance(classHandle);

                //  Entities must have an instance handle once they've been instantiated
                entity.setInstanceHandle(instanceHandle);
                this.federateState.addEntity(entity);
                this.evokeCallbacks(0.1, 0.2);

                //  After adding the entity, update the entity attributes internally
                entity.updateAttributes();

                //  Push the initialized attributes to the HLA
                this.updateEntityAttributes(entity);
                System.out.println("SUCCESS: Added " + entity.getClassName() + " entity!");
            } catch(Exception e) {
                String msg = "ERROR: Could not add entity " + entity + " to federation"
                           + "\n\tGot exception : " + e;
                System.out.println(msg);
                e.printStackTrace();
            }
        } else {
            System.out.println("WARNING: Attempting to add registered entity " + entity);
        }
    }


    /**
     *
     * Update the HLA attributes for an entity.  The entity should have been ticked
     * before calling (or else old data will get pushed to the HLA)
     *
     * @param entity
     */
    public void updateEntityAttributes(Entity entity) {
        try {
            String tag = "TAG: " + entity.getClassName() + "-" + System.currentTimeMillis();

            //  The AttributeHandleValueMap is a mapping from AttributeHandle -> byte[]
            //  Each mapped value represents the value stored for each attribute on this instance.
            //  This scheme allows HLA to easily maintain object instance representations
            //  in a uniform, generic way (IE: we don't need to define classes in the simulator,
            //  rather use an instanceHandle to point to the instance of the object and pass the
            //  mappings which are expected to comply with the entity as defined in the XML)
            AttributeHandleValueMap attributeHandleValueMap = entity.getAttributeHandleValueMap();
            this.rtiAmbassador.updateAttributeValues(entity.getInstanceHandle(), attributeHandleValueMap, tag.getBytes());
        } catch(Exception e) {
            String msg = "ERROR: Could not update attributes for entity " + entity
                       + "\n\tGot Exception : " + e;

            System.out.println(msg);
        }
    }

    /**
     *
     * Not sure how the implementation of this should look yet, blank for now
     * TODO
     * @param a
     * @param b
     */
    public void evokeCallbacks(double a, double b) {

    }

    public FederateState getFederateState() {
        return this.federateState;
    }

    public RTIambassador getRtiAmbassador() {
        return this.rtiAmbassador;
    }

    public EntityDef getEntityDefForClass(String className) {
        return this.classNameToEntityDefMap.get(className);
    }

    /**
     *
     * Tick the federate state.  Each federate tick consists of an update
     * of all the internal representation of entities owned by this federate
     * and a call to this.updateEntityAttributes which will push the internal
     * representations to the HLA via the rtiAmbassador.
     *
     */
    public void tick() {
        for(Entity entity : this.federateState.getEntityCollection()) {
            //  Tick the entity and push all the attributes to the HLA
            entity.tick();
            this.updateEntityAttributes(entity);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
