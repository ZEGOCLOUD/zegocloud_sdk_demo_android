package com.zegocloud.demo.bestpractice.internal.utils;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ZegoUtil {


    
    /**
     * 拷贝文件
     * @param context
     * @param assetsFilePath
     * @param targetFileFullPath
     */
    public static void copyFileFromAssets(Context context, String assetsFilePath, String targetFileFullPath) {

        try {
            if(assetsFilePath.endsWith(File.separator))
            {
                assetsFilePath = assetsFilePath.substring(0,assetsFilePath.length()-1);
            }
            String fileNames[] = context.getAssets().list(assetsFilePath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {
                File file = new File(targetFileFullPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    copyFileFromAssets(context, assetsFilePath + File.separator + fileName, targetFileFullPath + File.separator + fileName);
                }
            } else {//如果是文件

                File file = new File(targetFileFullPath);
//                File fileTemp = new File(targetFileFullPath+".temp");
//                if(file.exists())
//                {
//                    Log.d("Tag","文件存在");
//                    return;
//                }
                file.getParentFile().mkdir();

                InputStream is = context.getAssets().open(assetsFilePath);

                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();

//                fileTemp.renameTo(file);
            }

        } catch (Exception e) {
            Log.d("Tag", "copyFileFromAssets " + "IOException-" + e.getMessage());
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
