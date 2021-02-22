package root.inv.ap.invoice;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.db.DbFactory;
import root.report.sys.SysContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApInvoiceService {

    private static Logger log = Logger.getLogger(ApInvoiceService.class);

    public  Map<String,Object> getApInvoiceListByPage(Map<String,Object> map){
        Map<String,Object> map1=new HashMap<>();

        try {
            RowBounds bounds = null;
            if (map == null) {
                bounds = RowBounds.DEFAULT;
            } else {
                Integer startIndex = Integer.parseInt(map.get("pageNum").toString());
                Integer perPage = Integer.parseInt(String.valueOf(map.get("perPage")));
                if (startIndex == 1 || startIndex == 0) {
                    startIndex = 0;
                } else {
                    startIndex = (startIndex - 1) * perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            List<Map<String, Object>> resultList = DbSession.selectList("ap_invoice.getApInvoiceListByPage", map, bounds);
            Long totalSize = 0L;
            if (map != null && map.size() != 0) {
                totalSize = ((PageRowBounds) bounds).getTotal();
            } else {
                totalSize = Long.valueOf(resultList.size());
            }

            map1.put("list", resultList);
            map1.put("total", totalSize);

        }catch (Exception e){
            e.printStackTrace();
        }
        return map1;
    }




    public Map<String,Object> getApInvoiceById(Map<String,Object> params){
        return DbSession.selectOne("ap_invoice.getApInvoiceById",params);
    }

    public long saveApInvoice(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.insert("ap_invoice.saveApInvoice",params);
        if(params.containsKey("invoice_id")){
            return  Long.parseLong(params.get("invoice_id").toString());
        }
        return  -1;
    }

    public void updateApInvoiceById(SqlSession sqlSession,JSONObject params){
        sqlSession.update("ap_invoice.updateApInvoiceById",params);
    }


    public void deleteApInvoiceByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("ap_invoice.deleteApInvoiceByIds",map);
    }

  

}
