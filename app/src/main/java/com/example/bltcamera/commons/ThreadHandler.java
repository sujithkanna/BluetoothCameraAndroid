package com.example.bltcamera.commons;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by hmspl on 15/1/16.
 */
public class ThreadHandler {

    private static ThreadHandler sThreadHandler;

    private Random random = new Random();

    public Map<Integer, Thread> mTasksMap = new HashMap<>();

    private ThreadHandler() {
    }

    public static ThreadHandler getInstance() {
        if (sThreadHandler == null) {
            sThreadHandler = new ThreadHandler();
        }
        return sThreadHandler;
    }

    public int timerTask(final Runnable runnable, final long delay) {
        return doInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                doInForground(runnable);
            }
        });
    }


    public int doInBackground(final Runnable runnable) {
        final int taskId = generateTaskId();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                runnable.run();
                mTasksMap.remove(taskId);
            }
        });
        thread.start();
        mTasksMap.put(taskId, thread);
        return taskId;
    }

    public void doInForground(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public void stopTask(int taskId) {
        if (mTasksMap.containsKey(taskId)) {
            mTasksMap.get(taskId).interrupt();
            mTasksMap.remove(taskId);
        }
    }

    private int generateTaskId() {
        int taskId = random.nextInt(10000);
        if (mTasksMap.containsKey(taskId)) {
            return generateTaskId();
        }
        return taskId;
    }


}
