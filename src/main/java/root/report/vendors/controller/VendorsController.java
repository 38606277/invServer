package root.report.vendors.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.vendors.service.VendorsService;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/vendors")
public class VendorsController extends RO {

    private static Logger log = Logger.getLogger(VendorsController.class);

    @Autowired
    public VendorsService vendorsService;

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/saveVendors", produces = "text/plain;charset=UTF-8")
    public String saveDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            Map resmap = this.vendorsService.saveOrUpdateVendors(sqlSession, jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",resmap);
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
            map.put("vendor_name",jsonFunc.getString("vendor_name"));
            map.put("vendor_address",jsonFunc.getString("vendor_address"));
            map.put("vendor_link",jsonFunc.getString("vendor_link"));
            map.put("vendor_type",jsonFunc.getString("vendor_type"));
            Map<String,Object> map1 = vendorsService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

     //返回数据
    @RequestMapping(value = "/getVendorsByID", produces = "text/plain;charset=UTF-8")
    public String getVendorsByID(@RequestBody String pjson) {
        JSONObject obj=JSON.parseObject(pjson);
        try{
            Map map = new HashMap();
            map.put("vendor_id",obj.get("vendor_id"));
            Map jsonObject = this.vendorsService.getVendorsByID(map);
            return SuccessMsg("查询成功",jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }




    // 从json数据当中解析 ，批量删除
    @RequestMapping(value = "/deleteVendorsById", produces = "text/plain;charset=UTF-8")
    public String deleteVendorsById(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj=JSON.parseObject(pJson);
            String ids = obj.getString("vendor_id");
            String[] arrId=ids.split(",");
            for(int i = 0; i < arrId.length; i++){
                //删除
                this.vendorsService.deleteVendorsById(sqlSession,arrId[0]);
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

    /**
     * 获取区域信息
     *
     * @return
     */
    @RequestMapping(value = "/getArea", produces = "text/plain;charset=UTF-8")
    public String getArea(@RequestBody JSONObject pJson) throws UnsupportedEncodingException {
        String parentCode = pJson.getString("parentCode");
        String maxLevel = pJson.getString("maxLevel");
        List<Map<String, Object>> areaList = DbSession.selectList("sys_area.getAreaByParentCode", parentCode);

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Map areaData : areaList) {
            Map data = new HashMap();
            data.put("label", areaData.get("name").toString());
            data.put("value", areaData.get("code").toString());
            data.put("merger_name", areaData.get("merger_name").toString());
            data.put("level", areaData.get("level_type").toString());
            data.put("isLeaf", areaData.get("level_type").toString().equals(maxLevel));
            dataList.add(data);
        }
        return SuccessMsg("", dataList);
    }
}
