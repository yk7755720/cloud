package com.common.utils;

import com.lowagie.text.pdf.PdfReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PDF2IMAGE {



    public static void main(String[] args) {
        //pdf2Image("D:/xdemo.pdf", "D:/pdf", 300);
    }

    /***
     * PDF文件转PNG图片，全部页数
     *
     * @param PdfFilePath pdf完整路径
     * @param imgFilePath 图片存放的文件夹
     * @param dpi dpi越大转换后越清晰，相对转换速度越慢
     * @return
     */
    public static List<StringBuffer> pdf2Image(String baseProjectPath,String PdfFilePath, String dstImgFolder, int dpi) {
        File file = new File(baseProjectPath+PdfFilePath);
        PDDocument pdDocument;
        String imagePDFName="";
        List<StringBuffer> imgPath=new ArrayList<>();
        try {
            String imgPDFPath = file.getParent();
            int dot = file.getName().lastIndexOf('.');
            imagePDFName = file.getName().substring(0, dot); // 获取图片文件名
            String imgFolderPath = imgPDFPath + File.separator;// 获取图片存放的文件夹路径
            /*if (dstImgFolder.equals("")) {
                imgFolderPath = imgPDFPath + File.separator;// 获取图片存放的文件夹路径
            } else {
                imgFolderPath = dstImgFolder + File.separator;
            }*/

            if (createDirectory(imgFolderPath)) {
                pdDocument = PDDocument.load(file);
                PDFRenderer renderer = new PDFRenderer(pdDocument);
                /* dpi越大转换后越清晰，相对转换速度越慢 */
                PdfReader reader = new PdfReader(baseProjectPath+PdfFilePath);
                int pages = reader.getNumberOfPages();
                StringBuffer imgFilePath = null;
                StringBuffer imgFilePath2 = null;
                for (int i = 0; i < pages; i++) {
                    String imgFilePathPrefix = imgFolderPath  + imagePDFName;
                    imgFilePath = new StringBuffer();
                    imgFilePath.append(imgFilePathPrefix+i);
                    imgFilePath.append(".png");
                    imgFilePath2=new StringBuffer();
                    imgFilePath2.append(dstImgFolder+"/"+imagePDFName+i);
                    imgFilePath2.append(".png");
                    imgPath.add(imgFilePath2);
                    File dstFile = new File(imgFilePath.toString());
                    BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                    ImageIO.write(image, "png", dstFile);
                }
                System.out.println("PDF文档转PNG图片成功！");
            } else {
                System.out.println("PDF文档转PNG图片失败：" + "创建" + imgFolderPath + "失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgPath;
    }

    private static boolean createDirectory(String folder) {
        File dir = new File(folder);
        if (dir.exists()) {
            return true;
        } else {
            return dir.mkdirs();
        }
    }

}
