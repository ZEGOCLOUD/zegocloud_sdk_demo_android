package com.zegocloud.demo.quickstart;


import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class LoadLibraryUtil {

    private static final String TAG = "Tinker.LoadLibrary";

    /**
     * you can reflect your current abi to classloader library path as you don't need to use load*Library method above
     */
    public static boolean addToNativeLibraryPath(Context context, File soDir) {
        ClassLoader classLoader = context.getClassLoader();
        if (classLoader == null) {
            Log.e(TAG, "classloader is null");
            return false;
        }
        Log.i(TAG, "before hack classloader:" + classLoader.toString());

        try {
            installNativeLibraryPath(classLoader, soDir);
            return true;
        } catch (Throwable throwable) {
            Log.e(TAG, "installNativeLibraryPath fail:" + throwable);
            return false;
        } finally {
            Log.i(TAG, "after hack classloader:" + classLoader.toString());
        }
    }

    /**
     * All version of install logic obey the following strategies: 1. If path of {@code folder} is not injected into the
     * classloader, inject it to the beginning of pathList in the classloader.
     * <p>
     * 2. Otherwise remove path of {@code folder} first, then re-inject it to the beginning of pathList in the
     * classloader.
     */
    private static void installNativeLibraryPath(ClassLoader classLoader, File folder) throws Throwable {
        if (folder == null || !folder.exists()) {
            Log.e(TAG, "installNativeLibraryPath, " + folder + " is illegal");
            return;
        }
        // android o sdk_int 26
        // for android o preview sdk_int 25
        if ((Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT != 0) || Build.VERSION.SDK_INT > 25) {
            try {
                V25.install(classLoader, folder);
            } catch (Throwable throwable) {
                // install fail, try to treat it as v23
                // some preview N version may go here
                Log.e(TAG, "installNativeLibraryPath, v25 fail, sdk: %d, error: %s, try to fallback to V23");
                V23.install(classLoader, folder);
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            try {
                V23.install(classLoader, folder);
            } catch (Throwable throwable) {
                // install fail, try to treat it as v14
                Log.e(TAG, "installNativeLibraryPath, v23 fail, sdk: %d, error: %s, try to fallback to V14");

                V14.install(classLoader, folder);
            }
        } else if (Build.VERSION.SDK_INT >= 14) {
            V14.install(classLoader, folder);
        } else {
            V4.install(classLoader, folder);
        }
    }

    private static final class V4 {

        private static void install(ClassLoader classLoader, File folder) throws Throwable {
            String addPath = folder.getPath();
            Field pathField = findField(classLoader, "libPath");
            final String origLibPaths = (String) pathField.get(classLoader);
            final String[] origLibPathSplit = origLibPaths.split(":");
            final StringBuilder newLibPaths = new StringBuilder(addPath);

            for (String origLibPath : origLibPathSplit) {
                if (origLibPath == null || addPath.equals(origLibPath)) {
                    continue;
                }
                newLibPaths.append(':').append(origLibPath);
            }
            pathField.set(classLoader, newLibPaths.toString());

            final Field libraryPathElementsFiled = findField(classLoader, "libraryPathElements");
            final List<String> libraryPathElements = (List<String>) libraryPathElementsFiled.get(classLoader);
            final Iterator<String> libPathElementIt = libraryPathElements.iterator();
            while (libPathElementIt.hasNext()) {
                final String libPath = libPathElementIt.next();
                if (addPath.equals(libPath)) {
                    libPathElementIt.remove();
                    break;
                }
            }
            libraryPathElements.add(0, addPath);
            libraryPathElementsFiled.set(classLoader, libraryPathElements);
        }
    }

    private static final class V14 {

        private static void install(ClassLoader classLoader, File folder) throws Throwable {
            final Field pathListField = findField(classLoader, "pathList");
            final Object dexPathList = pathListField.get(classLoader);

            final Field nativeLibDirField = findField(dexPathList, "nativeLibraryDirectories");
            final File[] origNativeLibDirs = (File[]) nativeLibDirField.get(dexPathList);

            final List<File> newNativeLibDirList = new ArrayList<>(origNativeLibDirs.length + 1);
            newNativeLibDirList.add(folder);
            for (File origNativeLibDir : origNativeLibDirs) {
                if (!folder.equals(origNativeLibDir)) {
                    newNativeLibDirList.add(origNativeLibDir);
                }
            }
            nativeLibDirField.set(dexPathList, newNativeLibDirList.toArray(new File[0]));
        }
    }

    private static final class V23 {

        private static void install(ClassLoader classLoader, File folder) throws Throwable {
            final Field pathListField = findField(classLoader, "pathList");
            final Object dexPathList = pathListField.get(classLoader);

            final Field nativeLibraryDirectories = findField(dexPathList, "nativeLibraryDirectories");

            List<File> origLibDirs = (List<File>) nativeLibraryDirectories.get(dexPathList);
            if (origLibDirs == null) {
                origLibDirs = new ArrayList<>(2);
            }
            final Iterator<File> libDirIt = origLibDirs.iterator();
            while (libDirIt.hasNext()) {
                final File libDir = libDirIt.next();
                if (folder.equals(libDir)) {
                    libDirIt.remove();
                    break;
                }
            }
            origLibDirs.add(0, folder);

            final Field systemNativeLibraryDirectories = findField(dexPathList, "systemNativeLibraryDirectories");
            List<File> origSystemLibDirs = (List<File>) systemNativeLibraryDirectories.get(dexPathList);
            if (origSystemLibDirs == null) {
                origSystemLibDirs = new ArrayList<>(2);
            }

            final List<File> newLibDirs = new ArrayList<>(origLibDirs.size() + origSystemLibDirs.size() + 1);
            newLibDirs.addAll(origLibDirs);
            newLibDirs.addAll(origSystemLibDirs);

            final Method makeElements = findMethod(dexPathList, "makePathElements", List.class, File.class, List.class);
            final ArrayList<IOException> suppressedExceptions = new ArrayList<>();

            final Object[] elements = (Object[]) makeElements.invoke(dexPathList, newLibDirs, null,
                suppressedExceptions);

            final Field nativeLibraryPathElements = findField(dexPathList, "nativeLibraryPathElements");
            nativeLibraryPathElements.set(dexPathList, elements);
        }
    }

    private static final class V25 {

        private static void install(ClassLoader classLoader, File folder) throws Throwable {
            final Field pathListField = findField(classLoader, "pathList");
            final Object dexPathList = pathListField.get(classLoader);

            final Field nativeLibraryDirectories = findField(dexPathList, "nativeLibraryDirectories");

            List<File> origLibDirs = (List<File>) nativeLibraryDirectories.get(dexPathList);
            if (origLibDirs == null) {
                origLibDirs = new ArrayList<>(2);
            }
            final Iterator<File> libDirIt = origLibDirs.iterator();
            while (libDirIt.hasNext()) {
                final File libDir = libDirIt.next();
                if (folder.equals(libDir)) {
                    libDirIt.remove();
                    break;
                }
            }
            origLibDirs.add(0, folder);

            final Field systemNativeLibraryDirectories = findField(dexPathList, "systemNativeLibraryDirectories");
            List<File> origSystemLibDirs = (List<File>) systemNativeLibraryDirectories.get(dexPathList);
            if (origSystemLibDirs == null) {
                origSystemLibDirs = new ArrayList<>(2);
            }

            final List<File> newLibDirs = new ArrayList<>(origLibDirs.size() + origSystemLibDirs.size() + 1);
            newLibDirs.addAll(origLibDirs);
            newLibDirs.addAll(origSystemLibDirs);

            final Method makeElements = findMethod(dexPathList, "makePathElements", List.class);

            final Object[] elements = (Object[]) makeElements.invoke(dexPathList, newLibDirs);

            final Field nativeLibraryPathElements = findField(dexPathList, "nativeLibraryPathElements");
            nativeLibraryPathElements.set(dexPathList, elements);
        }
    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name     field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static Field findField(Class<?> originClazz, String name) throws NoSuchFieldException {
        for (Class<?> clazz = originClazz; clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + originClazz);
    }

    /**
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance       an object to search the method into.
     * @param name           method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    public static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException(
            "Method " + name + " with parameters " + Arrays.asList(parameterTypes) + " not found in "
                + instance.getClass());
    }

    public static String getSupportABI() {
        if (Build.SUPPORTED_ABIS == null || Build.SUPPORTED_ABIS.length == 0) {
            return "armeabi";
        }
        return Build.SUPPORTED_ABIS[0];
    }
}