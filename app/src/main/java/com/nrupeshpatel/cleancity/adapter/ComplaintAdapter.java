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

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nrupeshpatel.cleancity.R;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.MyViewHolder> {

    private List<Complaint> complaintList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView complaintId, ndc, status;
        public ImageView star;
        private ImageView complaintImage;
        private TextView complaintStatus;
        private TextView complaintTime;
        private TextView complaintLocation;
        ProgressBar pBar;

        MyViewHolder(View view) {
            super(view);
            star = (ImageView) view.findViewById(R.id.star);
            complaintId = (TextView) view.findViewById(R.id.complaintID);
            complaintStatus = (TextView) view.findViewById(R.id.complaintStatus);
            complaintTime = (TextView) view.findViewById(R.id.complaintTime);
            complaintLocation = (TextView) view.findViewById(R.id.complaintLocation);
            complaintImage = (ImageView) view.findViewById(R.id.complaintImage);
            pBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    public ComplaintAdapter(Context context, List<Complaint> complaintList) {
        this.context = context;
        this.complaintList = complaintList;
    }

    @Override
    public int getItemViewType(int position) {
        return complaintList.get(position) != null ? 1 : 0;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        if (viewType == 1) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.complaint_row, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.complaint_loading_row, parent, false);
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        if (getItemViewType(position) == 1) {
            final Complaint complaint = complaintList.get(position);

            final boolean isStared = complaint.getStarred();
            if (!isStared) {
                holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_row));
            } else {
                holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_selected_row));
            }
            holder.star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isStared) {
                        //complaint.setStar(true);
                        holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_selected_row));
                    } else {
                        //complaint.setStar(false);
                        holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_row));
                    }
                }
            });

            holder.complaintId.setText(complaint.getId());
            holder.complaintStatus.setText(complaint.getStatus());
            holder.complaintTime.setText(complaint.getDate());
            holder.complaintLocation.setText(complaint.getAddress());

            Glide.with(context)
                    .load(complaint.getImage())
                    .centerCrop()
                    .into(holder.complaintImage);
            holder.pBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }
}
