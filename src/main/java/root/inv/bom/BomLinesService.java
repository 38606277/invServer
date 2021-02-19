package root.inv.bom;

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
public class BomLinesService {

    private static Logger log = Logger.getLogger(BomLinesService.class);

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
                    jsonObject.put("item_id",itemId);
                    jsonObject.put("material_pid",parentId);

                    boolean hasChildren  = jsonObject.containsKey("children");
                    jsonObject.put("isLeaf",hasChildren?0:1);

                    //临时数据 执行新增
                    sqlSession.insert("bom_lines.saveBomLines",jsonObject);
                    lineId = jsonObject.getString("line_id");
                    if(hasChildren){
                        JSONArray jsonArray = jsonObject.getJSONArray("children");
                        saveOrUpdate(sqlSession,userId,itemId,lineId,jsonArray);
                    }
                }else{

                    boolean hasChildren  = jsonObject.containsKey("children");
                    jsonObject.put("isLeaf",hasChildren?0:1);

                    //执行更新
                    sqlSession.update("bom_lines.updateBomLinesById",jsonObject);
                    if(hasChildren){
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
    public void insertBomLinesAll(SqlSession sqlSession ,List lines){
        if(lines == null || lines.isEmpty()){
            return ;
        }
        // 保存行数据
        sqlSession.insert("bom_lines.saveBomLinesAll",lines);
    }

    /**
     * 递归返回数据
     * @param itemId
     * @return
     */
    public List<Map<String,Object>> getAllChildrenRecursionByItemId(String itemId){
        Map<String,Object> param = new HashMap<>();
        param.put("item_id",itemId);
        param.put("material_pid","-1");
        List<Map<String,Object>> lines =  getBomLinesByItemId(param);
        if(lines!=null && 0 <lines.size()){
            for(Map<String,Object> child : lines){
                recursion(child);
            }
        }
        return lines;
    }

    public List<Map<String,Object>> getBomLinesByItemId(Map<String,Object>  map){
        return  DbSession.selectList("bom_lines.getBomLinesByItemId",map);
    }
    public List<Map<String,Object>> getBomLinesLeafByItemId(Map<String,Object>  map){
        return  DbSession.selectList("bom_lines.getBomLinesLeafByItemId",map);
    }

    /**
     * 递归 - 层级形式
     * @param parentMap
     */
    public void recursion(Map<String,Object> parentMap){
        HashMap<String,Object> tempMap = new HashMap<>();
        tempMap.put("material_pid",parentMap.get("line_id"));
        tempMap.put("item_id",parentMap.get("item_id"));
        List<Map<String,Object>>  CategoryList =  getBomLinesByItemId(tempMap);
        if(CategoryList != null && 0 < CategoryList.size()){
            parentMap.put("children",CategoryList);
            for(Map<String,Object> child : CategoryList){
                recursion(child);
            }
        }
    }




    public void deleteBomLinesByHeaderIds(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("bom_lines.deleteByHeaderIds",map);
    }

    public void deleteBomLines(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("bom_lines.deleteByIds",map);
    }

}
