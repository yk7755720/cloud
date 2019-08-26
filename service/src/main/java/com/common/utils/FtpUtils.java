package com.common.utils;


import com.github.pagehelper.util.StringUtil;
import com.common.freamwork.BusinessException;
import com.common.freamwork.NoResourceException;
import com.common.freamwork.SystemException;
import com.enity.FileResult;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: mapengfei
 * @Date: 2018/5/19
 * @Description:
 */
@Component
@PropertySource(value = "classpath:config/${spring.profiles.active}/ftp.properties")
public class FtpUtils {

    @Value("${ftp.host}")
    private String host;
    @Value("${ftp.port}")
    private int port;
    @Value("${ftp.username}")
    private String username;
    @Value("${ftp.password}")
    private String password;
    @Value("${ftp.basePath}")
    private String basePath;
    @Value("${ftp.projectPath}")
    private String projectPath;

    public List<FileResult> uploadFile(MultipartFile[] files, String filePath) {
        List<FileResult> fileResult = new ArrayList<FileResult>();
        FTPClient ftp = new FTPClient();
        String baseProjectPath = basePath + projectPath;
        try {
            int reply;
            ftp.connect(host, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)){
                throw new SystemException("FTP服务器拒绝连接");
            }
            if (!ftp.changeWorkingDirectory(baseProjectPath)) {
                throw new SystemException("根目录不存在，需要创建");
            }
            // 切换到上传目录
            if (!ftp.changeWorkingDirectory(baseProjectPath + filePath)) {
                // 如果目录不存在创建目录
                String[] dirs = filePath.split("/");
                String tempPath = baseProjectPath;
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)){
                        continue;
                    }
                    tempPath += "/" + dir;
                    if (!ftp.changeWorkingDirectory(tempPath)) {
                        if (!ftp.makeDirectory(tempPath)) {
                            throw new SystemException("创建文件目录失败");
                        }
                        ftp.changeWorkingDirectory(tempPath);
                    }
                }
            }
            // 设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            ftp.setControlEncoding("UTF-8");
            for (MultipartFile file : files) {
                if (saveFile(file, ftp)) {
                    fileResult.add(new FileResult().setPath(projectPath + filePath + file.getOriginalFilename())
                            .setName(file.getOriginalFilename()).setSize(String.valueOf(file.getSize())));
                }
            }
            ftp.logout();
        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return fileResult;
    }

    public  void mkdirFromUrl(String path) {
        FTPClient ftp = new FTPClient();
        try {
            if (!"".equals(path)) {

                int reply;
                ftp.connect(host, port);// 连接FTP服务器
                // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
                ftp.login(username, password);// 登录
                reply = ftp.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                }
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                if (!ftp.changeWorkingDirectory(basePath + projectPath)) {
                    throw new SystemException("根目录不存在，需要创建");
                }
                // 切换到上传目录
                if (!ftp.changeWorkingDirectory(path)) {
                    String[] dirs = path.split("/");
                    String tempPath = basePath + projectPath;
                    for (String dir : dirs) {
                        if (null == dir || "".equals(dir)){
                            continue;
                        }
                        tempPath += "/" + dir;
                        if (!ftp.changeWorkingDirectory(tempPath)) {
                            if (!ftp.makeDirectory(tempPath)) {
                                throw new SystemException("创建文件目录失败");
                            }
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
                ftp.logout();
                if (ftp.isConnected()) {
                    ftp.disconnect();
                }
            }
        } catch (IOException e) {
            throw new BusinessException("创建dir失败");
        }
    }

    private boolean saveFile(MultipartFile file, FTPClient ftp) {
        boolean success = false;
        InputStream inStream = null;
        try {
            inStream = file.getInputStream();
            // 上传文件
            if (!ftp.storeFile(new String(file.getOriginalFilename().getBytes("UTF-8"), "iso-8859-1"), inStream)) {
                throw new SystemException("上传文件失败");
            }
            success = true;
        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public void downloadFile(String filePath, HttpServletResponse response) {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host, port);
            ftp.login(username, password);
            //设置文件编码格式
            ftp.setControlEncoding("UTF-8");
            //ftp通信有两种模式
            //PORT(主动模式)客户端开通一个新端口(>1024)并通过这个端口发送命令或传输数据,期间服务端只使用他开通的一个端口，例如21
            //PASV(被动模式)客户端向服务端发送一个PASV命令，服务端开启一个新端口(>1024),并使用这个端口与客户端的21端口传输数据
            //由于客户端不可控，防火墙等原因，所以需要由服务端开启端口，需要设置被动模式
            ftp.enterLocalPassiveMode();
            //设置传输方式为流方式
            ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            //获取状态码，判断是否连接成功
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new RuntimeException("FTP服务器拒绝连接");
            }

            int index = filePath.lastIndexOf("/");
            //获取文件的路径
            String path = filePath.substring(0, index);
            //获取文件名
            String name = filePath.substring(index + 1);
            //判断是否存在目录
            if (!ftp.changeWorkingDirectory(basePath + path)) {
                throw new RuntimeException("文件路径不存在：" + basePath + path);
            }
            //获取该目录所有文件
            FTPFile[] files = ftp.listFiles();
            for (FTPFile file : files) {
                //判断是否有目标文件
                if (file.getName().equals(name)) {
                    response.setHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes(), "ISO-8859-1"));
                    response.setContentType("text/html;charset=UTF-8");
                    InputStream in = ftp.retrieveFileStream(new String(file.getName().getBytes(), "ISO-8859-1"));
                    OutputStream out = response.getOutputStream();
                    response.setContentType("application/octet-stream");
                    response.setContentLengthLong(file.getSize());
                    int b = -1;
                    byte[] buffer = new byte[1024];
                    while ((b = in.read(buffer)) != -1) {
                        out.write(buffer, 0, b);
                    }
                    in.close();
                    ftp.completePendingCommand();
                    out.flush();
                    out.close();
                    break;
                }
            }
            ftp.logout();
        } catch (Exception e) {
            throw new NoResourceException(e.toString());
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean downloadFile(String filename, String localPath) {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host, port);
            ftp.login(username, password);
            //设置文件编码格式
            ftp.setControlEncoding("UTF-8");
            //ftp通信有两种模式
            //PORT(主动模式)客户端开通一个新端口(>1024)并通过这个端口发送命令或传输数据,期间服务端只使用他开通的一个端口，例如21
            //PASV(被动模式)客户端向服务端发送一个PASV命令，服务端开启一个新端口(>1024),并使用这个端口与客户端的21端口传输数据
            //由于客户端不可控，防火墙等原因，所以需要由服务端开启端口，需要设置被动模式
            ftp.enterLocalPassiveMode();
            //设置传输方式为流方式
            ftp.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            //获取状态码，判断是否连接成功
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new RuntimeException("FTP服务器拒绝连接");
            }

            int index = filename.lastIndexOf("/");
            //获取文件的路径
            String path = filename.substring(0, index);
            //获取文件名
            String name = filename.substring(index + 1);
            //判断是否存在目录
            if (!ftp.changeWorkingDirectory(basePath + path)) {
                throw new RuntimeException("文件路径不存在：" + basePath + path);
            }
            //获取该目录所有文件
            FTPFile[] files = ftp.listFiles();
            for (FTPFile file : files) {
                //判断是否有目标文件
                //System.out.println("文件名"+file.getName()+"---"+name);
                if (file.getName().equals(name)) {
                    //System.out.println("找到文件");
                    //如果找到，将目标文件复制到本地
                    File localFile = new File(localPath + "/" + file.getName());
                    OutputStream out = new FileOutputStream(localFile);
                    ftp.retrieveFile(file.getName(), out);
                    out.close();
                }
            }
            ftp.logout();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean deleteFile(String filename) {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host, port);
            ftp.login(username, password);
            //设置编码格式
            ftp.setControlEncoding("UTF-8");
            ftp.enterLocalPassiveMode();
            //获取状态码，判断是否连接成功
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new SystemException("FTP服务器拒绝连接");
            }
            int index = filename.lastIndexOf("/");
            //获取文件的路径
            String path = filename.substring(0, index);
            //获取文件名
            String name = filename.substring(index + 1);
            //判断是否存在目录,不存在则说明文件存在
            if (!ftp.changeWorkingDirectory(basePath + path)) {
                return true;
            }
            if (ftp.deleteFile(name)) {
                clearDirectory(ftp, basePath, path);
                ftp.logout();
                return true;
            }
            ftp.logout();
            return false;
        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new SystemException(e.getMessage());
                }
            }
        }
    }

    private void clearDirectory(FTPClient ftp, String basePath, String path) throws IOException {
        //如果路径长度小于2，说明到顶了
        if (path.length() < 2) {
            return;
        }
        //如果当前目录文件数目小于1则删除此目录
        if (ftp.listNames(basePath + path).length < 1) {
            //删除目录
            System.out.println("删除目录：" + basePath + path);
            ftp.removeDirectory(basePath + path);
            int index = path.lastIndexOf("/");
            //路径向上一层
            path = path.substring(0, index);
            //继续判断
            clearDirectory(ftp, basePath, path);
        }
    }

    public static String getFileSizeDescription(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("#######.###");
        bytes.append(format.format((size / (1024.0))));
        return bytes.toString();
    }

    public boolean validFile(String filename) {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host, port);
            ftp.login(username, password);
            //设置编码格式
            ftp.setControlEncoding("UTF-8");
            ftp.enterLocalPassiveMode();
            //获取状态码，判断是否连接成功
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new SystemException("FTP服务器拒绝连接");
            }
            int index = filename.lastIndexOf("/");
            //获取文件的路径
            String path = filename.substring(0, index);
            //获取文件名
            String name = filename.substring(index + 1);
            //判断是否存在目录,不存在则说明文件存在
            if (!ftp.changeWorkingDirectory(basePath + path)) {
                return false;
            }
            //获取该目录所有文件
            FTPFile[] files = ftp.listFiles();
            for (FTPFile file : files) {
                //判断是否有目标文件
                if (file.getName().equals(name)) {
                    return true;
                }
            }
            ftp.logout();
            return false;
        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new SystemException(e.getMessage());
                }
            }
        }
    }

    public String getFilePath(String filePath) {
    	String baseProjectPath = projectPath;
    	String[] dirs = filePath.split("/");
        for (String dir : dirs) {
            if (null == dir || "".equals(dir)){
                continue;
            }
            baseProjectPath += "/" + dir;
        }
        return baseProjectPath;
    }
    
    public boolean uploadFile(String originFileName,String filePath,InputStream input){
        boolean success = false;
        FTPClient ftp = new FTPClient();
        String baseProjectPath = basePath + projectPath;
        try {
            int reply;
            ftp.connect(host, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (!ftp.changeWorkingDirectory(baseProjectPath)) {
                throw new SystemException("根目录不存在，需要创建");
            }
            // 切换到上传目录
            if (!ftp.changeWorkingDirectory(baseProjectPath + filePath)) {
            	 String[] dirs = filePath.split("/");
                 String tempPath = baseProjectPath;
                 for (String dir : dirs) {
                     if (null == dir || "".equals(dir)){
                         continue;
                     }
                     tempPath += "/" + dir;
                     if (!ftp.changeWorkingDirectory(tempPath)) {
 						if (!ftp.makeDirectory(tempPath)) {
 							throw new SystemException("创建文件目录失败");
 						}
 						ftp.changeWorkingDirectory(tempPath);
 					}
            }
            }
            ftp.setControlEncoding("UTF-8");
            ftp.storeFile(originFileName,input);
            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("上传失败");
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

    public String getUploadPath(String filePath) {
    	if(StringUtil.isNotEmpty(filePath) && filePath.contains(projectPath)) {
    		String[] dirs = filePath.split("/");
    		String tempPath = "";
			for(int i=0;i<dirs.length;i++) {
				if (null == dirs[i] || "".equals(dirs[i]) || projectPath.contains(dirs[i])){
                    continue;
				}
                tempPath += "/" + dirs[i];
			}
			filePath = tempPath;
    	}
    	return filePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getProjectPath() {
        return projectPath;
    }

}
