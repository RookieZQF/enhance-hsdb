package com.perfma.hotspot;

import com.perfma.hotspot.proxy.CLHSDBProxy;
import sun.misc.URLClassPath;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;

/**
 * @author: ZQF
 * @date: 2021-02-22
 * @description: desc
 */
public class CustomCLHSDB {
    private static final String JRE = "jre";
    public static void main(String[] args) {
        try {
            Class.forName("javax.swing.JFrame");
        }catch (Exception ignore){
        }
        String javaHome = System.getProperty("java.home");
        if(javaHome.endsWith(File.separator + JRE)){
            javaHome = javaHome.substring(0, javaHome.lastIndexOf(File.separator));
        }
        String hsdbPath = javaHome + File.separator + "lib" + File.separator + "sa-jdi.jar";
        File file = new File(hsdbPath);
        if (file.exists()) {
            try {
                URLClassLoader classLoader = (URLClassLoader) CustomCLHSDB.class.getClassLoader();
                Class<? extends URLClassLoader> aClass = URLClassLoader.class;
                Field ucp = aClass.getDeclaredField("ucp");
                ucp.setAccessible(true);
                Field acc = aClass.getDeclaredField("acc");
                acc.setAccessible(true);

                URLClassPath urlClassPath = (URLClassPath) ucp.get(classLoader);
                URL[] urLs = urlClassPath.getURLs();
                URL[] newUrls = new URL[urLs.length + 1];
                System.arraycopy(urLs, 0, newUrls, 0, urLs.length);
                newUrls[urLs.length] = file.toURI().toURL();
                URLClassPath path = new URLClassPath(newUrls, (AccessControlContext)acc.get(classLoader));
                ucp.set(classLoader, path);
            }catch (Throwable e){
                System.err.println("ERROR : load sa-jdi.jar exception : " + hsdbPath);
                e.printStackTrace();
                return ;
            }
        } else {
            System.err.println("ERROR : sa-jdi.jar is not exist : " + hsdbPath);
            return ;
        }

        CLHSDBProxy.run(args);
    }
}
