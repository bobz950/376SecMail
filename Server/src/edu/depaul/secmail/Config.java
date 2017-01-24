/*
 * Copyright 2016. DePaul University. All rights reserved. 
 * This work is distributed pursuant to the Software License
 * for Community Contribution of Academic Work, dated Oct. 1, 2016.
 * For terms and conditions, please see the license file, which is
 * included in this distribution.
 */
package edu.depaul.secmail;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Config {
	private final String LOG_FILE_DEFAULT_PATH = "./SecMailServer.log";
	private int port = 57890;
	private int backlog = 15;
	private String configFilePath = null;
	private File configFile = null;
	private String logFilePath = null;
	private File logFile = null;
	private String mailDir = "./mail/";
	private String domain = "localhost";
	
	//Jacob Burkamper
	Config(String[] args) 
	{
		//iterate through each of the arguments
		for (int i = 0; i < args.length; i++)
		{
			//only read arguments with the - switch
			if (args[i].startsWith("-"))
			{
				switch (args[i].substring(1))
				{
				case "c":
				case "-configfile":
					this.LoadConfigFile(args[++i]);
					break;
				case "l":
				case "-logfile":
					this.SetLogFile(args[++i]);
					break;
				case "d":
				case "-domain":
					this.setDomain(args[++i]);
					break;
				default:
					System.err.println("Unknown command line option: " + args[i]);
				}
			}
		}
		if (logFile == null) // if the log file hasn't been set up yet.
		{
			SetLogFile(LOG_FILE_DEFAULT_PATH);
		}
		Log.Debug("finished constructing Config object");
	}
	
	//Jacob Burkamper
	public File getLogFile()
	{
		return this.logFile;
	}
	
	//Jacob Burkamper
	public String getConfigFilePath()
	{
		return this.configFilePath;
	}
	
	//Jacob Burkamper
	public int getBacklog()
	{
		return backlog;
	}
	
	//Jacob Burkamper
	public int getPort()
	{
		return this.port;
	}
	
	//Jacob Burkamper
	public String getMailRoot()
	{
		return mailDir;
	}
	
	//Jacob Burkamper
	//Get the directory for the user on this server
	public String getUserDirectory(String user)
	{
		return mailDir + user + "/";
	}
	
	//Jacob Burkamper
	private void LoadConfigFile(String path)
	{
		Log.Out("Loading config file from \"" + path + "\"");
		try {
			ReadConfigFile(path);
		} catch (IOException e) {
			System.err.println("Error reading config file \"" + path +"\"");
			System.err.println(e);
			
			//reset the config file path vars
			configFilePath = null;
			configFile = null;
		}
	}
	
	//Jacob Burkamper
	private void ReadConfigFile(String path) throws IOException
	{
		configFilePath = path;
		configFile = new File(path);
		BufferedReader inStream = null;
		
		try {
			inStream = new BufferedReader(new FileReader(configFile));
			String line;
			while ((line = inStream.readLine()) != null)
			{
				//skip comments
				if (line.startsWith("#"))
					continue;
				
				String[] parts = line.split("=");
				
				switch(parts[0])
				{
				case "LogFile":
					this.SetLogFile(parts[1]);
					break;
				case "Domain":
					this.setDomain(parts[1]);
					break;
				default:
					System.err.println("Invalid configuration option: " + parts[0]);
				}
			}
		} finally {
			if (inStream != null)
				inStream.close();
		}
	}
	
	//Jacob Burkamper
	private void SetLogFile(String newLogFilePath)
	{
		Log.Debug("Setting Log File path to + \"" + newLogFilePath + "\"");
		
		//get the file handle
		File file = new File(newLogFilePath);
		try {
			if ((file.exists() && file.canWrite()) || file.createNewFile())
			{
				logFilePath = newLogFilePath;
				logFile = file;
			}
			else
			{
				System.err.println("Unable to open file \""+newLogFilePath+"\" for writing as log file");
				//kill the program
				System.exit(20);
			}
		} catch (IOException e) {
			System.err.println("Error trying to create log file");
			System.exit(21);
		}
		
	}
	
	//Jacob Burkamper
	private void setDomain(String newDomain)
	{ 
		this.domain = newDomain;
	}
	
	//Jacob Burkamper
	public String getDomain()
	{
		return this.domain;
	}
}
