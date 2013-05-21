package com.androidesk.camera.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * 自定义序列化抽象类
 * 
 * @author androidesk
 * 
 */
public abstract class FileSerialize {
	/**
	 *写入文件记录版本号，当文件的数据读写方式改变后，需要更改版本号，更通过版本兼容老文件 
	 */
	protected String mVision = "1.0";
	/**
	 * 写入时间
	 */
	protected Date mWriteTime;
	
	public void unserialize(File file) {
		try {
			FileInputStream fs = new FileInputStream(file);
			DataInputStream ds = new DataInputStream(fs);
			read(ds);
			ds.close();
			fs.close();
		} catch (Exception e) {
		}
	}

	public void serialize(File file) {
		try {
			if (!file.exists())
				file.createNewFile();
			FileOutputStream fs = new FileOutputStream(file);
			DataOutputStream ds = new DataOutputStream(fs);
			write(ds);
			ds.close();
			fs.close();
		} catch (Exception e) {
		}
	}

	public void write(DataOutputStream ds) throws IOException{
		ds.writeUTF(mVision);
		ds.writeLong(System.currentTimeMillis());//记录写入时间
	}

	public void read(DataInputStream ds) throws IOException{
		mVision = ds.readUTF();
		this.mWriteTime = new Date(ds.readLong());
	}

	public String getVision() {
		return mVision;
	}

	public Date getWriteTime() {
		return mWriteTime;
	}
}
