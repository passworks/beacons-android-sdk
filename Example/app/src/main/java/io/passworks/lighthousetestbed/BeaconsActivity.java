package io.passworks.lighthousetestbed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;

import io.passworks.lighthouse.Lighthouse;
import io.passworks.lighthouse.beacons.BeaconsListener;
import io.passworks.lighthouse.beacons.EventsListener;
import io.passworks.lighthouse.beacons.EventsTriggerFilter;
import io.passworks.lighthouse.db.DatabaseHelper;
import io.passworks.lighthouse.model.Beacon;
import io.passworks.lighthouse.model.BeaconProxy;
import io.passworks.lighthouse.model.Event;
import io.passworks.lighthouse.model.enums.Proximity;

public class BeaconsActivity extends AppCompatActivity implements BeaconsListener, EventsListener {

    private ListView mListView;
    private BaseAdapter mAdapter;
    private List<Beacon> mBeacons;
    private List<Event> mEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String token = getIntent().getStringExtra("token");
        setContentView(R.layout.activity_beacons);
        Lighthouse.setup(this, token);
        Lighthouse.getInstance().setEventsTriggerFilter(new EventsTriggerFilter() {
            @Override
            public boolean shouldTriggerEvent(Event event, Beacon beacon) {
                return event.getName().contains("Android");
            }
        });
        Lighthouse.getInstance().setBeaconsListener(this);
        Lighthouse.getInstance().setEventsListener(this);
        Lighthouse.getInstance().lightUp();
        mListView = (ListView) findViewById(R.id.listView);
        mBeacons = Lighthouse.getInstance().getBeacons();
        mEvents = Lighthouse.getInstance().getEvents();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mBeacons.size() + mEvents.size();
            }

            @Override
            public Object getItem(int position) {
                return position >= mBeacons.size() ? mEvents.get(position - mBeacons.size()) : mBeacons.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView;
                if (convertView != null) {
                    textView = (TextView) convertView;
                } else {
                    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                    textView = (TextView) layoutInflater.inflate(android.R.layout.simple_list_item_1, null, false);
                }
                textView.setTextColor(Color.BLACK);
                Object object = getItem(position);
                if (object instanceof Beacon) {
                    Beacon beacon = (Beacon) object;

                    textView.setText(beacon.getName() + " - " + Event.getProximityString(beacon.getProximity()));
                } else if (object instanceof Event) {
                    Event event = (Event) object;
                    textView.setText(event.getName() + " - " + Event.getTypeString(event.getType()));
                }
                return textView;
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = mAdapter.getItem(position);
                if (object instanceof Beacon) {
                    final Beacon beacon = (Beacon) object;
                    AlertDialog.Builder adb = new AlertDialog.Builder(BeaconsActivity.this);
                    adb.setSingleChoiceItems(new CharSequence[]{"Unknown", "Immediate", "Near", "Far"}, beacon.getProximity().ordinal(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BeaconProxy.changeBeaconProximity(beacon, Proximity.values()[which]);
                            dialog.dismiss();
                        }
                    });
                    adb.setNegativeButton("Cancel", null);
                    adb.setTitle("Change the proximity of " + beacon.getName());
                    adb.show();
                    // do nothing
                } else if (object instanceof Event) {
                    Event event = (Event) object;
                    Beacon beacon = event.getBeacons().get(0);
                    Lighthouse.getInstance().getNotificationsManager().handleEvent(event, beacon);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_beacons, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_user) {
            startActivity(new Intent(this, UserActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void beaconsRefreshed(List<Beacon> beacons) {
        mBeacons = Lighthouse.getInstance().getBeacons();
        mEvents = Lighthouse.getInstance().getEvents();
        refreshAdapter();
        DatabaseHelper.getInstance().printDB();
    }

    @Override
    public void beaconChangedProximity(Beacon beacon) {
        mBeacons = Lighthouse.getInstance().getBeacons();
        mEvents = Lighthouse.getInstance().getEvents();
        refreshAdapter();
    }

    @Override
    public void eventTriggered(Event event, Beacon beacon) {
        mBeacons = Lighthouse.getInstance().getBeacons();
        mEvents = Lighthouse.getInstance().getEvents();
        refreshAdapter();
    }
}
