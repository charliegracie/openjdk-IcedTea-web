/* CacheReproducerTest.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerAccess.ProcessResult;
import org.junit.AfterClass;
import org.junit.Assert;

import org.junit.Test;

public class CacheReproducerTest {

    private static final ServerAccess server = new ServerAccess();
    private static final List<String> clear = Arrays.asList(new String[]{server.getJavawsLocation(), "-Xclearcache",  ServerAccess.HEADLES_OPTION});
    private static final List<String> trusted = Arrays.asList(new String[]{"-Xtrustall", ServerAccess.HEADLES_OPTION});
    private static final String home = System.getProperty("user.home");
    private static final String name = System.getProperty("user.name");
    private static final String tmp = System.getProperty("java.io.tmpdir");
    private static final File icedteaDir = new File(home + "/" + ".icedtea");
    private static final File icedteaCache = new File(icedteaDir, "cache");
    private static final File icedteaCacheFile = new File(icedteaCache, "recently_used");
    private static final File netxLock = new File(tmp + "/" + name + "/netx/locks/netx_running");
    private static final String lre = "LruCacheException";
    private static final String corruptRegex = "\\d{13}";
    private static final Pattern corruptPatern = Pattern.compile(corruptRegex);
    private static final String corruptString = "156dsf1562kd5";

    @Test
    public void cacheIsWorkingTest() throws Exception {
        clearAndEvaluateCache();
        evaluateSimpleTest1OkCache(runSimpleTest1());
        assertCacheIsNotEmpty();
    }

    @Test
    public void cacheIsWorkingTestSigned() throws Exception {
        clearAndEvaluateCache();
        evaluateSimpleTest1OkCache(runSimpleTest1Signed());
        assertCacheIsNotEmpty();
    }

     private class ParallelSimpleTestRunner extends Thread {
           public boolean b=false;
            @Override
            public void run() {
                try {

                    ServerAccess.ProcessResult pr = runSimpleTest1();
                    evaluateSimpleTest1OkCache(pr);
                    b=true;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

    @Test
    public void startParallelInstancesUponBrokenCache() throws Exception {
        clearAndEvaluateCache();
        evaluateSimpleTest1OkCache(runSimpleTest1());
        assertCacheIsNotEmpty();
        breakCache1();
        ParallelSimpleTestRunner t1=new ParallelSimpleTestRunner();
        ParallelSimpleTestRunner t2=new ParallelSimpleTestRunner();
        ParallelSimpleTestRunner t3=new ParallelSimpleTestRunner();
        t1.start();
        t2.start();
        t3.start();
        int c=0;
        while(true){
            c++;
            Thread.sleep(100);
            if (c>600) throw new Error("threads have not died in time");
            if (!t1.isAlive() && !t2.isAlive() && !t3.isAlive()) break;
        }
        Thread.sleep(1000);
        Assert.assertTrue(t1.b);
        Assert.assertTrue(t2.b);
        Assert.assertTrue(t3.b);
    }


    private void assertCacheIsNotEmpty() {
        Assert.assertTrue("icedtea cache " + icedteaCache.getAbsolutePath() + " should exist some any run", icedteaCache.exists());
        Assert.assertTrue("icedtea cache file " + icedteaCacheFile.getAbsolutePath() + " should exist some any run", icedteaCacheFile.exists());
        Assert.assertTrue("icedtea cache file " + icedteaCacheFile.getAbsolutePath() + " should not be empty", icedteaCacheFile.length() > 0);
    }

    @Test
    public void coruptAndRunCache1() throws Exception {
        clearAndEvaluateCache();
        evaluateSimpleTest1OkCache(runSimpleTest1());
        assertCacheIsNotEmpty();
        breakCache1();
        ProcessResult pr = runSimpleTest1();
        assertLruExceptionAppeared(pr);
        evaluateSimpleTest1OkCache(pr);
        clearAndEvaluateCache();
        ProcessResult pr2 = runSimpleTest1();
        evaluateSimpleTest1OkCache(pr2);
        assertLruExceptionNOTappeared(pr2);
    }

    @Test
    public void coruptAndRunCache2() throws Exception {
         clearAndEvaluateCache();
        evaluateSimpleTest1OkCache(runSimpleTest1());
        assertCacheIsNotEmpty();
        breakCache1();
        ProcessResult pr = runSimpleTest1();
        assertLruExceptionAppeared(pr);
        evaluateSimpleTest1OkCache(pr);
        ProcessResult pr3 = runSimpleTest1();
        evaluateSimpleTest1OkCache(pr3);
        assertLruExceptionNOTappeared(pr3);
        clearAndEvaluateCache();
        ProcessResult pr2 = runSimpleTest1();
        evaluateSimpleTest1OkCache(pr2);
        assertLruExceptionNOTappeared(pr2);
    }

    private void assertLruExceptionNOTappeared(ProcessResult pr2) {
        Assert.assertFalse("serr should NOT contain " + lre, pr2.stderr.contains(lre));
    }

    private void assertLruExceptionAppeared(ProcessResult pr) {
        Assert.assertTrue("serr should contain " + lre, pr.stderr.contains(lre));
    }

    @Test
    public void coruptAndRunCache1Signed() throws Exception {
         clearAndEvaluateCache();
        evaluateSimpleTest1OkCache(runSimpleTest1());
        assertCacheIsNotEmpty();
        breakCache1();
        ProcessResult pr = runSimpleTest1Signed();
        assertLruExceptionAppeared(pr);
        evaluateSimpleTest1OkCache(pr);
        clearAndEvaluateCache();
        ProcessResult pr2 = runSimpleTest1Signed();
        evaluateSimpleTest1OkCache(pr2);
        assertLruExceptionNOTappeared(pr2);
    }

    @Test
    public void coruptAndRunCache2Signed() throws Exception {
         clearAndEvaluateCache();
        evaluateSimpleTest1OkCache(runSimpleTest1());
        assertCacheIsNotEmpty();
        breakCache1();
        ProcessResult pr = runSimpleTest1Signed();
        assertLruExceptionAppeared(pr);
        evaluateSimpleTest1OkCache(pr);
        ProcessResult pr3 = runSimpleTest1Signed();
        evaluateSimpleTest1OkCache(pr3);
        assertLruExceptionNOTappeared(pr3);
        clearAndEvaluateCache();
        ProcessResult pr2 = runSimpleTest1Signed();
        evaluateSimpleTest1OkCache(pr2);
        assertLruExceptionNOTappeared(pr2);
    }

    @Test
    public void clearCacheUnsucessfully() throws Exception {
        evaluateSimpleTest1OkCache(runSimpleTest1());
        assertCacheIsNotEmpty();
        ProcessResult pr;
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    System.out.println("connecting deadlocktest request");
                    System.err.println("connecting deadlocktest request");
                    ServerAccess.ProcessResult pr = server.executeJavawsHeadless(null, "/deadlocktest.jnlp");
                    System.out.println(pr.stdout);
                    System.err.println(pr.stderr);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        t.start();
        Thread.sleep(1000);
        pr = tryToClearcache();
        System.out.println(pr.stdout);
        System.err.println(pr.stderr);
        String q = "Can not clear cache at this time";
        Assert.assertTrue("Stderr should contain " + q + ", but did not.", pr.stderr.contains(q));
        assertCacheIsNotEmpty();
    }

     
    //next four tests are designed to ensure, that corupted cache wil not break already loaded cached files
    public static final String CR1 = "CacheReproducer1";
    public static final String CR2 = "CacheReproducer2";
    public static final String CR11 = "CacheReproducer1_1";
    public static final String CR21 = "CacheReproducer2_1";

    public void testsBody(String id, int breaker) throws Exception {
        clearAndEvaluateCache();
        ProcessResult pr1 = runSimpleTestSigned(id);
        assertLruExceptionNOTappeared(pr1);
        evaluateSimpleTest1OkCache(pr1);
        if (breaker < 0) {
            breakCache1();
        } else {
            breakCache2(breaker);
        }
        ProcessResult pr2 = runSimpleTestSigned(id);
        assertLruExceptionAppeared(pr2);
        evaluateSimpleTest1OkCache(pr2);
    }

    @Test
    public void testAlreadyLoadedCached1() throws Exception {
        testsBody(CR1, 1);
        testsBody(CR1, 2);
        testsBody(CR1, -1);
    }

    @Test
    public void testAlreadyLoadedCached2() throws Exception {
        testsBody(CR2, 1);
        testsBody(CR2, 2);
        testsBody(CR2, -1);
    }

    @Test
    public void testAlreadyLoadedCached11() throws Exception {
        testsBody(CR11, 1);
        testsBody(CR11, 2);
        testsBody(CR11, -1);
    }

    @Test
    public void testAlreadyLoadedCached21() throws Exception {
        testsBody(CR21, 1);
        testsBody(CR21, 2);
        testsBody(CR21, -1);
    }

    @AfterClass
    public static void clearCache() throws Exception {
        clearAndEvaluateCache();
    }

    private static void clearAndEvaluateCache() throws Exception {
        clearAndEvaluateCache(true);
    }

    private static void clearAndEvaluateCache(boolean force) throws Exception {
        System.out.println("clearing cache");
        System.err.println("clearing cache");
        if (force) {
            if (netxLock.isFile()) {
                boolean b = netxLock.delete();
                junit.framework.Assert.assertTrue(b);
            }

        }
        tryToClearcache();
        Assert.assertFalse("icedtea cache " + icedteaCache.getAbsolutePath() + " should not exist after clearing", icedteaCache.exists());
    }

    private static String loadFile(File f) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(icedteaCacheFile), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String s = r.readLine();
            if (s == null) {
                break;
            }
            sb.append(s).append("\n");

        }
        return sb.toString();
    }

    private static String loadCacheFile() throws IOException {
        return loadFile(icedteaCacheFile);
    }

    @Test
    public void assertBreakersAreWorking() {
        String s = "#netx file\n"
                + "#Mon Dec 12 16:20:46 CET 2011\n"
                + "1323703236508,0=/home/xp13/.icedtea/cache/0/http/localhost/ReadPropertiesBySignedHack.jnlp\n"
                + "1323703243086,2=/home/xp14/.icedtea/cache/2/http/localhost/ReadProperties.jar\n"
                + "1323703243082,1=/home/xp15/.icedtea/cache/1/http/localhost/ReadPropertiesBySignedHack.jar";
        String sp[] = s.split("\n");

        String ss[] = breakAll(s).split("\n");
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(sp[i], ss[i]);

        }
        for (int i = 2; i < ss.length; i++) {
            Assert.assertNotSame(sp[i], ss[i]);

        }
        String sb = breakOne(s, 0);
        junit.framework.Assert.assertEquals(s, sb);
        for (int x = 1; x <= 3; x++) {
            String[] sx = breakOne(s, x).split("\n");
            for (int i = 0; i < sx.length; i++) {
                if (i == x + 1) {
                    Assert.assertNotSame(sp[i], sx[i]);
                } else {
                    Assert.assertEquals(sp[i], sx[i]);
                }

            }
        }
        String sbb = breakOne(s, 4);
        Assert.assertEquals(s, sbb);
    }

    private static String breakAll(String s) {
        return s.replaceAll(corruptRegex, corruptString);
    }

    private static String breakOne(String s, int i) {
        Matcher m1 = corruptPatern.matcher(s);
        int x = 0;
        while (m1.find()) {
            x++;
            String r = (m1.group(0));
            if (x == i) {
                return s.replace(r, corruptString);
            }
        }
        return s;
    }

    private static void breakCache1() throws IOException {
        String s = loadCacheFile();
        s = breakAll(s);
        ServerAccess.saveFile(s, icedteaCacheFile);
    }

    private static void breakCache2(int i) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        String s = loadCacheFile();
        s = breakOne(s, i);
        ServerAccess.saveFile(s, icedteaCacheFile);
    }

    private static ServerAccess.ProcessResult runSimpleTest1() throws Exception {
        return runSimpleTest1(null, "simpletest1");
    }

    private static ServerAccess.ProcessResult runSimpleTest1(List<String> args, String s) throws Exception {
        System.out.println("connecting " + s + " request");
        System.err.println("connecting " + s + " request");
        ServerAccess.ProcessResult pr2 = server.executeJavawsHeadless(args, "/" + s + ".jnlp");
        System.out.println(pr2.stdout);
        System.err.println(pr2.stderr);
        return pr2;
    }

    private static ServerAccess.ProcessResult runSimpleTest1Signed() throws Exception {
        return runSimpleTestSigned("SimpletestSigned1");
    }

    private static ServerAccess.ProcessResult runSimpleTestSigned(String id) throws Exception {
        return runSimpleTest1(trusted, id);
    }

    private static void evaluateSimpleTest1OkCache(ServerAccess.ProcessResult pr2) throws Exception {
        String s = "Good simple javaws exapmle";
        Assert.assertTrue("test stdout should contain " + s + " but didn't", pr2.stdout.contains(s));
        Assert.assertFalse(pr2.wasTerminated);
        Assert.assertEquals((Integer) 0, pr2.returnValue);
    }

    private static ProcessResult tryToClearcache() throws Exception {
        ServerAccess.ProcessResult pr1 = ServerAccess.executeProcess(clear);
        System.out.println(pr1.stdout);
        System.err.println(pr1.stderr);
        return pr1;
    }
}
