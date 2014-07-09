package com.sky.opam.view;

import com.sky.opam.tool.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;

public class TimeLineView extends View{
	private int startTime ;
	private int endTime ;
	private float time_distance ;
	private float time_wdith;
	private float time_hight;
	//private Paint linePaint = new Paint();
	private Paint timePaint = new Paint();

	public TimeLineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initia(context);
	}

	public TimeLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initia(context);
	}

	public TimeLineView(Context context) {
		super(context);
		initia(context);
	}	

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;	
	}
	
	public void setTimeDistance(float time_distance) {
		this.time_distance = time_distance;
	}

	private void initia(Context context){
		timePaint.setColor(Color.BLACK);
		timePaint.setTextSize(Util.dip2px(context, 12));
		time_wdith = timePaint.measureText("08 ");
		time_hight = getTextHeight(timePaint);		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < endTime-startTime+1; i++) {
			int timeInt = i + startTime;
			String timeString = (timeInt < 10) ? ("0" + timeInt): (timeInt +"");
			canvas.drawText(timeString, 0, getTextY(i*time_distance, timePaint),timePaint);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension((int)(time_wdith), (int)((endTime-startTime)*time_distance + time_hight));
	}
	
	public float getViewWidth() {
		return time_wdith;
	}
	
	private float getTextY(float y, Paint p) {
		FontMetrics fm = p.getFontMetrics();
		return y-fm.ascent;
	}
	
	private float getTextHeight(Paint p) {
		FontMetrics fm = p.getFontMetrics();
		return (fm.descent - fm.ascent);
	}

}
