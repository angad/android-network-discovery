/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package info.lamatricexiste.network;

import info.lamatricexiste.network.Utils.Prefs;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class RootDaemon extends AbstractRoot {
    private final String TAG = "RootDaemon";
    private final String DAEMON = "scand";
    private WeakReference<Activity> mActivity;
    private String dir;
    public boolean hasRoot;

    public RootDaemon(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
        hasRoot = checkRoot();
        dir = getDir();
        // Check if daemon is installed, if not run MainActivity again
        final Activity d = mActivity.get();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(d
                .getApplicationContext());
        if (hasRoot && prefs.getInt(Prefs.KEY_ROOT_INSTALLED, Prefs.DEFAULT_ROOT_INSTALLED) == 0) {
            d.startActivity(new Intent(d.getApplicationContext(), ActivityMain.class));
        }
    }

    public void start() {
        if (hasRoot) {
            execute(new String[] { "killall -9 " + DAEMON, dir + DAEMON });
        }
    }

    public void kill() {
        if (hasRoot) {
            execute(new String[] { "killall -9 " + DAEMON });
        }
    }

    public void install() {
        if (hasRoot) {
            if ((new File(dir + DAEMON)).exists() == false) {
                createDir(dir);
                //copyFile(dir + DAEMON, R.raw.scand);
            }
        }
    }

    public void permission() {
        if (hasRoot) {
            execute(new String[] { "chmod 755 " + dir + DAEMON, "chown root " + dir + DAEMON });
        }
    }

    public void restartActivity() {
        final Activity d = mActivity.get();
        Intent intent = new Intent();
        intent.setClass(d, d.getClass());
        d.startActivity(intent);
        d.finish();
    }

    private String getDir() {
        final Activity d = mActivity.get();
        return d.getFilesDir().getParent() + "/bin/";
    }

    private void createDir(String dirname) {
        File dir = new File(dirname);
        if (dir.exists() == false) {
            dir.mkdir();
        }
    }

    // private void copyFile(String file, int resource) {
    // final Activity activity = mActivity.get();
    // try {
    // InputStream in = activity.getResources().openRawResource(resource);
    // OutputStream out = new FileOutputStream(file);
    // final ReadableByteChannel inputChannel = Channels.newChannel(in);
    // final WritableByteChannel outputChannel = Channels.newChannel(out);
    // DownloadFile.fastChannelCopy(inputChannel, outputChannel);
    // } catch (IOException e) {
    // Log.e(TAG + ":copyFile", e.getMessage());
    // }
    // }

    // private void execute(final String cmd) {
    // int ret = Command.runCommand(cmd);
    // Log.i(TAG, "cmd=" + cmd + ", ret=" + ret);
    // }

    private void execute(final String[] cmds) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    for (String cmd : cmds) {
                        Log.v(TAG, "run=" + cmd);
                        os.writeBytes(cmd + "\n");
                    }
                    os.writeBytes("exit\n");
                    os.flush();
                    os.close();
                    process.waitFor();
                } catch (IOException e) {
                    Log.e(TAG + ":execute", e.getMessage());
                } catch (InterruptedException e) {
                    Log.e(TAG + ":execute", e.getMessage());
                }
            }
        }).start();
    }
}
