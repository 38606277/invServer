package root.inv.store;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行数据
 */
@Service
public class InvBillLineService {

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
        int number = sqlSession.insert("inv_bill_lines.saveBillLinesAll",lines);
        return  0 < number;
    }

    public List<Map<String,Object>> getBillLinesByHeaderId(String headerId){
        Map<String,Object> param = new HashMap<>();
        param.put("header_id",headerId);
       return  DbSession.selectList("inv_bill_lines.getBillLinesByHeaderId",param);
    }

    /**
     * 批量保存或更新
     * @param sqlSession
     * @param lines
     */
    public void saveOrUpdateBillLinesList(SqlSession sqlSession, JSONArray lines){
        for(int i =0 ; i < lines.size();i++){
            JSONObject jsonObject = lines.getJSONObject(i);
            String lineId = jsonObject.getString("line_id");
            if(lineId.startsWith("NEW_TEMP_ID_")){
                //临时数据 执行新增
                sqlSession.insert("inv_bill_lines.saveBillLines",jsonObject);
            }else{
                //执行更新
                sqlSession.update("inv_bill_lines.updateBillLinesById",jsonObject);
            }
        }
    }



    public void deleteBillLinesByHeaderIds(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("inv_bill_lines.deleteByHeaderIds",map);
    }

    public void deleteBillLines(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("inv_bill_lines.deleteByIds",map);
    }


}
