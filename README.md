# Passworks Beacon Management Platform

Lighthouse is the SDK that connects Passworks.io beacons service to your Android application.
In the Passworks.io dashboard you can add your own beacons, manage your tags and the events associated with beacon entry or exit.

## Integration

Add Lighthouse to your `gradle.build` file:

```java
// Build.gradle
dependencies {
	compile 'io.passworks:lighthouse:2.0'
}
```

## Setup

### Step 1 - SDK Initialization

To initialize the SDK all you are required to do is create an instance on your `Activity`'s `onCreate()` method.

```java
public class BeaconsActivity extends Activity {

	private Lighthouse mLighthouse;

    @Override
    public void onCreate() {
        super.onCreate();
        mLighthouse = new Lighthouse(this);
    }
}
```

### Step 2 - AndroidManifest.xml

To make sure the SDK can communicate with the Lighthouse webservice, you should add the SDK token to the `AndroidManifest.xml`.

```xml
<meta-data android:name="LighthouseSDKToken" android:value="YOUR-LIGHTHOUSE-SDK-TOKEN"/>
```

Or set the SDK Token by code:
```java
Lighthouse.setToken("YOUR-LIGHTHOUSE-SDK-TOKEN");
```

#### Step 2.1 - Integration without Gradle

**This step is only necessary if you are not integrating through gradle.**

To make sure your application can scan for beacons and communicate with our servers, it requires the `BLUETOOTH`, `BLUETOOTH_ADMIN` and `INTERNET` permissions to be added to your application. 

Lighthouse also requires youto add the `LighthouseService` to perform actions when the user clicks on a Lighthouse triggered notification.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest
    package="your.package.name"
    xmlns:android="http://schemas.android.com/apk/res/android">

	<!-- Lighthouse permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <user-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
        android:name=".BeaconsApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        
        ...
        
        <service android:name="io.passworks.lighthouse.service.LighthouseService"/>
    </application>

</manifest>

```

### Step 3 - Runtime Permissions (Android Marshmellow 6.0)

Before calling `lightUp()` on `Lighthouse` under Android 6.0, you should check if you have either `ACCESS_COARSE_LOCATION` or `ACCESS_FINE_LOCATION` permissions. 

In case you want Lighthouse to handle it, the following helper functions are available.

```java
public class BeaconsActivity extends Activity {

	private Lighthouse mLighthouse;
	private static int coarsePermissionRequestCode = 0;
	
    @Override
    public void onCreate() {
        super.onCreate();
        mLighthouse = new Lighthouse(this);
        if (mLighthouse.requestCoarseLocationPermission(BeaconsActivity.coarsePermissionRequestCode)) {
        	mLighthouse.lightUp()
        }
    }
    
    @Override
	public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
    switch (requestCode) {
        case coarsePermissionRequestCode: {
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					mLighthouse.lightUp()
            } 
        }
    }
    
}
```


```

## Beacon Scanning

### Starting

Lighthouse allows your app to be on the lookout for nearby beacons, as such, it requires you to turn on the beacon scanning feature by calling the `lightUp()` method on Lighthouse.

```java
mLighthouse.lightUp()
```

This method call will turn on the bluetooth in the user's device and start scanning for beacons.

### Stopping

In case you want to stop ranging for beacons, you can call the `turnOff()` method which will then stop all location based beacon ranging.

```java
mLighthouse.turnOff()
```


### Updating

Lighthouse updates its database in several cases, when the SDK is setup, when the application returns to foreground or when an explicit call to `refresh()` is triggered. This will then refresh all the beacons, events and tags set in the Passworks.io dashboard.

```java
mLighthouse.refresh()
```

### Delegating

Lighthouse will adopt the behavior defined in the Passworks.io dashboard, so any events will be triggered in the background without requiring any aditional code from you. But for a more advanced usage, Lighthouse supports a delegating interfaces for more precise control over functionality.

#### BeaconsListener

To be notified when beacons are refreshed or change proximity you can implement and set the `BeaconsListener` interface.

```java
mLighthouse.setBeaconsListener(new BeaconsListener() {
            @Override
            public void beaconsRefreshed(List<Beacon> beacons) {
                Log.d("Lighthouse", beacons.size() + " beacons in the database.");
            }

            @Override
            public void beaconChangedProximity(Beacon beacon) {
					Log.d("Lighthouse", beacon.getName() + " changed the proximity!");
            }
        });
```

`beaconsRefreshed(List<Beacon>)` is called when the database is refreshed by calling `refresh()` or by resuming the application.

`beaconChangedProximity(Beacon)` is called whenever a beacon changes its proximity.

#### EventsListener

The same can be applied to the `EventsListener` interface, where the application can be notified when an event is triggered.

```java
mLighthouse.setEventsListener(new EventsListener() {
            @Override
            public void eventTriggered(Event event, Beacon beacon) {
                Log.d("Lighthouse", "Triggered event " + event.getName() + " on beacon " + beacon.getName());
            }
        });
```

`eventTriggered(Event, Beacon)` is called whenever an event is triggered, which will afterwards (with the exception of EventType WebHook) trigger a local notification for the user.

#### EventsTriggerFilter

For even greater control on how and when events are triggered, the application can implement a `EventsTriggerFilter` interface to filter out unwanted events.

```java
mLighthouse.setEventsTriggerFilter(new EventsTriggerFilter() {
            @Override
            public boolean shouldTriggerEvent(Event event, Beacon beacon) {
                return event.getName().contains("iOS");
            }
        });
```


`shouldTriggerEvent(Event, Beacon)` is called right before triggering an event, in case the application returns **true** the event will not be triggered.


## Analytics

Lighthouse allows user tracking to get the best out of the analytics on top of the beacons solution. In order to identify the users to the Lighthouse SDK, you can call the `identifyUser()`, `identifyUser(userId)`, `identifyUser(userId, attributes)` and `logout()` methods.

`identifyUser()` will trigger an annonymous identification (in case your application does not require your users to login and signup).

`identifyUser(userId)` will take a `userId` for identification (which can be an e-mail, a username or even an actual id).

`identifyUser(userId, attributes)` allows you to identify a user through a `userId` as well as receiving a dictionary of user attributes which may contain all the user information you want.

`logout()` forces a previously identified user to become anonymous again. (same behavior as `identifyUser()`)

## Database

Lighthouse allows you to dig into the database which is composed by `Beacon`, `Tag` and `Event`. 

The Lighthouse `getInstance()` contains `getBeacons()`, `getBeaconsInRange()`, `getTags()` and `getEvents()`. These represent lists of their corresponding object models mentioned above.

### Tag

The `Tag` model represents the tags associated with beacons, in order to create **sessions**. It is composed by:

| attribute  | type | description |
|------------|------|-----------|
| id | long | the tag identifier in Passworks.io.
| name | String | the tag name.
| timeout | long | the timeout for closing sessions.
| lastSeenAt | Date | the timestamp for the last time the tag was found.
| sessionId | String | a unique session identifier randomly generated.

### Beacon

The `Beacon` model represents the physical beacons and is composed by:

| attribute  | type | description |
|------------|------|-----------|
| id | long | the beacon identifier in Passworks.io.
| name | String | the beacon name.
| uuid | String | the UUID set in the physical beacon.
| major | int | the major identifier set in the physical beacon.
| minor | Int | the minor identifier set in the physical beacon.
| vendorName | String | the name of the beacon's vendor.
| vendorSlug | String | the slug of the beacon's vendor.
| tags | List | a list containing all the associated Tag objects.
| events | List | a list containing all the associated Event objects.
| proximity | Proximity  | the current proximity of the beacon.

### Event

The `Event` model represents the behavior of the Lighthouse framework when it encounters a `Beacon`. When certain conditions align the `Event` should be triggered and a notification will be shown to the user. The model is composed by:

| Attribute  | Type | Description |
|------------|------|-----------|
| id |  long |  the event identifier in Passworks.io.
| name | String | the event name.
| proximity|  Proximity | the proximity in which the event should be triggered.
| triggerOn|  Trigger | a switch between ENTER or EXIT in which the event should be | triggered.
| triggerLimit|  TriggerLimit | ONCE_PER_SESSION, ONCE_PER_LIFETIME and MORE_THAN_ONCE.
| type | | TYPE - a switch between URL, NOTIFICATION, WEBHOOK, IMAGE and PASSBOOK, | which will correspond to the action to take when the user opens up the local notification triggered by the event.
| triggeredAt|  Date  | a timestamp for when the event was last triggered.
| timeout|  long  | the timeout to which the framework should not trigger multiple times the same event.
| payload|  String  | the content to use in the notification action.
| notificationMessage | String  | the message to be presented to the user on the notification.
| startAt|  Date  | the starting date for when the event can be triggered.
| endAt|  Date  | the end date for when the event can no longer be triggered.
| dayStartAt|  Date  | the time of the day from which the event can be triggered.
| dayEndAt|  Date  | the time of the day from which the event can no longer be triggered.
| weekdays|  List  | the weekdays in which the event can be triggered.

## Logging

Lighthouse allows the developer to choose whether to display logs or not, this can be set with the `setVerbose()` call. Verbose mode is enabled by default in debug builds and disabled otherwise.

```java
Lighthouse.setVerbose(true)
```
