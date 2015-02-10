package com.sky.opam.view;

import com.sky.opam.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;

public class TimeLineView extends View
{
	private int startTime = 7;
	private int endTime = 19;
	private float time_vertical_distance;
	private float time_wdith;
	private float time_hight;
	//private Paint linePaint = new Paint();
	private Paint timePaint = new Paint();

	public TimeLineView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		initia(context);
	}

	public TimeLineView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		initia(context);
	}

	public TimeLineView(Context context) 
	{
		super(context);
		initia(context);
	}	

	public void setStartTime(int startTime) 
	{
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) 
	{
		this.endTime = endTime;	
	}

	private void initia(Context context)
	{
		timePaint.setColor(Color.BLACK);
		timePaint.setTextSize(getResources().getDimension(R.dimen.week_agenda_timeline_time_text_size));
		time_wdith = timePaint.measureText("08 ");
		time_hight = getTextHeight(timePaint);
		
		time_vertical_distance = getResources().getDimensionPixelSize(R.dimen.week_agenda_timeline_time_vertical_distance);
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		for (int i = 0; i < endTime - startTime + 1; i++) 
		{
			int timeInt = i + startTime;
			String timeString = (timeInt < 10) ? ("0" + timeInt): (timeInt + "");
			canvas.drawText(timeString, 0, getTextY(i * time_vertical_distance, timePaint), timePaint);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		setMeasuredDimension((int)(time_wdith), (int)((endTime - startTime) * time_vertical_distance + time_hight));
	}
	
	public float getViewWidth() 
	{
		return time_wdith;
	}
	
	public float getTimeHeight() 
	{
		return time_hight;
	}
	
	private float getTextY(float y, Paint p) 
	{
		FontMetrics fm = p.getFontMetrics();
		return y - fm.ascent;
	}
	
	private float getTextHeight(Paint p) 
	{
		FontMetrics fm = p.getFontMetrics();
		return (fm.descent - fm.ascent);
	}

}
