package com.sky.opam.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sky.opam.model.Cours;
import com.sky.opam.tool.Tool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DayTabClassView extends View implements GestureDetector.OnGestureListener{
	private int startTime ;
	private int endTime ;
	private float view_width ;
	private float time_distance ;
	private List<Cours> class_list = new ArrayList<Cours>();
	private Paint outLinePaint = new Paint();
	private Paint backgroundPaint = new Paint();
	private TextPaint textPaint = new TextPaint();
	private float d;
	private GestureDetector mGestureDetector;
	private ClassInfoClickListener myClcLis;

	public DayTabClassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initia(context);
	}

	public DayTabClassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initia(context);
	}

	public DayTabClassView(Context context) {
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
		mGestureDetector = new GestureDetector(getContext(), this);
		outLinePaint.setColor(Color.BLUE);
		outLinePaint.setStrokeWidth(Tool.dip2px(context, 1));
		backgroundPaint.setColor(Color.GRAY);
		backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);//设置填满
		textPaint.setColor(Color.WHITE);
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
		float startP = getTimeDistance(c.debut);
		float endP = getTimeDistance(c.fin);
		canvas.drawRect(d, startP+d, view_width-d, endP-d, backgroundPaint);
		canvas.save();
		StaticLayout sl= new StaticLayout(c.name, textPaint, (int)(view_width-2*d), Alignment.ALIGN_CENTER, 1f, 0f, false);
		float MaxLine = (endP-startP-2*d)/getTextHeight(textPaint);
		if(MaxLine < sl.getLineCount()){
			float taux = MaxLine / sl.getLineCount();
			int numText = (int) (c.name.length() * taux);
			sl= new StaticLayout(c.name.substring(0, numText), textPaint, (int)view_width, Alignment.ALIGN_CENTER, 1f, 0f, false);
		}
		canvas.translate(d,startP+d);
		sl.draw(canvas);
		canvas.restore();
	}
	
	private float getTimeDistance(String time) {
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
	
	public void addClass(List<Cours> list){
		if(list!=null && list.size()>0){
			class_list.addAll(list);
		}
	}
	
	private float getTextHeight(TextPaint p) {
		FontMetrics fm = p.getFontMetrics();
		return (fm.bottom - fm.top);
	}	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return true;
	}
	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if(myClcLis!=null){
			for (Cours c: class_list) {
				float startP = getTimeDistance(c.debut);
				float endP = getTimeDistance(c.fin);
				if (startP < e.getY() && e.getY() < endP) {
					myClcLis.onTouchEvent(this, e, c);
					break;
				}
			}
		}	
		return true;
	}
	
	public void setClickListener(ClassInfoClickListener clickListener) {
		this.myClcLis = clickListener;
	}
}
