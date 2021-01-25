package root.inv.onhand;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存量表
 */
@Service
public class InvItemOnHandService {

    /**
     * 批量存量表
     * @param lines
     * @return
     */
    public boolean saveItemOnHandAll(SqlSession sqlSession ,List<Map<String,Object>> lines){
        if(lines == null || lines.isEmpty()){
            return true;
        }
        // 保存行数据
        int number = sqlSession.insert("inv_item_on_hand.saveItemOnHandAll",lines);
        return  0 < number;
    }

    /**
     * 保存 存量表
     * @param sqlSession
     * @param params
     */
    public void saveItemOnHand(SqlSession sqlSession, Map<String,Object> params){
            sqlSession.insert("inv_item_on_hand.saveItemOnHand",params);
    }


    /**
     * 更新 存量表
     * @param sqlSession
     * @param params
     */
    public void updateItemOnHand(SqlSession sqlSession, Map<String,Object> params){
            sqlSession.update("inv_item_on_hand.updateItemOnHand",params);
    }

    /**
     * 批量保存或更新 存量表
     * @param sqlSession
     * @param lines
     */
    public void saveOrUpdateItemOnHandList(SqlSession sqlSession, JSONArray lines){
        for(int i =0 ; i < lines.size();i++){
            JSONObject jsonObject = lines.getJSONObject(i);
            String lineId = jsonObject.getString("line_id");
            if(lineId.startsWith("NEW_TEMP_ID_")){
                //临时数据 执行新增
                sqlSession.insert("inv_item_on_hand.saveItemOnHand",jsonObject);
            }else{
                //执行更新
                sqlSession.update("inv_item_on_hand.updateItemOnHand",jsonObject);
            }
        }
    }

    /**
     *  查询列表
     * @param map
     * @return
     */
    public List<Map<String,Object>> getItemOnHandByPage(Map<String,Object> map){
        return  DbSession.selectList("inv_item_on_hand.getItemOnHandByPage",map);
    }

    /**
     * 查询数量
     * @param map
     * @return
     */
    public long getItemOnHandByPageCount(Map<String,Object> map){
        return  DbSession.selectOne("inv_item_on_hand.getItemOnHandByPageCount",map);
    }



    /**
     *  查下存量，并按仓库分组
     * @param map
     * @return
     */
    public List<Map<String,Object>> getItemOnHandOrgByPage(Map<String,Object> map){
        return  DbSession.selectList("inv_item_on_hand.getItemOnHandOrgByPage",map);
    }

    /**
     * 查询存量数量，并按仓库分组
     * @param map
     * @return
     */
    public long getItemOnHandOrgByPageCount(Map<String,Object> map){
        return  DbSession.selectOne("inv_item_on_hand.getItemOnHandOrgByPageCount",map);
    }

    /**
     * 查询存量
     * @param map
     * @return
     */
    public Map<String,Object> getItemOnHandByParams(Map<String,Object> map){
        return  DbSession.selectOne("inv_item_on_hand.getItemOnHandByParams",map);
    }

}
