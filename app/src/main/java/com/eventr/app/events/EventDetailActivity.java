package com.eventr.app.events;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.eventr.app.events.models.Event;
import com.eventr.app.events.utils.CustomDialogFragment;
import com.eventr.app.events.utils.Utils;
import com.ticketmaster.api.discovery.operation.ByIdOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.discovery.model.Events;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Suraj on 23/08/16.
 */
public class EventDetailActivity extends AppCompatActivity {
    private static final String EVENT_DETAIL_URL = Utils.LIVE_URL +"events/";
    private static final String RSVP_STATUS_URL = Utils.LIVE_URL +"rsvp-event";

    private static final String REQUEST_TAG = "event_detail_activity";

    private Event eventDetail;

    @BindView(R.id.event_detail_progress_bar) public ProgressBar progressBar;
    @BindView(R.id.event_detail_container) public NestedScrollView eventDetailContainer;
    @BindView(R.id.start_time) public TextView startTime;
    @BindView(R.id.event_location) public TextView locationView;
    @BindView(R.id.floating_action) public FloatingActionButton floatingButton;
    @BindView(R.id.attending_count) public TextView attendingCount;
    @BindView(R.id.event_description) public TextView description;
    @BindView(R.id.event_Link) public TextView link;
    @BindView(R.id.event_detail_pic) public NetworkImageView cover;

    private String rsvpStatus;
    private Context mContext;
    private static final String DIALOG_TYPE = "confirm";
    private CustomDialogFragment attendingDialogFragment = new CustomDialogFragment(DIALOG_TYPE);

    private SharedPreferences userPreferences;
    private String accessToken;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        Bundle intentExtras = getIntent().getExtras();

        eventDetail = (Event)intentExtras.getSerializable(getResources().getString(R.string.intent_event_detail_key));
        setToolbar();

        userPreferences = getSharedPreferences(getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getString(R.string.access_token_key), null);
        rsvpStatus = eventDetail.getRsvpStatus();

        mContext = this;

        getEventDetail();
        setAttendingDialog();
        setFloatingButtonAction();
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_event_detail);
        toolbar.setTitle(eventDetail.getName());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void getEventDetail() {
        boolean isInternetConnected = Utils.isInternetConnected(this);
        if (!isInternetConnected) {
            onInternetFail();
        } else {
            (new GetEvent()).execute();

            //        try {
//            EventrRequestQueue.getInstance().getApi().getEvent(new ByIdOperation().id(eventDetail.getId()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    hideProgressBar();
//                    try {
//                        JSONObject event = response.getJSONObject("data");
//                        eventDetail.setDetails(event, rsvpStatus, false);
//                        updateEventPage();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//
//            Response.ErrorListener errorListener = new Response.ErrorListener() {
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    error.printStackTrace();
//
//                    if (error.networkResponse == null) {
//                        if (error.getClass().equals(TimeoutError.class)) {
//                            onInternetFail();
//                        }
//
//                        if (error.getClass().equals(NoConnectionError.class)) {
//                            onInternetFail();
//                        }
//                    } else {
//                        NetworkResponse networkResponse = error.networkResponse;
//                        if (networkResponse.statusCode == 401) {
//                            Utils.logout(mContext);
//                        }
//                    }
//                }
//            };
//
//            String eventsUrl = EVENT_DETAIL_URL + eventDetail.getId();
//
//            JsonObjectRequest request = new CustomJsonRequest(eventsUrl, null, listener, errorListener, accessToken);
//            request.setTag(REQUEST_TAG);
//            EventrRequestQueue.getInstance().add(request);
        }
    }

    private void onInternetFail() {
        Utils.showAlertWindow(this, "Internet connection failed", "Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                getEventDetail();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                finish();
            }
        });
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        eventDetailContainer.setVisibility(View.VISIBLE);
        floatingButton.setVisibility(View.VISIBLE);
    }

    private void updateEventPage() {
        description.setText(eventDetail.getDescription());
        cover.setImageUrl(eventDetail.getPicUrl(), EventrRequestQueue.getInstance().getImageLoader());
        startTime.setText(eventDetail.getTimeString());
        locationView.setText(eventDetail.getLocationString());
        attendingCount.setText(eventDetail.getPleaseNote());
        link.setText(eventDetail.getLink());
    }

    private void startGroupsActivity() {
        Intent intent = new Intent(getApplicationContext(), EventGroupsActivity.class);
        intent.putExtra(getString(R.string.intent_event_detail_key), eventDetail);
        startActivity(intent);
    }

    private void setFloatingButtonAction() {
        floatingButton.setOnClickListener(null);
        switch(rsvpStatus) {
            case "attending": {
                floatingButton.setImageResource(R.drawable.ic_people_white);
                floatingButton.setOnClickListener(floatingButtonGroupsListener);
                break;
            }
            default: {
                floatingButton.setOnClickListener(floatingButtonGoingListener);
            }
        }
    }

    private View.OnClickListener floatingButtonGroupsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startGroupsActivity();
        }
    };
    private View.OnClickListener floatingButtonGoingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            attendingDialogFragment.show(getSupportFragmentManager(), "ATTENDING_DIALOG");
        }
    };

    private void setAttendingDialog() {
        attendingDialogFragment.setTitle("Event action");
        attendingDialogFragment.setMessage("Do you want to attend this event?");
        attendingDialogFragment.setPositiveButton("Yes", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeRsvpStatus();
            }
        });

        attendingDialogFragment.setNegativeButton("No", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attendingDialogFragment.dismiss();
            }
        });
    }

    @OnClick(R.id.event_Link)
    void openLink(){
        if(link.getText().toString().trim().length()>0){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(link.getText().toString()));
            startActivity(i);
        }
    }

    @OnClick(R.id.event_location)
    void onClickLocation(){
        if(eventDetail.getLat() != null || eventDetail.getLng() != null) {
            Uri gmmIntentUri = Uri.parse("geo:" + eventDetail.getLat() + "," + eventDetail.getLng());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
    }

    private void changeRsvpStatus() {
        boolean isInternetConnected = Utils.isInternetConnected(this);
        if (!isInternetConnected) {
            onRSVPIntenetFail();
            return;
        }
        attendingDialogFragment.showProgressBar();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                attendingDialogFragment.hideProgressBar();
                attendingDialogFragment.hideError();
                attendingDialogFragment.dismiss();
                rsvpStatus = "attending";
                eventDetail.setRsvpStatus("attending");
                setFloatingButtonAction();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                attendingDialogFragment.hideProgressBar();
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        onRSVPIntenetFail();
                    }

                    if (error.getClass().equals(NoConnectionError.class)) {
                        onRSVPIntenetFail();
                    }
                } else {
                    NetworkResponse networkResponse = error.networkResponse;
                    attendingDialogFragment.dismiss();
                    if (networkResponse.statusCode == 401) {
                        Utils.logout(mContext);
                    }
                }
            }
        };

        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("fb_event_id", eventDetail.getId());
            requestObject.put("rsvp_state", "attending");
            JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, RSVP_STATUS_URL, requestObject, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void onRSVPIntenetFail() {
        attendingDialogFragment.showError("No internet connection");
    }

    @Override
    public void onStop() {
        super.onStop();
        EventrRequestQueue.getInstance().cancel(REQUEST_TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetEvent extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                com.ticketmaster.api.discovery.response.Response<com.ticketmaster.discovery.model.Event> ev = EventrRequestQueue.getInstance().getApi().getEvent(new ByIdOperation().id(eventDetail.getId()));
                return  ev.getJsonPayload();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            hideProgressBar();
            try {
                eventDetail.setDetails(new JSONObject(result), rsvpStatus, false);
                updateEventPage();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
