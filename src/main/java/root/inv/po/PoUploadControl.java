package root.inv.po;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.attoparser.util.TextUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import root.report.common.RO;
import root.report.util.FileUtils;
import root.report.util.StringUtil;
import root.report.util.UUIDUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * 采购文件上传
 */
@RestController
@RequestMapping(value = "/reportServer/poUpload")
public class PoUploadControl extends RO {

    @Value("${fileDirPath}")
    private String fileDirPath;

    @RequestMapping(value="/uploadFile",produces = "text/plain;charset=UTF-8")
    public String uploadFile(@RequestParam("file") MultipartFile file)  {

        if (file.isEmpty()) {
            return ErrorMsg("2000","上传失败，文件不能为空");
        }

        String originalFileName = file.getOriginalFilename();
        if(originalFileName == null || StringUtil.isBlank(originalFileName)){
            return ErrorMsg("2000","上传失败，未知文件");
        }

        String newFileName =  FileUtils.getFileName() + FileUtils.getFileNameSub(originalFileName);

        JSONObject jsonObject = new JSONObject();
        File dest = new File(fileDirPath + newFileName);
        try {
            if(!dest.exists()){
               boolean isMkdirs =  dest.mkdirs();
                if(!isMkdirs){
                    return ErrorMsg("2000","上传失败，文件夹创建失败");
                }
            }

            //保存文件
            //使用此方法保存必须要绝对路径且文件夹必须已存在,否则报错
            file.transferTo(dest);

            jsonObject.put("resultCode", "1000");
            jsonObject.put("message", "上传成功");
            jsonObject.put("data", new HashMap<String, String>(){{
                put("fileName", newFileName);
            }});
            System.out.println("文件路径:"  + dest.getPath());
        } catch (IOException e) {
            jsonObject.put("resultCode","2000");
            jsonObject.put("message","上传失败");
        }
        return JSON.toJSONString(jsonObject);
    }


    //文件下载相关代码
    @RequestMapping(value = "/downloadFile", produces = "text/plain;charset=UTF-8")
    public String downloadAssetImg(String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(fileName == null || fileName.length() == 0){
            System.err.println("downloadAssetImg fileName is empty");
            return null;
        }

        if(fileDirPath == null || fileDirPath.length() ==0){
            System.err.println("downloadAssetImg fileDirPath is empty");
            return null;
        }

        String fileUrl = fileDirPath + fileName;
        File file = new File(fileUrl);

        //判断文件是否存在
        if(file.exists()){
            //当前是从该工程的WEB-INF//File//下获取文件(该目录可以在下面一行代码配置)然后下载到C:\\users\\downloads即本机的默认下载的目录
           /* String realPath = request.getServletContdownLoadAssetImgext().getRealPath(
                    "//WEB-INF//");*/
            /*File file = new File(realPath, fileName);*/

            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment;fileName=" +   URLEncoder.encode(fileName,"UTF-8"));
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                System.out.println("success");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{
            //return ErrorMsg("2000","文件不存在");
        }
        return null;
    }


}
