package root.report.sales.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行数据
 */
@Service
public class WholeSaleLineService {

    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public boolean insertBillLinesAll(SqlSession sqlSession ,List lines){
        if(lines == null || lines.isEmpty()){
            return true;
        }
        // 保存行数据
        int number = sqlSession.insert("whole_sale_lines.saveBillLinesAll",lines);
        return  0 < number;
    }

    public List<Map<String,Object>> getBillLinesByHeaderId(String headerId){
        Map<String,Object> param = new HashMap<>();
        param.put("header_id",headerId);
       return  DbSession.selectList("whole_sale_lines.getBillLinesByHeaderId",param);
    }

    /**
     * 批量保存或更新
     * @param sqlSession
     * @param lines
     */
    public Boolean saveOrUpdateBillLinesList(SqlSession sqlSession, JSONArray lines,String orgid){
            for(int i =0 ; i < lines.size();i++){
                JSONObject jsonObject = lines.getJSONObject(i);
                String lineId = jsonObject.getString("line_id");
               /* Map map=new HashMap();
                map.put("item_id",jsonObject.get("item_id"));
                map.put("org_id",orgid);
                Map obj= sqlSession.selectOne("inv_item_on_hand.getItemOnHandByItemIdOrgId",map);
                double quantityOld = Double.parseDouble(obj.get("on_hand_quantity").toString());
                double quantityNew = Double.parseDouble(jsonObject.getString("quantity"));
                if(quantityOld-quantityNew<0){
                    return false;
                }else{
                    obj.put("on_hand_quantity",quantityOld-quantityNew);
                    sqlSession.update("inv_item_on_hand.updateItemOnHand",obj);
                }*/

                if(lineId.startsWith("NEW_TEMP_ID_")){
                    //临时数据 执行新增
                    sqlSession.insert("whole_sale_lines.saveBillLines",jsonObject);
                }else{
                    //执行更新
                    sqlSession.update("whole_sale_lines.updateBillLinesById",jsonObject);
                }
            }
            return true;
    }



    public void deleteBillLines(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("whole_sale_lines.deleteByIds",map);
    }


}
