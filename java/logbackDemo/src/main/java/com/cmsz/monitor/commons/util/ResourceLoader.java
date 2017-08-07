package com.cmsz.monitor.commons.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public final class ResourceLoader {

    //create singleton
    private static ResourceLoader loader = new ResourceLoader();
    private static Map<String,Properties> loaderMap    = new HashMap<String,Properties>();

    private ResourceLoader() {
    }

    public static ResourceLoader getInstance() {
        return loader;
    }

    public Properties getProp(String fileName) throws IOException {
        Properties prop = loaderMap.get(fileName);
        if (prop != null) {
            return prop ;
        }
        prop = new Properties();
        prop.load(getInputStream(fileName));
        loaderMap.put(fileName, prop);
        return prop;
    }

    public InputStream getInputStream(String fileName) throws FileNotFoundException {

        Class<? extends ResourceLoader> theClass = this.getClass();
        ClassLoader theLoader = theClass.getClassLoader();
        InputStream in = theLoader.getResourceAsStream(fileName);
        if(in==null){
            return new FileInputStream(fileName);
        }
        return in;
    }

    public Object getClassInstance(String className)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        Class<? extends ResourceLoader> theClass = this.getClass();
        ClassLoader theLoader = theClass.getClassLoader();
        return theLoader.loadClass(className).newInstance();
    }

    @SuppressWarnings("rawtypes")
    public Object getClassInstance(String className, Object[] args)
            throws ClassNotFoundException, IllegalArgumentException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException, SecurityException, NoSuchMethodException {

        Class<? extends ResourceLoader> theClass = this.getClass();
        ClassLoader theLoader = theClass.getClassLoader();
        Class[] type = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            type[i] = args[i].getClass();
        }
        Class<?> cls = theLoader.loadClass(className);
        Constructor<?> construct = cls.getConstructor(type);
        Object o = construct.newInstance(args);
        return o;
    }

}
