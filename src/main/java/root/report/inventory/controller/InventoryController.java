package root.report.inventory.controller;

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
import root.report.inventory.service.InventoryService;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.mdmDict.service.MdmDictService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/inventory")
public class InventoryController extends RO {

    private static Logger log = Logger.getLogger(InventoryController.class);


    @Autowired
    public ItemCategoryService itemCategoryService;

    @Autowired
    public MdmDictService mdmDictService;

    @Autowired
    public InventoryService inventoryService;

    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,Object> map=new HashMap();
            map.put("startIndex",jsonFunc.getString("startIndex"));
            map.put("perPage",jsonFunc.getString("perPage"));
            map.put("item_description",jsonFunc.getString("item_description"));
            map.put("item_category_id",jsonFunc.getString("item_category_id"));
            map.put("item_id",jsonFunc.getString("item_id"));
            map.put("org_id",jsonFunc.getString("org_id"));
            Map<String,Object> map1 = inventoryService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }



    @RequestMapping(value = "/getAllPage2", produces = "text/plain;charset=UTF-8")
    public String getAllPage2(@RequestBody JSONObject obj) {
        try {
            String categoryId = String.valueOf(obj.get("item_category_id"));
            String segment = String.valueOf(obj.get("segment"));

            Map<String,Object> map  = new HashMap<>();
            map.put("category_id",categoryId);

            Map<String,Object> segmentMap = null;
            //获取类别对应的segment
            List<Map> segmentList = itemCategoryService.getItemCategorySegmentByPId(map);

            for(Map segmentItem : segmentList){
                if(segment.equals( segmentItem.get("segment"))){
                    segmentMap = segmentItem;
                    break;
                }
            }

            //需要查询的segment值
            List<Map> dictValueList  = mdmDictService.getDictValueListByDictId(String.valueOf(segmentMap.get("dict_id")));

            //拼接语句
            //拼接需要查询的字段
            String[] segmentSqlArr = buildSegmentSql(segmentList,segment);
            String[] sumSqlArr = buildSumSql(dictValueList,segment);

            String sql = "SELECT " + segmentSqlArr[0] + "," + sumSqlArr[0] + " FROM (SELECT " + segmentSqlArr[1] + ","+sumSqlArr[1] + " FROM "
                    + " mdm_item mi "
                    + " INNER JOIN mdm_item_category ic ON mi.item_category_id = ic.category_id "
                    + " INNER JOIN inv_item_onhand iio ON mi.item_id = iio.item_id "
                    + " INNER JOIN fnd_org fo ON iio.org_id = fo.org_id "
                    + " WHERE 1 = 1 "
                    + " AND mi.item_category_id = '" + categoryId +"'"
                    + " GROUP BY "
                    + segmentSqlArr[1]
                    + ") a"
                    +" GROUP BY "
                    + segmentSqlArr[0];

            System.out.println("getItemCategoryByID2  " + sql);

            obj.put("sql",sql);

            Map<String,Object> map1 = inventoryService.getAllPage2(obj);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
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

}
