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
import root.report.util.ArrayUtils;
import root.report.util.JsonUtil;

import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/reportServer/itemCategory")
public class ItemCategoryController extends RO {

    private static Logger log = Logger.getLogger(ItemCategoryController.class);

    @Autowired
    public ItemCategoryService itemCategoryService;

    @Autowired
    public MdmDictService mdmDictService;

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

    //返回数据
    @RequestMapping(value = "/getItemCategoryBatchByID", produces = "text/plain;charset=UTF-8")
    public String getItemCategoryBatchByID(@RequestBody String pjson) {
        JSONObject obj=JSON.parseObject(pjson);
        try{
            Map map = new HashMap();
            map.put("category_id",obj.get("category_id"));
            Map jsonObject = this.itemCategoryService.getItemCategoryById(map);
            map.put("qualifier","mkey");
            List<Map> listmkey = this.itemCategoryService.getItemCategorySegmentBatchByPId(map);
            map.put("qualifier","skey");
            List<Map> listskey = this.itemCategoryService.getItemCategorySegmentBatchByPId(map);
            List<Map> list2 = this.itemCategoryService.getItemCategoryAttributeByPid(map);
            Map mmm=new HashMap();
            mmm.put("mainForm",jsonObject);
            mmm.put("listmkey",listmkey);
            mmm.put("listskey",listskey);
            mmm.put("lineForm2",list2);
            return SuccessMsg("查询成功",mmm);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }


    /**
     * 获取列，传入需要横排的segment
     * @param obj
     * @return
     */
    @RequestMapping(value = "/getItemCategoryById2", produces = "text/plain;charset=UTF-8")
    public String getItemCategoryById2(@RequestBody JSONObject obj) {
        String categoryId = String.valueOf(obj.get("category_id"));
        String segment = String.valueOf(obj.get("segment"));
        List<Map<String,Object>> columnList = itemCategoryService.getItemCategoryById2(categoryId,segment);
        return SuccessMsg("查询成功",columnList);
    }

    /**
     *
     * @param segmentList segment列表
     * @param filterSegment 需要过滤的segment
     * @return
     */
    private String[] buildSegmentSql( List<Map> segmentList ,String filterSegment){
        StringBuilder stringBuilder  = new StringBuilder();//
        for(Map map:segmentList){
            String segmentName = String.valueOf(map.get("segment"));
            if(!segmentName.equals(filterSegment)){//过滤掉横向的segment
               String sql = "a." + segmentName;
                stringBuilder.append(sql).append(",");
            }
        }
        if(0 < stringBuilder.length()){
            stringBuilder.delete(stringBuilder.length()-1,stringBuilder.length());
        }

        String outSql = stringBuilder.toString();
        String inSql = outSql.replace("a.","mi.");

        return new String[]{outSql,inSql};
    }


    /**
     * 构建segment的sum
     * @param dictValueList
     * @param segment
     * @return
     */
    private String[] buildSumSql(List<Map> dictValueList , String segment){
        StringBuilder stringBuilderOut  = new StringBuilder();//外层sum
        StringBuilder stringBuilderIn  = new StringBuilder();//内存sum

        for(Map map:dictValueList){
            String valueName = String.valueOf(map.get("value_name"));
            String sqlOut = "sum( a."+valueName+" ) as "+valueName;
            stringBuilderOut.append(sqlOut).append(",");
            String sqlIn = "sum(( CASE mi."+segment+" WHEN '"+valueName+"' THEN iio.on_hand_quantity ELSE 0 END)) as "+ valueName;
            stringBuilderIn.append(sqlIn).append(",");
        }

        //删除最后的逗号
        if(0 < stringBuilderIn.length()){
            stringBuilderOut.delete(stringBuilderOut.length()-1,stringBuilderOut.length());
            stringBuilderIn.delete(stringBuilderIn.length()-1,stringBuilderIn.length());
        }


        return new String[]{stringBuilderOut.toString(),stringBuilderIn.toString()};

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

    /**
     * 分解数据
     * */
    @RequestMapping(value = "/decomposingDataList", produces = "text/plain;charset=UTF-8")
    public String decomposingDataList(@RequestBody JSONObject pJson) {
        ArrayList<List<Object>> list3 = new ArrayList<>();
        JSONObject oldJson=pJson.getJSONObject("arrId");
        if(null!=oldJson && !"".equals(oldJson)) {
            for (Map.Entry<String, Object> entry : oldJson.entrySet()) {
                JSONArray arrayjson = (JSONArray) entry.getValue();
                List<Object> templist = new ArrayList<>();
                for (int i = 0; i < arrayjson.size(); i++) {
                    if (null != arrayjson.get(i) && !"".equals(arrayjson.get(i))) {
                        templist.add(arrayjson.get(i));
                    }
                }
                list3.add(templist);
            }

//        String[] arr=pJson.getString("arrId").trim().replaceAll("，","。").replaceAll("；",";").split(";");
//        for(int i=0;i<arr.length;i++){
//            if(null!=arr[i] && !"".equals(arr[i])) {
//                list3.add(Arrays.asList(arr[i].split("。")));
//            }
//        }

            List<JSONObject> lists = new ArrayList<>();
            if (!list3.isEmpty()) {
                List<List<Object>> descartesList = ArrayUtils.getDescartes(list3);
                // descartesList.forEach(System.out::println);
                for (List<Object> lo : descartesList) {
                    JSONObject jsonThree = new JSONObject();
                    for (Object o : lo) {
                        JSONObject newb = JSONObject.parseObject(o.toString());
                        jsonThree.putAll(newb);
                    }
                    lists.add(jsonThree);
                }
                System.err.println(lists);
                return SuccessMsg("分解成功", lists);
            }
        }
        return null;
    }

    /**
     * 根据pid获取类别
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemCategoryByPid", produces = "text/plain;charset=UTF-8")
    public String getItemCategoryByPid(@RequestBody JSONObject pJson) {
        String categoryId =  String.valueOf(pJson.get("category_pid"));
        List list = itemCategoryService.getItemCategoryByPid(categoryId);
        return SuccessMsg("", list);
    }



    /**
     * 获取列，传入类别id和需要横排的segment
     * @param obj
     * @return
     */
    @RequestMapping(value = "/getColumnListByCategoryId", produces = "text/plain;charset=UTF-8")
    public String getColumnListByCategoryId(@RequestBody JSONObject obj) {
        String categoryId = String.valueOf(obj.get("category_id"));
        String segment = String.valueOf(obj.get("segment"));

        Map<String,Object> map  = new HashMap<>();
        map.put("category_id",categoryId);

        //获取类别对应的segment
        List<Map> segmentList = itemCategoryService.getItemCategorySegmentByPId(map);
        List<Map<String,Object>> columnList = new ArrayList<>();

        //生成列结构
        for(Map segmentItem : segmentList){
            Map<String,Object> columnMap = new HashMap<>();
            columnMap.put("title",String.valueOf(segmentItem.get("segment_name")));
            columnMap.put("dataIndex",String.valueOf(segmentItem.get("segment")));

            if(segment.equals(segmentItem.get("segment"))){
                //需要查询的segment值
                List<Map> dictValueList  = mdmDictService.getDictValueListByDictId(String.valueOf(segmentItem.get("dict_id")));

                List<Map<String,Object>> childrenColumnList = new ArrayList<>();
                for(Map dictValueMap : dictValueList){
                    Map<String,Object> childrenColumnMap = new HashMap<>();
                    childrenColumnMap.put("title",String.valueOf(dictValueMap.get("value_name")));
                    childrenColumnMap.put("dataIndex",String.valueOf(dictValueMap.get("value_name")));
                    childrenColumnList.add(childrenColumnMap);
                }
                columnMap.put("children",childrenColumnList);
            }
            columnList.add(columnMap);
        }
        try{

            return SuccessMsg("查询成功",columnList);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    /**
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getMKeySegmentByCategoryId", produces = "text/plain;charset=UTF-8")
    public String getMKeySegmentByCategoryId(@RequestBody JSONObject pJson) {
        String categoryId =  String.valueOf(pJson.get("item_category_id"));
        List list = itemCategoryService.getMKeySegmentByCategoryId(categoryId);
        return SuccessMsg("", list);
    }



}
