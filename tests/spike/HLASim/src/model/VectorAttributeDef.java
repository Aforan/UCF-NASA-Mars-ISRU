package model;

/**
 * Created by Andrew on 10/9/2016.
 */
public class VectorAttributeDef extends AttributeDef {


    /**
     *
     * Simple vector attribute of form
     *
     * byte[] = "(x,y)"
     * object = int[] {x, y}
     *
     * @param name
     */
    public VectorAttributeDef(String name) {
        super(name);
    }

    @Override
    public byte[] encode(Object value) {
        int[] data = (int[]) value;
        return ("("+data[0]+","+data[1]+")").getBytes();
    }

    @Override
    public Object decode(byte[] bytes) {
        return null;
    }
}
