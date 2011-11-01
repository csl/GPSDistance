package com.monitortracker;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import com.google.android.maps.GeoPoint;

import android.util.Log;
import android.widget.Toast;

//����Tracker�ǰeGPS�M�O�_�W�X���P�_���G
public class SocketServer implements Runnable
{
	private int port;
	private ServerSocket sc;
	private MyGoogleMap MonitorMap;
	
	public boolean ChildPhoneReady;

	public SocketServer(int port, MyGoogleMap mmap)throws IOException
	{
		this.port = port;
		this.sc = new ServerSocket(port);
		MonitorMap = mmap;
	}
	
	public void run()
	{
		Socket con = null;
		while(true)
		{
			try
			{
			  //Listen Port
				con = this.sc.accept();
				DataInputStream in = new DataInputStream(con.getInputStream());
				//�N���쪺���
				String str = in.readUTF();
				Log.v("vDEBUG: ", "vClient " + str);
				//��,���}��
		    StringTokenizer Tok = new StringTokenizer(str, ",");
		    double GPSData[] = new double[3];
		    int i=0;
		    while (Tok.hasMoreElements())
		    {
	        GPSData[i] = Double.valueOf((String) Tok.nextElement());
		      i++;
		    }      
				 
        MonitorMap.refreshDouble2Geo(GPSData[0], GPSData[1]);
		    
		    //�^�ǵ�Tracker, OK
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        
        out.writeUTF(Long.toString(System.currentTimeMillis()/1000));
        out.writeUTF("OK");
        out.flush();
        
				in.close();
				con.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
