package com.example.gonza.reproductor;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by gonza on 10/10/16.
 */

public class BaseElementAdapter extends RecyclerView.Adapter<BaseElementAdapter.MyViewHolder> {

	private List<Song> mDataset;

	public interface OnItemClickListener {
		public void onClick(View view, int position);
		public void onButtonClick(View view, int position);
	}
	private BaseElementAdapter.OnItemClickListener clickListener;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		public TextView title;
		public TextView subtitle;
		public ImageView albumArt;
		public ImageView elementButton;

		public MyViewHolder(View v) {
			super(v);
			v.setOnClickListener(this);
			title = (TextView) v.findViewById(R.id.title);
			subtitle = (TextView) v.findViewById(R.id.subtitle);
			albumArt = (ImageView) v.findViewById(R.id.album_art);
			elementButton = (ImageView) v.findViewById(R.id.elementButton);
			elementButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("BASE", "MyViewHolder");
				}
			});
		}

		@Override
		public void onClick(View v) {
			clickListener.onClick(v, getAdapterPosition());
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public BaseElementAdapter(List<Song> myDataset) {
		mDataset = myDataset;
	}

	public void setOnClickListener(OnItemClickListener itemClickListener) {
		this.clickListener = itemClickListener;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public BaseElementAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
	                                                        int viewType) {
		// create a new view
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.colection_element, parent, false);
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
		holder.title.setText(mDataset.get(position).getAlbum());
		holder.subtitle.setText(mDataset.get(position).getArtist());
		try {
			holder.albumArt.setImageURI(Uri.parse(mDataset.get(position).getAlbumArt()));
			holder.albumArt.setAdjustViewBounds(true);
			holder.albumArt.setMaxHeight(100);
			holder.albumArt.setMaxWidth(100);
		} catch (Exception expected) {
			System.err.println("Error con la portada: " + mDataset.get(position).getAlbumArt());
		}
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}
}
