package root.report.sales.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.itemCategory.service.ItemCategoryService;

import java.sql.SQLException;
import java.util.*;

/**
 * 行数据
 */
@Service
public class WholeSaleLineService {

    @Autowired
    public ItemCategoryService itemCategoryService;

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

    public List<Map> getSoLinesByHeaderId(String headerId){
        Map<String,Object> param = new HashMap<>();
        param.put("header_id",headerId);
        List<Map> list= DbSession.selectList("whole_sale_lines.getBillLinesByHeaderId",param);
        Set<String> itemcate = new HashSet<>();
        Map<String,Object> ppmap = new HashMap();
        for(int i=0;i<list.size();i++) {
            Map tempmap = list.get(i);
            String mkeyRes="", skeyRes="";
            List<Map> mlist=null, slist=null;
            String catid=tempmap.get("item_category_id").toString();
            if(!itemcate.contains(catid)) {
                Map msmap = new HashMap();
                mlist= itemCategoryService.getItemCategorySegmentByCatIdAndKey(catid,"mkey");
                slist= itemCategoryService.getItemCategorySegmentByCatIdAndKey(catid,"skey");
                msmap.put("mkey",mlist);
                msmap.put("skey",slist);
                ppmap.put(catid,msmap);
                itemcate.add(catid);
            }else{
                Map m= (Map) ppmap.get(catid);
                mlist = (List<Map>) m.get("mkey");
                slist = (List<Map>) m.get("skey");
            }
            if(null!=mlist) {
                for (int ii = 0; ii < mlist.size(); ii++) {
                    String segment = mlist.get(ii).get("segment").toString();
                    mkeyRes = tempmap.get(segment)+ " " + mkeyRes;
                }
            }
            if(null!=slist) {
                for (int iii = 0; iii < slist.size(); iii++) {
                    String segment = slist.get(iii).get("segment").toString();
                    skeyRes = tempmap.get(segment)+ " " + skeyRes;
                }
            }
            tempmap.put("mkeyRes",mkeyRes);
            tempmap.put("skeyRes",skeyRes);
        }
        return list;
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
