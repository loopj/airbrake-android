/*
    Hoptoad Notifier for Android
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

package com.loopj.android.hoptoad;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;

public class HoptoadNotifier {
    // Basic settings
    private static final String HOPTOAD_ENDPOINT = "http://hoptoadapp.com/notifier_api/v2/notices";
    private static final String UNSENT_EXCEPTION_PATH = "/unsent_hoptoad_exceptions/";

    // Exception meta-data
    private static String environmentName = "default";
    private static String packageName = "unknown";
    private static String versionName = "unknown";
    private static String phoneModel = android.os.Build.MODEL;
    private static String androidVersion = android.os.Build.VERSION.RELEASE;

    // Hoptoad api key
    private static String apiKey;

    // Exception storage info
    private static String filePath;
    private static boolean diskStorageEnabled = false;

    // Wrapper class to send uncaught exceptions to hoptoad
    private static class HoptoadExceptionHandler implements UncaughtExceptionHandler {
        private UncaughtExceptionHandler defaultExceptionHandler;

        public HoptoadExceptionHandler(UncaughtExceptionHandler defaultExceptionHandlerIn) {
            defaultExceptionHandler = defaultExceptionHandlerIn;
        }

        public void uncaughtException(Thread t, Throwable e) {
            HoptoadNotifier.notify(e);
            defaultExceptionHandler.uncaughtException(t, e);
        }
    }

    // Register to send exceptions to hoptoad
    public static void register(Context context, String apiKey) {
        register(context, apiKey, null);
    }

    // Register to send exceptions to hoptoad (with an environment name)
    public static void register(Context context, String apiKey, String environmentName) {
        // Require a hoptoad api key
        if(apiKey != null) {
            HoptoadNotifier.apiKey = apiKey;
        } else {
            throw new RuntimeException("HoptoadNotifier requires a Hoptoad API key.");
        }

        // Fill in environment name if passed
        if(environmentName != null) {
            HoptoadNotifier.environmentName = environmentName;
        }

        // Connect our default exception handler
        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if(!(currentHandler instanceof HoptoadExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new HoptoadExceptionHandler(currentHandler));
        }

        // Load up current package name and version
        try {
            packageName = context.getPackageName();
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {}

        // Prepare the file storage location
        filePath = context.getFilesDir().getAbsolutePath() + UNSENT_EXCEPTION_PATH;
        File outFile = new File(filePath);
        outFile.mkdirs();
        diskStorageEnabled = outFile.exists();

        // Flush any existing exception info
        flushExceptions();
    }

    // Fire an exception to hoptoad manually
    public static void notify(Throwable e) {
        if(e != null && diskStorageEnabled) {
            writeExceptionToDisk(e);
            flushExceptions();
        }
    }

    private static void writeExceptionToDisk(Throwable e) {
        try {
            // Set up the output stream
            int random = new Random().nextInt(99999);
            String filename = filePath + versionName + "-" + String.valueOf(random) + ".xml";
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));

            // Build and write xml to output stream
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.write("<notice version=\"2.0\">");
            out.write("  <api-key>" + apiKey + "</api-key>");
            out.write("  <notifier>");
            out.write("    <name>Android Hoptoad Notifier</name>");
            out.write("    <version>1.0.0</version>");
            out.write("    <url>http://loopj.com</url>");
            out.write("  </notifier>");
            out.write("  <error>");
            out.write("    <class>" + e.getClass().getName() + "</class>");
            out.write("    <message>[" + versionName + "] " + e.getLocalizedMessage() + "</message>");
            out.write("    <backtrace>");

            // Extract the stack traces
            Throwable currentEx = e;
            while(e != null) {
                StackTraceElement[] stackTrace = e.getStackTrace();
                for(StackTraceElement el : stackTrace) {
                    out.write("      <line method=\"" + el.getClassName() + "." + el.getMethodName() + "\" file=\"" + el.getFileName() + "\" number=\"" + String.valueOf(el.getLineNumber()) + "\" />");
                }

                e = e.getCause();
                if(e != null) {
                    out.write("      <line file=\"CAUSED BY\" number=\"\" />");
                }
            }

            out.write("    </backtrace>");
            out.write("  </error>");
            out.write("  <request>");
            out.write("     <url/>");
            out.write("     <component/>");
            out.write("     <action/>");
            out.write("     <cgi-data>");
            out.write("         <var key=\"Device\">" + phoneModel + "</var>");
            out.write("         <var key=\"Android Version\">" + androidVersion + "</var>");
            out.write("         <var key=\"App Version\">" + versionName + "</var>");
            out.write("     </cgi-data>");
            out.write("  </request>");
            out.write("  <server-environment>");
            out.write("    <environment-name>" + environmentName + "</environment-name>");
            out.write("  </server-environment>");
            out.write("</notice>");

            out.flush();
            out.close();
        } catch (Exception bade) {
            bade.printStackTrace();
        }
    }

    private static void sendExceptionData(File file) {
        try {
            URL url = new URL(HOPTOAD_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                // Set up the connection
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

                // Read from the file and send it
                FileInputStream is = new FileInputStream(file);
                OutputStream os = conn.getOutputStream();
                byte[] buffer = new byte[4096];
                int numRead;
                while((numRead = is.read(buffer)) >= 0) {
                  os.write(buffer, 0, numRead);
                }
                os.flush();
                os.close();
                is.close();

                // Flush the request through
                conn.getResponseCode();

                // Delete file now we've sent the exceptions
                file.delete();
            } catch(IOException e) {
                // Ignore any file stream issues
            } finally {
                conn.disconnect();
            }
        } catch(IOException e) {
            // Ignore any connection failure when trying to open the connection
            // We can try again next time
        }
    }

    private static void flushExceptions() {
        File exceptionDir = new File(filePath);
        if(exceptionDir.exists() && exceptionDir.isDirectory()) {
            File[] exceptions = exceptionDir.listFiles();
            for(File f : exceptions) {
                if(f.exists() && f.isFile()) {
                    sendExceptionData(f);
                }
            }
        }
    }
}