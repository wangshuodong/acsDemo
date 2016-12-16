package com.cmiot.rms.common.ftp;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FtpBusiness {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FtpBusiness.class);

    public boolean upload(File srcFile,String dir,String port,String ip,String username,String password) {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        boolean uploadResult = false;
        try {
            if(StringUtils.isBlank(port)){
                port = "21";
            }
            ftpClient.connect(ip,Integer.valueOf(port));
            ftpClient.login(username, password);
            fis = new FileInputStream(srcFile);
            //设置上传目录
//            ftpClient.changeWorkingDirectory("/");
            showServerReply(ftpClient);
            String[] dirs = dir.split("/");
            if(dirs.length > 0){
                for(int i=0;i<dirs.length;i++){
                    ftpClient.makeDirectory(dirs[i]);
                    showServerReply(ftpClient);
                    ftpClient.changeWorkingDirectory(dirs[i]);
                    showServerReply(ftpClient);
                }
            }
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("GBK");
            //设置文件类型（二进制）
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            uploadResult = ftpClient.storeFile(srcFile.getName(), fis);
        } catch (IOException e) {
            logger.info(exceptionInfo (e));
            throw new RuntimeException("FTP客户端出错！", e);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                logger.info(exceptionInfo (e));
            }
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                logger.info(exceptionInfo (e));
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
        //上传成功后删除文件
        String path = srcFile.getAbsolutePath().toString();
        this.deleteFiles(path);
        return  uploadResult;
    }

    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                logger.info("SERVER: " + aReply);
            }
        }
    }

    public static void deleteFiles(String path){
        File file = new File(path);
        //1級文件刪除
        if(!file.isDirectory()){
            file.delete();
        }else if(file.isDirectory()){
            //2級文件列表
            String []filelist = file.list();
            //獲取新的二級路徑
            for(int j=0;j<filelist.length;j++){
                File filessFile= new File(path+"\\"+filelist[j]);
                if(!filessFile.isDirectory()){
                    filessFile.delete();
                }else if(filessFile.isDirectory()){
                    //遞歸調用
                    deleteFiles(path+"\\"+filelist[j]);
                }
            }
            file.delete();
        }
    }

    public File downloadFromUrl(String url,String dir){
        File f = null;
        try {
            URL httpurl = new URL(url);
            String fileName = getFileNameFromUrl(url);
            logger.info(fileName);
            f = new File(dir + fileName);

            logger.info(f.toString());
            FileInputStream fis = new FileInputStream(f);
            logger.info(fis.toString());
            fis.close();
            logger.info(" newFileName " + f);
            FileUtils.copyURLToFile(httpurl, f);

        } catch (Exception e) {
            logger.info(exceptionInfo (e));
            return f;
        }
        return f;
    }

    public String getFileNameFromUrl(String url){
        String name = new Long(System.currentTimeMillis()).toString() + ".X";
        int index = url.lastIndexOf("/");
        if(index > 0){
            name = url.substring(index + 1);
            if(name.trim().length()>0){
                return name;
            }
        }
        return name;
    }

    public static boolean isExist(String filePath) {
        logger.info("ing .......");
        String paths[] = filePath.split("\\\\");
        String dir = paths[0];
        for (int i = 0; i < paths.length - 2; i++) {
            try {
                dir = dir + File.separator + paths[i + 1];
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                    logger.info("create dir : " + dir);
                }else{
                    logger.info(" dir : " + dir + " exist ?");
                }
                dirFile.setWritable(true, false);
            } catch (Exception err) {
                logger.info("ELS - Chart : create dir exception");
                logger.info(exceptionInfo(err));
            }
        }
        File fp = new File(filePath);

        if(!fp.exists()){
            logger.info("end ....... true");
            return true;
        }else{
            logger.info("end ....... false");
            return false;
        }
    }


    /**
     * 是否创建目录
     * @param path
     * @return
     */
    public boolean isexitsPath(String path)throws InterruptedException{
        String [] paths=path.split("\\\\");
        StringBuffer fullPath=new StringBuffer();
        for (int i = 0; i < paths.length; i++) {
            fullPath.append(paths[i]).append("\\\\");
            File file=new File(fullPath.toString());
            if(paths.length-1!=i){
                if(!file.exists()){
                    file.mkdir();
                    logger.info("创建目录为："+fullPath.toString());
                    Thread.sleep(1500);
                }
            }
        }
        File file=new File(fullPath.toString());
        logger.info(file.toString());
        if (!file.exists()) {
            return true;
        }else{
            return false;
        }
    }


    public String sendTofileServer(String url, String filePath) throws IOException {
        logger.info(" ftp Server Name " + url);
        logger.info(" filePath " + filePath);
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return "false";
        }
        /**
         * 第一部分
         */
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        /**
         * 设置关键值
         */
        con.setRequestMethod("POST"); // 以Post方式提交表单，默认get方式
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false); // post方式不能使用缓存

        // 设置请求头信息
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        // 设置边界
        String BOUNDARY = "----------" + System.currentTimeMillis();
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                + BOUNDARY);

        // 请求正文信息
        // 第一部分：
        StringBuilder sb = new StringBuilder();
        sb.append("--"); // ////////必须多两道线
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data;name=\"file\";filename=\""
                + file.getName() + "\"\r\n");
        sb.append("Content-Type:application/octet-stream\r\n\r\n");
        byte[] head = sb.toString().getBytes("utf-8");
        // 获得输出流
        OutputStream out = new DataOutputStream(con.getOutputStream());
        out.write(head);
        // 文件正文部分
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes = 0;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();
        // 结尾部分
        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
        out.write(foot);
        out.flush();
        out.close();
        /**
         * 读取服务器响应，必须读取,否则提交不成功
         */
        /**
         * 下面的方式读取也是可以的
         */
        String line = null;
        String lineData = "";

        try {
            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = reader.readLine()) != null) {
                lineData += line;
                logger.info(" line " + line);
            }
        } catch (Exception e) {
            logger.info("发送POST请求出现异常！" + e);
            logger.info("send POST req Exception" + e);
            e.printStackTrace();
        }
        String ftpFile ="";
        JSONObject jsonObject = JSONObject.parseObject(lineData) ;
        for(java.util.Map.Entry<String,Object> entry : jsonObject.entrySet()){
            if(entry.getKey().equals("ftpurl")){
                ftpFile = entry.getValue().toString();
                logger.info(entry.getValue().toString());
            }
        }
        return ftpFile;
    }

    FTPClient ftp = null;
    public boolean connect(String path, String addr, int port, String username, String password) {
        try {
            ftp = new FTPHTTPClient(addr, port, username, password);
            ftp = new FTPClient();
            int reply;
            ftp.connect(addr);
            logger.info("连接到：" + addr + ":" + port);
            logger.info(ftp.getReplyString());
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                logger.info("FTP目标服务器积极拒绝.");
                return false;
            }else{
                ftp.login(username, password);
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftp.changeWorkingDirectory(path);
                logger.info("已连接：" + addr + ":" + port);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return false;
        }
    }


    public void disconnect(){
        try {
            ftp.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws IOException {
        FtpBusiness up = new FtpBusiness();
        System.out.println(up.sendTofileServer("http://172.19.10.8:8080/up?ftp=true",
                "e:\\uploadFile\\importExcel.xls"));
    }
    public static String exceptionInfo(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString().toLowerCase();
    }
}