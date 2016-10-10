package federate;

import hla.rti1516e.NullFederateAmbassador;

/**
 *
 * TODO: Move all RTIambassador specific functionality here?
 *
 * Why do we need an rtiAmbassador AND a custom ambassador implementation?
 *
 *
 * Created by Andrew on 10/8/2016.
 */
public class SimFederateAmbassador extends NullFederateAmbassador {

    private FederateState federateState;

    public SimFederateAmbassador(FederateState federateState) {
        this.federateState = federateState;
    }

}
