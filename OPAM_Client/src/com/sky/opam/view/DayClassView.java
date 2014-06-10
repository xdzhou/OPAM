package com.sky.opam.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sky.opam.R.integer;
import com.sky.opam.model.Cours;
import com.sky.opam.tool.Tool;

import android.R.color;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class DayClassView extends View{
	private int startTime ;
	private int endTime ;
	private float view_width ;
	private float time_distance ;
	private List<Cours> class_list = new ArrayList<Cours>();
	private Paint outLinePaint = new Paint();
	private Paint backgroundPaint = new Paint();
	private TextPaint textPaint = new TextPaint();
	private float d;

	public DayClassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initia(context);
	}

	public DayClassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initia(context);
	}

	public DayClassView(Context context) {
		super(context);
		initia(context);
	}
	
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public void setViewWidth(float view_width) {
		this.view_width = view_width;
	}
	
	public void setTimeDistance(float time_distance) {
		this.time_distance = time_distance;
	}

	private void initia(Context context){
		outLinePaint.setColor(Color.BLUE);
		outLinePaint.setStrokeWidth(Tool.dip2px(context, 1));
		backgroundPaint.setColor(Color.GRAY);
		backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);//设置填满
		textPaint.setColor(Color.RED);
		textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(Tool.dip2px(context, 12));
        d = Tool.dip2px(context,1);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//draw the out line
		canvas.drawLine(0, 0, 0, (endTime-startTime)*time_distance, outLinePaint);
		canvas.drawLine(view_width, 0, view_width, (endTime-startTime)*time_distance, outLinePaint);
		//canvas.drawLine(0, 0, view_width, 0, outLinePaint);
		for(int i=0; i<endTime-startTime+1; i++){
			canvas.drawLine(0, i*time_distance, view_width, i*time_distance, outLinePaint);
		}
		//draw class info
		for(Cours c : class_list){
			drawClassInfo(canvas, c);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension((int)(view_width), (int)((endTime-startTime)*time_distance));
	}
	
	private void drawClassInfo(Canvas canvas, Cours c) {
		float startP = getDistance(c.debut);
		float endP = getDistance(c.fin);
		canvas.drawRect(d, startP+d, view_width-d, endP-d, backgroundPaint);
		StaticLayout sl= new StaticLayout(c.name, textPaint, (int)(view_width-2*d), Alignment.ALIGN_CENTER, 1f, 0f, false);
		float taux = (endP-startP-2*d)/getTextHeight(textPaint) / sl.getLineCount();
		System.out.println(taux);
		int numText = (int) (c.name.length() * taux);
		sl= new StaticLayout(c.name.substring(0, numText), textPaint, (int)view_width, Alignment.ALIGN_CENTER, 1f, 0f, false);
		canvas.translate(d,startP+d);
		sl.draw(canvas);
	}
	
	private float getDistance(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		String FinDuMonde = "20121221";
		String timeString = (startTime < 10) ? ("0" + startTime + ":00"): (startTime + ":00");
		Date t2 = null, t1 = null;
		try {
			t2 = sdf.parse(FinDuMonde + " " + time);
			t1 = sdf.parse(FinDuMonde + " " + timeString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long sed = (t2.getTime() - t1.getTime()) / 1000;
		return (time_distance * sed / 3600);
	}
	
	public void addClass(Cours c){
		class_list.add(c);
	}
	
	private float getTextHeight(TextPaint p) {
		FontMetrics fm = p.getFontMetrics();
		return (fm.bottom - fm.top);
	}
}
