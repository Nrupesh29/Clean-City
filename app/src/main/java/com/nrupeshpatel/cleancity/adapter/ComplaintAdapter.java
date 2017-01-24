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

package com.nrupeshpatel.cleancity.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nrupeshpatel.cleancity.R;
import com.nrupeshpatel.cleancity.helper.OnLoadMoreListener;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<Complaint> complaintList;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;


    public ComplaintAdapter(List<Complaint> complaintList1, RecyclerView recyclerView) {

        complaintList = complaintList1;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading
                                    && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                // End has been reached
                                // Do something
                                if (onLoadMoreListener != null) {
                                    onLoadMoreListener.onLoadMore();
                                }
                                loading = true;
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return complaintList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.complaint_row, parent, false);

            vh = new MyViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.complaint_loading_row, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {

            final Complaint complaint = complaintList.get(position);

            final boolean isStared = complaint.getStarred();
            if (!isStared) {
                ((MyViewHolder) holder).star.setImageDrawable(((MyViewHolder) holder).star.getContext().getResources().getDrawable(R.drawable.ic_star_row));
            } else {
                ((MyViewHolder) holder).star.setImageDrawable(((MyViewHolder) holder).star.getContext().getResources().getDrawable(R.drawable.ic_star_selected_row));
            }
            ((MyViewHolder) holder).star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isStared) {
                        //complaint.setStar(true);
                        ((MyViewHolder) holder).star.setImageDrawable(((MyViewHolder) holder).star.getContext().getResources().getDrawable(R.drawable.ic_star_selected_row));
                    } else {
                        //complaint.setStar(false);
                        ((MyViewHolder) holder).star.setImageDrawable(((MyViewHolder) holder).star.getContext().getResources().getDrawable(R.drawable.ic_star_row));
                    }
                }
            });

            ((MyViewHolder) holder).complaintId.setText(complaint.getId());
            ((MyViewHolder) holder).complaintStatus.setText(complaint.getStatus());
            ((MyViewHolder) holder).complaintTime.setText(complaint.getDate());
            ((MyViewHolder) holder).complaintLocation.setText(complaint.getAddress());

            Glide.with(((MyViewHolder) holder).complaintImage.getContext())
                    .load(complaint.getImage())
                    .placeholder(((MyViewHolder) holder).complaintImage.getContext().getResources().getDrawable(R.drawable.logo))
                    .centerCrop()
                    .into(((MyViewHolder) holder).complaintImage);


        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView complaintId;
        ImageView star;
        private ImageView complaintImage;
        private TextView complaintStatus;
        private TextView complaintTime;
        private TextView complaintLocation;

        MyViewHolder(View view) {
            super(view);
            star = (ImageView) view.findViewById(R.id.star);
            complaintId = (TextView) view.findViewById(R.id.complaintID);
            complaintStatus = (TextView) view.findViewById(R.id.complaintStatus);
            complaintTime = (TextView) view.findViewById(R.id.complaintTime);
            complaintLocation = (TextView) view.findViewById(R.id.complaintLocation);
            complaintImage = (ImageView) view.findViewById(R.id.complaintImage);
        }
    }


    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }
}
