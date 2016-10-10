package model;

import federate.Federate;
import hla.rti1516e.*;

import java.util.HashMap;

/**
 * Entity classes encapsulate data related to
 * entities in the simulation.
 *
 *
 *
 * Created by Andrew on 10/8/2016.
 */
public abstract class Entity {

    //  A unique id for this entity instance
    private String id;

    //  The className for the type of this entity (as defined in the XML)
    private String className;

    //  The federate this entity belongs to
    private Federate federate;

    //  The definition of the entity
    private EntityDef definition;

    //  A handle to the HLA object instance (Needed to access/update in-sim entity)
    private ObjectInstanceHandle instanceHandle;

    //  A mapping from AttributeHandles -> byte[] values for
    //  all the attributes belonging to this entity instance
    private AttributeHandleValueMap attributeHandleValueMap;

    //  A mapping from AttributeNames -> Object representations
    //  of their value.  Objects will need to be encoded to byte[]
    //  For processing by the HLA (IE: translate to attributeHandleValueMap)
    private HashMap<String, Object> attributeNameToValueMap;

    /**
     *
     * Entity objects are java representations of simulation objects.  Entities are defined
     * by their ObjectClassHandle which points to their HLA definition and is needed for
     * calls to instantiate HLA objects of this type.  Entities are also defined by an
     * instance of EntityDef which defines how the Java object representation relates
     * to the XML defined HLA objects.
     *
     * @param definition
     * @param federate
     */
    public Entity(EntityDef definition, Federate federate) {
        //  Simple, reasonably valid way of creating a unique entity id.
        this.id = className + System.currentTimeMillis();
        this.definition = definition;
        this.federate = federate;

        this.className = this.definition.getClassName();

        //  Create the attributeNameToValueMap and attributeHandleValueMap
        this.initializeAttributes();
    }

    public void setInstanceHandle(ObjectInstanceHandle instanceHandle) {
        this.instanceHandle = instanceHandle;
    }

    /**
     *
     * Initialize the attributeNameToValueMap and the attributeHandleValueMap
     *
     */
    private void initializeAttributes() {
        this.attributeNameToValueMap = new HashMap<>();
        this.createAttributeHandleValueMap();

        for(AttributeDef attributeDef : this.definition.getAttributeDefinitions()) {
            this.attributeNameToValueMap.put(attributeDef.getName(), null);
            this.attributeHandleValueMap.put(attributeDef.getHandle(), new byte[] {});
        }
    }

    public String getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public EntityDef getEntityDefinition() { return this.definition; }

    public ObjectInstanceHandle getInstanceHandle() {
        return instanceHandle;
    }

    public String[] getAttributeNames() {
        return this.federate.getEntityDefForClass(this.className).getAttributeNames();
    }

    /**
     * Get/Create the attributeHandleValueMap
     *
     * @return
     */
    public AttributeHandleValueMap getAttributeHandleValueMap() {
        if(this.attributeHandleValueMap == null) {
            this.createAttributeHandleValueMap();
            assert this.attributeHandleValueMap != null;
        }

        return this.attributeHandleValueMap;
    }

    /**
     * Simply create the attributeHandleValueMap, don't populate with anything
     *
     */
    private void createAttributeHandleValueMap() {
        try {
            AttributeHandleValueMapFactory factory;
            factory = this.federate.getRtiAmbassador()
                          .getAttributeHandleValueMapFactory();

            this.attributeHandleValueMap = factory.create(this.definition.getNumAttributes());
        } catch(Exception e) {
            String msg = "ERROR: Exception creating attribute handle to value map : " + e;
            e.printStackTrace();
            System.out.println(msg);
        }
    }

    /**
     *
     * Get the byte representation of the attribute value of attrName by using
     * the decode method defined by the corresponding attribute definition
     *
     * @param attrName
     * @return
     */
    protected byte[] getAttributeBytes(String attrName) {
        AttributeDef def = this.definition.getAttributeDef(attrName);
        return def.encode(this.attributeNameToValueMap.get(attrName));
    }

    /**
     *
     * Update the internal value of the attribute, not to be confused with
     * Federate.updateAttribute which will update the attribute values
     * within the global HLA scope.  Attributes updates here will not
     * be reflected within other federates until federate.updateAttributes
     * is called for this entity.
     *
     * @param attrName
     * @param attrValue
     */
    protected void updateAttribute(String attrName, Object attrValue) {
        AttributeHandle handle = this.definition.getAttributeDef(attrName).getHandle();

        //  Update the internal representation of the attribute first
        //  Then update the handle->value map
        this.attributeNameToValueMap.put(attrName, attrValue);

        if(this.attributeHandleValueMap != null) {
            this.attributeHandleValueMap.put(handle, this.getAttributeBytes(attrName));
        }
    }

    /**
     *
     * Child classes must define a tick method which will do all
     * update calculations on attributes and update them accordingly.
     *
     */
    public abstract void tick();

    /**
     *
     * Child classes must define a method to call updateAttribute on all defined attributes.
     * This can probably be refactored in a generic way so subclasses won't have to implement.
     *
     */
    public abstract void updateAttributes();
}
