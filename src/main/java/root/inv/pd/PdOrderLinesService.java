package root.inv.pd;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.sys.SysContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdOrderLinesService {

    private static Logger log = Logger.getLogger(PdOrderLinesService.class);

    /**
     * 新增或更新
     * @param sqlSession
     * @param itemId
     * @param lines
     */
    public void insetOrUpdate(SqlSession sqlSession ,String itemId,JSONArray lines){
        int userId = SysContext.getId();
        saveOrUpdate(sqlSession,userId,itemId,"-1",lines);
    }

    private void saveOrUpdate(SqlSession sqlSession ,int userId,String itemId,String parentId,JSONArray lines){
        if(lines !=null && 0 <lines.size()){
            for(int i = 0; i < lines.size(); i++){
                JSONObject jsonObject = lines.getJSONObject(i);
                String lineId = jsonObject.getString("line_id");
                if(lineId.startsWith("NEW_TEMP_ID_")){
                    jsonObject.remove("line_id");
                    jsonObject.put("line_number",i);
                    jsonObject.put("create_by",userId);
                    jsonObject.put("header_id",itemId);
                    jsonObject.put("item_pid",parentId);
                    //临时数据 执行新增
                    sqlSession.insert("pd_order_lines.savePdOrderLines",jsonObject);

                    lineId = jsonObject.getString("line_id");
                    if(jsonObject.containsKey("children")){
                        JSONArray jsonArray = jsonObject.getJSONArray("children");
                        saveOrUpdate(sqlSession,userId,itemId,lineId,jsonArray);
                    }
                }else{
                    //执行更新
                    sqlSession.update("pd_order_lines.updatePdOrderLinesById",jsonObject);
                    if(jsonObject.containsKey("children")){
                        JSONArray jsonArray = jsonObject.getJSONArray("children");
                        saveOrUpdate(sqlSession,userId,itemId,lineId,jsonArray);
                    }
                }
            }
        }
    }


    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public void insertPdOrderLinesAll(SqlSession sqlSession ,List lines){
        if(lines == null || lines.isEmpty()){
            return ;
        }
        // 保存行数据
        sqlSession.insert("pd_order_lines.savePdOrderLinesAll",lines);
    }

    /**
     * 递归返回数据
     * @param itemId
     * @return
     */
    public List<Map<String,Object>> getAllChildrenRecursionByItemId(String itemId){
        Map<String,Object> param = new HashMap<>();
        param.put("header_id",itemId);
        param.put("item_pid","-1");
        List<Map<String,Object>> lines =  getPdOrderLinesByItemId(param);
        if(lines!=null && 0 <lines.size()){
            for(Map<String,Object> child : lines){
                recursion(child);
            }
        }
        return lines;
    }

    public List<Map<String,Object>> getPdOrderLinesByItemId(String  itemId){
        Map<String,Object> param = new HashMap<>();
        param.put("header_id",itemId);
        return  DbSession.selectList("pd_order_lines.getPdOrderLinesByHeaderId",param);
    }

    public List<Map<String,Object>> getPdOrderLinesByItemId(Map<String,Object>  map){
        return  DbSession.selectList("pd_order_lines.getPdOrderLinesByHeaderId",map);
    }


    /**
     * 递归 - 层级形式
     * @param parentMap
     */
    public void recursion(Map<String,Object> parentMap){
        HashMap<String,Object> tempMap = new HashMap<>();
        tempMap.put("item_pid",parentMap.get("line_id"));
        tempMap.put("header_id",parentMap.get("header_id"));
        List<Map<String,Object>>  CategoryList =  getPdOrderLinesByItemId(tempMap);
        if(CategoryList != null && 0 < CategoryList.size()){
            parentMap.put("children",CategoryList);
            for(Map<String,Object> child : CategoryList){
                recursion(child);
            }
        }
    }


    public void deletePdOrderLines(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("pd_order_lines.deleteByIds",map);
    }

    public void updatePdLinesRcvQuantity(SqlSession sqlSession, String headerId,String itemId,double rcvQuantity){
        Map<String,Object> map = new HashMap<>();
        map.put("header_id",headerId);
        map.put("item_id",itemId);
        map.put("rcv_quantity",rcvQuantity);
        sqlSession.update("pd_order_lines.updatePdLinesRcvQuantity",map);
    }

}
