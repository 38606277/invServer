package root.report.itemCategory.controller;

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
import root.report.itemCategory.service.ItemCategoryService;
import root.report.mdmDict.service.MdmDictService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/itemCategory")
public class ItemCategoryController extends RO {

    private static Logger log = Logger.getLogger(ItemCategoryController.class);

    @Autowired
    public ItemCategoryService itemCategoryService;

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/saveItemCategory", produces = "text/plain;charset=UTF-8")
    public String saveDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String id = this.itemCategoryService.saveOrUpdateCategory(sqlSession, jsonObject);

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
            map.put("category_code",jsonFunc.getString("category_code"));
            map.put("category_name",jsonFunc.getString("category_name"));
            map.put("category_pid",jsonFunc.getString("category_pid"));
            Map<String,Object> map1 = itemCategoryService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

//    @RequestMapping(value = "/getAllPageById", produces = "text/plain;charset=UTF-8")
//    public String getAllPageByPid(@RequestBody String pJson) {
//        try {
//            JSONObject jsonFunc = JSONObject.parseObject(pJson);
//            Map<String,String> map=new HashMap();
//
//            map.put("category_id",jsonFunc.getString("category_id"));
//            Map itemCategory = itemCategoryService.getItemCategoryById(map);
//
//            List<Map> list = itemCategoryService.getItemCategorySegmentByPid(map);
//
//            List<Map> list2 = itemCategoryService.getItemCategoryAttributeByPid(map);
//
//            Map resMap = new HashMap();
//            resMap.put("dataInfo",itemCategory);
//            resMap.put("list",list);
//            resMap.put("list2",list2);
//            return SuccessMsg("", resMap);
//        } catch (Exception ex){
//            ex.printStackTrace();
//            return ExceptionMsg(ex.getMessage());
//        }
//    }

    @RequestMapping(value = "/getAllPageByIdForLine", produces = "text/plain;charset=UTF-8")
    public String getAllPageByIdForLine(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("category_id",jsonFunc.getString("category_id"));
            List<Map> map1 = itemCategoryService.getItemCategorySegmentByPid(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getAllList", produces = "text/plain;charset=UTF-8")
    public String getAllList(@RequestBody String pjson) {
        try {
            Map map = new HashMap();
            JSONObject obj=JSON.parseObject(pjson);
            map.put("category_pid",obj.get("category_pid"));
            List<Map> map1 = itemCategoryService.getItemCategoryByPid(map);
            map.put("category_id","-1");
            map.put("category_name","全部");
            map.put("key","-1");
            map.put("title","全部");
            map.put("children",map1);
            List<Map> newamp = new ArrayList<>();
            newamp.add(map);
            return SuccessMsg("", newamp);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }


     //返回数据
    @RequestMapping(value = "/getItemCategoryByID", produces = "text/plain;charset=UTF-8")
    public String getItemCategoryByID(@RequestBody String pjson) {
        JSONObject obj=JSON.parseObject(pjson);
        try{
            Map map = new HashMap();
            map.put("category_id",obj.get("category_id"));
            Map jsonObject = this.itemCategoryService.getItemCategoryById(map);
            List<Map> list = this.itemCategoryService.getItemCategorySegmentByPid(map);
            List<Map> list2 = this.itemCategoryService.getItemCategoryAttributeByPid(map);
            Map mmm=new HashMap();
            mmm.put("mainForm",jsonObject);
            mmm.put("lineForm",list);
            mmm.put("lineForm2",list2);
            return SuccessMsg("查询成功",mmm);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }


    // 从json数据当中解析 ，批量删除
    @RequestMapping(value = "/deleteItemCategoryById", produces = "text/plain;charset=UTF-8")
    public String deleteItemCategoryById(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj=JSON.parseObject(pJson);
            String ids = obj.getString("category_id");
            String[] arrId=ids.split(",");
            for(int i = 0; i < arrId.length; i++){
                //删除
                Integer count=itemCategoryService.countChildren(sqlSession,arrId[0]);
                if(count==0) {
                    this.itemCategoryService.deleteItemCategoryByID(sqlSession, arrId[0]);
                }else{
                    sqlSession.getConnection().rollback();
                    return ExceptionMsg("包含子类不可以删除");
                }
            }
            sqlSession.getConnection().commit();
            return SuccessMsg("删除成功",null);
        }catch (Exception ex){

            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


}
