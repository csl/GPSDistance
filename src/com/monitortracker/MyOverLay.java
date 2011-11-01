package com.monitortracker;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Environment;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class MyOverLay  extends Overlay {

	/**
	 * Stored as global instances as one time initialization is enough
	 */
    private Bitmap mBubbleIcon, mShadowIcon;
    
    private Bitmap mNowIcon;
    
    private MyGoogleMap mLocationViewers;
    
    private Paint	mInnerPaint, mBorderPaint, mTextPaint;
    
    private List<GeoPoint> gp;
	
    private int infoWindowOffsetX;
    private int infoWindowOffsetY;
    
    private boolean showWinInfo;
    
    private boolean ReadyShowRange;

    private boolean ShowTracker;    
    
    private ArrayList<GeoPoint> tracker; 

    private int mRadius=6;
    
	/**
	 * It is used to track the visibility of information window and clicked location is known location or not 
	 * of the currently selected Map Location
	 */
    
  //建構子, 初始化
	public MyOverLay(MyGoogleMap mLocationViewers) {
		
		this.mLocationViewers = mLocationViewers;
		
		mBubbleIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.bubble);
		mShadowIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.shadow);
		mNowIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.mappin_blue);
		showWinInfo = false;
		
		gp = new ArrayList<GeoPoint>();
		tracker = new ArrayList<GeoPoint>();
		ReadyShowRange = false;
		ShowTracker = false;
	}
	
	public void setTracker(boolean st)
	{
	  ShowTracker = st;
	}

  public boolean addGeoPoint(GeoPoint p)
  {
      if (p != null)
      {
        tracker.add(p);
        return true;
      }     
      
      return false;
  }

  public GeoPoint getGeoPoint(int index)
  {
      return tracker.get(index);
  }

	@Override
  //處理draw map上的圖案
	public boolean onTap(GeoPoint p, MapView mapView)  {
		
		return true;
	}
	
  @Override
  //draw method  
	public void draw(Canvas canvas, MapView	mapView, boolean shadow) 
  {
      //畫現在位置
      drawNowGeoMap(canvas, mapView, shadow);
      //畫地圖座標
      drawOtherGeoMap(canvas, mapView, shadow);
  }
    
  //清除GPS Range座標
  public void clearRange()
  {
    ReadyShowRange = false;
    gp.clear();
  }

  /**
     * Test whether an information balloon should be displayed or a prior balloon hidden.
     */
    private void drawNowGeoMap(Canvas canvas, MapView mapView, boolean shadow) 
    {
      //顯示現在位置
      if (mLocationViewers.nowGeoPoint != null)
      {
        Paint paint = new Paint();
        Point myScreenCoords = new Point();
        
        //顯示現在位置  
        mapView.getProjection().toPixels(mLocationViewers.nowGeoPoint, myScreenCoords);
        paint.setStrokeWidth(1);
        paint.setARGB(255, 255, 0, 0);
        paint.setStyle(Paint.Style.STROKE);
  
        canvas.drawBitmap(mNowIcon, myScreenCoords.x, myScreenCoords.y, paint);
        canvas.drawText("現在位置", myScreenCoords.x, myScreenCoords.y, paint);
      }
    }
    
    private void drawOtherGeoMap(Canvas canvas, MapView mapView, boolean shadow) 
    {
      //顯示現在位置
      if (mLocationViewers.otherGeoPoint != null)
      {
        Paint paint = new Paint();
        Point myScreenCoords = new Point();
        
        //顯示現在位置  
        mapView.getProjection().toPixels(mLocationViewers.nowGeoPoint, myScreenCoords);
        paint.setStrokeWidth(1);
        paint.setARGB(255, 255, 0, 0);
        paint.setStyle(Paint.Style.STROKE);
  
        canvas.drawBitmap(mNowIcon, myScreenCoords.x, myScreenCoords.y, paint);
        canvas.drawText("另一隻手機的位置", myScreenCoords.x, myScreenCoords.y, paint);
      }
    }

}