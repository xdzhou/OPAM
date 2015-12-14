package com.sky.opam.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.loic.common.FailException;
import com.loic.common.LibApplication;
import com.sky.opam.R;
import com.sky.opam.model.ClassEvent;
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
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class IntHttpService extends Service 
{
    private static final String TAG = IntHttpService.class.getSimpleName();
    
    public static final String CoursLoadedBroadCaset = "IntHttpService_CoursLoadedBroadCaset";
    public static final String CoursLoaded_Date_Info = "IntHttpService_CoursLoaded_Date_Info";
    public static final String CoursLoaded_Error_Enum_Index_Info = "IntHttpService_CoursLoaded_Error_Enum_Index_Info";
    public static final String CoursLoaded_Cours_Size_Info = "IntHttpService_CoursLoaded_Cours_Size_Info";
    
    private static final String CAS_HOST = "https://cas.tem-tsp.eu/";  //CAS : host 157.159.10.172
    private static final String SI_HOST = "http://si-etudiants.tem-tsp.eu/";  //SI_Student : host 157.159.10.180
    public static final String ENCRPT_KEY = "opam";
    public static final String PROFILE_CACHE_FLOLDER;

    static
    {
        PROFILE_CACHE_FLOLDER = LibApplication.getContext().getFilesDir().getAbsolutePath();
    }
    
    private final IBinder mBinder = new LocalBinder();
    private HttpContext httpContext;
    private AndroidHttpClient client;
    
    private DateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private String login;    // "he_huilo";
    private String password; // "pancakes";
    private AgendaWebParams agendaWebParams;
    
    private final Object loginPasswordLock = new Object();
    
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
            final InputStream in = LibApplication.getContext().getResources().openRawResource(R.raw.mystore);
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
    
    /**
     * login to INT server
     * 
     * @param login user name
     * @param password user's password
     */
    public Observable<User> requestLogin(final String login, final String password)
    {
        return Observable.create(new Observable.OnSubscribe<Pair<String, String>>()
        {
            @Override
            public void call(Subscriber<? super Pair<String, String>> subscriber)
            {
                subscriber.onStart();
                if(TextUtils.isEmpty(login) || TextUtils.isEmpty(password))
                {
                    subscriber.onError(new IllegalArgumentException("login or password invalid"));
                }
                else
                {
                    subscriber.onNext(Pair.create(login, password));
                    subscriber.onCompleted();
                }
            }
        }).observeOn(Schedulers.io())
        .flatMap(new Func1<Pair<String, String>, Observable<User>>()
        {
            @Override
            public Observable<User> call(Pair<String, String> pair)
            {
                User user = null;
                requestLogout(true);
                setLoginPassword(pair.first, pair.second);
                HttpServiceErrorEnumReference errorEnumRef = new HttpServiceErrorEnumReference(HttpServiceErrorEnum.OkError);
                HttpResponse response = executeHttpRequest(new HttpGet(SI_HOST), errorEnumRef);

                if (response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
                {
                    response = executeHttpRequest(new HttpGet(SI_HOST + "/OpDotNet/Noyau/Bandeau.aspx?"), errorEnumRef);
                }

                if (response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
                {
                    String userName = getUserName(errorEnumRef, response);
                    Log.d(TAG, "userName : " + userName);
                    user = new User(pair.first, Chiffrement.encrypt(password, ENCRPT_KEY), userName);

                    if (userName == null)
                    {
                        Log.e(TAG, "Can't find user name, error : " + errorEnumRef.errorEnum.getDescription());
                    }

                    //clear errorEnum, even we have't got user name
                    errorEnumRef.errorEnum = HttpServiceErrorEnum.OkError;
                    loadProfileImg(pair.first);
                }

                if (errorEnumRef.errorEnum != HttpServiceErrorEnum.OkError)
                {
                    //request login failed, clear user login info
                    setLoginPassword(null, null);
                }
                requestLogout(false);

                if (user == null)
                {
                    return Observable.error(new FailException(errorEnumRef.errorEnum.getDescription()));
                }
                else
                {
                    return Observable.just(user);
                }
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }
    
    private void requestLogout(Boolean clearLogin)
    {
        boolean isLogin;
        synchronized (loginPasswordLock)
        {
            isLogin = (login != null) && (password != null);
        }
        
        if(isLogin)
        {
            executeHttpRequest(new HttpGet("https://ecampus.tem-tsp.eu/uPortal/Logout"), new HttpServiceErrorEnumReference());
        }
        reset();
        
        if(clearLogin)
        {
            setLoginPassword(null, null);
        }
    }
    
    private String getUserName(HttpServiceErrorEnumReference errorEnumRef, @NonNull HttpResponse response)
    {
        String userName = null;
        try
        {
            String rspHtml = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Document doc = Jsoup.parse(rspHtml);
            Element element = doc.getElementById("Menu");
            if(element != null)
            {
                Elements spanEles = element.getElementsByTag("span");
                if(spanEles != null)
                {
                    for(Element ele : spanEles)
                    {
                        if(ele.text().contains("Bonjour"))
                        {
                            userName = ele.text().replace("Bonjour", "");
                            userName = userName.replace(getSpecialSpace(), "");
                            break;
                        }
                    }
                }

                if(userName == null)
                {
                    Log.e(TAG, "Can't find userName, html : "+element.text());
                }
            }
        }
        catch (IOException e)
        {
            errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
            errorEnumRef.errorEnum.setDescription(e.getMessage());
        }
        
        if(userName == null)
        {
            errorEnumRef.errorEnum = HttpServiceErrorEnum.HtmlContentError;
            errorEnumRef.errorEnum.setDescription("Can't find user name ...");
        }
        return userName;
    }
    
    private void loadProfileImg(String login)
    {
        try 
        {
            String imgPath = getUserProfileFilePath(login);
            File document = new File(imgPath);
            if(!document.exists())
            {
                InputStream in = new java.net.URL("http://trombi.it-sudparis.eu/photo.php?uid=" + login + "&h=80&w=80").openStream();
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
    
    /**
     * get the list of class information for a month
     * 
     * @param year the date selected
     * @param month the date selected
     */
    public Observable<List<ClassEvent>> requestClassInfos(final int year, final int month)
    {
        return Observable.create(new Observable.OnSubscribe<Date>()
        {
            @Override
            public void call(Subscriber<? super Date> subscriber)
            {
                subscriber.onStart();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                subscriber.onNext(calendar.getTime());
                subscriber.onCompleted();
            }
        })
        .observeOn(Schedulers.io())
        .flatMap(new Func1<Date, Observable<String>>()
        {
            @Override
            public Observable<String> call(Date date)
            {
                HttpResponse response = null;
                HttpServiceErrorEnumReference errorEnumRef = new HttpServiceErrorEnumReference();
                try
                {
                    if(! agendaWebParams.isFormItemsReady())
                    {
                        //request SI main page html and auto connect
                        response = executeHttpRequest(new HttpGet(SI_HOST), errorEnumRef);
                        if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
                        {
                            response.getEntity().consumeContent();
                            //request SI bandeau page html
                            response = executeHttpRequest(new HttpGet(SI_HOST + "/OpDotNet/Noyau/Bandeau.aspx?"), errorEnumRef);
                        }
                        if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
                        {
                            agendaWebParams.groupID = getGroupID(EntityUtils.toString(response.getEntity()));
                            response.getEntity().consumeContent();
                            if(agendaWebParams.groupID != null)
                            {
                                //request SI left menu page html
                                response = executeHttpRequest(new HttpGet(SI_HOST + "/OpDotNet/Noyau/Rubriques.aspx?groupe=" + agendaWebParams.groupID), errorEnumRef);
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
                                response = executeHttpRequest(new HttpGet(SI_HOST + "OpDotnet/commun/Login/aspxtoasp.aspx?url=/Eplug/Agenda/Agenda.asp?IdApplication=" + agendaWebParams.appId + "&TypeAcces=Utilisateur&IdLien=" + agendaWebParams.lienID + "&groupe=" + agendaWebParams.groupID), errorEnumRef);
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
                            response = executeHttpRequest(request, errorEnumRef);
                        }
                    }

                    HttpPost request = createGetClassInfoRequest(response, date, errorEnumRef);
                    if(response != null)
                    {
                        response.getEntity().consumeContent();
                    }
                    //request SI get class info
                    if(request != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
                    {
                        response = executeHttpRequest(request, errorEnumRef);
                    }

                    if(response != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
                    {
                        String rspHtml = EntityUtils.toString(response.getEntity());
                        response.getEntity().consumeContent();
                        return Observable.just(rspHtml);
                    }
                    else
                    {
                        return Observable.error(new FailException(errorEnumRef.errorEnum.getDescription()));
                    }
                }
                catch (IOException e)
                {
                    return Observable.error(new FailException("IOException : "+e.getMessage()));
                }
            }
        })
        .flatMap(new Func1<String, Observable<List<ClassEvent>>>()
        {
            @Override
            public Observable<List<ClassEvent>> call(String rspHtml)
            {
                Pattern pattern = Pattern.compile("onmouseover=\"DetEve\\(\'([0-9]+)\',\'([^']+)\',\'([0-9]+)\'\\)");
                Matcher matcher = pattern.matcher(rspHtml);
                List<ClassEvent> classInfos = new ArrayList<ClassEvent>();

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

                //try to broadcast cet info
                Intent i = new Intent(CoursLoadedBroadCaset);
                i.putExtra(CoursLoaded_Date_Info, date.getTime());
                i.putExtra(CoursLoaded_Cours_Size_Info, classInfos == null ? 0 : classInfos.size());
                i.putExtra(CoursLoaded_Error_Enum_Index_Info, errorEnumRef.errorEnum.ordinal());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);

                return Observable.just(classInfos);
            }
        });
    }
    
    private String getGroupID(String html)
    {
        Pattern pattern = Pattern.compile("IdGroupe = (\\d+)");
        Matcher matcher = pattern.matcher(html);
        
        return matcher.find() ? matcher.group(1) : null;
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
        if(searchDate != null && errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
        {
            httpPost = new HttpPost(SI_HOST + "/Eplug/Agenda/Agenda.asp");
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            try
            {
                if(! agendaWebParams.isFormItemsReady() && previousResponse != null)
                {
                    String rspHtml = EntityUtils.toString(previousResponse.getEntity());
                    Document doc = Jsoup.parse(rspHtml);
                    Element divAllElement = doc.getElementById("DivAll");
                    
                    if(divAllElement != null)
                    {
                        Elements elements = divAllElement.getElementsByTag("table");
                        if(elements != null)
                        {
                            elements.remove();
                        }
                        elements = divAllElement.getElementsByTag("input");
                        for(Element ele : elements)
                        {
                            String name = ele.attr("name");
                            String value = ele.attr("value");
                            
                            value = (value == null) ? "" : value;
                            value = name.equals("NumDat") ? df.format(searchDate) : value;
                            
                            if(name.equals("ValGra"))
                            {
                                agendaWebParams.ValGra = value;
                            }
                            else if (name.equals("NomCal"))
                            {
                                agendaWebParams.NomCal = value;
                            }
                            
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
    
    /**
     * search students by name
     * 
     * @param name the student's name to search (first name or last name)
     * @param school TSP or TEM
     * @param grade first / second ... year
     * 
     */
    public Observable<List<Student>> requestSearchStudents(@NonNull final String name, @Nullable final String school, @Nullable final String grade)
    {
        return Observable.create(new Observable.OnSubscribe<String>()
        {
            @Override
            public void call(Subscriber<? super String> subscriber)
            {
                subscriber.onStart();

                Log.v(TAG, "onStart : "+ (Looper.getMainLooper() == Looper.myLooper()));

                if (TextUtils.isEmpty(name))
                {
                    subscriber.onError(new IllegalArgumentException("Name can't be null"));
                }
                else
                {
                    subscriber.onNext(name);
                    subscriber.onNext(school);
                    subscriber.onNext(grade);
                    subscriber.onCompleted();
                }
            }
        }).toList().observeOn(Schedulers.io())
        .flatMap(new Func1<List<String>, Observable<String>>()
        {
            @Override
            public Observable<String> call(List<String> list)
            {
                Log.v(TAG, "generate Html : "+ (Looper.getMainLooper() == Looper.myLooper()));
                HttpPost post = new HttpPost("http://trombi.tem-tsp.eu/etudiants.php");
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user", list.get(0)));
                if (!TextUtils.isEmpty(list.get(1)))
                {
                    params.add(new BasicNameValuePair("ecole", list.get(1)));
                }
                if (!TextUtils.isEmpty(list.get(2)))
                {
                    params.add(new BasicNameValuePair("annee", list.get(2)));
                }
                params.add(new BasicNameValuePair("submit", "Rechercher"));

                try
                {
                    post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    HttpResponse response = client.execute(post);
                    int status = response.getStatusLine().getStatusCode();
                    if (status == 200)
                    {
                        String rspHtml = EntityUtils.toString(response.getEntity());
                        return Observable.just(rspHtml);
                    } else
                    {
                        return Observable.error(new FailException("Http bad status : " + status));
                    }
                } catch (IOException e)
                {
                    return Observable.error(new FailException("IOException : " + e.getMessage()));
                }
            }
        })
        .flatMap(new Func1<String, Observable<List<Student>>>()
        {
            @Override
            public Observable<List<Student>> call(String rspHtml)
            {
                Log.v(TAG, "generate students : " + (Looper.getMainLooper() == Looper.myLooper()));
                List<Student> students = new ArrayList<Student>();

                Document doc = Jsoup.parse(rspHtml);
                Element etudiantsEle = doc.getElementById("etudiants");
                Elements temElements = etudiantsEle.getElementsByClass("INTM");
                if (temElements != null && !temElements.isEmpty())
                {
                    for (Element ele : temElements)
                    {
                        Student s = createStudent(ele, SchoolEnum.TEM);
                        if (s != null)
                        {
                            students.add(s);
                        }
                    }
                }
                Elements tspElements = etudiantsEle.getElementsByClass("TINT");
                if (tspElements != null && !tspElements.isEmpty())
                {
                    for (Element ele : tspElements)
                    {
                        Student s = createStudent(ele, SchoolEnum.TSP);
                        if (s != null)
                        {
                            students.add(s);
                        }
                    }
                }

                return Observable.just(students);
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }
    
    public Student createStudent(Element ele, SchoolEnum schoolEnum)
    {
        Student student = null;
        if(ele != null)
        {
            student = new Student(schoolEnum);
            Elements elements = ele.getElementsByTag("img");
            if(elements != null && elements.size() > 0)
            {
                String login = elements.get(0).attr("src");
                if(login != null && login.length() > 0)
                {
                    student.login = login.substring(15, login.length() - 10);
                }
            }
            elements = ele.getElementsByClass("ldapNom");
            if(elements != null && elements.size() > 0)
            {
                student.name = elements.get(0).text();
            }
            
            elements = ele.getElementsByClass("ldapInfo");
            Element infoEle = null;
            if(elements != null && elements.size() > 0)
            {
                infoEle = elements.get(0);
            }
            
            if(infoEle != null)
            {
                elements = infoEle.getElementsByTag("li");
                if(elements != null && elements.size() > 0)
                {
                    student.grade = elements.get(0).text();
                }
                elements = infoEle.getElementsByTag("p");
                if(elements != null && elements.size() > 0)
                {
                    String email = elements.get(0).text().substring(8);
                    student.email = email.replace("[AT]", "@");
                }
            }
        }
        return student;
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
                    {
                        classInfo.name = className;
                    }
                    else
                    {
                        classInfo.name = className.split("-")[0].trim();
                    }
                    
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
                                classInfo.room = classInfo.room.replace(getSpecialSpace(), "");
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
                                classInfo.type.replace(getSpecialSpace(), "");
                                if(classInfo.type.toUpperCase(Locale.FRANCE).contains("EXAMEN"))
                                {
                                    classInfo.bgColor = Color.RED;
                                }
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
                            {
                                classInfo.room += ("Amphi "+match_salle.group(2).trim()+"__");
                            }
                            else
                            {
                                classInfo.room += (match_salle.group(2).trim()+"__");
                            }
                        }
                        
                        if(!classInfo.room.isEmpty())
                        {
                            classInfo.room = classInfo.room.substring(0, classInfo.room.length() - 2);
                            classInfo.room.replace(getSpecialSpace(), "");
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
    private HttpResponse executeHttpRequest(@NonNull HttpUriRequest request, HttpServiceErrorEnumReference errorEnumRef)
    {
        HttpResponse response = null;
        
        if(errorEnumRef.errorEnum == HttpServiceErrorEnum.OkError)
        {
            try
            {
                response = client.execute(request, httpContext);

                int status = response.getStatusLine().getStatusCode();

                Log.d(TAG, "execute " + request.getMethod() + " with status:" + status + " URL:" + request.getURI());
                if(request.getMethod().equalsIgnoreCase("POST"))
                {
                    Log.d(TAG, EntityUtils.toString(((HttpPost)request).getEntity()));
                }

                if(status == 302)
                {
                    String newUrl = response.getFirstHeader("Location").getValue();
                    if(newUrl.startsWith("/"))
                    {
                        newUrl = request.getURI().getHost() + newUrl;
                    }
                    if(!newUrl.startsWith("http"))
                    {
                        newUrl = "http://" + newUrl;
                    }

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
                else if(status != 200)
                {
                    errorEnumRef.errorEnum = HttpServiceErrorEnum.HttpBadStatusError;
                    errorEnumRef.errorEnum.setDescription("Http status : "+status+" - for URL : "+request.getURI());
                }
            }
            catch (IOException e)
            {
                errorEnumRef.errorEnum = HttpServiceErrorEnum.ExceptionError;
                errorEnumRef.errorEnum.setDescription(e.getMessage());
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
                    {
                        actionUrl = CAS_HOST + actionUrl;
                    }
                    
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
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        if(cookieStore != null)
        {
            cookieStore.clear();
        }
        agendaWebParams.clear();
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
     ********************Public function ******************
     ******************************************************/
    public static String getUserProfileFilePath(String login)
    {
        if(login != null)
        {
            return PROFILE_CACHE_FLOLDER + "/" + login +".jpg";
        }
        else
        {
            return null;
        }
    }

    public static String getSpecialSpace()
    {
        byte[] bytes = {-62, -96};
        return new String(bytes);
    }

    public boolean isConnected()
    {
        boolean isLogin;
        synchronized (loginPasswordLock)
        {
            isLogin = login != null && password != null;
        }
        return isLogin;
    }

    public @Nullable String getLogin()
    {
        return login;
    }
    
    /******************************************************
     **********************Inner Class*********************
     ******************************************************/
    private static class AgendaWebParams
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
    private static class HttpServiceErrorEnumReference
    {
        private HttpServiceErrorEnum errorEnum;

        public HttpServiceErrorEnumReference()
        {
            this(HttpServiceErrorEnum.OkError);
        }

        public HttpServiceErrorEnumReference(HttpServiceErrorEnum errorEnum) 
        {
            this.errorEnum = errorEnum;
        }
    }
    
    public enum HttpServiceErrorEnum
    {
        OkError(R.string.OA2009),
        UnknowError(R.string.OA2010),
        ExceptionError(R.string.OA2011),
        ServerError(R.string.OA2012),
        HttpBadStatusError(R.string.OA2013),
        HtmlContentError(R.string.OA2014),
        LoginFailedError(R.string.OA2015),
        NoInternetError(R.string.OA0004);
        
        private int descriptionResID;
        private String customedDescription;
        
        private HttpServiceErrorEnum(int descriptionResID)
        {
            this.descriptionResID = descriptionResID;
        }
        
        public String getDescription() 
        {
            if(customedDescription != null)
            {
                return customedDescription;
            }
            else
            {
                return LibApplication.getContext().getString(descriptionResID);
            }
        }
        
        public void setDescription(String description) 
        {
            this.customedDescription = description;
        }
    }
    
    @Override
    public void onDestroy() 
    {
        super.onDestroy();

        if(client != null)
        {
            client.close();
        }
    }
}
