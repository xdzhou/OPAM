package com.sky.opam;

import java.io.IOException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;

public class MyBackupAgent extends BackupAgent 
{

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,ParcelFileDescriptor newState) throws IOException 
	{
		
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException 
	{
		
	}

}
