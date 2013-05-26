package com.sky.opam.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sky.opam.model.Cours;

import android.R.integer;
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
import android.view.GestureDetector;
import android.view.View.OnClickListener;

public class CopyOfClassView extends View  implements OnClickListener,GestureDetector.OnGestureListener {
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
	private int SH;
	private MyViewclickListener myClcLis ;
	private GestureDetector mGestureDetector; 
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

	public CopyOfClassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initia();
	}
	public CopyOfClassView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		initia();	
	}
	public CopyOfClassView(Context context) {
		super(context);
		initia();
	}
	private void initia(){
		mGestureDetector = new GestureDetector(getContext(), this); 
		linePaint.setColor(Color.BLUE);
		linePaint.setStrokeWidth(2);
		//show the at left
		timePaint.setColor(Color.WHITE);
		timePaint.setTextSize(4*r);
		delta = getTextHeight(timePaint)/2-r;
		timeW=timePaint.measureText("08:00 ");
		//show the name of class
		NamePaint.setTextAlign(Paint.Align.CENTER); //centre!!!!!
		NamePaint.setColor(Color.argb(255, 41, 199, 230));
		NamePaint.setTextSize(16);
		NamePaint.setTypeface(Typeface.DEFAULT_BOLD);
		//show other info of class
		TextPaint.setTextAlign(Paint.Align.CENTER); 
		TextPaint.setColor(Color.WHITE);
		TextPaint.setTextSize(12);
		//show the line selected of class
		smallPaint.setColor(Color.RED);
		smallPaint.setStrokeWidth(3);
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
		if(sW==240){
			timePaint.setTextSize(3*r);
			delta = getTextHeight(timePaint)/2-r;
			timeW=timePaint.measureText("08:00 ");
			NamePaint.setTextSize(14);
			TextPaint.setTextSize(10);
			smallPaint.setStrokeWidth(2);
		}
		int nbMaxText = (int) ((SW-(timeW+5))/NamePaint.measureText(" "));
	}
	public void setSH(int sH) {
		SH = sH;
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
			canvas.drawLine(timeW, delta+r+i*l, timeW+5, delta+r+i*l, linePaint);
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
	float px,py;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) { 
        case MotionEvent.ACTION_DOWN: 
        	System.out.println("ACTION_DOWN x:"+event.getX());
        	System.out.println("ACTION_DOWN y:"+event.getY());
        break; 

        case MotionEvent.ACTION_UP: 
        	System.out.println("ACTION_UP x:"+event.getX());
        	System.out.println("ACTION_UP y:"+event.getY());
        break; 
        } 
       return mGestureDetector.onTouchEvent(event); 
	}
	@Override
	public boolean onDown(MotionEvent arg0) {		
		return false;
	}
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,float arg3) {
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		if(e.getX()>timeW){
			for(int i=0;i<flag.size();i++){
				float[] f = flag.get(i);
				if(f[0]<e.getY() && e.getY()<f[1]){
					myClcLis.onTouchEvent(this, e,i);
				}
			}
		}
	}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {		
		return false;
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}
