package com.sky.opam.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sky.opam.model.Cours;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("SimpleDateFormat")
public class ClassView extends View {
	private float scale; //screen density
	private float delta;
	private float timeW;
	private int offset=2;
	private int r=3;   //radius of circle
	private int l=45;  //longeur of every 2 circles
	private String FinDuMonde = "20121221";
	SimpleDateFormat sdf=new  SimpleDateFormat("yyyyMMdd HH:mm");
	private List<Cours> cours = new ArrayList<Cours>();
	private List<float[]> flag = new ArrayList<float[]>();
	private int SW;
	private MyViewclickListener myClcLis ;
	private boolean istoday = false; 
	private boolean timeOut = true;
	private int cr =4; 
	long d; //the distance of time now
	
	Paint linePaint = new Paint();
	Paint timePaint = new Paint();
	Paint NamePaint = new Paint();
	Paint TextPaint = new Paint();
	Paint smallPaint = new Paint();
	Paint classPaint = new Paint();
	Paint starPaint = new Paint();

	public ClassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initia(context);
	}
	public ClassView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		initia(context);	
	}
	public ClassView(Context context) {
		super(context);
		initia(context);
	}
	private void initia(Context context){
		scale = context.getResources().getDisplayMetrics().density;
		//initiation the values dip
		offset = dip2px(2);
		r = dip2px(3);
		l = dip2px(45);
		cr = dip2px(4);
		//mGestureDetector = new GestureDetector(getContext(), this); 
		linePaint.setColor(Color.BLUE);
		linePaint.setStrokeWidth(dip2px(2));
		//show the at left
		timePaint.setColor(Color.WHITE);
		timePaint.setTextSize(4*r);
		delta = getTextHeight(timePaint)/2-r;
		timeW=timePaint.measureText("08:00 ");
		//show the name of class
		NamePaint.setTextAlign(Paint.Align.CENTER); //centre!!!!!
		NamePaint.setColor(Color.argb(255, 41, 199, 230));
		NamePaint.setTextSize(dip2px(16));
		NamePaint.setTypeface(Typeface.DEFAULT_BOLD);
		//show other info of class
		TextPaint.setTextAlign(Paint.Align.CENTER); 
		TextPaint.setColor(Color.WHITE);
		TextPaint.setTextSize(dip2px(12));
		//show the line selected of class
		smallPaint.setColor(Color.RED);
		smallPaint.setStrokeWidth(dip2px(3));
		//show the line catche the class
		classPaint.setColor(Color.argb(200, 230, 237, 18));
		classPaint.setStyle(Style.STROKE);
		PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);  
		classPaint.setPathEffect(effects);
	}
	
	public void setClickListener(MyViewclickListener clickListener) {
		this.myClcLis = clickListener;
	}
	public void setCours(List<Cours> cours) {
		this.cours = cours;
	}
	
	public void setIstoday(boolean istoday) {
		this.istoday = istoday;
		starPaint.setColor(Color.YELLOW);
		starPaint.setStyle(Paint.Style.STROKE);
		starPaint.setStrokeWidth(1);
		new Thread(myRun).start();
	}
	public void setSW(int sW) {
		SW = sW;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		flag.clear();
		canvas.drawLine(timeW, delta+r, timeW, delta+r+11*l, linePaint);
		for(int i=0;i<12;i++){			
			int temps = i+8;
			String time = (temps<10)? ("0"+temps+":00"):(""+temps+":00");		
			canvas.drawText(time, 0, getTextY(delta+r+l*i, timePaint), timePaint);
			//canvas.drawCircle(12*r, delta+r+i*l, r, paint);
			canvas.drawLine(timeW, delta+r+i*l, timeW+dip2px(5), delta+r+i*l, linePaint);
		}
		if(istoday){
			canvas.drawCircle(timeW, d+delta+r, cr, starPaint);
		}
		for(Cours c : cours){
			drawCours(canvas, c);
		}
		if(cours.size()==0){
			for(int i=0;i<4;i++){
				float notificaY = getTextY(r+delta+i*3*l+getTextHeight(NamePaint)/2, NamePaint);
				canvas.drawText("no course today", (SW+timeW+r)/2, notificaY, NamePaint);
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(2*SW, (int) (2*r+11*l+2*delta));
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private long getDistance(String time){
		Date t2 = null,t1 = null;
		try {
			t2 = sdf.parse(FinDuMonde+" "+time);
			t1 = sdf.parse(FinDuMonde+" 08:00");	
		} catch (ParseException e){
			e.printStackTrace();
		}
		long sed=(t2.getTime()-t1.getTime())/1000;
		return (l*sed/3600);
	}
	
	private float getTextY(float centre, Paint p){
		FontMetrics fm = p.getFontMetrics(); 
		return centre-(fm.descent+fm.ascent)/2;
	}
	private float getTextHeight(Paint p){
		FontMetrics fm = p.getFontMetrics(); 
		return (fm.descent-fm.ascent);
	}
	private void drawCours(Canvas canvas,Cours c){
		if(c.type.contains("Examen")){
			NamePaint.setColor(Color.argb(255, 248, 220, 24));
		}else {
			NamePaint.setColor(Color.argb(255, 41, 199, 230));
		}
		float nameY,timeY,salleY;
		long d1 = getDistance(c.debut);
		long d2 = getDistance(c.fin);
		float totalHeight = getTextHeight(NamePaint)+2*getTextHeight(TextPaint)+2*offset;				
		
		FontMetrics fm = NamePaint.getFontMetrics();
		nameY = (d1+d2)/2+r+delta-totalHeight/2-fm.ascent;
		fm = TextPaint.getFontMetrics();
		salleY = (d1+d2)/2+r+delta+totalHeight/2-fm.descent;
		timeY = salleY-offset-getTextHeight(TextPaint);
		
		//write the lines
		canvas.drawCircle(timeW, d1+r+delta, r, smallPaint);
		canvas.drawCircle(timeW, d2+r+delta, r, smallPaint);
		canvas.drawLine(timeW, d1+r+delta, timeW, d2+r+delta, smallPaint);
		
		float jiao = (d2-d1-totalHeight)/2;
		canvas.drawLine(timeW, d1+r+delta, timeW+jiao, d1+r+delta+jiao, classPaint);
		canvas.drawLine(timeW, d2+r+delta, timeW+jiao, d2+r+delta-jiao, classPaint);
		canvas.drawLine(timeW+jiao, d1+r+delta+jiao, SW, d1+r+delta+jiao, classPaint);
		canvas.drawLine(timeW+jiao, d2+r+delta-jiao, SW, d2+r+delta-jiao, classPaint);
		
		float[] a = {d1+r+delta+jiao,d2+r+delta-jiao};
		flag.add(a);
		
		//write the contenu of class
		canvas.drawText(c.name, (SW+timeW+r)/2, nameY, NamePaint);
		canvas.drawText(c.debut+"--"+c.fin, (SW+timeW+r)/2, timeY, TextPaint);
		canvas.drawText(c.salle, (SW+timeW+r)/2, salleY, TextPaint);
	}
	
	private Runnable myRun = new Runnable() {
		@Override
		public void run() {
			int count=100;			
			while (timeOut) {							
				if(count==100){
					Date now = new Date();
					SimpleDateFormat sdf0 = new  SimpleDateFormat("HH:mm");
					sdf0.applyPattern("H");
					String time = sdf0.format(now);
					time+=":";
					sdf0.applyPattern("m");
					time+=sdf0.format(now);
					d = getDistance(time);
					if(d<0 || d>11*l) timeOut=false;
					count=0;
				}
				count++;
				cr--;
				cr = (cr==-1)?(12):(cr);
				//cr=cr%15;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}
				postInvalidate();
			}
			
		}
		
	};



////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) { 
        case MotionEvent.ACTION_DOWN:
        	if(e.getX()>timeW){
    			for(int i=0;i<flag.size();i++){
    				float[] f = flag.get(i);
    				if(f[0]<e.getY() && e.getY()<f[1]){
    					myClcLis.onTouchEvent(this, e,i);
    				}
    			}
    		}
        	break;       
        case MotionEvent.ACTION_MOVE:
        	break;
        case MotionEvent.ACTION_UP:      	
        	break; 
        } 
		return false;
	}

	public int dip2px(int dipValue){
		return (int)(dipValue * scale + 0.5f);
	}
	
}
