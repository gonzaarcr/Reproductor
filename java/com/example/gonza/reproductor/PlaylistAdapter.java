package com.example.gonza.reproductor;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by gonza on 23/10/16.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {

	private List<Song> mDataset;

	public interface OnItemClickListener {
		public void onClick(View view, int position);
	}
	private PlaylistAdapter.OnItemClickListener clickListener;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		// each data item is just a string in this case
		public TextView title;
		public TextView duration;

		public MyViewHolder(View v) {
			super(v);
			v.setOnClickListener(this);
			title = (TextView) v.findViewById(R.id.title);
			duration = (TextView) v.findViewById(R.id.duration);
		}

		@Override
		public void onClick(View v) {
			clickListener.onClick(v, getAdapterPosition());
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public PlaylistAdapter(List<Song> myDataset) {
		mDataset = myDataset;
	}

	public void setOnClickListener(OnItemClickListener itemClickListener) {
		this.clickListener = itemClickListener;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public PlaylistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
	                                                        int viewType) {
		// create a new view
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_element, parent, false);
		// set the view's size, margins, paddings and layout parameters
		// ...
		//TextView tv = new TextView(parent.getContext(), null);
		return new MyViewHolder(view);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		holder.title.setText(mDataset.get(position).getTitle());
		long duration = mDataset.get(position).getDuration() / 1000;
		String minutos = String.valueOf(duration / 60);
		String segundos = String.valueOf(duration % 60);
		String tmp = segundos.length() == 1? "0" + segundos : segundos;
		holder.duration.setText( minutos + ":" + tmp);
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}
}
