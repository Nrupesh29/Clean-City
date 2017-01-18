package com.nrupeshpatel.cleancity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nrupeshpatel.cleancity.R;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.MyViewHolder> {

    private List<Complaint> complaintList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView complaintId, ndc, status;
        public ImageView star;
        ProgressBar pBar;

        MyViewHolder(View view) {
            super(view);
            star = (ImageView) view.findViewById(R.id.star);
            complaintId = (TextView) view.findViewById(R.id.complaintID);
            /*ndc = (TextView) view.findViewById(R.id.scanNdc);
            number = (TextView) view.findViewById(R.id.scanNumber);
            status = (TextView) view.findViewById(R.id.scanStatus);
            image = (ImageView) view.findViewById(R.id.scanImage);
            pBar = (ProgressBar) view.findViewById(R.id.progressBar);*/
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
            holder.complaintId.setText(String.valueOf(position));
            final boolean isStared = complaint.getStar();
            if (!isStared) {
                holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_row));
            } else {
                holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_selected_row));
            }
            holder.star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isStared) {
                        complaint.setStar(true);
                        holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_selected_row));
                    } else {
                        complaint.setStar(false);
                        holder.star.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_row));
                    }
                }
            });
        }
        /*holder.ndc.setText("NDC: " + history.getNdc());
        holder.number.setText("Recall Number: " + history.getNumber());
        holder.status.setText("Recall Status: " + history.getStatus());

        Glide.with(context)
                .load(getResId(history.getImage(), R.drawable.class))
                .listener(new RequestListener<Integer, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.pBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.image);*/
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }
}
