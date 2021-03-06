package net.aooms.rbac.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import net.aooms.core.AoomsVar;
import net.aooms.core.authentication.AuthenticationInfo;
import net.aooms.core.authentication.SSOAuthentication;
import net.aooms.core.id.IDGenerator;
import net.aooms.core.module.mybatis.Db;
import net.aooms.core.module.mybatis.SqlPara;
import net.aooms.core.record.Record;
import net.aooms.core.record.RecordGroup;
import net.aooms.core.service.GenericService;
import net.aooms.core.util.Kv;
import net.aooms.core.util.PasswordHash;
import net.aooms.core.util.TreeUtils;
import net.aooms.core.web.AoomsContext;
import net.aooms.rbac.mapper.RbacMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 用户管理
 * Created by 风象南(yuboon) on 2018-09-17
 */
@Service
public class UserService extends GenericService {

    @Autowired
    private Db db;

    @Transactional(readOnly = true)
    //@DS("slave")
    public void findList() {
        SqlPara sqlPara = SqlPara.fromDataBoss().paging();
        sqlPara.tableAlias("t").and("status","org_id")
               .andLikeStart("user_name","account","phone","user_nickname","email")
               .gte("create_time","update_time")
               .lteCp("create_time","create_time_end")
               .lteCp("update_time","update_time_end")
		       .tableAlias("o")
		       .andLikeStart("data_permission")
		;

		RecordGroup recordGroup = db.findRecords(RbacMapper.PKG.by("UserMapper.findList"),sqlPara);
		this.setResultValue(AoomsVar.RS_DATA, recordGroup);
	}

    public void findResourceByUserId() {
        SqlPara sqlPara = SqlPara.empty();
        AuthenticationInfo ssoAuthentication = SSOAuthentication.getAuthenticationInfo();
        String statementId = "";
        // admin 时不限制菜单
        if(ssoAuthentication.isAdmin()){
            statementId = "ResourceMapper.findList";
            sqlPara.set("status",AoomsVar.YES);
            sqlPara.and("status");
        }else{
            statementId = "UserMapper.findResourceByUserId";
            sqlPara.set("user_id", SSOAuthentication.getAuthenticationInfo().getId());
        }

        RecordGroup recordGroup = db.findRecords(RbacMapper.PKG.by(statementId),sqlPara);
        TreeUtils treeUtils = new TreeUtils(recordGroup.getList());
        treeUtils.setParentIdKey("parent_resource_id");
        treeUtils.setDefaultValue(Kv.fkv("icon","el-icon-news"));
        List<Record> treeRecords = treeUtils.listTree(AoomsVar.TREE_ROOT);
        this.setResultValue(AoomsVar.RS_TREE, treeRecords);
    }

	@Transactional
	public void insert() {
		Record record = Record.empty();
		record.set(AoomsVar.ID,IDGenerator.getStringValue());
		record.setByJsonKey("formData");
		record.set("is_admin", AoomsVar.NO);
		record.set("create_time", DateUtil.now());
		String password = record.getString("password");
		if(StrUtil.isNotBlank(password)){
			record.set("password", PasswordHash.createHash(password));
		}
		db.insert("aooms_rbac_user",record);
        addRoles(record.getString("id"));
    }

	@Transactional
	public void update() {
        Record record = Record.empty();
        record.setByJsonKey("formData");
        record.set("update_time",DateUtil.now());
		String password = record.getString("password");
		if(StrUtil.isNotBlank(password)){
			record.set("password", PasswordHash.createHash(password));
		}
        db.update("aooms_rbac_user",record);

		String userId = record.getString("id");
		db.update(RbacMapper.PKG.by("UserMapper.deleteRoleByUserId"),SqlPara.empty().set("user_id",userId));
		addRoles(userId);
	}

    @Transactional
    public void updatePassword() {
        String id = getParaString("id");
        String oldPassword = getParaString("old_password");
        String password = getParaString("password");

        Record user = db.findByPrimaryKey("aooms_rbac_user",SSOAuthentication.getAuthenticationInfo().getId());
        if(user != null){
            String storePassword = user.getString("password");
            boolean isTrue = PasswordHash.validatePassword(oldPassword,storePassword);
            if(!isTrue){
                this.getResult().failure(AoomsVar.NO_FOR_INT , "旧密码错误");
            }else{
                user.set("password", PasswordHash.createHash(password));
                db.update("aooms_rbac_user", user);
            }
        }
    }

	@Transactional
	public void updateStatus() {
		Record record = Record.empty();
		record.set(AoomsVar.ID, getParaString("id"));
		record.set("status", getParaString("status"));
		record.set("update_time",DateUtil.now());
		db.update("aooms_rbac_user",record);
	}

	@Transactional
	public void delete() {
        List<Object> ids = this.getListFromJson("ids", AoomsVar.ID);
		db.batchDelete("aooms_rbac_user",ids.toArray());
	}

	@Transactional(readOnly = true)
    public void findRoleByUserId(){
        String userId = getParaString("user_id");
        if(StrUtil.isNotBlank(userId)){
            List<String> roleIds = db.findList(RbacMapper.PKG.by("UserMapper.findRoleByUserId"),SqlPara.empty().set("user_id",userId));
            this.setResultValue("roleIds", roleIds);
        }
    }

	private void addRoles(String userId){
        String roleIds = getParaString("roleIds");
        if(StrUtil.isNotBlank(roleIds)){
            JSONArray ids = JSONArray.parseArray(roleIds);
            ids.forEach(id -> {
                Record record = Record.empty();
                record.set(AoomsVar.ID,IDGenerator.getStringValue());
                record.set("user_id",userId);
                record.set("role_id",id);
                record.set("create_time",DateUtil.now());
                db.insert("aooms_rbac_userrole", record);
            });
        }
    }

}