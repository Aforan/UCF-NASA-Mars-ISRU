package model;

import hla.rti1516e.AttributeHandle;

/**
 * Created by Andrew on 10/8/2016.
 */
public abstract class AttributeDef {

    protected String name;
    protected AttributeHandle handle;


    /**
     *
     * An attribute definition encapsulates definition data for all attributes
     * Most importantly is the handle to the HLA attribute definition, which
     * is used to instantiate/access/update attributes on each entity as well
     * as encode/decode functionality which provides a deterministic way to
     * de/serialize byte[]/object representations of the attribute value.
     *
     * @param name
     */
    public AttributeDef(String name) {
        this.name = name;
    }

    public String getName() { return this.name; }
    public AttributeHandle getHandle() { return this.handle; }

    public void setHandle(AttributeHandle handle) {
        //  We can't use a null attribute handle, if this is null
        //  Something went wrong in setup and we want to error now
        assert handle != null;
        this.handle = handle;
    }

    public abstract byte[] encode(Object value);
    public abstract Object decode(byte[] bytes);
}
