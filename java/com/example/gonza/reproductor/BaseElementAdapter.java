package com.example.gonza.reproductor;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Adaptador base para todas las listas (recycler view) del reproductor.
 * Define interfaces para poder delegar el comportamiento respecto a los
 * clicks y labels.
 */
public class BaseElementAdapter extends RecyclerView.Adapter<BaseElementAdapter.MyViewHolder> {

	private List<Song> mDataset;
	boolean removeButton = false;

	public interface OnItemClickListener {
		void onClick(View view, int position);
		void onButtonClick(View view, int position);
	}
	private BaseElementAdapter.OnItemClickListener clickListener;

	/**
	 * Para ser implementada por la actividad a la que pertenece este Adapter.
	 */
	interface ContentManager {
		String getTitle(int position);
		String getSubtitle(int position);
		String getAlbumArt(int position);
	}
	ContentManager contentManager;

	/**
	 * Provide a reference to the views for each data item
	 * Complex data items may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder
	 */
	public class MyViewHolder extends RecyclerView.ViewHolder {

		RelativeLayout clickZone1;
		RelativeLayout clickZone2;
		TextView title;
		TextView subtitle;
		ImageView albumArt;
		ImageView elementButton;

		public MyViewHolder(View v) {
			super(v);
			title = (TextView) v.findViewById(R.id.title);
			subtitle = (TextView) v.findViewById(R.id.subtitle);
			albumArt = (ImageView) v.findViewById(R.id.album_art);
			elementButton = (ImageView) v.findViewById(R.id.elementButton);
			elementButton.setMaxHeight(100);
			elementButton.setMaxWidth(100);

			clickZone1 = (RelativeLayout) v.findViewById(R.id.click_zone_1);
			clickZone2 = (RelativeLayout) v.findViewById(R.id.click_zone_2);
			clickZone1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clickListener.onClick(v, getAdapterPosition());
				}
			});
			if (removeButton == true) {
				elementButton.setImageResource(R.drawable.ic_menu_remove);
			}
			clickZone2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clickListener.onButtonClick(v, getAdapterPosition());
				}
			});
		}
	}

	public BaseElementAdapter(List<Song> myDataset) {
		mDataset = myDataset;
	}

	public void setOnClickListener(OnItemClickListener itemClickListener) {
		this.clickListener = itemClickListener;
	}

	void setContentManager(ContentManager cm) {
		this.contentManager = cm;
	}

	/**
	 * Para cambiar la imágen del botón según sea un signo de "más" (agregar
	 * a la lista) o "menos" (quitar de la lista).
	 */
	void setRemoveButton(boolean b) {
		this.removeButton = b;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public BaseElementAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// create a new view
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element, parent, false);
		// set the view's size, margins, paddings and layout parameters
		//TextView tv = new TextView(parent.getContext(), null);
		return new MyViewHolder(view);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		holder.title.setText(contentManager.getTitle(position));
		holder.subtitle.setText(contentManager.getSubtitle(position));
		if (contentManager.getAlbumArt(position) != null) {
			holder.albumArt.setImageURI(Uri.parse(contentManager.getAlbumArt(position)));
			holder.albumArt.setAdjustViewBounds(true);
			holder.albumArt.setMaxHeight(100);
			holder.albumArt.setMaxWidth(100);
		}
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}
}
