package com.shishuheng.reader.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shishuheng.reader.R;
import com.shishuheng.reader.process.ReadWordDoc;
import com.shishuheng.reader.ui.activities.FullscreenActivity;

import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.converter.core.BasicURIResolver;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument;
import org.w3c.dom.Document;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class OfficeFragment extends Fragment {
    private FullscreenActivity parentActivity;

    private String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    //读取Office文件解压的暂存目录
    private String ROOT_PATH = SDCARD+"/Reader/Office";
    private String IMG_PATH = ROOT_PATH+"/.images";
    private String WORD_PATH = ROOT_PATH+"/.word";
    private String POWERPOINT_PATH = ROOT_PATH+"/.ppt";
    private String EXCEL_PATH = ROOT_PATH+"/.excel";


    private File outFile;

    private String filePath;
    private WebView webView;

    public OfficeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (this.filePath != null) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_office, container, false);
            parentActivity = (FullscreenActivity) getActivity();
            webView = view.findViewById(R.id.officeWebView);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl("file:///android_asset/html/loading.html");

//            ReadWordDoc readWordDoc = new ReadWordDoc(filePath, WORD_PATH+"/html");
//            webView.loadUrl("file://"+WORD_PATH+"/html");
//            /*
            try {
                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ReadWordDoc readWordDoc = new ReadWordDoc(filePath, WORD_PATH+"/html");
                        //通过View的post方法切换回主线程（UI线程） 更新webView显示内容
                        webView.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl("file://"+WORD_PATH+"/html");
                            }
                        });
                    }
                });
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            */
            return view;
        } else
            return null;
    }

    public void setFile(String filePath) {
        this.filePath = filePath;
    }

    public void readFile(File file) {
        String filename = file.getName();
        String extension_name = "";
        if (filename.lastIndexOf('.') > -1) {
//            extension_name = filename.substring(filename.lastIndexOf('.'));
        }
        if (extension_name.equalsIgnoreCase(".doc") || extension_name.equalsIgnoreCase(".docx")) {
//            POIWordToHtml.wordToHtml(file.getAbsolutePath(), IMG_PATH,WORD_PATH);
        } else if (extension_name.equalsIgnoreCase(".ppt") || extension_name.equalsIgnoreCase(".pptx")) {
//            POIPptToHtml.pptToHtml(file.getAbsolutePath(), POWERPOINT_PATH);
        } else if (extension_name.equalsIgnoreCase(".xls") || extension_name.equalsIgnoreCase(".xlsx")) {
//            POIExcelToHtml.excelToHtml(file.getAbsolutePath(), EXCEL_PATH, false);
        }
    }

    //*
    public void readDocx(File file) {
        try {
            InputStream is = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(is);
            String imgDesPath = IMG_PATH;
            File imgFile = new File(imgDesPath);
            if (!imgFile.exists()) {
                imgFile.mkdirs();
            }
            XHTMLOptions options = XHTMLOptions.create().URIResolver(new BasicURIResolver(imgDesPath));
            options.setExtractor(new FileImageExtractor(imgFile));
            options.setIgnoreStylesIfUnused(false);
            options.setFragment(true);
            outFile = new File(WORD_PATH+"/docx.html");
            if (!outFile.exists())
                outFile.createNewFile();
            OutputStream os = new FileOutputStream(outFile);
            XHTMLConverter.getInstance().convert(document, os, options);
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("错误", "读取docx文件失败");
        }
    }

    //参考 http://blog.csdn.net/yjclsx/article/details/51441632
    public void readDoc(File file) {
        try {
            InputStream is = new FileInputStream(file);
            HWPFDocument document = new HWPFDocument(is);
            String imgDesPath = IMG_PATH;
            File imgFile = new File(imgDesPath);
            if (!imgFile.exists()) {
                imgFile.mkdirs();
            }
            WordToHtmlConverter converter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            converter.setPicturesManager(new PicturesManager() {
                @Override
                public String savePicture(byte[] bytes, PictureType pictureType, String s, float v, float v1) {
                    return s;
                }
            });
            converter.processDocument(document);
            List<Picture> pics = document.getPicturesTable().getAllPictures();
            if (pics != null) {
                for (int i = 0; i < pics.size(); i++) {
                    Picture pic = pics.get(i);
                    try {
                        pic.writeImageContent(new FileOutputStream(IMG_PATH+"/"+pic.suggestFullFileName()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            Document htmlFile = converter.getDocument();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DOMSource domSource = new DOMSource(htmlFile);
            StreamResult streamResult = new StreamResult(os);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer serializer = transformerFactory.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
            os.close();
            outFile = new File(WORD_PATH+"/doc.html");
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(os.toByteArray());
            fos.close();
        } catch (Exception e) {
            Log.v("错误", "读取doc文件失败");
        }
    }

    public void readXls(File file) {
        try {
            InputStream is = new FileInputStream(file);
            HSSFWorkbook document = new HSSFWorkbook(is);
            String imgDesPath = IMG_PATH;
            File imgFile = new File(imgDesPath);
            if (!imgFile.exists()) {
                imgFile.mkdirs();
            }
            ExcelToHtmlConverter converter = new ExcelToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            converter.processWorkbook(document);
            List pics = document.getAllPictures();
            if (pics != null) {
                for (int i = 0; i < pics.size(); i++) {
                    Picture pic = (Picture) pics.get(i);
                    try {
                        pic.writeImageContent(new FileOutputStream(IMG_PATH+"/"+pic.suggestFullFileName()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            Document htmlFile = converter.getDocument();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DOMSource domSource = new DOMSource(htmlFile);
            StreamResult streamResult = new StreamResult(os);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer serializer = transformerFactory.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
            os.close();
            outFile = new File(WORD_PATH+"/doc.html");
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(os.toByteArray());
            fos.close();
        } catch (Exception e) {
            Log.v("错误", "读取doc文件失败");
        }
    }

    //*/
}
