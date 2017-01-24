/*
 * Copyright (c) 2017 Nrupesh Patel
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.nrupeshpatel.cleancity.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.nrupeshpatel.cleancity.R;
import com.nrupeshpatel.cleancity.adapter.Complaint;
import com.nrupeshpatel.cleancity.adapter.ComplaintAdapter;
import com.nrupeshpatel.cleancity.helper.Config;
import com.nrupeshpatel.cleancity.helper.OnLoadMoreListener;
import com.nrupeshpatel.cleancity.helper.RequestHandler;
import com.nrupeshpatel.cleancity.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    public List<Complaint> complaintList = new ArrayList<>();
    ComplaintAdapter mAdapter;
    RecyclerView recyclerView;
    private int totalCount = 0;
    private SessionManager session;
    Handler handler;
    int pageNumber = 0;
    ProgressBar pBar;
    SwipeRefreshLayout mySwipeRefreshLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        session = new SessionManager(getActivity().getApplicationContext());

        pBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mySwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);

        new GetComplaints().execute(0);

        handler = new Handler();

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());

        recyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ComplaintAdapter(complaintList, recyclerView);

        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                if (complaintList.size() < totalCount) {
                    complaintList.add(null);
                    mAdapter.notifyItemInserted(complaintList.size() - 1);

                    new GetComplaints().execute(complaintList.size() - 1);
                }
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        complaintList.clear();
                        new GetComplaints().execute(0);
                    }
                }
        );


        return v;
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    private class GetComplaints extends AsyncTask<Integer, Void, String> {

        String JSON_STRING;
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            parsejosnData(s);
        }

        @Override
        protected String doInBackground(Integer... params) {

            HashMap<String, String> user = session.getUserDetails();
            RequestHandler rh = new RequestHandler();

            if (params[0] == 0) {
                JSON_STRING = rh.sendGetRequest(Config.getComplaintCount + user.get(SessionManager.KEY_EMAIL));
                try {
                    jsonObject = new JSONObject(JSON_STRING);

                    totalCount = jsonObject.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            JSON_STRING = null;
            jsonObject = null;

            JSON_STRING = rh.sendGetRequest(Config.getComplaints + user.get(SessionManager.KEY_EMAIL) + "/" + String.valueOf(params[0]));
            JSON_STRING = "{\"result\":" + JSON_STRING + "}";

            pageNumber = params[0];

            return JSON_STRING;

        }
    }


    public void parsejosnData(String response) {

        JSONObject jsonObject;

        try {

            if (pageNumber > 0) {
                complaintList.remove(complaintList.size() - 1);
                mAdapter.notifyItemRemoved(complaintList.size());
            } else {
                mAdapter.notifyDataSetChanged();
            }

            jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");

            for (int i = 0; i < result.length(); i++) {
                JSONObject jo = result.getJSONObject(i);
                String id = jo.getString("_id");
                boolean starred = jo.getBoolean("starred");
                String status = jo.getString("status");
                String detail = jo.getString("detail");
                String date = jo.getString("date");
                String image = jo.getString("imagePath") + id + ".png";
                String address = jo.getString("address");

                Complaint c = new Complaint(id, status, detail, date, address, starred, image);
                complaintList.add(c);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyItemInserted(complaintList.size());
                    }
                });
            }

            mAdapter.setLoaded();

            if (complaintList.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
            }

            pBar.setVisibility(View.GONE);
            if (mySwipeRefreshLayout.isRefreshing()) {
                mySwipeRefreshLayout.setRefreshing(false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
