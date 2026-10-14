package nl.schnobias.personaltools;
import org.junit.Test;
import static org.junit.Assert.*;
public class BatteryNotificationPolicyTest {
    @Test public void disconnectOverrideDoesNotBypassUpdates(){assertTrue(BatteryNotificationPolicy.shouldNotify(true,80,30,true,true));assertFalse(BatteryNotificationPolicy.shouldNotify(true,80,30,false,true));assertFalse(BatteryNotificationPolicy.shouldNotify(true,80,30,true,false));}
    @Test public void unknownDisconnectCanBeExplicitlyAllowed(){assertTrue(BatteryNotificationPolicy.shouldNotify(true,-1,30,true,true));assertFalse(BatteryNotificationPolicy.shouldNotify(true,-1,30,false,true));}
    @Test public void includesBoundaryAndExcludesAbove(){assertTrue(BatteryNotificationPolicy.shouldNotify(true,30,30));assertFalse(BatteryNotificationPolicy.shouldNotify(true,31,30));}
    @Test public void filterSkipsUnknownButOffPreservesUnavailableReminder(){assertFalse(BatteryNotificationPolicy.shouldNotify(true,-1,30));assertTrue(BatteryNotificationPolicy.shouldNotify(false,-1,30));}
    @Test public void supportsZeroAndFullThreshold(){assertTrue(BatteryNotificationPolicy.shouldNotify(true,0,0));assertFalse(BatteryNotificationPolicy.shouldNotify(true,1,0));assertTrue(BatteryNotificationPolicy.shouldNotify(true,100,100));}
}
