package com.semlerscientific.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;

public class SSLogger
{
    private static final LoggerRunnable mLoggerRunnable;

    static
    {
        mLoggerRunnable = new LoggerRunnable();
        Thread t = new Thread(mLoggerRunnable);
        t.start();

        mLoggerRunnable.log(SSSensorPluginDetails.PluginName + " version " + SSSensorPluginDetails.Version);
        mLoggerRunnable.log("");//blank line
    }

    public static void writeData(String logData)
    {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss: ");
        formatter.setTimeZone(tz);

        logData = formatter.format(new Date()) + logData;

        mLoggerRunnable.log(logData);
    }

    public static void writeException(Exception e, String logData)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        writeData(e.getClass().getName() + ": " + e.getMessage() + "; " + sw.toString() + "; " + logData);
    }

    private static class LoggerRunnable implements Runnable
    {
        private BufferedWriter mWriter = null;

        private Queue<String> mQueue;

        public LoggerRunnable()
        {
            mQueue = new LinkedList<String>();

            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss");
            formatter.setTimeZone(tz);
            File logFile = new File("sdcard/log_" + formatter.format(new Date()) +".txt");

            try
            {
                logFile.createNewFile();
                mWriter = new BufferedWriter(new FileWriter(logFile, true));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                mWriter = null;
            }
        }

        public synchronized void log(String logData)
        {
            mQueue.add(logData);

            notifyAll();
        }

        private synchronized Queue<String> emptyQueue()
        {
            Queue<String> localQueue = new LinkedList<String>();

            try
            {
                wait();
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }

            while (!mQueue.isEmpty())
            {
                localQueue.add(mQueue.remove());
            }

            return localQueue;
        }

        @Override
        public void run()
        {
            Queue<String> localQueue;

            while(true)
            {
                if(null != mWriter)
                {
                    localQueue = emptyQueue();

                    try
                    {
                        while(!localQueue.isEmpty())
                        {
                            mWriter.append(localQueue.remove());
                            mWriter.newLine();
                        }
                        mWriter.flush();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
