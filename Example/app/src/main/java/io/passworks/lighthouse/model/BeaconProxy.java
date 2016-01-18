package io.passworks.lighthouse.model;

import io.passworks.lighthouse.Lighthouse;
import io.passworks.lighthouse.db.DatabaseHelper;
import io.passworks.lighthouse.model.enums.Proximity;
import io.passworks.lighthouse.networking.Networking;
import io.passworks.lighthouse.utils.AppUtils;

/**
 * Created by ivanbruel on 17/09/15.
 */
public class BeaconProxy {

    public static void changeBeaconProximity(Lighthouse lighthouse, Beacon beacon, Proximity proximity) {
        Proximity previousProximity = beacon.getProximity();
        beacon.setProximity(lighthouse.getNetworking(), lighthouse.getDatabaseHelper(), lighthouse.getContext(),
                lighthouse.getToken(), AppUtils.getEnvironment(lighthouse.getContext()), proximity, true,
                lighthouse.getEventsTriggerFilter(),
                lighthouse.getNotificationsManager(),
                lighthouse.getEventsListener());
        if (previousProximity != proximity && lighthouse.getBeaconsListener() != null) {
            lighthouse.getBeaconsListener().beaconChangedProximity(beacon);

        }
    }

}
