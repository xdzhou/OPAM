package com.sky.opam.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.loic.common.Chiffrement;
import com.loic.common.LibApplication;
import com.sky.opam.R;
import com.sky.opam.model.ClassEvent;
import com.sky.opam.model.ClassUpdateInfo;
import com.sky.opam.model.Student;
import com.sky.opam.model.Student.SchoolEnum;
import com.sky.opam.model.User;
import com.sky.opam.tool.AdditionalKeyStoresSSLSocketFactory;
import com.sky.opam.tool.DBworker;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class IntHttpService extends Service 
{
	private static final String TAG = IntHttpService.class.getSimpleName();
	private static final String CAS_HOST = "https://cas.tem-tsp.eu/";  //CAS : host 157.159.10.172
	private static final String SI_HOST = "http://si-etudiants.tem-tsp.eu/";  //SI_Student : host 157.159.10.180
	public static final String ENCRPT_KEY = "opam";
	public static final String PROFILE_CACHE_FLOLDER;

	static
	{
		PROFILE_CACHE_FLOLDER = LibApplication.getAppContext().getFilesDir().getAbsolutePath();
	}
	
	private final IBinder mBinder = new LocalBinder();
	private HttpContext httpContext;
	private AndroidHttpClient client;
	
	private DateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.US);
	private DBworker worker;
	private String login;    // "he_huilo";
	private String password; // "pancakes";
	private AgendaWebParams agendaWebParams;
	
	private HandlerThread workerThread;
	private Handler workerHandler;
	private LinkedList<LoadClassTaskParams> loadClassParamsQueue;
	private Date classLoadingDate;
	
	private Object loginPasswordLock = new Object();
	
	public class LocalBinder extends Binder 
	{
		public IntHttpService getService()
		{
            return IntHttpService.this;
        }
    }
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		initHttpClient();
		loadClassParamsQueue = new LinkedList<IntHttpService.LoadClassTaskParams>();
		workerThread = new HandlerThread("IntHttpService-workerThread");
		workerThread.start();
		workerHandler = new Handler(workerThread.getLooper());
		
		worker = DBworker.getInstance();
		User defautUser = worker.getDefaultUser();
		if(defautUser != null)
		{
			setLoginPassword(defautUser.login, Chiffrement.decrypt(defautUser.password, ENCRPT_KEY));
		}
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}
	
	private void initHttpClient()
	{
		agendaWebParams = new AgendaWebParams();
		
		httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
		client = AndroidHttpClient.newInstance("Mozilla/5.0 (Linux; Android 5.0; MI 2 Build/LRX21M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Mobile Safari/537.36");

		HttpConnectionParams.setConnectionTimeout(client.getParams(), 4000);
		//HttpConnectionParams.setSoTimeout(client.getParams(), 6000);
		
		SchemeRegistry schemeRegistry = client.getConnectionManager().getSchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", createAdditionalCertsSSLSocketFactory(), 443));
	}
	
	private SSLSocketFactory createAdditionalCertsSSLSocketFactory() 
	{
	    try 
	    {
	        final KeyStore ks = KeyStore.getInstance("BKS");
	        // the bks file we generated above
	        final InputStream in = LibApplication.getAppContext().getResources().openRawResource(R.raw.mystore);  
	        try 
	        {
	            ks.load(in, "opam123".toCharArray());
	        } 
	        finally 
	        {
	            in.close();
	        }
	        return new AdditionalKeyStoresSSLSocketFactory(ks);
	    } 
	    catch(Exception e) 
	    {
	        throw new RuntimeException(e);
	    }
	}
	
	/******************************************************
	 **********************Task Runnable*******************
	 ******************************************************/
	private Runnable loginRunnable = new Runnable() 
	{	
		@Override
		public void run() 
		{
		}
	};
	
	private Runnable LoadClassRunnable = new Runnable() 
	{	
		@Override
		public void run() 
		{
			LoadClassTaskParams taskParams;
			synchronized (loadClassParamsQueue) 
			{
				taskParams = loadClassParamsQueue.poll();
			}
			if(taskParams != null)
			{
				requestAgendaInfo(taskParams.searchDate, taskParams.callbackWeakReference);
			}
		}
	};
	
	private Runnable SearchStudentRunnable = new Runnable() 
	{	
		@Override
		public void run() 
		{
			
		}
	};
	
	private Runnable StopRunnable = new Runnable() 
	{	
		@Override
		public void run() 
		{
			
		}
	};
	
	/*
	 * login to INT server
	 * 
	 * @param login user name
	 * @param password 
	 * @param callback
	 */
	public void asyncLogin(final String login, final String password, asyncLoginReponse callback)
	{
		final WeakReference<asyncLoginReponse> callbackWeakReference = new WeakReference<asyncLoginReponse>(callback);
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				requestLogin(login, password, callbackWeakReference);
			}
		}).start();
	}
	
	private void requestLogout()
	{
		boolean isLogin = false;
		synchronized (loginPasswordLock)
		{
			isLogin = (login != null) && (password != null);
		}
		
		if(isLogin)
		{
			executeHttpRequest(new HttpGet("https://ecampus.tem-tsp.eu/uPortal/Logout"), new HttpServiceErrorEnumReference(HttpServiceErrorEnum.OkError));
		}
		reset();
		setLoginPassword(null, null);
	}
	
	private void requestLogin(String login, String password, WeakReference<asyncLoginReponse> callbackWeakReference)
	{
		requestLogout();
		setLoginPassword(login, password);
		HttpServiceErrorEnumReference errorEnumRef = new HttpServiceErrorEnumReference(HttpServiceErrorEnum.OkError);
		HttpResponse response = executeHttpRequest(new HttpGet(SI_HOST), errorEnumRef);
		
		if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
		{
			response = this.executeHttpRequest(new HttpGet(SI_HOST + "/OpDotNet/Noyau/Bandeau.aspx?"), errorEnumRef);
		}

		if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
		{
			String userName = getUserName(errorEnumRef, response);
			User user = new User(login, Chiffrement.encrypt(password, ENCRPT_KEY), userName);
			
			DBworker dBworker = DBworker.getInstance();
			dBworker.insertData(user);
			
			if(userName == null)
				Log.e(TAG, "Can't find user name, error : "+errorEnumRef.errorEnum.getDescription());
			
			errorEnumRef.errorEnum = HttpServiceErrorEnum.OkError;
			loadProfilImg(login);
		}
		
		if(errorEnumRef.errorEnum != HttpServiceErrorEnum.OkError)
		{
			requestLogout();
		}
		
		if(callbackWeakReference != null && callbackWeakReference.get() != null)
			callbackWeakReference.get().onAsyncLoginReponse(login, errorEnumRef.errorEnum);
	}
	
	private String getUserName(HttpServiceErrorEnumReference errorEnumRef, HttpResponse response)
	{
		String userName = null;
		if(errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError && response != null)
		{
			try 
			{
				String rspHtml = EntityUtils.toString(response.getEntity());
				response.getEntity().consumeContent();
				Document doc = Jsoup.parse(rspHtml);
				Element element = doc.getElementById("Menu");
				if(element != null)
				{
					Log.i(TAG, element.text());
					
					Elements spanEles = element.getElementsByTag("span");
					if(spanEles != null)
					{
						for(Element ele : spanEles)
						{
							if(ele.text().contains("Bonjour"))
							{
								userName = ele.text().replace("Bonjour", "");
								userName = userName.replace(getSpecailSpace(), "");
								break;
							}
						}
					}
				}
			} 
			catch (Exception e) 
			{
				errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
				errorEnumRef.errorEnum.setDescription(e.getMessage());
			}
		}
		
		if(userName == null)
		{
			errorEnumRef.errorEnum = HttpServiceErrorEnum.HtmlContentError;
			errorEnumRef.errorEnum.setDescription("Can't find user name ...");
		}
		return userName;
	}
	
	private void loadProfilImg(String login)
	{
		try 
        {
        	String imgPath = getUserProfileFilePath(login);
    		File document = new File(imgPath);
    		if(!document.exists())
    		{
    			InputStream in = new java.net.URL("http://trombi.it-sudparis.eu/photo.php?uid="+login+"&h=80&w=80").openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                FileOutputStream fos = new FileOutputStream(document);  
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                in.close();
    		}
        } 
        catch (Exception e) 
        {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
	}
	
	/*
	 * get the list of class information for a month
	 * 
	 * @param date the date selected
	 * @param callback 
	 */
	public void asyncLoadClassInfo(int year, int month, asyncGetClassInfoReponse callback)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		asyncLoadClassInfo(calendar.getTime(), callback);
	}
	
	public void asyncLoadClassInfo(Date date, asyncGetClassInfoReponse callback)
	{
		if(date != null && callback != null && login != null && password != null)
		{
			if(classLoadingDate != null && classLoadingDate.getYear() == date.getYear() && classLoadingDate.getMonth() == date.getMonth())
			{
				Log.i(TAG, "classes for this year / month is loading : "+(date.getYear()+1900)+"/"+date.getMonth());
			}
			else 
			{
				WeakReference<asyncGetClassInfoReponse> callbackWeakReference = new WeakReference<asyncGetClassInfoReponse>(callback);
				synchronized (loadClassParamsQueue) 
				{
					loadClassParamsQueue.push(new LoadClassTaskParams(date, callbackWeakReference));
				}
				workerHandler.post(LoadClassRunnable);
			}
		}
	}
	
	private void requestAgendaInfo(Date date, WeakReference<asyncGetClassInfoReponse> callbackWeakReference)
	{
		classLoadingDate = date;
		HttpServiceErrorEnumReference errorEnumRef = new HttpServiceErrorEnumReference(HttpServiceErrorEnum.OkError);
		List<ClassEvent> classInfos = null;
		try 
		{
			HttpResponse response = null;
			
			if(! agendaWebParams.isFormItemsReady())
			{
				//request SI main page html and auto connect
				response = this.executeHttpRequest(new HttpGet(SI_HOST), errorEnumRef);
				if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
				{
					response.getEntity().consumeContent();
					//request SI bandeau page html
					response = this.executeHttpRequest(new HttpGet(SI_HOST + "/OpDotNet/Noyau/Bandeau.aspx?"), errorEnumRef);
				}
				if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
				{
					agendaWebParams.groupID = getGroupID(EntityUtils.toString(response.getEntity()));
					response.getEntity().consumeContent();
					if(agendaWebParams.groupID != null)
					{
						//request SI left menu page html
						response = this.executeHttpRequest(new HttpGet(SI_HOST + "/OpDotNet/Noyau/Rubriques.aspx?groupe=" + agendaWebParams.groupID), errorEnumRef);
					}
					else 
					{
						errorEnumRef.errorEnum = HttpServiceErrorEnum.HtmlContentError;
						errorEnumRef.errorEnum.setDescription("Can't find groupId from html ...");
					}
				}
				if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
				{
					getAppIDLienID(EntityUtils.toString(response.getEntity()));
					response.getEntity().consumeContent();
					if(agendaWebParams.appId != null && agendaWebParams.lienID != null)
					{
						//request SI Login/aspxtoasp page html
						response = this.executeHttpRequest(new HttpGet(SI_HOST + "OpDotnet/commun/Login/aspxtoasp.aspx?url=/Eplug/Agenda/Agenda.asp?IdApplication="+agendaWebParams.appId+"&TypeAcces=Utilisateur&IdLien="+agendaWebParams.lienID+"&groupe="+agendaWebParams.groupID), errorEnumRef);
					}
					else 
					{
						errorEnumRef.errorEnum = HttpServiceErrorEnum.HtmlContentError;
						errorEnumRef.errorEnum.setDescription("Can't find appID and lienID from html ...");
					}
				}
				if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
				{
					HttpPost request = createAspxtoaspRequest(response, errorEnumRef);
					response.getEntity().consumeContent();
					//request SI agenda page html
					response = this.executeHttpRequest(request, errorEnumRef);
				}
			}
			
			HttpPost request = createGetClassInfoRequest(response, date, errorEnumRef);
			if(response != null)
				response.getEntity().consumeContent();
			//request SI get class info
			if(request != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
				response = this.executeHttpRequest(request, errorEnumRef);

			if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
			{
				String rspHtml = EntityUtils.toString(response.getEntity());
				response.getEntity().consumeContent();
				Pattern pattern = Pattern.compile("onmouseover=\"DetEve\\(\'([0-9]+)\',\'([^']+)\',\'([0-9]+)\'\\)");
				Matcher matcher = pattern.matcher(rspHtml);
				classInfos = new ArrayList<ClassEvent>();
				
				while(matcher.find())
				{
					HttpServiceErrorEnumReference tempErrorEnumRef = new HttpServiceErrorEnumReference(HttpServiceErrorEnum.OkError);
					ClassEvent course = createClassInfo(tempErrorEnumRef, matcher.group(1), matcher.group(3));
					if(course != null && tempErrorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
					{
						classInfos.add(course);
					}
					else
					{
						Log.e(TAG, "Load Class failed, error : " + tempErrorEnumRef.errorEnum.getDescription());
					}
				}
				
				if(classInfos.isEmpty() && false) // can't find class info
				{
					//reset all params
					clearCookies();
					agendaWebParams.clear();
					//relance get class info request
					requestAgendaInfo(date, callbackWeakReference);
					//remove callback to avoid lance callback for second times
					callbackWeakReference = null;
				}
			}
		} 
		catch (Exception e) 
		{
			errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
			errorEnumRef.errorEnum.setDescription(e.toString());
		}
		
		DBworker worker = DBworker.getInstance();
		ClassUpdateInfo updateInfo = worker.getUpdateInfo(login, date);
		boolean needSave = false;
		if(updateInfo == null)
		{
			updateInfo = new ClassUpdateInfo(login);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			updateInfo.year = calendar.get(Calendar.YEAR);
			updateInfo.month = calendar.get(Calendar.MONTH);
			needSave = true;
		}
		
		if(errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
		{
			worker.deleteClassEvents(login, date);
			for(ClassEvent classEvent : classInfos)
				worker.insertData(classEvent);
			updateInfo.lastSuccessUpdateDate = new Date();
			updateInfo.classNumber = classInfos.size();
		}
		else 
		{
			updateInfo.lastFailUpdateDate = new Date();
			updateInfo.errorEnum = errorEnumRef.errorEnum;
		}
		
		if(needSave)
			worker.insertData(updateInfo);
		else
			worker.updateClassUpdateInfro(updateInfo);
		
		classLoadingDate = null;
		
		if(callbackWeakReference != null && callbackWeakReference.get() != null)
			callbackWeakReference.get().onAsyncGetClassInfoReponse(errorEnumRef.errorEnum, date, classInfos);
		else
			Log.e(TAG, "NO callback for asyncGetClassInfo");
	}
	
	private String getGroupID(String html)
	{
		Pattern pattern = Pattern.compile("IdGroupe = (\\d+)");
		Matcher matcher = pattern.matcher(html);
		if(matcher.find())
			return matcher.group(1);
		else
			return null;
	}
	
	private void getAppIDLienID(String html)
	{
		Pattern pattern = Pattern.compile("url=/Eplug/Agenda/Agenda.asp\\?([^;]+)&groupe="+agendaWebParams.groupID);
		Matcher matcher = pattern.matcher(html);
		if(matcher.find())
		{
			Log.i(TAG, matcher.group(1));
			String[] strings = matcher.group(1).split("&");
			for(int i = 0; i < strings.length; i++)
			{
				if(strings[i].startsWith("IdApplication"))
				{
					agendaWebParams.appId = strings[i].split("=")[1];
				}
				else if (strings[i].startsWith("IdLien")) 
				{
					agendaWebParams.lienID = strings[i].split("=")[1];
				}
			}
		}
	}
	
	private HttpPost createAspxtoaspRequest(HttpResponse response, HttpServiceErrorEnumReference errorEnumRef)
	{
		HttpPost httpPost = null;
		try 
		{
			httpPost = new HttpPost(SI_HOST + "/commun/aspxtoasp.asp");
			String rspHtml = EntityUtils.toString(response.getEntity());
			Document doc = Jsoup.parse(rspHtml);
			Elements inputElements = doc.getElementsByTag("INPUT");
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			
			if(inputElements != null)
			{
				for(Element ele : inputElements)
				{
					data.add(new BasicNameValuePair(ele.attr("name"), ele.attr("value")));
				}
			}
			if(data.isEmpty())
			{
				errorEnumRef.errorEnum = HttpServiceErrorEnum.HtmlContentError;
				errorEnumRef.errorEnum.setDescription("Cant find input elements for URL : "+httpPost.getURI());
			}
			else 
			{
				httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
			errorEnumRef.errorEnum.setDescription(e.toString());
		}
		return httpPost;
	}
	
	private HttpPost createGetClassInfoRequest(HttpResponse previousResponse, Date searchDate, HttpServiceErrorEnumReference errorEnumRef)
	{
		HttpPost httpPost = null;
		if(previousResponse != null && searchDate != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
		{
			httpPost = new HttpPost(SI_HOST + "/Eplug/Agenda/Agenda.asp");
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			try
			{
				if(! agendaWebParams.isFormItemsReady())
				{
					String rspHtml = EntityUtils.toString(previousResponse.getEntity());
					Document doc = Jsoup.parse(rspHtml);
					Element divAllElement = doc.getElementById("DivAll");
					
					if(divAllElement != null)
					{
						Elements elements = divAllElement.getElementsByTag("table");
						if(elements != null)
							elements.remove();
						elements = divAllElement.getElementsByTag("input");
						for(Element ele : elements)
						{
							String name = ele.attr("name");
							String value = ele.attr("value");
							
							value = (value == null) ? "" : value;
							value = name.equals("NumDat") ? df.format(searchDate) : value;
							
							if(name.equals("ValGra"))
								agendaWebParams.ValGra = value;
							else if (name.equals("NomCal"))
								agendaWebParams.NomCal = value;
							
							//Log.i(TAG, name+" - "+value);
							data.add(new BasicNameValuePair(name, value));
						}
					}
				}
				else 
				{
					data.add(new BasicNameValuePair("NumDat", df.format(searchDate)));
					data.add(new BasicNameValuePair("DebHor", "07"));
					data.add(new BasicNameValuePair("FinHor", "20"));
					data.add(new BasicNameValuePair("ValGra", agendaWebParams.ValGra));			
					data.add(new BasicNameValuePair("NomCal", agendaWebParams.NomCal));
					data.add(new BasicNameValuePair("NumLng", "1"));
					data.add(new BasicNameValuePair("FromAnn", "NO"));
				}
				data.add(new BasicNameValuePair("TypVis", "Vis-Moi.xsl"));
				
				if(data.size() > 1)
				{
					httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
				}
				else
				{
					errorEnumRef.errorEnum = HttpServiceErrorEnum.HtmlContentError;
					errorEnumRef.errorEnum.setDescription("Can't find get agenda post data...");
				}
			} 
			catch (Exception e) 
			{
				errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
				errorEnumRef.errorEnum.setDescription(e.toString());
			}
		}
		return httpPost;
	}
	
	/*
	 * search students by name
	 * 
	 * @param name the student's name to search (first name or last name)
	 * @param callback request finished callback
	 * 
	 */
	
	public void asyncSearchEtudiantByName(final String name, asyncSearchEtudiantByNameReponse callback)
	{
		final WeakReference<asyncSearchEtudiantByNameReponse> callbackWeakReference = new WeakReference<asyncSearchEtudiantByNameReponse>(callback);
		
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				List<Student> serachedEtudiants = null;
				HttpServiceErrorEnum errorEnum = HttpServiceErrorEnum.OkError;
				HttpPost post = new HttpPost("http://trombi.tem-tsp.eu/etudiants.php");
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user", name));
				//data.add(new BasicNameValuePair("ecole", ""));
				//data.add(new BasicNameValuePair("annee", ""));
				params.add(new BasicNameValuePair("submit", "Rechercher"));
				
				try 
				{
					post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
					HttpResponse response = client.execute(post, httpContext);
					int status = response.getStatusLine().getStatusCode();
					if(status == 200)
					{
						serachedEtudiants = new ArrayList<Student>();
						String rspHtml = EntityUtils.toString(response.getEntity());
						Document doc = Jsoup.parse(rspHtml);
						Element etudiantsEle = doc.getElementById("etudiants");
						Elements temElements = etudiantsEle.getElementsByClass("INTM");
						if(temElements != null && !temElements.isEmpty())
						{
							for(Element ele : temElements)
							{
								Student etudiant = createEtudiant(ele, SchoolEnum.TEM);
								if(etudiant != null)
									serachedEtudiants.add(etudiant);
							}
						}
						Elements tspElements = etudiantsEle.getElementsByClass("TINT");
						if(tspElements != null && !tspElements.isEmpty())
						{
							for(Element ele : tspElements)
							{
								Student etudiant = createEtudiant(ele, SchoolEnum.TSP);
								if(etudiant != null)
									serachedEtudiants.add(etudiant);
							}
						}
					}
					else 
					{
						errorEnum = HttpServiceErrorEnum.HttpBadStatusError;
						errorEnum.setDescription("Http bad status : "+status);
					}
					post.abort();
				} 
				catch (Exception e) 
				{
					errorEnum = HttpServiceErrorEnum.ExceptionError;
					errorEnum.setDescription(e.getMessage());
				}
				
				if(callbackWeakReference != null && callbackWeakReference.get() != null)
					callbackWeakReference.get().onAsyncSearchEtudiantByNameReponse(errorEnum, serachedEtudiants);
				else
					Log.e(TAG, "NO callback for asyncSearchEtudiantByName");
			}
		}).start();
	}
	
	public Student createEtudiant(Element ele, SchoolEnum schoolEnum)
	{
		Student etudiant = null;
		if(ele != null)
		{
			etudiant = new Student(schoolEnum);
			Elements elements = ele.getElementsByTag("img");
			if(elements != null && elements.size() > 0)
			{
				String login = elements.get(0).attr("src");
				if(login != null && login.length() > 0)
					etudiant.login = login.substring(15, login.length() - 10);
			}
			elements = ele.getElementsByClass("ldapNom");
			if(elements != null && elements.size() > 0)
				etudiant.name = elements.get(0).text();
			
			elements = ele.getElementsByClass("ldapInfo");
			Element infoEle = null;
			if(elements != null && elements.size() > 0)
				infoEle = elements.get(0);
			
			if(infoEle != null)
			{
				elements = infoEle.getElementsByTag("li");
				if(elements != null && elements.size() > 0)
					etudiant.grade = elements.get(0).text();
				elements = infoEle.getElementsByTag("p");
				if(elements != null && elements.size() > 0)
				{
					String email = elements.get(0).text().substring(8);
					etudiant.email = email.replace("[AT]", "@");
				}
			}
		}
		return etudiant;
	}
	
	/******************************************************
	 ********************** Async Task ********************
	 ******************************************************/
	private Pattern TablePattern = Pattern.compile("parent.MajDet\\('(.+)'\\);</");
	private Pattern TimePattern = Pattern.compile("\\d\\d:\\d\\d");
	
	private ClassEvent createClassInfo(HttpServiceErrorEnumReference errorEnumRef, String... params)
	{
		ClassEvent classInfo = new ClassEvent();
		classInfo.login = login;
		classInfo.NumEve = Long.parseLong(params[0]);
		String DatSrc = params[1];
		try 
		{
			HttpResponse response = executeHttpRequest(new HttpGet(SI_HOST+"/Eplug/Agenda/Eve-Det.asp?NumEve="+params[0]+"&DatSrc="+DatSrc+"&NomCal="+agendaWebParams.NomCal), errorEnumRef);
			if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
			{
				String rspHtml = EntityUtils.toString(response.getEntity());
				response.getEntity().consumeContent();
				//get table html
				Matcher matcher = TablePattern.matcher(rspHtml);
				if(matcher.find())
				{
					rspHtml = matcher.group(1);
				}
				else 
				{
					errorEnumRef.errorEnum = HttpServiceErrorEnum.HtmlContentError;
					errorEnumRef.errorEnum.setDescription("TablePattern : "+TablePattern.pattern()+" - can't matcher");
					return null;
				}
				//remove special char '\'
				rspHtml = rspHtml.replace("\\", "");
				
				Document doc = Jsoup.parse(rspHtml);
				Elements elements = doc.child(0).getElementsByTag("tr");
				if(elements != null && ! elements.isEmpty())
				{
					String className = elements.get(0).getElementsByTag("b").get(0).text();
					if(className.contains("Cours de Langue"))
						classInfo.name = className;
					else
						classInfo.name = className.split("-")[0].trim();
					
					for(int i = 1; i < elements.size(); i++)
					{
						Elements TDeles = elements.get(i).getElementsByTag("td");
						Elements tableEles = elements.get(i).getElementsByTag("table");
						
						if(tableEles != null && !tableEles.isEmpty())
						{
							classInfo.room = "";
							for(Element e : tableEles)
							{
								classInfo.room += (e.text().trim()+"__");
							}

							if(classInfo.room.equals(""))
							{
								classInfo.room = null;
							}
							else 
							{
								classInfo.room = classInfo.room.replace(getSpecailSpace(), "");
								classInfo.room = classInfo.room.substring(0, classInfo.room.length() - 2);
							}
						}
						else if(TDeles != null && TDeles.size() == 2)
						{
							String key = TDeles.get(0).text();
							Matcher m = TimePattern.matcher(key);
							if(key.toUpperCase(Locale.FRANCE).contains("TYPE"))
							{
								classInfo.type = getContent(TDeles.get(1)).trim();
								classInfo.type.replace(getSpecailSpace(), "");
								if(classInfo.type.toUpperCase(Locale.FRANCE).contains("EXAMEN"))
									classInfo.bgColor = Color.RED;
							}
							else if(m.find()) 
							{
								String time = m.group();
								classInfo.startTime = ClassEvent.dtf.parse(DatSrc + time);
								m = TimePattern.matcher(TDeles.get(1).text());
								if(m.find())
								{
									time = m.group();
									classInfo.endTime = ClassEvent.dtf.parse(DatSrc + time);
								}
							}
							else if(key.toUpperCase(Locale.FRANCE).contains("AUTEUR")) 
							{
								classInfo.auteur = getContent(TDeles.get(1));
							}
							else if(key.toUpperCase(Locale.FRANCE).contains("FORMATEUR")) 
							{
								classInfo.teacher = getContent(TDeles.get(1));
							}
							else if(key.toUpperCase(Locale.FRANCE).contains("APPRENANT")) 
							{
								classInfo.students = getContent(TDeles.get(1));
							}
							else if(key.toUpperCase(Locale.FRANCE).contains("PERSONNES")) 
							{
								classInfo.groupe = getContent(TDeles.get(1));
							}
						}
					}
					
					if(classInfo.room == null)
					{
						// PHY3001 - TP3 - Gpe G2 : salle A308 - Gpe G1 : salle A309
						Pattern pt = Pattern.compile("(salle|en|Amphi) ([^-]+)",Pattern.CASE_INSENSITIVE); //不区分大小写//可能存在关键字salle, en, Amphi
						Matcher match_salle = pt.matcher(className);
						classInfo.room = "";
						while(match_salle.find())
						{
							if(match_salle.group(1).toLowerCase(Locale.FRANCE).contains("amphi"))
								classInfo.room += ("Amphi "+match_salle.group(2).trim()+"__");
							else
								classInfo.room += (match_salle.group(2).trim()+"__");
						}
						
						if(!classInfo.room.isEmpty())
						{
							classInfo.room = classInfo.room.substring(0, classInfo.room.length() - 2);
							classInfo.room.replace(getSpecailSpace(), "");
						}
						else 
						{
							classInfo.room = null;
						}
					}
					Log.i(TAG, classInfo.toString());
				}
			}
		}
		catch (Exception e) 
		{
			errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
			errorEnumRef.errorEnum.setDescription(e.toString());
			classInfo = null;
			e.printStackTrace();
		}
		return classInfo;
	}
	
	private String getContent(Element element)
	{
		String content = "";
		Elements AEles = element.getElementsByTag("a");
		if(AEles != null && !AEles.isEmpty())
		{
			for(Element e : AEles)
			{
				content += (e.text()+"__");
			}
			content = content.substring(0, content.length() - 2);
		}
		else 
		{
			content = element.text();
		}
		return content;
	}
	
	/******************************************************
	 ********************Common function ******************
	 ******************************************************/
	private HttpResponse executeHttpRequest(HttpUriRequest request, HttpServiceErrorEnumReference errorEnumRef)
	{
		HttpResponse response = null;
		
		if(request != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
		{
			try 
			{
				response = client.execute(request, httpContext);
				
				int status = response.getStatusLine().getStatusCode();
				
				Log.d(TAG, "execute "+request.getMethod()+" with status:"+status+" URL:" + request.getURI());
				if(request.getMethod().equalsIgnoreCase("POST"))
				{
					Log.d(TAG, EntityUtils.toString(((HttpPost)request).getEntity()));
					
				}
				
				if(status == 302)
				{
					String newUrl = response.getFirstHeader("Location").getValue();
					if(newUrl.startsWith("/"))
						newUrl = request.getURI().getHost() + newUrl;
					if(!newUrl.startsWith("http"))
						newUrl = "http://" + newUrl;
					
					response.getEntity().consumeContent();
					response = executeHttpRequest(new HttpGet(newUrl), errorEnumRef);
				}
				else if (status == 200 && CAS_HOST.contains(request.getURI().getHost())) //auto connect to CAS
				{
					HttpUriRequest casRequest = createCASHandleRequest(response, errorEnumRef);
					if(casRequest != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
					{
						response.getEntity().consumeContent();
						response = executeHttpRequest(casRequest, errorEnumRef);
					}
				}
				else if(status != 302 && status != 200)
				{
					errorEnumRef.errorEnum = HttpServiceErrorEnum.HttpBadStatusError;
					errorEnumRef.errorEnum.setDescription("Http status : "+status+" - for URL : "+request.getURI());
				}
			} 
			catch (IOException e) 
			{
				errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
				errorEnumRef.errorEnum.setDescription(e.toString());
				e.printStackTrace();
				response = null;
			}
		}
		return response;
	}
	
	private HttpUriRequest createCASHandleRequest(HttpResponse handleResponse, HttpServiceErrorEnumReference errorEnumRef)
	{
		if(handleResponse == null || errorEnumRef.errorEnum != HttpServiceErrorEnum.OkError)
			return null;
		
		HttpUriRequest request = null;
		try 
		{
			String rspHtml = EntityUtils.toString(handleResponse.getEntity());
			Document doc = Jsoup.parse(rspHtml);
			Element element = doc.getElementById("content");
			if(element != null)
			{
				Element formElement = element.getElementById("fm1");
				if(formElement != null)
				{
					if(formElement.getElementById("status") != null)
					{
						errorEnumRef.errorEnum = HttpServiceErrorEnum.LoginFailedError;
						return null;
					}
					Log.i(TAG, "connect with login and password ...");
					String actionUrl = formElement.attr("action");
					if(actionUrl.startsWith("/"))
						actionUrl = CAS_HOST + actionUrl;
					
					List<NameValuePair> data = new ArrayList<NameValuePair>();
					Elements elements = doc.getElementsByTag("input");
					if(elements != null && elements.size() > 2)
					{
						for(int i = 2; i < elements.size(); i++)
						{
							Element ele = elements.get(i);
							data.add(new BasicNameValuePair(ele.attr("name"), ele.attr("value")));
						}
					}
					if(data.size() > 0)
					{
						data.add(0, new BasicNameValuePair("password", password));
						data.add(0, new BasicNameValuePair("username", login));
						
						HttpPost httpPost = new HttpPost(actionUrl);
						httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
						request = httpPost;
					}
				}
				else 
				{
					Log.i(TAG, "auto connect ...");
					Elements elements = element.getElementsByTag("a");
					if(elements != null && elements.size() == 1)
					{
						String href = elements.get(0).attr("href");
						request = new HttpGet(href);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
			errorEnumRef.errorEnum.setDescription(e.toString());
			e.printStackTrace();
		}
		return request;
	}
	
	private void reset()
	{
		//reset all params
		clearCookies();
		agendaWebParams.clear();
	}
	
	private void clearCookies()
	{
		CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		if(cookieStore != null)
			cookieStore.clear();
	}
	
	private String getSpecailSpace()
	{
		byte[] bytes = {-62, -96};
		return new String(bytes);
	}
	/******************************************************
	 ********************Public function ******************
	 ******************************************************/
	public static String getUserProfileFilePath(String login)
	{
		if(login != null)
			return PROFILE_CACHE_FLOLDER + "/" + login +".jpg";
		else
			return null;
	}
	
	public void prepareToQuit()
	{
		workerHandler.post(new Runnable() 
		{
			@Override
			public void run() 
			{
				IntHttpService.this.stopSelf();
			}
		});
	}
	
	private void setLoginPassword(String login, String password)
	{
		synchronized (loginPasswordLock) 
		{
			this.login = login;
			this.password = password;
		}
	}
	/******************************************************
	 **********************Inner Class*********************
	 ******************************************************/
	private class LoadClassTaskParams
	{
		private Date searchDate;
		private WeakReference<asyncGetClassInfoReponse> callbackWeakReference;
		
		public LoadClassTaskParams(Date searchDate, WeakReference<asyncGetClassInfoReponse> callbackWeakReference) 
		{
			this.searchDate = searchDate;
			this.callbackWeakReference = callbackWeakReference;
		}
	}
	
	private class AgendaWebParams
	{
		private String groupID;
		private String appId;
		private String lienID;
		
		private String ValGra;
		private String NomCal;
		
		public boolean isIdParamsReady()
		{
			return groupID != null && appId != null && lienID != null;
		}
		
		public boolean isFormItemsReady()
		{
			return ValGra != null && NomCal != null;
		}
		
		public void clear()
		{
			groupID = appId = lienID = null;
			ValGra = NomCal = null;
		}
	}
	/******************************************************
	 ********************HttpService Enum******************
	 ******************************************************/
	private class HttpServiceErrorEnumReference
	{
		private HttpServiceErrorEnum errorEnum;

		public HttpServiceErrorEnumReference(HttpServiceErrorEnum errorEnum) 
		{
			this.errorEnum = errorEnum;
		}
	}
	
	public static enum HttpServiceErrorEnum
	{
		OkError("OK, no error"),
		UnknowError("unknow error"),
		ExceptionError("a exception error"),
		ServerError("server error"),
		HttpBadStatusError("bad http status error"),
		HtmlContentError("bad html content error"),
		LoginFailedError("login or password error"),
		NoInternetError("no internet error");
		
		private String description;
		
		private HttpServiceErrorEnum(String description)
		{
			this.description = description;
		}
		
		public String getDescription() 
		{
			return description;
		}
		
		public void setDescription(String description) 
		{
			this.description = description;
		}
		
		@Override
		public String toString()
		{
			String retVal = description;
			if(retVal == null)
				retVal = super.toString();
			return retVal;
		}
	}
	
	/******************************************************
	 *********************Async Listener*******************
	 ******************************************************/
	
	public static interface asyncSearchEtudiantByNameReponse
	{
		public void onAsyncSearchEtudiantByNameReponse(HttpServiceErrorEnum errorEnum, List<Student> results);
	}
	
	public static interface asyncLoginReponse
	{
		public void onAsyncLoginReponse(String login, HttpServiceErrorEnum errorEnum);
	}
	
	public static interface asyncGetClassInfoReponse
	{
		public void onAsyncGetClassInfoReponse(HttpServiceErrorEnum errorEnum, Date searchDate, List<ClassEvent> results);
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		if(client != null)
			client.close();
		workerThread.quit();
	}
}
