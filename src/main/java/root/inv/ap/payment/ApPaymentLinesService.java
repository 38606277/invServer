package root.inv.ap.payment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.inv.ap.invoice.ApInvoiceService;
import root.report.common.DbSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApPaymentLinesService {

    private static Logger log = Logger.getLogger(ApPaymentLinesService.class);




    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public void insertApPaymentLinesAll(SqlSession sqlSession ,List lines){
        if(lines == null || lines.isEmpty()){
            return ;
        }
        // 保存行数据
        sqlSession.insert("ap_payment_lines.saveApPaymentLinesAll",lines);
    }

    public List<Map<String,Object>> getApPaymentLinesByPaymentId(String headerId){
        Map<String,Object> param = new HashMap<>();
        param.put("payment_id",headerId);
        return  DbSession.selectList("ap_payment_lines.getApPaymentLinesByPaymentId",param);
    }

    /**
     * 批量保存或更新
     * @param sqlSession
     * @param lines
     */
    public void saveOrUpdateApPaymentLinesList(SqlSession sqlSession, JSONArray lines){
        for(int i =0 ; i < lines.size();i++){
            JSONObject jsonObject = lines.getJSONObject(i);
            String lineId = jsonObject.getString("line_id");
            if(lineId.startsWith("NEW_TEMP_ID_")){
                //临时数据 执行新增
                sqlSession.insert("ap_payment_lines.saveApPaymentLines",jsonObject);
            }else{
                //执行更新
                sqlSession.update("ap_payment_lines.updateApPaymentLinesById",jsonObject);
            }
        }
    }

    public void deleteApPaymentLinesByPaymentIds(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("ap_payment_lines.deleteByPaymentIds",map);
    }

    public void deleteApPaymentLines(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("ap_payment_lines.deleteByIds",map);
    }

}
