package org.thehealth.faceDemo.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImgUtil {

    public static int thImgMaxWidth = 410;
    public static int thImgMaxHeight = 410;

    /**
     * @param srcFile源文件
     * @param outFile输出文件
     * @param x坐标
     * @param y坐标
     * @param width宽度
     * @param height高度
     * @return
     */
    public static boolean cutPic(String srcFile, String outFile, int x, int y, int width, int height) {
        FileInputStream is = null;
        ImageInputStream iis = null;
        try {
            // 如果源图片不存在
            if (!new File(srcFile).exists()) {
                return false;
            }

            // 读取图片文件
            is = new FileInputStream(srcFile);

            // 获取文件格式
            String ext = srcFile.substring(srcFile.lastIndexOf(".") + 1);

            // ImageReader声称能够解码指定格式
            Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(ext);
            ImageReader reader = it.next();

            // 获取图片流
            iis = ImageIO.createImageInputStream(is);

            // 输入源中的图像将只按顺序读取
            reader.setInput(iis, true);

            // 描述如何对流进行解码
            ImageReadParam param = reader.getDefaultReadParam();

            // 图片裁剪区域
            Rectangle rect = new Rectangle(x, y, width, height);

            // 提供一个 BufferedImage，将其用作解码像素数据的目标
            param.setSourceRegion(rect);

            // 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象
            BufferedImage bi = reader.read(0, param);

            // 保存新图片
            File tempOutFile = new File(outFile);
            if (!tempOutFile.exists()) {
                tempOutFile.mkdirs();
            }
            ImageIO.write(bi, ext, new File(outFile));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (iis != null) {
                    iis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //创建小于1M图片文件
    public static File createLowOneMImg(String ownUploadPath, String parent, String child, int oldSize, int newSize) throws IOException {
        File f = new File(ownUploadPath, parent);
        File thf = new File(ownUploadPath, child);
        BufferedImage bi = ImageIO.read(f);
        double ratio = newSize * 1.0 / oldSize;
        System.out.println("压缩比：" + ratio);
        int width = (int)Math.floor(bi.getWidth() * ratio);
        int height = (int)Math.floor(bi.getHeight() * ratio);
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        AffineTransform atf = new AffineTransform();
        atf.setToScale(ratio, ratio);
        Graphics2D gd = temp.createGraphics();
        gd.drawImage(bi, atf, null);
        gd.dispose();
        ImageIO.write(temp, "jpg", thf);
        return thf;
    }

    //创建缩略图
    public static File createThumbnails(String ownUploadPath, String parent, String child) throws IOException {
        File f = new File(ownUploadPath, parent);
        File thf = new File(ownUploadPath, child);
        BufferedImage bi = ImageIO.read(f);
        BufferedImage temp = new BufferedImage(thImgMaxWidth, thImgMaxHeight, BufferedImage.TYPE_INT_RGB);
        double ratio;
        if ((bi.getHeight() != thImgMaxHeight) || (bi.getWidth() != thImgMaxWidth)) {
            if (bi.getHeight() > bi.getWidth())
                ratio = thImgMaxHeight * 1.0 / bi.getHeight();
            else ratio = thImgMaxWidth * 1.0 / bi.getWidth();
            System.out.println(ratio);
            AffineTransform atf = new AffineTransform();
            atf.setToScale(ratio, ratio);
            Graphics2D gd = temp.createGraphics();
            gd.drawImage(bi, atf, null);
            gd.dispose();
            ImageIO.write(temp, "jpg", thf);
        }
        return thf;
    }
}
