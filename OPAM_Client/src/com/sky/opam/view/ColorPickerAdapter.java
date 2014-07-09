package com.sky.opam.view;

import java.util.ArrayList;
import java.util.List;

import com.sky.opam.tool.AndroidUtil;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ColorPickerAdapter extends BaseAdapter {
	private Context context;
	// list which holds the colors to be displayed
	private List<Integer> colorList = new ArrayList<Integer>();
	// width of grid column
	private int colorGridColumnWidth;
	private ImageView selectedImage = null;
	private String[] colors  = { "#999999", "#000000", "#F7A7C0",  "#83334C",
			"#B694E8",  "#41236D" , "#6D9EEB",  "#1C4587",
			"#68DFA9",  "#1A764D" , "#44B984",  "#076239",
			"#FCDA83",  "#AA8831" , "#FFBC6B",  "#A46A21",
			"#E66550",  "#822111" };

	public ColorPickerAdapter(Context context) {
		this.context = context;
		// defines the width of each color square
		colorGridColumnWidth = AndroidUtil.dip2px(context, 55);

		colorList = new ArrayList<Integer>();
		// add the color array to the list
		for (int i = 0; i < colors.length; i++) {
			colorList.add(Color.parseColor(colors[i]));			
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		// can we reuse a view?
		if (convertView == null) {
			imageView = new ImageView(context);
			// set the width of each color square
			imageView.setLayoutParams(new GridView.LayoutParams(colorGridColumnWidth, colorGridColumnWidth));
		} else {
			imageView = (ImageView) convertView;
		}
		imageView.setBackgroundColor(colorList.get(position));
		imageView.setImageResource(android.R.drawable.radiobutton_off_background);
		imageView.setId(0);
		imageView.setAlpha(0);
		return imageView;
	}
	
	public void reselectColor(ImageView imageView){
		if(selectedImage!=null) changeImage(selectedImage);
		changeImage(imageView);
		selectedImage = imageView;
	}
	
	private void changeImage(ImageView imageView){
		if(imageView.getId() == 0){
			imageView.setAlpha(255);
			imageView.setId(255);
		}else {
			imageView.setAlpha(0);
			imageView.setId(0);
		}
	}
	
	public String getBgColor(int p){
		return colors[p];
	}
	
	public int getCount() {
		return colorList.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}
}
