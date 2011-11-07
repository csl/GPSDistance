package com.monitortracker;

//import java.util.ArrayList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context; 
import android.content.DialogInterface;
//import android.graphics.drawable.Drawable;
import android.location.Criteria; 
import android.location.Location; 
import android.location.LocationListener; 
import android.location.LocationManager; 
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle; 
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
//import android.util.Log;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.TextView;
//import android.widget.Toast;

import com.google.android.maps.GeoPoint; 
//import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapController; 
import com.google.android.maps.MapView; 
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;

public class MyGoogleMap extends MapActivity 
{
  private String TAG = "MyGoogleMap";
  
  private static final int MSG_DIALOG_TIMEOUT = 1;  
  
  private static final int MENU_EXIT = Menu.FIRST;

  //private TextView mTextView01;
  static public MyGoogleMap my;
  private MyGoogleMap mMyGoogleMap = this;
  private Timer timer;
  private SocketServer s_socket = null;
  
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private MyOverLay overlay;
  
  private Button mButton02,mButton03,mButton04;
  private int intZoomLevel=0;//geoLatitude,geoLongitude; 

  public GeoPoint nowGeoPoint;
  public GeoPoint otherGeoPoint;
  
  public String IPAddress;
  private SendDataSocket sData;
  private SendDataSocket rData;
  
  private int serve_port = 12341;
  
  private String strLocationProvider = ""; 
  private LocationManager mLocationManager01; 
  private Location mLocation01; 
  
  public int port;
  public TextView label;
  public String oldGPSRangeData;
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 

    //Checking Status
    if (CheckInternet(3))   //判別是否有網路和GPS
    {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(provider == null)
        {
         openOptionsDialog("NO GPS");
         return;
        }
    }
    else
    {
      openOptionsDialog("NO Internet");
      return;
    }   
   
    //抓取現在IP
    String ip = getLocalIpAddress();

    timer = new Timer();
    otherGeoPoint = null;
    my = this;
    
    //googleMAP
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 

    //訊息顯示
    label = (TextView) findViewById(R.id.cstaus);
    
    //參數設定 
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    intZoomLevel = 18; 
    mMapController01.setZoom(intZoomLevel); 
    
    //取得gps services
    mLocationManager01 = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
     
    getLocationProvider();     
    nowGeoPoint = getGeoByLocation(mLocation01); 

    IPAddress ="192.168.0.50";
    port = 12341;
    
    final EditText input = new EditText(mMyGoogleMap);
    input.setText(IPAddress);
      AlertDialog.Builder alert = new AlertDialog.Builder(mMyGoogleMap);

    //openOptionsDialog(getLocalIpAddress());

      alert.setTitle("現在的IP" + ip);
      alert.setMessage("請輸入另一隻 Phone IP位置");
        
      // Set an EditText view to get user input 
      alert.setView(input);
      
      alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) 
      {
        try
        {
          IPAddress = input.getText().toString();  
          //timer.schedule(new DateTask(), 0, 60000);    
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      });
    
      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // Canceled.
          }
        });
    
      alert.show();      
        
     //命令gps, 只要有資料更新call mLocationListener01的function
    mLocationManager01.requestLocationUpdates(strLocationProvider, 2000, 10, mLocationListener01); 
     
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());    

    //建構畫在GoogleMap的overlay
    overlay = new MyOverLay(this);
    mMapView.getOverlays().add(overlay);
    
    //nowGeoPoint = new GeoPoint((int) (24.070801 * 1000000),(int) (120.715486 * 1000000));
    if (nowGeoPoint != null)
      refreshMapViewByGeoPoint(nowGeoPoint, mMapView, intZoomLevel);

    //放大地圖
    mButton02 = (Button)findViewById(R.id.myButton2); 
    mButton02.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub 
        intZoomLevel++; 
        if(intZoomLevel>mMapView.getMaxZoomLevel()) 
        { 
          intZoomLevel = mMapView.getMaxZoomLevel(); 
        } 
        mMapController01.setZoom(intZoomLevel); 
      } 
    }); 
     
    //縮小地圖
    mButton03 = (Button)findViewById(R.id.myButton3); 
    mButton03.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub 
        intZoomLevel--; 
        if(intZoomLevel<1) 
        { 
          intZoomLevel = 1; 
        } 
        mMapController01.setZoom(intZoomLevel); 
      } 
    });

    //Satellite或街道
    mButton04 = (Button)findViewById(R.id.myButton4); 
    mButton04.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub
       String str = mButton04.getText().toString();
        
       if (str.equals("衛星"))
       {
        mButton04.setText("街道");
        mMapView.setStreetView(false);
        mMapView.setSatellite(true);
        mMapView.setTraffic(false);
       }
       else
       {
         mButton04.setText("衛星");
         mMapView.setStreetView(true);
         mMapView.setSatellite(false);
         mMapView.setTraffic(false);
       }
      } 
    }); 

    //Log.v("IPADDRESS", getLocalIpAddress());
    
    //Open Server Socket, for trakcer傳來的資料
    try {
        s_socket = new SocketServer(serve_port, this);
        Thread socket_thread = new Thread(s_socket);
        socket_thread.start();
    } 
    catch (IOException e) {
        e.printStackTrace();
    }
    catch (Exception e) {
        e.printStackTrace();
    }
  }
  
  public final LocationListener mLocationListener01 =  new LocationListener() 
  { 
    public void onLocationChanged(Location location) 
    { 
      // TODO Auto-generated method stub 
      mLocation01 = location; 
      
       //我這手機經緯度拿取
      nowGeoPoint = getGeoByLocation(location); 
      double Latitude = nowGeoPoint.getLatitudeE6()/ 1E6;
      double Longitude = nowGeoPoint.getLongitudeE6()/ 1E6;
      
      //現在的經緯度送出去給另一隻手機
      SendGPSData(Latitude + "," + Longitude);
      
      //refresh distance
      if (nowGeoPoint != null || otherGeoPoint != null)
      {
        //計算距離
        double gpsdis = GetDistance(nowGeoPoint, otherGeoPoint);
        label.setText("現在點距離：" + gpsdis);
      }
      else if (otherGeoPoint == null)
      {
        label.setText("尚未收到對方手機GPS訊號");        
      }
      else if (nowGeoPoint == null)
      {
        label.setText("尚未設置GPS訊號");        
      }
    }

    @Override
    public void onProviderDisabled(String arg0)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onProviderEnabled(String arg0)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
      // TODO Auto-generated method stub
      
    }
  };
  
  
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_EXIT, 1 ,R.string.menu_exit)
    .setAlphabeticShortcut('E');
  
     return true;  
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    { 
          case MENU_EXIT:
            timer.cancel();
            android.os.Process.killProcess(android.os.Process.myPid());           
            MyGoogleMap.this.finish();
            break ;
    }
    
  return true ;
  }
  
  public void getLocationProvider() 
  { 
    try 
    { 
      Criteria mCriteria01 = new Criteria(); 
      mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
      mCriteria01.setAltitudeRequired(false); 
      mCriteria01.setBearingRequired(false); 
      mCriteria01.setCostAllowed(true); 
      mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
      strLocationProvider = mLocationManager01.getBestProvider(mCriteria01, true); 
       
      mLocation01 = mLocationManager01.getLastKnownLocation (strLocationProvider); //?
    } 
    catch(Exception e) 
    { 
      //mTextView01.setText(e.toString()); 
      e.printStackTrace(); 
    } 
  }
  
  private GeoPoint getGeoByLocation(Location location) 
  { 
    GeoPoint gp = null; 
    try 
    { 
      if (location != null) 
      { 
        double geoLatitude = location.getLatitude()*1E6; 
        double geoLongitude = location.getLongitude()*1E6; 
        gp = new GeoPoint((int) geoLatitude, (int) geoLongitude); 
      } 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
    return gp; 
  } 
  
  public static void refreshMapViewByGeoPoint(GeoPoint gp, MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(gp); 
      myMC.setZoom(zoomLevel); 
      //mapview.setSatellite(false);
      
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  }
  
  //傳送GPS座標出去
  public void SendGPSData(String GPSData)
  {
    sData = new SendDataSocket(this);
    sData.SetAddressPort(IPAddress , port);
    sData.SetSendData(GPSData);
    sData.SetFunction(1); 
    sData.start();
  }
  
  public int refreshDouble2Geo(double lat, double longa)
  {
    GeoPoint gp = new GeoPoint((int)(lat * 1e6),
        (int)(longa * 1e6));
    
    otherGeoPoint = gp;
    
    return 1;
  }
  
  //check Internet alive or not
  private boolean CheckInternet(int retry)
  {
    boolean has = false;
    for (int i=0; i<=retry; i++)
    {
      has = HaveInternet();
      if (has == true) break;       
    }
    
  return has;
  }
  
  private boolean HaveInternet()
  {
     boolean result = false;
     
     ConnectivityManager connManager = (ConnectivityManager) 
                                getSystemService(Context.CONNECTIVITY_SERVICE); 
      
     NetworkInfo info = connManager.getActiveNetworkInfo();
     
     if (info == null || !info.isConnected())
     {
       result = false;
     }
     else 
     {
       if (!info.isAvailable())
       {
         result =false;
       }
       else
       {
         result = true;
       }
   }
  
   return result;
  }
  
  
  private double ConvertDegreeToRadians(double degrees)
  {
    return (Math.PI/180)*degrees;
  }
  
  public double GetDistance(GeoPoint gp1, GeoPoint gp2)
  {
    double Lat1r = ConvertDegreeToRadians(gp1.getLatitudeE6()/1E6);
    double Lat2r = ConvertDegreeToRadians(gp2.getLatitudeE6()/1E6);
    double Long1r= ConvertDegreeToRadians(gp1.getLongitudeE6()/1E6);
    double Long2r= ConvertDegreeToRadians(gp2.getLongitudeE6()/1E6);

    double R = 6371;
    double d = Math.acos(Math.sin(Lat1r)*Math.sin(Lat2r)+
               Math.cos(Lat1r)*Math.cos(Lat2r)*
               Math.cos(Long2r-Long1r))*R;
    return d*1000;
  }
    
   
  public void getLocationProvider1() 
  { 
    try 
    { 
      Criteria mCriteria01 = new Criteria(); 
      mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
      mCriteria01.setAltitudeRequired(false); 
      mCriteria01.setBearingRequired(false); 
      mCriteria01.setCostAllowed(true); 
      mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
      //strLocationProvider = mLocationManager01.getBestProvider(mCriteria01, true); 
       
      //mLocation01 = mLocationManager01.getLastKnownLocation (strLocationProvider); //?
    } 
    catch(Exception e) 
    { 
      //mTextView01.setText(e.toString()); 
      e.printStackTrace(); 
    } 
  }
  
  @Override 
  protected boolean isRouteDisplayed() 
  { 
    // TODO Auto-generated method stub 
    return false; 
  } 
  
  //抓取手機的IP
  public String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
      {
          NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
            {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress().toString();
                }
            }
      }
    }
    catch (SocketException ex) {
        Log.e("", ex.toString());
    }

    return null;
  }
  
  void GPSRhander(String gpsdata)
  {
  }
  
  public int timeouthandler()
  {
    Message msg = new Message();
    msg.what = MSG_DIALOG_TIMEOUT;
    myHandler.sendMessage(msg);
    return 1;
  }
  
  //處理HANDER: refreshDouble2Geo會傳送Message出來，決定要顯示什麼
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_DIALOG_TIMEOUT:
            label.setText("送出gps timeout");
          default:
                label.setText(Integer.toString(msg.what));
        }
        super.handleMessage(msg);
    }
  };
  
  public class DateTask extends TimerTask 
  {
    public void run() 
    {
      int port = 12341;
      rData = new SendDataSocket(my);
      rData.SetAddressPort(IPAddress , port);
      rData.SetFunction(2); 
      rData.start();
    }
  }
  
  //show message
  public void openOptionsDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
           finish();
         }
         }
        )
    .show();
  }
  }

  




