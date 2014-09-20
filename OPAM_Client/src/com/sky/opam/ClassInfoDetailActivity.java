package com.sky.opam;

import com.sky.opam.core.DBworker;
import com.sky.opam.model.ClassInfo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ClassInfoDetailActivity extends ActionBarActivity{
	private DBworker worker;
	private ClassInfo classInfo;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classinfo_detail_activity);
		setActionBar();
//		long classId = getIntent().getExtras().getLong("classId");
//		worker = new DBworker(this);
//		classInfo = worker.getClassInfo(classId);
		test();
		initView();
	}
	
	private void initView(){
		((TextView)findViewById(R.id.className)).setText(classInfo.name);
		((TextView)findViewById(R.id.classType)).setText(classInfo.classType.name);
		((TextView)findViewById(R.id.classTime)).setText(classInfo.startTime+" -- "+classInfo.endTime);
		((TextView)findViewById(R.id.classGroup)).setText(classInfo.groupe);
		((TextView)findViewById(R.id.classRoom)).setText(classInfo.room.name);
		((TextView)findViewById(R.id.classTeacher)).setText(classInfo.teacher);	
		
		ListView student_list = (ListView)findViewById(R.id.student_list);
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.date_dropdown_spinner_layout, classInfo.students.split("_"));
		StudentListAdapter adapter = new StudentListAdapter(this, classInfo.students.split("_"));
		student_list.setAdapter(adapter);
		student_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView tv = (TextView)view;
				String name = tv.getText().toString();
				
			}
		});
	}

	private void setActionBar(){
		ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
	}
	
	private void test(){
		classInfo = new ClassInfo();
		classInfo.name = "JAVA and UML";
		classInfo.teacher = "Teacher Wang";
		classInfo.startTime = "08:30";
		classInfo.endTime = "10:30";
		classInfo.classType.name = "TP";
		classInfo.room.name = "B302";
		classInfo.groupe = "Gp EI3 ASR";
		classInfo.students = "ABBES Mohamed Salim_ABDEDAYEM Ghassen_ABDELAZIZ Hanifa_AKPOUE Ahou Ange Dominique_ALVAREZ SANTANA GUILLERMO_ASSOU Mohamed_AUGUIN Nicolas_AZOUNI Haroun_BACCARI Ahmed_BADAOUI Yasmine_BADIANE Aïssatou_BALLESTEROS Hugo_BALTA Silvia Iona_BARON Fabien_BARZYKOWSKI Alexandre_BATAILLE Arthur_BAYEUX Adrien_BELGHAZI Bassim_BENKIRANE Karima_BENZAKOUR Mohammed_BENZEKRI Mohammed_BERENGER Olivier_BERGELIN Baptiste_BERLAND Clément_BERNIER Paul_BERRADA Mohammed_BERRADA SOUNI Salma_BLAISOT Aurélie_BLANC-PATIN Marc_BOGAERT ELODIE_BOUCHFAA Samir_BOUMIZA Inès_BOURAOUI Sadok_BRAHIM Souheil_BRIAND Tugdual_BROUARD Paul_BRUNETTA Mickaël_BUDA Sophie_BURON Cédric_CAMARA Idy_CAMPION Amaury_CASTELLI Raphaël_CAUDRELIER Baptiste_CERLES Clément_CHAAL Jalal_CHAHIBI Nassime_CHAPUZET Benjamin_CHARKSI Ahmed_CHAVOT Tristan_CHENEBAUX Maixent_CHERMITI Souhir_CHEVALIER Marion_CID Jean-Baptiste_CISSE Mohamed dit Seyba_CORSALETTI-KANPARIAN Maxime_COURMES-VARGUES Marianne_COUTÉ Lucie_COYNE Christel_DAMNON Geoffroy_DAVID Mathilde_DEBURGHGRAEVE Clément_DESFONDS Thomas_DUPRAZ Théophile_EL AFRIT Khadija_EL GAOUZI Imane_ELAASSAL Ayoub_EL-ALEM Mohamed_ERSCHIG Cyprien_ETCHEBERRY Victor_EYMARD Romain_FADLAOUI Omar_FAKAM SIEMBA Mylène Jackelle_FAURE Guillaume_FERT Dimitri_FISCHER Louis_FOLTZ-RAHEM Arthur_FRADIN Marine_GABELLA Etienne_GALL Pierrick_GANDON Lorenz_GARIN Thomas_GAY-PERRET Ameline_GHARBI Mohamed Mehdi_GIOVANGIGLI Antoine_GORDE Maxence_GRAMA Adina_GRAUX Damien_GUEZ Yohan_GUILLAUME Eric_GUO Dongdong_HADJ TAIEB Mahdi_HAENER Guillaume_HAN Xiao_HASCOËT Nicolas_HASSAINE Mohammed Djalal_HENNIART Mathieu_HENRY Corentin_ISMAILI Mohammed_JABRI Mohamed_JABROUNI Malek_JACQUOT Vincent_JAMLI Maissa_JI Cai_JIAO Yang_JOURDREN Arthur_KACIM Khalil_KAMMOUN Amal_KERVELLA Jean Sébastien_KHEMLICHE Aghiles_KHERIBECHE Malik_KHSIBA Mohamed Achraf_KOTARBA Romain_KUITCHENG MAGANG Omeride Cristelle_LACROIX Camille_LADJIMI Farid_LAHBABI Leïla_LAMOTHE Jean-Loup_LARBI Abbas_LATOUR Eric_LAVABRE Aurore_LAZAAR DOUNIA_LEFEVRE Matthieu_LIU Ji_LOUARAK Soufiane_LUCAS Charles_MAHO Anaëlle_MALTRAIT Léo_MANEA Ioana Constanta_MARECAUX Samantha_MARGUERIT Manon_MARTIN Hugo_MAYOU Ilham_MESLI Kamila_MGHAZLI Zyad_MOATASSIM Mehdi_MONTABRUN Pierre-Edouard_MONTEL Florent_MOREL Coralie_MRAD Asma_MUSELLI Hugo_NANGO TEMAH Paulette Linda_NARBONI Jonathan_NGUYEN Estelle_NIKOLAYEVA Iryna_OLIVEIRA Audrey_OMBREDANE Aurore_OTMAN Abdulrahman_OUILLON Stéphanie_OUMZIL Youness_PELAMI MONGA Aurélie_PÉRONNET Adrien_PESENTI Vincent_PETIT Antoine_PIRET Antoine_PRAT Matthieu_PRON Alexandre_PUIBARAUD Marine_REBOUD Nathan_RICHARD Nicolas_RICHER Ariane_ROCHÉ Maxime_RODRIGUEZ ALVAREZ Natalia Maria_ROLLAND Lucrèce_ROUCOU Anne-Cécile_ROUSSEAU Sylvain_RUBIANO Julien_SACARABANY Pauline_SANGARÉ Amadou_SAUVAGE Eric_SCAICEANU Victor Petru_SERRANO Léa_SHI Yi_SOSTHÈNE Julien_SOUZA Pauline_SUIHLI Inès_TALEB Youssef_TILLET Philippe_TOSSOU Laureen_TOUIL Mohamed_VAN Kha-Man_VANDEWOESTYNE Elodie_VENKATESAN Valérie_VERHEECKE Allan_WANG Yue_ZAHOUANI Zineb_ZARKA Jodie_ZEBBOUCHE Samir_ZERBIB Daniel_ZHOU Xiandong_ZHOU Yong_ZOUARI Slim";
		
	}
	
	private class StudentListAdapter extends ArrayAdapter<String>{
		private Context context;
		public StudentListAdapter(Context context, String[] data) {		
			super(context, 0,data);
			this.context = context;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(context);
			tv.setText(getItem(position));			
			return tv;
		}	
	}

}
