package model;

import federate.Federate;

/**
 * Created by Andrew on 10/8/2016.
 */
public class BallEntityDef extends EntityDef {

    private static AttributeDef[] attributeDefs = {
                            new VectorAttributeDef("PositionVector"),
                            new VectorAttributeDef("VelocityVector"),
                            new IntAttributeDef("Radius")};


    /**
     *
     * A Java Representation of the BallEntity class defined in the XML.  See
     * XML for details.
     *
     * @param federate
     */
    public BallEntityDef(Federate federate) {
        super("BallEntity", attributeDefs, federate);
    }

}
