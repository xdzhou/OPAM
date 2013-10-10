package com.sky.opam.adapter;

import java.util.List;

import com.sky.opam.R;
import com.sky.opam.model.Cours;
import com.sky.opam.outil.DBworker;
import com.sky.opam.widget.ClassView;
import com.sky.opam.widget.MyViewclickListener;

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
import android.widget.TextView;

@SuppressLint("ValidFragment")
//fragment who show the class view
public class ClassFragment extends Fragment {
	private static final String KEY_LOGIN = "ClassFragment:login";
	private static final String KEY_FLAG = "ClassFragment:flag";
	private static final String KEY_ISTODAY = "ClassFragment:istoday";
	private static final String KEY_ISTW = "ClassFragment:isTW";
	private String login;
	private String flag;
	private boolean isToday;
	private boolean isTW;

	public static ClassFragment newInstance(String login, String flag,
			boolean isToday, boolean isTW) {
		ClassFragment fragment = new ClassFragment();
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null)
			return null;
		ScrollView scroller = new ScrollView(getActivity());
		if (!isTW)
			scroller.setBackgroundColor(Color.argb(255, 153, 102, 0));
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
		win.setContentView(R.layout.cours_detail_2);

		TextView name = (TextView) win.findViewById(R.id.className);
		TextView type = (TextView) win.findViewById(R.id.classType);
		TextView time = (TextView) win.findViewById(R.id.classTime);
		TextView group = (TextView) win.findViewById(R.id.classGroup);
		TextView room = (TextView) win.findViewById(R.id.classRoom);
		TextView teacher = (TextView) win.findViewById(R.id.classTeacher);
		name.setText(c.name);
		type.setText(c.type);
		time.setText(c.debut + "--" + c.fin);
		group.setText(c.groupe);
		room.setText(c.salle);
		teacher.setText(c.formateur);

		Button button = (Button) win.findViewById(R.id.dialog_button_cancel);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.cancel();
			}
		});
	}
}
