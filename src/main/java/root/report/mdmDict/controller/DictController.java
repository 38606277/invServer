package root.report.mdmDict.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.mdmDict.service.MdmDictService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/mdmDict")
public class DictController extends RO {

    private static Logger log = Logger.getLogger(DictController.class);

    @Autowired
    public MdmDictService mdmDictService;

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/saveDict", produces = "text/plain;charset=UTF-8")
    public String saveDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String id =null;
            if(jsonObject.getString("dict_type").equals("list")) {
                id = this.mdmDictService.saveOrUpdateDict(sqlSession, jsonObject);
            }else if(jsonObject.getString("dict_type").equals("tree")) {
                id = this.mdmDictService.saveOrUpdateDictTree(sqlSession, jsonObject);
            }
            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("startIndex",jsonFunc.getString("startIndex"));
            map.put("perPage",jsonFunc.getString("perPage"));
            map.put("dict_code",jsonFunc.getString("dict_code"));
            map.put("dict_name",jsonFunc.getString("dict_name"));
            Map<String,Object> map1 = mdmDictService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/getAll", produces = "text/plain;charset=UTF-8")
    public String getAll(@RequestParam(required = false) String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            List<Map> map1 = mdmDictService.getAll(jsonFunc);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

     //返回数据
    @RequestMapping(value = "/getDictByID", produces = "text/plain;charset=UTF-8")
    public String getDictByID(@RequestBody String pjson) {
        JSONObject obj=JSON.parseObject(pjson);
        try{
            Map map = new HashMap();
            map.put("dict_id",obj.get("dict_id"));
            Map jsonObject = this.mdmDictService.getDictByID(map);
            List<Map> list = this.mdmDictService.getDictValueByDictId(map);
            Map mmm=new HashMap();
            mmm.put("mainForm",jsonObject);
            mmm.put("lineForm",list);
            return SuccessMsg("查询成功",mmm);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }


    // 从json数据当中解析 ，批量删除
    @RequestMapping(value = "/deleteDictById", produces = "text/plain;charset=UTF-8")
    public String deleteDictById(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj=JSON.parseObject(pJson);
            String ids = obj.getString("dict_id");
            String[] arrId=ids.split(",");
            for(int i = 0; i < arrId.length; i++){
                //删除
                this.mdmDictService.deleteDictById(sqlSession,arrId[0]);
            }
            sqlSession.getConnection().commit();
            return SuccessMsg("删除成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


}
