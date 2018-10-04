package org.iot.dsa.dslink;

import org.iot.dsa.DSRuntime;
import org.testng.Assert;

public class DSRuntimeTest {
    
    private static int runs = 0;
    private static Object lock1 = new Object();
    private static int runsWithDelay = 0;
    private static Object lock2 = new Object();
    private static boolean success = false;
    private static Object lock3 = new Object();
    private static boolean successWithDelay = false;
    private static Object lock4 = new Object();
    
    public void test1() throws Exception {
        runs = 0;
        DSRuntime.run(new Runnable() {

            @Override
            public void run() {
                runs+=1;
                if (runs >= 5) {
                    synchronized (lock1) {
                        lock1.notifyAll();
                    }
                }
            }
            
        }, System.currentTimeMillis() + 400, 100);
        
        synchronized(lock1) {
            lock1.wait(1000); 
        }
        Assert.assertTrue(runs >= 5);
    }
    
    public void test2() throws Exception {
        runsWithDelay = 0;
        DSRuntime.runAfterDelay(new Runnable() {

            @Override
            public void run() {
                runsWithDelay+=1;
                if (runsWithDelay >= 5) {
                    synchronized (lock2) {
                        lock2.notifyAll();
                    }
                }
            }
            
        }, 400, 100);
        
        synchronized(lock2) {
            lock2.wait(1000); 
        }
        Assert.assertTrue(runsWithDelay >= 5);
    }
    
    public void test3() throws Exception {
        success = false;
        DSRuntime.runAt(new Runnable() {

            @Override
            public void run() {
                success = true;
                synchronized (lock3) {
                    lock3.notifyAll();
                }
            }
            
        }, System.currentTimeMillis() + 400);
        
        synchronized(lock3) {
            lock3.wait(500); 
        }
        Assert.assertTrue(success);
    }
    
    public void test4() throws Exception {
        successWithDelay = false;
        DSRuntime.runDelayed(new Runnable() {

            @Override
            public void run() {
                successWithDelay = true;
                synchronized (lock4) {
                    lock4.notifyAll();
                }
            }
            
        }, 400);
        
        synchronized(lock4) {
            lock4.wait(500); 
        }
        Assert.assertTrue(successWithDelay);
    }

}
