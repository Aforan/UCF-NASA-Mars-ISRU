package model;

import federate.Federate;
import hla.rti1516e.*;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Andrew on 10/8/2016.
 *
 */
public abstract class EntityDef {

    private int numAttributes;

    private String className;
    private Federate federate;
    private ObjectClassHandle classHandle;
    private AttributeHandleSet attributeHandleSet;

    private String[] attributeNames;
    private HashMap<String, AttributeDef> attributeNameToDefMap;
    private HashMap<AttributeHandle, AttributeDef> attributeHandleToDefMap;

    /**
     *
     * Entity Definitions are the Java representation of objects defined by the
     * HLA XML file.  Definitions provide encapsulation of federate-wide
     * definition variables such as the ObjectClassHandle and AttributeHandleSet.
     * They also provide access to attribute definitions and their encode/decode.
     *
     * @param className
     * @param attributeDefs
     * @param federate
     */
    public EntityDef(String className, AttributeDef[] attributeDefs, Federate federate) {
        this.numAttributes = -1;
        this.federate = federate;
        this.className = className;

        this.attributeNameToDefMap = new HashMap<>();
        for(AttributeDef def : attributeDefs) {
            this.attributeNameToDefMap.put(def.getName(), def);
        }
    }

    /**
     *
     * Initialize all the attributes for which connection to the FedEx is required
     * (ObjectClassHandle, AttributeHandleSet both require factory methods from HLA)
     *
     */
    public void initializeFederationAttributes() {
        assert this.federate.isConnected();

        //  These must complete successfully for all definitions
        assert this.initializeObjectHandle();
        assert this.initializeAttributeHandleSet();

        System.out.println("SUCCESS: InitializedFederationAttributes entityDef: " + getClassName());
    }

    /**
     * Initialize the handle which points to the HLA class definition for this ClassName
     * Must be connected to the fedex or getObjectClassHandle will fail.
     *
     * @return
     */
    private boolean initializeObjectHandle() {
        try {
            this.classHandle = this.federate.getRtiAmbassador()
                    .getObjectClassHandle(this.className);
            System.out.println("SUCCESS: initializeObjectHandle : " + this.classHandle);
            return true;
        } catch(Exception e) {
            String msg = "ERROR: Exception initializingObjectHandle for class : " + this.className
                       + "Got Exception : " + e;

            System.out.println(msg);
            return false;
        }
    }

    /**
     *
     * Get/Create the ObjectClassHandle
     *
     * @return
     */
    public ObjectClassHandle getClassHandle() {
        if(this.classHandle == null) {
            this.initializeObjectHandle();
            assert this.classHandle != null;
        }

        return this.classHandle;
    }

    /**
     *
     * Create the AttributeHandleSet for this class.  AttributeHandleSets are a set
     * of attribute handles for all the attributes belonging to this class.  Must
     * be connected to the FedEx.
     *
     * @return
     */
    private boolean initializeAttributeHandleSet() {
        try {
            AttributeHandleSetFactory factory = this.federate.getRtiAmbassador().getAttributeHandleSetFactory();
            this.attributeHandleSet = factory.create();
            this.attributeHandleToDefMap = new HashMap<>();

            for(AttributeDef attributeDef : this.attributeNameToDefMap.values()) {
                AttributeHandle handle = this.federate.getRtiAmbassador().getAttributeHandle(
                                                    this.getClassHandle(), attributeDef.getName());

                attributeDef.setHandle(handle);
                this.attributeHandleSet.add(handle);
                this.attributeHandleToDefMap.put(handle, attributeDef);
            }
            System.out.println("SUCCESS: attributeHandleSet : " + this.attributeHandleSet);
            return true;
        } catch(Exception e) {
            String msg = "ERROR: Exception initializeAttributeHandleSet for class : " + this.className
                       + "Got Exception : " + e;

            System.out.println(msg);
            return false;
        }
    }

    /**
     *
     * Get/Create the AttributeHandleSet
     *
     * @return
     */
    public AttributeHandleSet getAttributeHandleSet() {
        if(this.attributeHandleSet == null) {
            this.initializeAttributeHandleSet();
            assert this.attributeHandleSet != null;
        }

        return this.attributeHandleSet;
    }

    public String getClassName() {
        return this.className;
    }

    /**
     *
     * Convenience
     *
     * @return
     */
    public int getNumAttributes() {
        if(this.numAttributes == -1) {
            this.numAttributes = this.attributeNameToDefMap.values().size();
        }

        return this.numAttributes;
    }

    /**
     *
     * Convenience
     *
     * @return
     */
    public String[] getAttributeNames() {
        if(this.attributeNames == null) {
            this.attributeNames = new String[this.getNumAttributes()];

            int i = 0;

            for(AttributeDef attrDef : this.attributeNameToDefMap.values()) {
                this.attributeNames[i] = attrDef.getName();
                i++;
            }
        }

        return this.attributeNames;
    }

    public Collection<AttributeDef> getAttributeDefinitions() {
        return this.attributeNameToDefMap.values();
    }

    public AttributeDef getAttributeDef(String attributeName) {
        return this.attributeNameToDefMap.get(attributeName);
    }
}
