package root.report.itemCategory.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.mdmDict.service.MdmDictService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/itemCategory")
public class ItemCategoryController extends RO {

    private static Logger log = Logger.getLogger(ItemCategoryController.class);

    @Autowired
    public ItemCategoryService itemCategoryService;

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/saveItemCategory", produces = "text/plain;charset=UTF-8")
    public String saveDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String id = this.itemCategoryService.saveOrUpdateCategory(sqlSession, jsonObject);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("startIndex",jsonFunc.getString("startIndex"));
            map.put("perPage",jsonFunc.getString("perPage"));
            map.put("category_code",jsonFunc.getString("category_code"));
            map.put("category_name",jsonFunc.getString("category_name"));
            map.put("category_pid",jsonFunc.getString("category_pid"));
            Map<String,Object> map1 = itemCategoryService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

//    @RequestMapping(value = "/getAllPageById", produces = "text/plain;charset=UTF-8")
//    public String getAllPageByPid(@RequestBody String pJson) {
//        try {
//            JSONObject jsonFunc = JSONObject.parseObject(pJson);
//            Map<String,String> map=new HashMap();
//
//            map.put("category_id",jsonFunc.getString("category_id"));
//            Map itemCategory = itemCategoryService.getItemCategoryById(map);
//
//            List<Map> list = itemCategoryService.getItemCategorySegmentByPid(map);
//
//            List<Map> list2 = itemCategoryService.getItemCategoryAttributeByPid(map);
//
//            Map resMap = new HashMap();
//            resMap.put("dataInfo",itemCategory);
//            resMap.put("list",list);
//            resMap.put("list2",list2);
//            return SuccessMsg("", resMap);
//        } catch (Exception ex){
//            ex.printStackTrace();
//            return ExceptionMsg(ex.getMessage());
//        }
//    }

    @RequestMapping(value = "/getAllPageByIdForLine", produces = "text/plain;charset=UTF-8")
    public String getAllPageByIdForLine(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("category_id",jsonFunc.getString("category_id"));
            List<Map> map1 = itemCategoryService.getItemCategorySegmentByPid(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getAllList", produces = "text/plain;charset=UTF-8")
    public String getAllList(@RequestBody String pjson) {
        try {
            Map map = new HashMap();
            JSONObject obj=JSON.parseObject(pjson);
            map.put("category_pid",obj.get("category_pid"));
            List<Map> map1 = itemCategoryService.getItemCategoryByPid(map);
            map.put("category_id","-1");
            map.put("category_name","全部");
            map.put("key","-1");
            map.put("title","全部");
            map.put("children",map1);
            List<Map> newamp = new ArrayList<>();
            newamp.add(map);
            return SuccessMsg("", newamp);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }


     //返回数据
    @RequestMapping(value = "/getItemCategoryByID", produces = "text/plain;charset=UTF-8")
    public String getItemCategoryByID(@RequestBody String pjson) {
        JSONObject obj=JSON.parseObject(pjson);
        try{
            Map map = new HashMap();
            map.put("category_id",obj.get("category_id"));
            Map jsonObject = this.itemCategoryService.getItemCategoryById(map);
            List<Map> list = this.itemCategoryService.getItemCategorySegmentByPid(map);
            List<Map> list2 = this.itemCategoryService.getItemCategoryAttributeByPid(map);
            Map mmm=new HashMap();
            mmm.put("mainForm",jsonObject);
            mmm.put("lineForm",list);
            mmm.put("lineForm2",list2);
            return SuccessMsg("查询成功",mmm);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }


    // 从json数据当中解析 ，批量删除
    @RequestMapping(value = "/deleteItemCategoryById", produces = "text/plain;charset=UTF-8")
    public String deleteItemCategoryById(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj=JSON.parseObject(pJson);
            String ids = obj.getString("category_id");
            String[] arrId=ids.split(",");
            for(int i = 0; i < arrId.length; i++){
                //删除
                Integer count=itemCategoryService.countChildren(sqlSession,arrId[0]);
                if(count==0) {
                    this.itemCategoryService.deleteItemCategoryByID(sqlSession, arrId[0]);
                }else{
                    sqlSession.getConnection().rollback();
                    return ExceptionMsg("包含子类不可以删除");
                }
            }
            sqlSession.getConnection().commit();
            return SuccessMsg("删除成功",null);
        }catch (Exception ex){

            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

    @RequestMapping(value = "/doExchangeList", produces = "text/plain;charset=UTF-8")
    public void doExchangeList(@RequestBody JSONObject pJson) {
        List list=new ArrayList();

        String[] color={"1","2","3","4"};

        //String[] size={"X","L","M"};


        list.add(color);
        //list.add(size);

        List newArrayLists=new ArrayList<>();
        newArrayLists= this.doExchange(list,newArrayLists);
        System.err.println(newArrayLists);
    }

    public List  doExchange(List arrayLists,List lists){
        int len=arrayLists.size();
        //判断数组size是否小于2，如果小于说明已经递归完成了，否则你们懂得的，不懂？断续看代码
        if (len<2){
            return lists;
        }
        //拿到第一个数组
        int len0;
        if (arrayLists.get(0) instanceof String[]){
            String[] arr0= (String[]) arrayLists.get(0);
            len0=arr0.length;
        }else {
            len0=((ArrayList<String>)arrayLists.get(0)).size();
        }

        //拿到第二个数组
        String[] arr1= (String[]) arrayLists.get(1);
        int len1=arr1.length;

        //计算当前两个数组一共能够组成多少个组合
        int lenBoth=len0*len1;

        //定义临时存放排列数据的集合
        ArrayList<ArrayList<String>> tempArrayLists=new ArrayList<>(lenBoth);

        //第一层for就是循环arrayLists第一个元素的
        for (int i=0;i<len0;i++){
            //第二层for就是循环arrayLists第二个元素的
            for (int j=0;j<len1;j++){
                //判断第一个元素如果是数组说明，循环才刚开始
                if (arrayLists.get(0) instanceof String[]){
                    String[] arr0= (String[]) arrayLists.get(0);
                    ArrayList<String> arr=new ArrayList<>();
                    arr.add(arr0[i]);
                    arr.add(arr1[j]);
                    //把排列数据加到临时的集合中
                    tempArrayLists.add(arr);
                }else {
                    //到这里就明循环了最少一轮啦，我们把上一轮的结果拿出来继续跟arrayLists的下一个元素排列
                    ArrayList<ArrayList<String>> arrtemp= (ArrayList<ArrayList<String>>) arrayLists.get(0);
                    ArrayList<String> arr=new ArrayList<>();
                    for (int k=0;k<arrtemp.get(i).size();k++){
                        arr.add(arrtemp.get(i).get(k));
                    }
                    arr.add(arr1[j]);
                    tempArrayLists.add(arr);
                }
            }
        }

        //这是根据上面排列的结果重新生成的一个集合
        List newArrayLists=new ArrayList<>();
        //把还没排列的数组装进来，看清楚i=2的喔，因为前面两个数组已经完事了，不需要再加进来了
        for (int i=2;i<arrayLists.size();i++){
            newArrayLists.add(arrayLists.get(i));
        }
        //记得把我们辛苦排列的数据加到新集合的第一位喔，不然白忙了
        newArrayLists.add(0,tempArrayLists);

        //你没看错，我们这整个算法用到的就是递归的思想。
        lists=newArrayLists;
        lists=  doExchange(newArrayLists,lists);
        return lists;
    }


}
