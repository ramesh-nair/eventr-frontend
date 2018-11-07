package com.eventr.app.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eventr.app.events.adapters.EventListRecyclerAdapter;
import com.eventr.app.events.models.Event;
import com.eventr.app.events.utils.Utils;
import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.discovery.model.Events;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Suraj on 04/08/16.
 */
public class EventViewPagerFragment extends Fragment {
    private static final String TAB_POSITION = "tab_position";
    private static final String EVENTS_URL = Utils.LIVE_URL +"events?rsvp_state=";
    private static final String NEARBY_URL = Utils.LIVE_URL +"nearby-events?";
    private CharSequence[] rsvpStates = {"nearby", "attending", "maybe"};
    private EventListRecyclerAdapter listAdapter;
    private SharedPreferences userPreferences;
    private String accessToken;
    private int tabPosition;
    private LocationManager locationManager;
    private Double latitude;
    private Double longitude;



    @BindView(R.id.recyclerView) public RecyclerView recyclerView;
    @BindView(R.id.event_list_progress_bar) public ProgressBar progressBar;
    @BindView(R.id.swipeRefreshEvents) public SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.no_event_view) public TextView noEventView;

    private static final String REQUEST_TAG = "main_activity";

//    private JSONArray items = new JSONArray();
    private ArrayList<Event> items = new ArrayList<Event>();


    public EventViewPagerFragment() {

    }

    public static EventViewPagerFragment newInstance(int tabPosition) {
        EventViewPagerFragment fragment = new EventViewPagerFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        tabPosition = args.getInt(TAB_POSITION);
        View v =  inflater.inflate(R.layout.event_list_fragment, container, false);
        ButterKnife.bind(this, v);
        setSwipeRefreshListener();
        int screenSize = getActivity().getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                break;
            default:
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        }

        try {
            listAdapter = new EventListRecyclerAdapter(items);
            recyclerView.setAdapter(listAdapter);
        } catch (Exception e) {

        }

        userPreferences = getContext().getSharedPreferences(getContext().getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getContext().getString(R.string.access_token_key), null);
        latitude = Double.longBitsToDouble(userPreferences.getLong(getContext().getString(R.string.latitude), Double.doubleToLongBits(0.0)));
        longitude = Double.longBitsToDouble(userPreferences.getLong(getContext().getString(R.string.longitude), Double.doubleToLongBits(0.0)));

        fetchEvents(false);
        return v;
    }

    private void fetchEvents(final boolean swipeRefresh) {
        boolean isInternetConnected = Utils.isInternetConnected(getContext());
        if (!isInternetConnected) {
            if (swipeRefresh) {
                swipeRefreshLayout.setRefreshing(false);
            }
            onInternetFail();
        } else {
            (new GetEvents(swipeRefresh)).execute();
        }




//        boolean isInternetConnected = Utils.isInternetConnected(getContext());
//        if (!isInternetConnected) {
//            if (swipeRefresh) {
//                swipeRefreshLayout.setRefreshing(false);
//            }
//            onInternetFail();
//        } else {
//            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    if (swipeRefresh) {
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//                    hideProgressBar();
//                    try {
//                        items.clear();
//                        if (tabPosition > 0) {
//                            JSONArray events = (response.getJSONObject("data")).getJSONArray("data");
//                            for (int i = 0; i < events.length(); i++) {
//                                JSONObject event = (JSONObject) events.get(i);
//                                Event item = new Event(event, rsvpStates[tabPosition].toString(), false);
//                                items.add(item);
//                            }
//                        } else {
//                            JSONArray events = (response.getJSONObject("data")).getJSONArray("events");
//                            for (int i = 0; i < events.length(); i++) {
//                                JSONObject event = (JSONObject) events.get(i);
//                                Event item = new Event(event, rsvpStates[tabPosition].toString(), true);
//                                items.add(item);
//                            }
//                        }
//                        listAdapter.notifyDataSetChanged();
//                        if (items.size() > 0) {
//                            noEventView.setVisibility(View.GONE);
//                        } else {
//                            noEventView.setVisibility(View.VISIBLE);
//                        }
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
//                    if (swipeRefresh) {
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
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
//                            Utils.logout(getContext());
//                        }
//                    }
//                }
//            };
//
//            String eventsUrl = EVENTS_URL + rsvpStates[tabPosition];
//
//            if (tabPosition == 0) {
//                eventsUrl = NEARBY_URL + "lat=" + latitude + "&lng=" + longitude;
//            }
//
//            JsonObjectRequest request = new CustomJsonRequest(eventsUrl, null, listener, errorListener, accessToken);
//            request.setTag(REQUEST_TAG);
//            EventrRequestQueue.getInstance().add(request);
//        }
    }

    private void onInternetFail() {
        Utils.showActionableSnackBar(getActivity().findViewById(R.id.coordinator_main), "Internet connection failed", "Retry", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchEvents(false);
            }
        });
    }

    private void setSwipeRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchEvents(true);
            }
        });
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private class GetEvents extends AsyncTask<String, Void, String> {
        private boolean swipeRefresh;
        public GetEvents(boolean showLoading) {
            super();
            // do stuff
            swipeRefresh = showLoading;
        }

        @Override
        protected String doInBackground(String... params) {

            PagedResponse<Events> page = null;
            try {
                page = EventrRequestQueue.getInstance().getApi().searchEvents(new SearchEventsOperation().
                        latlong(String.valueOf(latitude), String.valueOf(longitude))
                        .radius(50)
                        .unit("km")
                        .startDateTime(Utils.getSearchDate(new Date()))
                        .endDateTime(Utils.getSearchDateTreeManth())
                        .sort("date,asc")
                        .pageSize(100));
                return  page.getJsonPayload();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (swipeRefresh) {
                swipeRefreshLayout.setRefreshing(false);
             }
             hideProgressBar();

            if(result!= null){
                items.clear();
                try {
                    JSONObject json = new JSONObject(result);
                    if (tabPosition > 0) {

                    } else {
                        if(json.has("_embedded")) {
                            JSONArray events = (json.getJSONObject("_embedded")).getJSONArray("events");
                            for (int i = 0; i < events.length(); i++) {
                                JSONObject eventObj = (JSONObject) events.get(i);
                                Event item = new Event(eventObj, rsvpStates[tabPosition].toString(), false);
                                items.add(item);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            listAdapter.notifyDataSetChanged();
            if (items.size() > 0) {
                noEventView.setVisibility(View.GONE);
            } else {
                noEventView.setVisibility(View.VISIBLE);
            }
        }
    }
}
