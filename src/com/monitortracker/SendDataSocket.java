package com.monitortracker;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.util.Log;
 
public class SendDataSocket extends Thread 
{

  private String TAG = "SendDataSocket";
	private String address;
	private int port;
	private int function;
	private int IsOK;
	private MyGoogleMap MonitorMap;
	public String error_string;
  public String send_Data;
	String line;
	
	public List<String> send_s; 
	
	public SendDataSocket(MyGoogleMap map) 
  {
		IsOK = 0;
		MonitorMap = map;
  }

	//設定IPAddress和Port
	public void SetAddressPort(String addr, int p)
	{		
		this.address = addr;
		this.port = p;
	}
	
  public void SetSendData(String sdata)
  {   
    this.send_Data = sdata;
  }	
	
	public void SetFunction(int func)
	{
		function = func;		
	}

	public int getIsOK()
	{
		return IsOK;
	}
	
	@Override
	public void run() 
	{
	  int timeout=0;
	  
	  do {
        //傳送
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(address, port);

        try {
            client.connect(isa, 20021);
            
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            if (function  == 1)
            {
              //傳送字串座標
             	out.writeUTF(send_Data);

            	// As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
               
              //是否傳送成功
              while (true)
              {
                line = is.readUTF();
                if (line.equals("OK")) 
                {
                  Log.v("vDEBUG: ", "SetGPSRange OK!!");
                  IsOK = 2;
                	break;
                }
              }
              is.close();
             }
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
        
        timeout++;
        if (timeout > 10)
        {
          MonitorMap.timeouthandler();
          Log.i("...", "timeout");
          break;
        }
        
	  } while (IsOK != 2);
	  
	}
}