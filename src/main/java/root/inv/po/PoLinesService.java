package root.inv.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PoLinesService {

    private static Logger log = Logger.getLogger(PoLinesService.class);

    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public void insertPoLinesAll(SqlSession sqlSession ,List lines){
        if(lines == null || lines.isEmpty()){
            return ;
        }
        // 保存行数据
        sqlSession.insert("po_lines.savePoLinesAll",lines);
    }

    public List<Map<String,Object>> getPoLinesByHeadId(String headId){
        Map<String,Object> param = new HashMap<>();
        param.put("head_id",headId);
        return  DbSession.selectList("po_lines.getPoLinesByHeadId",param);
    }

    /**
     * 批量保存或更新
     * @param sqlSession
     * @param lines
     */
    public void saveOrUpdatePoLinesList(SqlSession sqlSession, JSONArray lines){
        for(int i =0 ; i < lines.size();i++){
            JSONObject jsonObject = lines.getJSONObject(i);
            String lineId = jsonObject.getString("line_id");
            if(lineId.startsWith("NEW_TEMP_ID_")){
                //临时数据 执行新增
                sqlSession.insert("po_lines.savePoLines",jsonObject);
            }else{
                //执行更新
                sqlSession.update("po_lines.updatePoLinesById",jsonObject);
            }
        }
    }



    public void deletePoLines(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("po_lines.deleteByIds",map);
    }


}
