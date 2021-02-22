package root.inv.ap.invoice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApInvoiceLinesService {

    private static Logger log = Logger.getLogger(ApInvoiceLinesService.class);

    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public void insertApInvoiceLinesAll(SqlSession sqlSession ,List lines){
        if(lines == null || lines.isEmpty()){
            return ;
        }
        // 保存行数据
        sqlSession.insert("ap_invoice_lines.saveApInvoiceLinesAll",lines);
    }

    public List<Map<String,Object>> getApInvoiceLinesByInvoiceId(String headerId){
        Map<String,Object> param = new HashMap<>();
        param.put("invoice_id",headerId);
        return  DbSession.selectList("ap_invoice_lines.getApInvoiceLinesByInvoiceId",param);
    }

    /**
     * 批量保存或更新
     * @param sqlSession
     * @param lines
     */
    public void saveOrUpdateApInvoiceLinesList(SqlSession sqlSession, JSONArray lines){
        for(int i =0 ; i < lines.size();i++){
            JSONObject jsonObject = lines.getJSONObject(i);
            String lineId = jsonObject.getString("line_id");
            if(lineId.startsWith("NEW_TEMP_ID_")){
                //临时数据 执行新增
                sqlSession.insert("ap_invoice_lines.saveApInvoiceLines",jsonObject);
            }else{
                //执行更新
                sqlSession.update("ap_invoice_lines.updateApInvoiceLinesById",jsonObject);
            }
        }
    }

    public void deleteApInvoiceLinesByInvoiceIds(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("ap_invoice_lines.deleteByInvoiceIds",map);
    }

    public void deleteApInvoiceLines(SqlSession sqlSession, String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("ap_invoice_lines.deleteByIds",map);
    }


    public void updateApInvoiceLinesRcvQuantity(SqlSession sqlSession, String headerId,String itemId,double rcvQuantity){
        Map<String,Object> map = new HashMap<>();
        map.put("invoice_id",headerId);
        map.put("item_id",itemId);
        map.put("rcv_quantity",rcvQuantity);
        sqlSession.update("ap_invoice_lines.updateApInvoiceLinesRcvQuantity",map);
    }

}
