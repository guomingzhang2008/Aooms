package net.aooms.mybatis.record;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import net.aooms.core.configuration.Vars;
import net.aooms.mybatis.MyBatisConst;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Record
 */
public class Record extends LinkedCaseInsensitiveMap {

    // 内置key
    private static List<String> InternalKey = Arrays.asList(
            MyBatisConst.TABLE_NAME_PLACEHOLDER,
            MyBatisConst.TABLE_PK_NAME_PLACEHOLDER
    );

    // 一般属性，保存时，不会持久化到数据库
    private Map<String,Object> generalData = new LinkedCaseInsensitiveMap();

    /**
     * 创建Record
     * @return
     */
    public static Record NEW(){
        return new Record();
    }

    /**
     * 清空
     */
    @Override
    public void clear(){
        this.clear();
        generalData.clear();
    }

    public Record set(String key,Object value){
        this.put(key,value);
        return this;
    }

    public String getString(String key){
        return String.valueOf(this.get(key));
    }

    public Integer getInteger(String key){
        return Integer.parseInt(getString(key));
    }

    public Long getLong(String key){
        return Long.parseLong(getString(key));
    }

    public Float getFloat(String key){
        return Float.parseFloat(getString(key));
    }

    public Double getDouble(String key){
        return Double.parseDouble(getString(key));
    }

    public Date getDate(String key){
        return DateUtil.parseDate(getString(key));
    }

    public DateTime getDateTime(String key){
        return DateUtil.parseDateTime(getString(key));
    }

    public Boolean getBoolean(String key){
        return Boolean.parseBoolean(getString(key));
    }

    public Record pkIs(String key){
        this.put(MyBatisConst.TABLE_PK_NAME_PLACEHOLDER,key);
        return this;
    }


    public Record setGeneral(String key,Object value){
        generalData.put(key,value);
        return this;
    }

    public <T> T getGeneral(String key){
        return (T)generalData.get(key);
    }

    public Map<String,Object> getGeneralData(){
        return generalData;
    }

    /**
     * 移除所有内置属性
     */
    public void removeInternalKey(){
        InternalKey.forEach(key -> {
            this.remove(key);
        });
    }

    public static void main(String[] args) {
        Record record = Record.NEW();
        record
        .set(Vars.ID,System.currentTimeMillis())
        .set("name","lisi")
        .setGeneral("text","12312");


        System.err.println(JSON.toJSONString(record));
    }

}
