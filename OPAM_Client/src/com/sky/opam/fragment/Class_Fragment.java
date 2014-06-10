package com.sky.opam.fragment;

import java.util.List;

import com.sky.opam.R;
import com.sky.opam.model.Cours;
import com.sky.opam.tool.DBworker;
import com.sky.opam.view.ClassView;
import com.sky.opam.view.MyViewclickListener;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

@SuppressLint("ValidFragment")
//fragment who show the class view
public class Class_Fragment extends Fragment {
	private static final String KEY_LOGIN = "ClassFragment:login";
	private static final String KEY_FLAG = "ClassFragment:flag";
	private static final String KEY_ISTODAY = "ClassFragment:istoday";
	private static final String KEY_ISTW = "ClassFragment:isTW";
	private String login;
	private String flag;
	private boolean isToday;
	private boolean isTW;

	public static Class_Fragment newInstance(String login, String flag, boolean isToday, boolean isTW) {
		Class_Fragment fragment = new Class_Fragment();
		fragment.login = login;
		fragment.flag = flag;
		fragment.isToday = isToday;
		fragment.isTW = isTW;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_LOGIN)
				&& savedInstanceState.containsKey(KEY_FLAG)
				&& savedInstanceState.containsKey(KEY_ISTODAY)
				&& savedInstanceState.containsKey(KEY_ISTW)) {
			login = savedInstanceState.getString(KEY_LOGIN);
			flag = savedInstanceState.getString(KEY_FLAG);
			isToday = savedInstanceState.getBoolean(KEY_ISTODAY);
			isTW = savedInstanceState.getBoolean(KEY_ISTW);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_LOGIN, login);
		outState.putString(KEY_FLAG, flag);
		outState.putBoolean(KEY_ISTODAY, isToday);
		outState.putBoolean(KEY_ISTW, isTW);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) return null;
		ScrollView scroller = new ScrollView(getActivity());
		//if (!isTW) scroller.setBackgroundColor(Color.argb(255, 153, 102, 0));
		ClassView myview = new ClassView(getActivity());
		WindowManager manager = getActivity().getWindowManager();
		Display display = manager.getDefaultDisplay();
		myview.setSW(display.getWidth());
		if (isToday) {
			myview.setIstoday(true);
		}
		DBworker worker = new DBworker(getActivity().getApplicationContext());
		final List<Cours> cours = worker.trouverCours(login, flag);
		myview.setCours(cours);
		myview.setClickListener(new MyViewclickListener() {
			@Override
			public void onTouchEvent(View v, MotionEvent event, int i) {
				// if(v.getId()==R.id.myview){
				Cours c = cours.get(i);
				showClassInfo(c);
				// }
			}
		});

		scroller.addView(myview);
		return scroller;
	}

	private void showClassInfo(Cours c) {
		final Dialog dlg = new Dialog(getActivity(), R.style.MyDialog);
		dlg.show();
		Window win = dlg.getWindow();
		win.setContentView(R.layout.cours_detail_pop);

		((TextView) win.findViewById(R.id.className)).setText(c.name);
		((TextView) win.findViewById(R.id.classType)).setText(c.type);
		((TextView) win.findViewById(R.id.classTime)).setText(c.debut + "--" + c.fin);
		((TextView) win.findViewById(R.id.classGroup)).setText(c.groupe.replace("__", "\n"));
		if(c.salle!=null || !c.salle.equals("")) ((TextView) win.findViewById(R.id.classRoom)).setText(c.salle.replace("__", "\n"));
		if(c.formateur!=null || !c.formateur.equals("")) ((TextView) win.findViewById(R.id.classTeacher)).setText(c.formateur.replace("__", "\n"));
		

		Button button = (Button) win.findViewById(R.id.dialog_button_cancel);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}
	
	private void multiValue(){
		
	}
}
