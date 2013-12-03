package com.bazanski.movr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class SubRip {

	Context ctx;
	
	SubRip(Context ctx) {
		this.ctx = ctx;
	}
	
	public void writeSub(String FILENAME, String info) {
    	try {
    		//open stream for writing
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ctx.openFileOutput(FILENAME + ".srt", ctx.MODE_APPEND)));
			//FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/VideoLogger/" + FILENAME + ".srt", ctx.MODE_APPEND);
			//write data
            bw.write(info);
            Log.v(this.getClass().getName(),"� ����� ��������: \n" + info);
            
			//close stream
			bw.close();
			
		} catch (FileNotFoundException e) {
			Log.v(this.getClass().getName(), "������ ����� ������"+e.toString());
		} catch (IOException e) {
			Log.v(this.getClass().getName(), "������ ����� ������"+e.toString());
		}
    }
	
	void writeSubSD(String FILENAME, String info) {
	    // ��������� ����������� SD
	    if (!Environment.getExternalStorageState().equals(
	        Environment.MEDIA_MOUNTED)) {
	      Log.d(this.getClass().getName(), "SD-����� �� ��������: " + Environment.getExternalStorageState());
	      return;
	    }
	    // �������� ���� � SD
	    File sdPath;// = Environment.getExternalStorageDirectory();
	    // ��������� ���� ������� � ����
	    sdPath = new File(Environment.getExternalStorageDirectory().getPath() + "/VideoLogger");
	    // ������� �������
	    sdPath.mkdirs();
	    // ��������� ������ File, ������� �������� ���� � �����
	    File sdFile = new File(sdPath, FILENAME + ".srt");
	    try {
	      // ��������� ����� ��� ������
	      BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
	      // ����� ������
	      bw.write(info);
	      // ��������� �����
	      bw.close();
	      Log.d(this.getClass().getName(), "���� ������� �� SD: " + sdFile.getAbsolutePath());
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
}
