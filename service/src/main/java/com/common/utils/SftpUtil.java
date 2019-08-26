/**
 * 
 */
package com.common.utils;


import com.jcraft.jsch.*;
import com.common.freamwork.BusinessException;
import com.common.freamwork.NoResourceException;
import com.common.freamwork.SystemException;
import com.enity.FileResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * @author Administrator
 *
 */
@Component
@PropertySource(value = "classpath:config/${spring.profiles.active}/ftp.properties")
public class SftpUtil {
	/**
	 * SFTP 登录用户名
	 */
	@Value("${ftp.username}")
	private String username;
	/**
	 * SFTP 登录密码
	 */
	@Value("${ftp.password}")
	private String password;
	/**
	 * 私钥
	 */
	private String privateKey;
	/**
	 * SFTP 服务器地址IP地址
	 */
	@Value("${ftp.host}")
	private String host;
	/**
	 * SFTP 端口
	 */
	@Value("${sftp.port}")
	private int port;

	@Value("${ftp.basePath}")
	private String basePath;
	
	@Value("${ftp.projectPath}")
    private String projectPath;
	
	/**
	 * 连接sftp服务器
	 */
	public ChannelSftp login() {
		ChannelSftp sftp = null;
		Channel channel = null;
		Session session = null;
		try {
			JSch jsch = new JSch();
			if (privateKey != null) {
				jsch.addIdentity(privateKey);// 设置私钥
			}
	
			session = jsch.getSession(username, host, port);
	
			if (password != null) {
				session.setPassword(password);
			}
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
	
			session.setConfig(config);
			session.connect();
	
			channel = session.openChannel("sftp");
			channel.connect();
	
			sftp = (ChannelSftp) channel;
			return sftp;
		}
		catch (JSchException e) {
			throw new SystemException("sftp登录失败");
		}
	}

	/**
	 * 关闭连接 server
	 */
	public void logout(ChannelSftp sftp) {
		try {
			if (sftp != null) {
				Session session = sftp.getSession();
				sftp.quit();
				
				if (session != null) {
					if (session.isConnected()) {
						session.disconnect();
					}
				}
			}
		}
		catch (JSchException e) {
			throw new SystemException("sftp注销失败");
		}
		finally {
			if (null != sftp) {
				sftp.quit(); 
			}
		}
	}

	public List<FileResult> uploadFile(MultipartFile[] files, String filePath) {
        List<FileResult> fileResult = new ArrayList<FileResult>();
        String baseProjectPath = basePath + projectPath;
        ChannelSftp sftp = login();
        try {
        	try {
        		sftp.cd(baseProjectPath);
        	} catch (SftpException e) {
    			throw new SystemException("根目录不存在，需要创建");
    		}
        	
        	try {
    			sftp.cd(filePath);
    		} catch (SftpException e) {
    			// 目录不存在，则创建文件夹
    			String[] dirs = filePath.split("/");
    			for (String dir : dirs) {
    				if (null == dir || "".equals(dir)){
    					continue;
    				}
    				try {
    					sftp.cd(dir);
    				} catch (SftpException ex) {
    					sftp.mkdir(dir);
    					sftp.cd(dir);
    				}
    			}
    		}
        	
            for (MultipartFile file : files) {
                if (saveFile(file, sftp)) {
                    fileResult.add(new FileResult().setPath(projectPath + filePath + file.getOriginalFilename())
                            .setName(file.getOriginalFilename()).setSize(String.valueOf(file.getSize())));
                }
            }
            logout(sftp);
        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        } finally {
        	try {
    			if (sftp != null) {
    				Session session = sftp.getSession();
    				sftp.quit();
    				
    				if (session != null) {
    					if (session.isConnected()) {
    						session.disconnect();
    					}
    				}
    			}
    		}
    		catch (JSchException e) {
    			throw new SystemException("sftp注销失败");
    		}
    		finally {
    			if (null != sftp) {
    				sftp.quit(); 
    			}
    		}
        }
        return fileResult;
    }
	
	private boolean saveFile(MultipartFile file, ChannelSftp sftp) {
        boolean success = false;
        InputStream inStream = null;
        try {
            inStream = file.getInputStream();
            sftp.put(inStream, new String(file.getOriginalFilename().getBytes("UTF-8")));
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
		ChannelSftp sftp = login();
        try {
            int index = filePath.lastIndexOf("/");
            //获取文件的路径
            String path = filePath.substring(0, index);
            //获取文件名
            String name = filePath.substring(index + 1);
            //判断是否存在目录
            try {
        		sftp.cd(basePath + path);
        	} catch (SftpException e) {
    			throw new SystemException("文件路径不存在：" + basePath + path);
    		}
            
            InputStream in = sftp.get(name);
            response.setHeader("Content-Disposition", "attachment;fileName=" + new String(name.getBytes(), "ISO-8859-1"));
            response.setContentType("text/html;charset=UTF-8");
            response.setContentType("application/octet-stream");
            //response.setContentType("application/x-download");
            OutputStream out = response.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            int length = 0;
            byte[] buf = new byte[1024];
            while ((length = bis.read(buf)) > 0) {
            	bos.write(buf, 0, length);
            }
            bos.close();
            bis.close();
            out.close();
            logout(sftp);
        } catch (Exception e) {
            throw new NoResourceException(e.toString());
        } finally {
        	try {
    			if (sftp != null) {
    				Session session = sftp.getSession();
    				sftp.quit();
    				
    				if (session != null) {
    					if (session.isConnected()) {
    						session.disconnect();
    					}
    				}
    			}
    		}
    		catch (JSchException e) {
    			throw new SystemException("sftp注销失败");
    		}
    		finally {
    			if (null != sftp) {
    				sftp.quit(); 
    			}
    		}
        }
    }

	/**
	 * 删除文件
	 *
	 * @param directory  要删除文件所在目录
	 * @param deleteFile 要删除的文件
	 */
	public void delete(String directory, String deleteFile) throws SftpException {
		String baseProjectPath = basePath + projectPath;
		ChannelSftp sftp = login();
		sftp.cd(baseProjectPath+directory);
		sftp.rm(deleteFile);
	}

	/**
	 * 删除文件夹
	 *
	 * @param directory  要删除文件所在目录
	 *
	 */
	public void deletedir(String directory) throws SftpException {
		String baseProjectPath = basePath + projectPath;
		ChannelSftp sftp = login();
		sftp.cd(baseProjectPath+directory);
		sftp.rm("*");
		String[] split = directory.split("/");
		sftp.cd("..");
		sftp.rmdir(split[split.length-1]);
	}

	public boolean validFile(String filename) {
		ChannelSftp sftp = login();
		try {
			int index = filename.lastIndexOf("/");
			//获取文件的路径
			String path = filename.substring(0, index);
			//获取文件名
			String name = filename.substring(index + 1);
			//判断是否存在目录
			try {
				sftp.cd(basePath + path);
				Vector vector = sftp.ls(basePath + path);
				for(Object obj :vector){
					if(obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry){
						String fileName = ((com.jcraft.jsch.ChannelSftp.LsEntry)obj).getFilename();
						//判断是否有目标文件
						if (fileName.equals(name)) {
							return true;
						}
					}
				}
			} catch (SftpException e) {
				throw new BusinessException("文件路径不存在：" + basePath + path);
			}

			logout(sftp);
			return false;
		}
		catch (Exception e) {
			throw new BusinessException("sftp失败");
		}
		finally {
			if (null != sftp) {
				sftp.quit();
			}
		}
	}

	/**
	 * 列出目录下的文件
	 *
	 * @param directory 要列出的目录
	 * @param
	 */
	public Vector<?> listFiles(String directory) throws SftpException {
		ChannelSftp sftp = login();
		return sftp.ls(directory);
	}

	public String getBasePath() {
		return basePath;
	}



	public String getProjectPath() {
		return projectPath;
	}


}
