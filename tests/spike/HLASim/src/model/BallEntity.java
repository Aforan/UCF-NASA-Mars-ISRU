package model;

import federate.Federate;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;

/**
 * Created by Andrew on 10/8/2016.
 */
public class BallEntity extends Entity {

    private int x, y, vx, vy, r;

    /**
     *
     * Example of a simple entity.  Real attribute values can be stored in
     * object values (x, y) which should then be de/serialized by the
     * encode/decode methods defined by the attribute definition.
     *
     * @param x
     * @param y
     * @param vx
     * @param vy
     * @param r
     * @param federate
     * @param entityDef
     * @throws NameNotFound
     * @throws NotConnected
     * @throws RTIinternalError
     * @throws FederateNotExecutionMember
     */
    public BallEntity(int x, int y, int vx, int vy, int r, Federate federate, EntityDef entityDef)
        throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember {

        super(entityDef, federate);

        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.r = r;
    }

    /**
     *
     * Do the update calculations, in this case we simply "move" the ball
     * updating the object attributes (this.x, this.y) first and then
     * updating the AttributeNameToValue and AttributeHandleValue maps.
     *
     */
    @Override
    public void tick() {
        this.x += this.vx;
        this.y += this.vy;

        this.updateAttributes();

        System.out.println(this.getClassName() + "(" + this.getInstanceHandle() + ")");
        for(String attrName : this.getAttributeNames()) {
            System.out.println("\tattrName=" + new String(this.getAttributeBytes(attrName)));
        }
    }

    /**
     *
     * Make the proper calls to this.updateAttribute for all attributes, can probably
     * refactor this to be more generic.  This works for now.
     *
     */
    @Override
    public void updateAttributes() {
        this.updateAttribute("PositionVector", new int[] {this.x, this.y});
        this.updateAttribute("VelocityVector", new int[] {this.vx, this.vy});
        this.updateAttribute("Radius", this.r);
    }
}
