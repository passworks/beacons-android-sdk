package io.passworks.lighthouse.model;

import io.passworks.lighthouse.Lighthouse;
import io.passworks.lighthouse.model.enums.Proximity;

/**
 * Created by ivanbruel on 17/09/15.
 */
public class BeaconProxy {

    public static void changeBeaconProximity(Beacon beacon, Proximity proximity) {
        Proximity previousProximity = beacon.getProximity();
        beacon.setProximity(Lighthouse.getInstance().getContext(),
                Lighthouse.getInstance().getToken(), proximity, true,
                Lighthouse.getInstance().getEventsTriggerFilter(),
                Lighthouse.getInstance().getNotificationsManager(),
                Lighthouse.getInstance().getEventsListener());
        if (previousProximity != proximity && Lighthouse.getInstance().getBeaconsListener() != null) {
            Lighthouse.getInstance().getBeaconsListener().beaconChangedProximity(beacon);

        }
    }

}
