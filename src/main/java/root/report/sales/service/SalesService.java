package root.report.sales.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.sys.SysContext;
import root.report.util.DateUtil;

import java.util.*;

@Service
public class SalesService {

    private static Logger log = Logger.getLogger(SalesService.class);

    @Autowired
    public ItemCategoryService itemCategoryService;

    @Autowired
    public WholeSaleService wholeSaleService;

    @Autowired
    WholeSaleLineService wholeSaleLineService;

    public Map<String,Object> getAllPage(Map<String,String> map) {
        Map<String,Object> map1=new HashMap<>();

        try {
            SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
            RowBounds bounds = null;
            if (map == null) {
                bounds = RowBounds.DEFAULT;
            } else {
                Integer startIndex = Integer.parseInt(map.get("startIndex").toString());
                Integer perPage = Integer.parseInt(map.get("perPage"));
                if (startIndex == 1 || startIndex == 0) {
                    startIndex = 0;
                } else {
                    startIndex = (startIndex - 1) * perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            List<Map<String, Object>> resultList = sqlSession.selectList("mdmItem.getAllPageForSales", map, bounds);
            Long totalSize = 0L;
            if (map != null && map.size() != 0) {
                totalSize = ((PageRowBounds) bounds).getTotal();
            } else {
                totalSize = Long.valueOf(resultList.size());
            }
            Set<String> itemcate = new HashSet<>();
            Map<String,Object> ppmap = new HashMap();
            for(int i=0;i<resultList.size();i++) {
                Map tempmap = resultList.get(i);
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
            map1.put("list", resultList);
            map1.put("total", totalSize);

        }catch (Exception e){
            e.printStackTrace();
        }
        return map1;
    }

    public List<Map> getOrgAll() {
        return DbFactory.Open(DbFactory.FORM).selectList("retail_sales.getOrgAll");
    }

    public Map getSalesOrderByID(String sales_id, String status, JSONObject jsonObject) {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        Map map=new HashMap();
        map.put("sales_id",sales_id);
        map.put("status",status);
        map.put("type",1);
        Map m= sqlSession.selectOne("whole_sale_header.getSalesOrderBystatusAndSalesId",map);
        int userId = SysContext.getId();
        if(null!=m){
            Integer header_id = (Integer) m.get("so_header_id");
            JSONArray jsonArray = new JSONArray();
            Integer i= sqlSession.selectOne("whole_sale_header.getSalesOrderByheaderId",header_id);
            jsonObject.put("line_number",i);
            jsonObject.put("create_by",userId);
            jsonObject.put("header_id",header_id);
            jsonObject.put("create_date", DateUtil.getCurrentTimm());
            jsonObject.put("line_type_id",1);
            jsonArray.add(jsonObject);
            boolean isLinesSaveSuccess = wholeSaleLineService.insertBillLinesAll(sqlSession,jsonArray);
        }else{

            try {
                Map mainData = new HashMap();
                mainData.put("create_by",userId);
                mainData.put("so_type","deliver_wholesales");
                mainData.put("sales_id",jsonObject.getString("sales_id"));
                mainData.put("inv_org_id",jsonObject.getString("inv_org_id"));
                mainData.put("customer_id","");
                mainData.put("bill_to_location","");
                mainData.put("ship_to_location","");
                mainData.put("contract_code","");
                mainData.put("contract_name","");
                mainData.put("contract_file","");
                mainData.put("comments","");
                mainData.put("create_date",DateUtil.getCurrentTimm());
                mainData.put("create_by",userId);
                mainData.put("status","0");
                mainData.put("so_date",DateUtil.getCurrentTimm());
                mainData.put("type",1);
                //保存主数据
                long header_id = wholeSaleService.createWholeSale(sqlSession,mainData);
                if(header_id==-1){

                }else {
                    JSONArray jsonArray = new JSONArray();
                    jsonObject.put("line_number", 1);
                    jsonObject.put("create_by", userId);
                    jsonObject.put("header_id", header_id);
                    jsonObject.put("create_date", DateUtil.getCurrentTimm());
                    jsonObject.put("line_type_id", 1);
                    jsonArray.add(jsonObject);
                    boolean isLinesSaveSuccess = wholeSaleLineService.insertBillLinesAll(sqlSession, jsonArray);
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return m;
    }

    public Map getCarSalesByID(String sales_id, String status, JSONObject pJson) {
        Map resmap=new HashMap();
        List<Map> lists=new ArrayList<>();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        Map map=new HashMap();
        map.put("sales_id",sales_id);
        map.put("status",status);
        map.put("type",1);
        Map m= sqlSession.selectOne("whole_sale_header.getSalesOrderBystatusAndSalesId",map);
        if(null!=m) {
            String headerId = m.get("so_header_id").toString();
            lists =   wholeSaleLineService.getSoLinesByHeaderId(headerId);
        }
        resmap.put("maindata",m);
        resmap.put("lines",lists);
        return resmap;

    }

    public Map<String, Object> getListPage(Map<String, Object> pJson) {
        Map<String,Object> map1=new HashMap<>();

        try {
            RowBounds bounds = null;
            if (pJson == null) {
                bounds = RowBounds.DEFAULT;
            } else {
                int currentPage = Integer.valueOf(pJson.get("startIndex").toString());
                int perPage = Integer.valueOf(pJson.get("perPage").toString());
                if (1 == currentPage || 0 == currentPage) {
                    currentPage = 0;
                } else {
                    currentPage = (currentPage - 1) * perPage;
                }

                pJson.put("startIndex",currentPage);
                pJson.put("perPage",perPage);
                bounds = new PageRowBounds(currentPage, perPage);
            }
            int id = SysContext.getId();//用户的表id
            pJson.put("create_by",id);
            pJson.put("type",0);
            List<Map<String, Object>> resultList = DbSession.selectList("whole_sale_header.getWholeSaleListByPage", pJson, bounds);
            Long totalSize = 0L;
            if (pJson != null && pJson.size() != 0) {
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
}
