package root.inv.store;

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

    public List<Map<String,Object>> getBillLinesByItemId(Map<String,Object> jsonObject){
       return  DbSession.selectList("inv_item_on_hand.getBillLinesByItemId",jsonObject);
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
}
