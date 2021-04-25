package root.report.itemCategory.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.itemCategory.service.ItemService;
import root.report.mdmDict.service.MdmDictService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/item")
public class ItemController extends RO {

    private static Logger log = Logger.getLogger(ItemController.class);

    @Autowired
    public ItemService itemService;

    @Autowired
    public ItemCategoryService itemCategoryService;

    @Autowired
    public MdmDictService mdmDictService;




    /**
     * 单个数据进行保存
     */
    @RequestMapping(value = "/saveItem", produces = "text/plain;charset=UTF-8")
    public String saveItem(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String id = this.itemService.saveOrUpdate(sqlSession, jsonObject);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    /**
     * 批量进行保存
     * */
    @RequestMapping(value = "/saveItemBatch", produces = "text/plain;charset=UTF-8")
    public String saveItemBatch(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String id = this.itemService.saveOrUpdateBatch(sqlSession, jsonObject);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }


    /**
     * 根据类别ID获取数据
     * */
    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("startIndex",jsonFunc.getString("startIndex"));
            map.put("perPage",jsonFunc.getString("perPage"));
            map.put("item_category_id",jsonFunc.getString("item_category_id"));
            map.put("item_description",jsonFunc.getString("item_description"));
            Map<String,Object> map1 = itemService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getItemByItemId", produces = "text/plain;charset=UTF-8")
    public String getItemByItemId(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("item_id",jsonFunc.getString("item_id"));
            Map<String,Object> map1 = itemService.getItemByItemId(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/getAllPageByCategoryId", produces = "text/plain;charset=UTF-8")
    public String getAllPageByCategoryId(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("item_category_id",jsonFunc.getString("item_category_id"));
            Map<String,Object> map1 = itemService.getAllPageByCategoryId(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/deleteItemById", produces = "text/plain;charset=UTF-8")
    public String deleteItemById(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj=JSON.parseObject(pJson);
            String ids = obj.getString("item_id");
            String[] arrId=ids.split(",");
            for(int i = 0; i < arrId.length; i++){
                //删除
                this.itemService.deleteItemByID(sqlSession, arrId[0]);
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

    @RequestMapping(value = "/batchUpdateItem", produces = "text/plain;charset=UTF-8")
    public String batchUpdateItem(@RequestBody JSONObject pJson) {
        try {
            Map<String,String> map=new HashMap();

            itemService.batchUpdateItem(pJson);
            return SuccessMsg("", "");
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    /**
     * 快速添加 获取行和列数据
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemRowAndColumn", produces = "text/plain;charset=UTF-8")
    public String getItemRowAndColumn(@RequestBody JSONObject pJson) {
        try {

            //传入类别id  主信息等

            String itemCategoryId = pJson.getString("item_category_id");

            if("-1".equals(itemCategoryId) || itemCategoryId == null){
                return ErrorMsg("2000","物料类别不存在");
            }

            //获取行和列的segment字段名
            List<Map> rowAndCol = itemCategoryService.getRowAndColumnByCategoryId(itemCategoryId);

            if(rowAndCol==null || rowAndCol.size() < 2){
                return ErrorMsg("2000","请配置行和列信息");
            }

            //拼接查询条件
            StringBuilder segmentFilter = new StringBuilder();
            segmentFilter.append(" where item_category_id = ").append("'").append(itemCategoryId).append("'");
           for(String key: pJson.keySet()){
               String value =  pJson.getString(key);
               if(key.startsWith("segment")){//只包含segment的条件
                   segmentFilter.append(" and ").append(key).append("=").append("'").append(value).append("'");
               }
           }

            Map<String,Object> resultMap = new HashMap<>();
            for(Map rc :  rowAndCol){
                String spreadMode =  String.valueOf(rc.get("spread_mode"));//作为键返回
                String segment = String.valueOf(rc.get("segment"));
                String segmentName = String.valueOf(rc.get("segment_name"));

                String selectSqlFormat = "SELECT %s from mdm_item %s GROUP BY %s";
                String selectSql = String.format(selectSqlFormat,segment,segmentFilter.toString(),segment);
                List rcList =  itemService.paramStringSql(selectSql);

                Map<String,Object> listData = new HashMap<String,Object>();
                listData.put("segment", segment);
                listData.put("segmentName", segmentName);
                listData.put("list", rcList);
                resultMap.put(spreadMode,listData);
            }

            if(resultMap.size() < 2){
                return ErrorMsg("2000","请配置行和列信息");
            }

            return SuccessMsg("", resultMap);
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }




    /**
     * 快速添加 获取行和列数据 通过segment
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemRowAndColumn2", produces = "text/plain;charset=UTF-8")
    public String getItemRowAndColumn2(@RequestBody JSONObject pJson) {
        try {

            //传入类别id  主信息等
            String itemCategoryId = pJson.getString("item_category_id");

            if("-1".equals(itemCategoryId) || itemCategoryId == null){
                return ErrorMsg("2000","物料类别不存在");
            }

            //获取行和列的segment字段名
            List<Map> rowAndCol = itemCategoryService.getRowAndColumnByCategoryId(itemCategoryId);

            if(rowAndCol==null || rowAndCol.size() < 2){
                return ErrorMsg("2000","请配置行和列信息");
            }

            Map<String,Object> resultMap = new HashMap<>();

            //获取行和列segment对应的字典值
            for(Map rc :  rowAndCol){
                String spreadMode =  String.valueOf(rc.get("spread_mode"));//作为键返回
                String segment = String.valueOf(rc.get("segment"));
                String segmentName = String.valueOf(rc.get("segment_name"));
                String segmentDictId = String.valueOf(rc.get("dict_id"));
                List rcList =  mdmDictService.getDictValueListByDictId(segmentDictId);
                Map<String,Object> listData = new HashMap<String,Object>();
                listData.put("segment", segment);
                listData.put("segmentName", segmentName);
                listData.put("list", rcList);
                resultMap.put(spreadMode,listData);
            }

            //拼接查询条件
            StringBuilder segmentFilter = new StringBuilder();
            segmentFilter.append(" where item_category_id = ").append("'").append(itemCategoryId).append("'");
            for(String key: pJson.keySet()){
                String value =  pJson.getString(key);
                if(key.startsWith("segment")){//只包含segment的条件
                    segmentFilter.append(" and ").append(key).append("=").append("'").append(value).append("'");
                }
            }
            String selectSqlFormat = "SELECT * from mdm_item %s ";
            String selectSql = String.format(selectSqlFormat,segmentFilter.toString());
            List itemList =  itemService.paramStringSql(selectSql);

            resultMap.put("itemList",itemList);

            if(itemList.size() < 1){
                return ErrorMsg("2000","请配置物料信息");
            }

            return SuccessMsg("", resultMap);
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    /**
     * 快速添加 获取行和列数据 通过sku
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemRowAndColumnBySku", produces = "text/plain;charset=UTF-8")
    public String getItemRowAndColumnBySku(@RequestBody JSONObject pJson) {
        try {

            //传入类别id  主信息等
            String itemCategoryId = pJson.getString("item_category_id");
            String sku = pJson.getString("sku");
            if("-1".equals(itemCategoryId) || itemCategoryId == null){
                return ErrorMsg("2000","物料类别不存在");
            }

            //获取行和列的segment字段名
            List<Map> rowAndCol = itemCategoryService.getRowAndColumnByCategoryId(itemCategoryId);

            if(rowAndCol==null || rowAndCol.size() < 2){
                return ErrorMsg("2000","请配置行和列信息");
            }

            Map<String,Object> resultMap = new HashMap<>();

            //获取行和列segment对应的字典值
            for(Map rc :  rowAndCol){
                String spreadMode =  String.valueOf(rc.get("spread_mode"));//作为键返回
                String segment = String.valueOf(rc.get("segment"));
                String segmentName = String.valueOf(rc.get("segment_name"));
                String segmentDictId = String.valueOf(rc.get("dict_id"));
                List rcList =  mdmDictService.getDictValueListByDictId(segmentDictId);
                Map<String,Object> listData = new HashMap<String,Object>();
                listData.put("segment", segment);
                listData.put("segmentName", segmentName);
                listData.put("list", rcList);
                resultMap.put(spreadMode,listData);
            }

            //拼接查询条件
            StringBuilder segmentFilter = new StringBuilder();
            segmentFilter.append(" where item_category_id = ").append("'").append(itemCategoryId).append("'");
            segmentFilter.append(" and sku = ").append("'").append(sku).append("'");
            String selectSqlFormat = "SELECT * from mdm_item %s ";
            String selectSql = String.format(selectSqlFormat,segmentFilter.toString());
            System.out.println(selectSql);
            List itemList =  itemService.paramStringSql(selectSql);

            resultMap.put("itemList",itemList);

            if(itemList.size() < 1){
                return ErrorMsg("2000","请配置物料信息");
            }

            return SuccessMsg("", resultMap);
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }


    /**
     * 通过segment查询item
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemPageBySegment", produces = "text/plain;charset=UTF-8")
    public String getItemPageBySegment(@RequestBody JSONObject pJson) {
        Map<String,Object> result =  itemService.getItemPageBySegment(pJson);
        return SuccessMsg("查询成功",result);
    }

    @RequestMapping(value = "/getColumnListBySKU", produces = "text/plain;charset=UTF-8")
    public String getItemBySKU(@RequestBody JSONObject pJson) {

        //获取segment 加上 attribute 减去需要展开的segment 加上展开的字典字段

        String itemCategoryId = pJson.getString("item_category_id");
        String sku = pJson.getString("sku");
        String segment = pJson.getString("segment");

        List<Map> segmentList  = itemCategoryService.getItemCategorySegmentByCatId(itemCategoryId);

        List<Map> attributeList  = itemCategoryService.getItemCategoryAttributeByCatId(itemCategoryId);

        String sqlBase = "select DISTINCT sku,item_category_id";
        String whereStr = " from mdm_item where sku like '%"+sku+"%' and item_category_id = '"+itemCategoryId+"'";


        StringBuilder stringBase = new StringBuilder();
        stringBase.append(sqlBase);

        for(Map map :segmentList){
            String seg= String.valueOf(map.get("segment"));
            if(!seg.equals(segment)){
                stringBase.append(",").append(seg);
            }

        }
        for(Map map :attributeList){
            String attr = String.valueOf(map.get("attribute"));
            if(!attr.equals(segment)){
                stringBase.append(",").append(attr);
            }
        }
        stringBase.append(whereStr);
        String sql = stringBase.toString();

        log.debug(sql);

        List<Map> result = itemService.paramStringSql(sql);


        return SuccessMsg("查询成功",result);
    }





}
