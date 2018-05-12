package com.shishuheng.reader.process;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.shishuheng.reader.datastructure.LineAndBytesCount;
import com.shishuheng.reader.datastructure.TxtDetail;
import com.shishuheng.reader.ui.activities.FullscreenActivity;
import com.shishuheng.reader.ui.activities.MainActivity;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Created by shishuheng on 2018/1/8.
 */

public class Book extends Thread {
    //文件路径
    private File filePath;
    //一行展示的字数
    private int lineCharacterNumber;
    //总共可显示的行数
    private int lineTotalNumber;
    //处理后的文本
    private ArrayList<String> book;
    //上次读到的点
    private long readPointer = 0;
    //随机读取
    RandomAccessFile raf;
    //整个屏幕字数
    public ArrayList<String> bookFullScreen;
    //读到的行数
    public int position = 0;
    //记录上一次翻页操作 1:nextPage 2:lastPage 0:unknown
    private int pagging = 1;
    //编码方式1.GBK 2.UTF-8
    private int codingFormat = 1;
    //文本框的长高
    private int height = 0;
    private int width = 0;
    //相同字号下 英文字母与汉字的比例
    private float mRate = 0.46f;
    //书籍总字数
    private long totality = 0;
    //MainActivity
    private FullscreenActivity fullscreenActivity = null;

    public Book(Activity activity, File path, int lineTotalNumber, int lineCharacterNumber, long readPointer, long totality, int height, int width) {
        try {
            this.lineCharacterNumber = lineCharacterNumber;
            this.lineTotalNumber = lineTotalNumber;
            filePath = path;
            this.width = width;
            this.height = height;
            this.totality = totality;
            bookFullScreen = new ArrayList<>();
            book = new ArrayList<>();
            raf = new RandomAccessFile(filePath, "r");
            setReadPointer(readPointer);
            fullscreenActivity = (FullscreenActivity) activity;
            countTotality();
        } catch (Exception e) {
            bookFullScreen.add("读取文件失败!");
        }
    }

    @Override
    public void run() {
//        processBook();
    }

    public ArrayList<String> nextPage() {
        return readByte(readPointer);
    }

    public ArrayList<String> lastPage() {
        if (readPointer < 860 && readPointer > 0) {
            ArrayList<String> res = readByte(0);
            readPointer = 0;
            return res;
        }
        return reverseReadByte(readPointer);
    }

    //通过byte读取文件
    public ArrayList<String> readByte(long pointer) {
        readPointer = pointer;
        bookFullScreen.clear();
        try {
            //每行字数
            float countC = 0;
            //行数
            int lineC = 0;
            //读取的byte数
            int byteC = 0;
            StringBuilder sb = new StringBuilder("");
            byte[] bytes;
            if (codingFormat == 4)
                bytes = new byte[3];
            else
                bytes = new byte[2];
            raf.seek(pointer);

            while (lineC < lineTotalNumber) {
                if (raf.read(bytes) != -1) {
                    if (countC >= lineCharacterNumber) {
                        bookFullScreen.add(sb.toString());
                        sb = new StringBuilder("");
                        countC = 0;
                        lineC++;
                        raf.seek(raf.getFilePointer() - 2);
                    } else {
                        if (bytes[0] == (byte) '\r' && bytes[1] == (byte) '\n') {
                            raf.read(bytes);
                            if (bytes[0] == (byte) '\r' && bytes[1] == (byte) '\n') {
                                byteC += 2;
                            } else {
                                raf.seek(raf.getFilePointer() - 2);
                            }
                            sb.append('\n');
                            byteC += 2;
                            countC = 0;
                            lineC++;
                            bookFullScreen.add(sb.toString());
                            sb = new StringBuilder();
                            if (codingFormat == 4)
                                raf.seek(raf.getFilePointer() - 1);
                        } else if (bytes[0] == (byte) '\n') {
                            sb.append('\n');
                            byteC++;
                            lineC++;
                            countC = 0;
                            if (codingFormat == 4)
                                raf.seek(raf.getFilePointer() - 2);
                            else
                                raf.seek(raf.getFilePointer() - 1);
                            bookFullScreen.add(sb.toString());
                            sb = new StringBuilder();
                        } else if (bytes[0] >= 0 && bytes[0] < 128) {
                            sb.append((char) bytes[0]);
                            byteC++;
                            countC += mRate;
                            if (codingFormat == 4)
                                raf.seek(raf.getFilePointer() - 2);
                            else
                                raf.seek(raf.getFilePointer() - 1);
                        } else {
                            if (codingFormat == 1) {
                                sb.append(new String(bytes, "GBK"));
                                byteC += 2;
                            } else if (codingFormat == 4) {
                                sb.append(new String(bytes, "UTF-8"));
                                byteC += 3;
                            } else if (codingFormat == 0) {
                                sb.append(new String(bytes));
                                byteC += 2;
                            } else if (codingFormat == 2) {
                                sb.append(new String(bytes, "GB2312"));
                                byteC += 2;
                            } else if (codingFormat == 3) {
                                sb.append(new String(bytes, "GB18030"));
                                byteC += 2;
                            }
                            countC++;
                        }
                    }
                } else {
                    bookFullScreen.add(sb.toString());
                    break;
                }
            }
            readPointer += byteC;
            bookFullScreen.add(readPointer + "");
            return bookFullScreen;
        } catch (Exception e) {
            return null;
        }
    }

    //通过byte读取文件
    public ArrayList<String> reverseReadByte(long pointer) {
        bookFullScreen.clear();
        try {
            //每行字数
            float countC = 0;
            //行数
            int lineC = 0;
            //读取的byte数
            int byteC = 0;
            StringBuilder sb = new StringBuilder("");
            byte[] bytes;
            if (codingFormat == 4)
                bytes = new byte[3];
            else
                bytes = new byte[2];
            long position = 0;
            while (lineC < lineTotalNumber) {
                position = pointer - byteC - 2;
                if (position >= 0) {
                    raf.seek(position);
                    if (countC < lineCharacterNumber) {
                        raf.read(bytes);
                        if (bytes[0] == (byte) '\r' && bytes[1] == (byte) '\n') {
                            sb.append('\n');
                            byteC += 2;
                            countC = 0;
                            lineC++;
                            bookFullScreen.add(0, sb.toString());
                            sb = new StringBuilder();
                        } else if (bytes[0] == (byte) '\n') {
                            sb.append('\n');
                            byteC++;
                            lineC++;
                            countC = 0;
                            raf.seek(raf.getFilePointer() - 1);
                            bookFullScreen.add(0, sb.toString());
                            sb = new StringBuilder();
                        } //else if (bytes[0] == (byte)' ' || bytes[0] == (byte)'`' || bytes[0] == (byte)'~' || bytes[0] == (byte)'1' || bytes[0] == (byte)'2' || bytes[0] == (byte)'3' || bytes[0] == (byte)'4' || bytes[0] == (byte)'5' || bytes[0] == (byte)'7' || bytes[0] == (byte)'8' || bytes[0] == (byte)'9' || bytes[0] == (byte)'0' || bytes[0] == (byte)'!' || bytes[0] == (byte)'@' || bytes[0] == (byte)'#' || bytes[0] == (byte)'$' || bytes[0] == (byte)'%' || bytes[0] == (byte)'^' || bytes[0] == (byte)'&' || bytes[0] == (byte)'*' || bytes[0] == (byte)'(' || bytes[0] == (byte)')' || bytes[0] == (byte)'-' || bytes[0] == (byte)'_' || bytes[0] == (byte)'=' || bytes[0] == (byte)'+' || bytes[0] == (byte)'[' || bytes[0] == (byte)'{' || bytes[0] == (byte)']' || bytes[0] == (byte)'}' || bytes[0] == (byte)'|' || bytes[0] == (byte)'\\' || bytes[0] == (byte)';' || bytes[0] == (byte)':' || bytes[0] == (byte)'\'' || bytes[0] == (byte)'"' || bytes[0] == (byte)'<' || bytes[0] == (byte)',' || bytes[0] == (byte)'>' || bytes[0] == (byte)'.' || bytes[0] == (byte)'?' || bytes[0] == (byte)'/') {
                        else if (bytes[1] >= 0 && bytes[1] < 128) {
                            sb.append((char) bytes[1]);
                            byteC++;
                            countC += mRate;
                            raf.seek(raf.getFilePointer() - 1);
                        } else {
                            if (codingFormat == 1)
                                sb.append(new String(bytes, "GBK"));
                            else if (codingFormat == 4)
                                sb.append(new String(bytes, "UTF-8"));
                            else if (codingFormat == 0)
                                sb.append(new String(bytes));
                            else if (codingFormat == 2)
                                sb.append(new String(bytes, "GB2312"));
                            else if (codingFormat == 3)
                                sb.append(new String(bytes, "GB18030"));
                            byteC += 2;
                            countC++;
                        }
                    } else {
                        lineC++;
                        bookFullScreen.add(0, sb.reverse().toString());
                        sb = new StringBuilder();
                        countC = 0;
                    }
                } else {
                    bookFullScreen.add(1, sb.reverse().toString());
                    break;
                }
            }
            readPointer -= (byteC + 2);
            bookFullScreen.add(readPointer + "");
            return bookFullScreen;
        } catch (Exception e) {
            return null;
        }
    }

    //统计上一页的字数
    public synchronized long lastPageCount(long pointer) {
        try {
            //记录readPoint
            long readPointerBack = readPointer;
            //每行字数
            float countC = 0;
            //行数
            int lineC = 0;
            //读取的byte数
            int byteC = 0;
            byte[] bytes = new byte[2];
            long position = 0;
            boolean rc = false;
            while (lineC < lineTotalNumber) {
//                if (lineC == lineTotalNumber-1)
//                    Log.v("byte",byteC+"");
                position = pointer - byteC;
                if (position >= 0) {
                    raf.seek(position);
                    if (countC < lineCharacterNumber) {
                        raf.read(bytes);
                        if (bytes[0] == (byte) '\r' && bytes[1] == (byte) '\n') {
                            if (rc == false) {
                                byteC += 2;
                                rc = true;
                            } else {
                                byteC += 2;
                                countC = 0;
                                lineC++;
                                rc = false;
                            }
                            if (codingFormat == 4)
                                raf.seek(raf.getFilePointer() - 1);
                        } else if (bytes[0] == (byte) '\n') {
                            byteC++;
                            lineC++;
                            countC = 0;
                            if (codingFormat == 4)
                                raf.seek(raf.getFilePointer() - 2);
                            else
                                raf.seek(raf.getFilePointer() - 1);
                        } else if (bytes[1] >= 0 && bytes[1] < 128) {
                            byteC++;
                            countC += mRate;
                            if (codingFormat == 4)
                                raf.seek(raf.getFilePointer() - 2);
                            else
                                raf.seek(raf.getFilePointer() - 1);
                        } else {
                            if (codingFormat == 4)
                                byteC += 3;
                            else
                                byteC += 2;
                            countC++;
                        }
                    } else {
                        lineC++;
                        countC = 0;
                    }
                } else {
                    break;
                }
            }
//            readPointer = readPointerBack;
            Log.v("字数和", byteC + "");
            Log.v("记录值", readPointer + "");
//            readPointer -= (byteC);
//            bookFullScreen.add(readPointer+"");
            long result = readPointerBack - byteC + 2;
            Log.v("返回值", "" + result);
            return result;
        } catch (Exception e) {
            return -1;
        }
    }

    public void countTotality() {
        if (fullscreenActivity != null) {
            try {
                if (totality <= 0) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
//                                long s = 0;
//                                FileReader fr = new FileReader(filePath);
//                                BufferedReader br = new BufferedReader(fr);

                                FileInputStream in = new FileInputStream(filePath);
//                                int size = in.available();

//                                while (br.read() != -1) {
//                                    s++;
//                                }
                                totality = in.available();
                                Utilities.updateData(fullscreenActivity, Utilities.TABLE_BOOKS, -1, filePath.getAbsolutePath(), "totality", totality);

                                Looper.prepare();
                                Toast.makeText(fullscreenActivity, "书籍总字数获取完成", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    ArrayList<String> suitableText(String str, int hanziByte) {
        ArrayList<String> arrayList = new ArrayList<>();
        String text = str;
        int lineNum = lineTotalNumber;
        int lineCharNum = lineCharacterNumber;
        int count = 0;
        for (int i = 0; i < lineNum; i++) {
            if (text.length() == 0 || text == null || text.equals(""))
                break;
            int j = 0;
            float linCharCount = 0;
            int lineByteCount = 0;
            while (linCharCount < lineCharNum && j < lineCharNum && j < text.length()) {
                if (text.charAt(j) >= 40 && text.charAt(j) <= 176) {
                    linCharCount += mRate;
                    lineByteCount++;
                } else if (text.charAt(j) == '\n' || text.charAt(j) == '\r' || text.charAt(j) == '\t' || text.charAt(j) == '\b' || text.charAt(j) == '\f') {
                    lineByteCount++;
                } else {
                    lineByteCount += hanziByte;
                    linCharCount++;
                }
                j++;
            }
            String line = text.substring(0, j);
            arrayList.add(line);
            text = text.substring(j);
            count += lineByteCount;
        }
        //arrayList的最后一个元素存储总共读取的byte数
        arrayList.add(count+"");
        bookFullScreen = arrayList;
        return arrayList;
    }
    */

    public long getReadPointer() {
        return readPointer;
    }

    public void setReadPointer(long readPointer) {
        this.readPointer = readPointer;
    }

    public void setCodingFormat(int codingFormat) {
        this.codingFormat = codingFormat;
    }

    public int getCodingFormat() {
        return codingFormat;
    }

    public void setLineTotalNumber(int lineTotalNumber) {
        this.lineTotalNumber = lineTotalNumber;
    }

    public int getLineTotalNumber() {
        return lineTotalNumber;
    }

    public void setLineCharacterNumber(int lineCharacterNumber) {
        this.lineCharacterNumber = lineCharacterNumber;
    }

    public int getLineCharacterNumber() {
        return lineCharacterNumber;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public long getTotality() {
        return totality;
    }

    public void setTotality(long totality) {
        this.totality = totality;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    public File getFilePath() {
        return filePath;
    }

    public void sychronizationToDetail(TxtDetail detail) {
        detail.setHasReadPointer(readPointer);
        detail.setCodingFormat(codingFormat);
        detail.setName(filePath.getName().replace(".txt", ""));
        detail.setPath(filePath.getAbsolutePath());
        detail.setTotality(totality);
    }
}
