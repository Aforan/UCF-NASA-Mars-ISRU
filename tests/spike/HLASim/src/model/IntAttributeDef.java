package model;

/**
 * Created by Andrew on 10/9/2016.
 */
public class IntAttributeDef extends AttributeDef {

    /**
     *
     * Simple integer attribute definition of form
     *
     * byte[] = "int"
     * object = int
     *
     * @param name
     */
    public IntAttributeDef(String name) {
        super(name);
    }

    @Override
    public byte[] encode(Object value) {
        int data = (int) value;
        return (""+data).getBytes();
    }

    @Override
    public Object decode(byte[] bytes) {
        return null;
    }


}
