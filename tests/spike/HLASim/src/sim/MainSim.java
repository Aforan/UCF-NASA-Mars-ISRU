package sim;

import federate.Federate;
import model.BallEntity;
import model.BallEntityDef;
import model.EntityDef;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 *
 * This is a very basic test example to test the underlying sim framework.
 *
 * I suggest tracing through the execution as follows:
 * 1. Familiarize yourself with the following classes:
 *    Federate, FederateState, all classes in model package
 *
 * 2. Trace through initialization starting below
 *
 * 3. Trace through sim execution starting in run()
 *    This is a very simplistic way to handle simulation execution
 *    A Simple Tick() to update the federate and all entities.
 *
 * Created by Andrew on 10/8/2016.
 */
public class MainSim {

    private String[] args;
    private boolean running;
    private Federate federate;
    private List<EntityDef> entityDefinitions;

    public MainSim(String[] args) {
        this.args = args;
        this.running = false;

        this.initialize();
    }

    public void initialize() {
        try {

            URL fddPath = new File("MainSim.xml").toURL();

            this.federate = new Federate("Main Sim Federation", "Main Sim Federate", fddPath);
            this.federate.addEntityDefinition(new BallEntityDef(this.federate));

            if(!this.federate.initialize()) {
                System.out.println("ERROR: Could not initialize federate");
                System.exit(-1);
            }

            for(int i=0; i< 10; i++) {
                BallEntity entity = new BallEntity(0+i, 0+i, (5*i)%7, (8*i)%11, 1, this.federate, this.federate.getEntityDefForClass("BallEntity"));
                federate.addEntity(entity);
            }

        } catch (Exception e) {
            System.out.println("ERROR: Unhandled outer exception initializing simulation: " + e);
            e.printStackTrace();

            System.exit(-1);
        }
    }

    public void run() {
        this.running = true;

        while(this.running) {
            try {
                this.federate.tick();
                Thread.sleep(200);
            } catch(Exception e) {
                System.out.println("ERROR: Unhandled outer exception in run: " + e);
                e.printStackTrace();

                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) {
        MainSim sim = new MainSim(args);
        sim.run();
    }


}
