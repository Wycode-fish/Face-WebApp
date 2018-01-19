package org.thehealth.faceDemo.controller;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.thehealth.faceDemo.util.ImgUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

@Controller
public class MainController {

    @Autowired
    private HttpServletRequest request;


    private int maxPostSize = 1 * 1024 * 1024;
    private int fixMarginPercent = 50;

    private final String api_key = "......"; // private
    private final String api_secret = "......";

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

    @RequestMapping("/uploadFile")
    public
    @ResponseBody
    String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("pointType") String pointType) throws IOException {
        String message = null;
        if (!file.isEmpty()) {
            //创建文件路径
            String path = this.getClass().getResource("/upload").getPath();
            //写入磁盘
            String name = file.getOriginalFilename();
            File parentFile = new File(path + File.separator + name);
            file.transferTo(parentFile);
            String fileName = name;
            if (parentFile.exists() && parentFile.isFile()) {
                //判断文件大小是否超过1M
                FileInputStream fis = new FileInputStream(parentFile);
                if (fis.available() > maxPostSize){//超过1M压缩图片
                    System.out.println("原始文件大小：" + fis.available());
                    File childFile = ImgUtil.createLowOneMImg(path, name, "Low1M_" + name, fis.available(), maxPostSize);
                    if (childFile.exists())
                        fileName = "Low1M_" + name;

                }
            }
            //获取可识别文件路径
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + fileName;
            System.out.println("可识别文件路径：" + url);
            //裁剪大脸图片
            String identifyFilePath = path + File.separator + fileName;
            String faceFilePath = path + File.separator + "Face_" + name;
            if(getFaceFile(url, identifyFilePath, faceFilePath)){
                File faceFile = new File(faceFilePath);
                if (faceFile.exists() && faceFile.isFile()) {
                    //判断文件大小是否超过1M
                    FileInputStream fis = new FileInputStream(faceFile);
                    if (fis.available() > maxPostSize){//超过1M压缩图片
                        System.out.println("大脸图片文件大小：" + fis.available());
                        File childFile = ImgUtil.createLowOneMImg(path, faceFile.getName(), "Low1M_" + faceFile.getName(), fis.available(), maxPostSize);
                        if (childFile.exists())
                            fileName = "Low1M_" + faceFile.getName();
                    }
                    else
                        fileName = faceFile.getName();
                    //生成前端固定尺寸展示图
                    ImgUtil.createThumbnails(path, faceFile.getName(), "SL_" + name);
                }
            }
            //获取可识别的大脸文件路径
            url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + fileName;
            System.out.println("可识别的大脸文件路径：" + url);
            if ("5p".equals(pointType))
                message = getLandmarkOnFivePoint(url);
            else
                message = getLandmark(url, pointType);
            System.out.println(message);
        }
        return message;
    }

    @RequestMapping("/getLandmarkPoints")
    public
    @ResponseBody
    String getLandmarkPoints(HttpServletRequest request, HttpServletResponse response) {
        String message = null;
        String imgUrl = request.getParameter("imgUrl");
        String pointType = request.getParameter("pointType");
        if ("5p".equals(pointType))
            message = getLandmarkOnFivePoint(imgUrl);
        else
            message = getLandmark(imgUrl, pointType);
        return message;
    }

    @RequestMapping("/getHistoryImageLandmarkPoints")
    public
    @ResponseBody
    String getHistoryImageLandmarkPoints(HttpServletRequest request, HttpServletResponse response){
        String message = null;
        String fileName = request.getParameter("fileName");
        String pointType = request.getParameter("pointType");
        String path = this.getClass().getResource("/upload").getPath();
        String faceFilePath = path + File.separator + fileName;
        File faceFile = new File(faceFilePath);
        String url = null;
        if (faceFile.exists() && faceFile.isFile()) {
            //判断文件大小是否超过1M
            try {
                FileInputStream fis = new FileInputStream(faceFile);
                if (fis.available() > maxPostSize){//超过1M压缩图片
                    File childFile = ImgUtil.createLowOneMImg(path, faceFile.getName(), "Low1M_" + faceFile.getName(), fis.available(), maxPostSize);
                    if (childFile.exists())
                        fileName = "Low1M_" + faceFile.getName();
                }
                else
                    fileName = faceFile.getName();
                //获取可识别的大脸文件路径
                url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + fileName;
                System.out.println("可识别的大脸文件路径：" + url);
                if ("5p".equals(pointType))
                    message = getLandmarkOnFivePoint(url);
                else
                    message = getLandmark(url, pointType);
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        System.out.println(message);

        return message;
    }

    private String getLandmarkOnFivePoint(String url) {
        HttpRequests httpRequests = new HttpRequests(api_key, api_secret, true, true);
        JSONObject result = null;
        try {
            //detection/detect
//            result = httpRequests.detectionDetect(new PostParameters().setUrl("http://cn.faceplusplus.com/wp-content/themes/faceplusplus/assets/img/demo/2.jpg"));
            System.out.println("\ndetection/detect");
            result = httpRequests.detectionDetect(new PostParameters().setUrl(url));
            System.out.println(result);

        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        return getFormatResultOnFivePoint(result);
    }

    //人脸识别
    private String getLandmark(String url, String pointType) {
        HttpRequests httpRequests = new HttpRequests(api_key, api_secret, true, true);
        String faceId = null;
        JSONObject result = null;
        try {
            //detection/detect
//            result = httpRequests.detectionDetect(new PostParameters().setUrl("http://cn.faceplusplus.com/wp-content/themes/faceplusplus/assets/img/demo/2.jpg"));
            System.out.println("\ndetection/detect");
            result = httpRequests.detectionDetect(new PostParameters().setUrl(url));
            System.out.println(result);
            faceId = result.getJSONArray("face").getJSONObject(0).getString("face_id");

            //detection/landmark
            System.out.println("\ndetection/landmark");
            result = httpRequests.detectionLandmark(new PostParameters().setFaceId(faceId).setType(pointType));
            System.out.println(result);

        } catch (FaceppParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getFormatResult(result);
    }

    private String getFormatResult(JSONObject result) {
        String formatResult = null;
        StringBuffer sb = new StringBuffer();
        try {
            JSONArray faces = result.getJSONArray("result");
            for (int i = 0; i < faces.length(); i++) {
                JSONObject face = faces.getJSONObject(i);
                JSONObject landmark = face.getJSONObject("landmark");
                Set<String> keySet = landmark.keySet();
                for (String key : keySet) {
                    sb.append(landmark.get(key).toString());
                    sb.append(",");
                }
                String temp = sb.substring(0, sb.length() - 1);
                formatResult = "{\"facePoints\":[" + temp + "]},";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "{\"faces\":[" + formatResult.substring(0, formatResult.length() - 1) + "]}";
    }

    private String getFormatResultOnFivePoint(JSONObject result) {
        String formatResult = null;
        StringBuffer sb = new StringBuffer();
        try {
            JSONArray faces = result.getJSONArray("face");
            for (int i = 0; i < faces.length(); i++) {
                JSONObject face = faces.getJSONObject(i);
                JSONObject position = face.getJSONObject("position");
                Set<String> keySet = position.keySet();
                for (String key : keySet) {
                    boolean flag = key.equals("center") || key.equals("width") || key.equals("height");
                    if (!flag){
                        sb.append(position.get(key).toString());
                        sb.append(",");
                    }
                }
                String temp = sb.substring(0, sb.length() - 1);
                formatResult = "{\"facePoints\":[" + temp + "]},";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "{\"faces\":[" + formatResult.substring(0, formatResult.length() - 1) + "]}";
    }



    //在原图上裁剪大脸图
    private boolean getFaceFile(String url, String parentFilePath, String faceFilePath) throws IOException {
        boolean flag = false;
        try {
            //识别人脸
            HttpRequests httpRequests = new HttpRequests(api_key, api_secret, true, true);
            JSONObject result = null;
            //detection/detect
//            result = httpRequests.detectionDetect(new PostParameters().setUrl("http://cn.faceplusplus.com/wp-content/themes/faceplusplus/assets/img/demo/2.jpg"));
            result = httpRequests.detectionDetect(new PostParameters().setUrl(url));
            System.out.println("detection/detect:" + result);
            //获取原始图片大小
            int img_width = result.getInt("img_width");
            int img_height = result.getInt("img_height");
            JSONArray faces = result.getJSONArray("face");
            for (int i = 0; i < faces.length(); i++) {
                JSONObject face = faces.getJSONObject(i);
                JSONObject position = face.getJSONObject("position");
                double widthPercent =  position.getDouble("width");
                double heightPercent = position.getDouble("height");
                JSONObject centerPointPercent = position.getJSONObject("center");
                //获取脸长宽
                double faceWidth = img_width * widthPercent / 100;
                double faceHeight = img_height * heightPercent / 100;
                //获取中心点坐标
                double centerX = centerPointPercent.getDouble("x") * img_width / 100;
                double centerY = centerPointPercent.getDouble("y") * img_height / 100;
                int x;
                int y;
                int picWidth;
                int fixMargin = (int)Math.floor(faceHeight * fixMarginPercent / 100);
                if (faceWidth >= faceHeight){
                    x = (int)Math.floor(centerX - faceWidth/2 - fixMargin);
                    y = (int)Math.floor(centerY - faceWidth/2 - fixMargin);
                    picWidth = (int)Math.floor(faceWidth + 2*fixMargin);
                } else {
                    x = (int)Math.floor(centerX - faceHeight/2 - fixMargin);
                    y = (int)Math.floor(centerY - faceHeight/2 - fixMargin);
                    picWidth = (int)Math.floor(faceHeight + 2*fixMargin);
                }
                //图片特殊情况处理
                if (x < 0)
                    x = 0;
                if (y < 0)
                    y = 0;
                if (picWidth > img_height || picWidth > img_width){
                    if (img_height > img_width)
                        picWidth = img_width;
                    else
                        picWidth = img_height;
                }
                flag = ImgUtil.cutPic(parentFilePath, faceFilePath, x, y, picWidth, picWidth);
            }

        } catch (FaceppParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return flag;
    }
}
