package com.cmsz.commonbase.plugins.tmalldiffgen.services.impl;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.cmsz.commonbase.plugins.CDH.HiveJdbcClient;
import com.cmsz.commonbase.plugins.excsyscommd.bean.BCPErrorLogInfoBean;
import com.cmsz.commonbase.plugins.excsyscommd.excutor.LinuxExecutor;
import com.cmsz.commonbase.plugins.excsyscommd.excutor.LinuxSybaseBCPExecutor;
import com.cmsz.commonbase.plugins.logs.LoggerFactory;
import com.cmsz.commonbase.plugins.logs.logbeans.Logger;
import com.cmsz.commonbase.plugins.logs.logbeans.SupperLogger;
import com.cmsz.commonbase.plugins.tmalldiffgen.constant.CommonConstant;
import com.cmsz.commonbase.plugins.tmalldiffgen.constant.CommonConstant.SpeSymbol;
import com.cmsz.commonbase.plugins.tmalldiffgen.services.TmallEbizDiffDao;
import com.cmsz.commonbase.plugins.tmalldiffgen.util.FileUtil;
import com.cmsz.commonbase.plugins.tmallfilecompare.util.DateUtil;
import com.cmsz.commonbase.plugins.tmallfilecompare.util.DbConfigUtil;
import com.cmsz.commonbase.plugins.tmallfilecompare.util.StringUtil;
import com.cmsz.commonbase.plugins.upayBatAlarmLogs.service.UpayBatAlarmLogsService;
import com.cmsz.framework.mybatis.bean.TmallEbizUpayDiffBean;
import com.cmsz.framework.mybatis.bean.UpayBatTmallUpayTxnDiffBean;
import com.cmsz.framework.mybatis.mapper.UpayBatTmallUpayTxnDiffMapper;
import com.cmsz.platform.bean.TaskResult;

/**
 * 从Hive导出 支付数据到sybase
 *
 * @author
 *
 */
@Service("tmallEbizUpayDiffService")
@Scope("prototype")
public class ExportTmallEbizUpayDiffImpl implements TmallEbizDiffDao {

	Logger systemRunLogger = LoggerFactory.getSystemRunLogger();
	Logger alarmLogger = LoggerFactory.getAlarmLogger();
	Logger businessLogger = LoggerFactory.getBusinessLogger();

	SupperLogger supperLogger = LoggerFactory.getSupperLogger();

	@Autowired
	private UpayBatAlarmLogsService upayBatAlarmLogsService;
	@Autowired
	private UpayBatTmallUpayTxnDiffMapper  upayBatTmallUpayTxnDiffMapper;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	// 替换变量参数定义
	private static final String varPO = "{PARTNER_ORG}";
	private static final String varDate = "{STTL_DATE}";

	private static final String kHiveServer = "HIVE_SERVER";
	private static final String kHiveUser = "HIVE_USER";
	private static final String kHivePwd = "HIVE_PWD";
	private static final String kHdfsServer = "HDFS_SERVER";

	/** [天猫缴费对账差异结果表 天猫 – 统一支付] upay_batTmall_upayTxnDiff */
	private  static String DIFFSQL = "insert into upay_bat_tmall_upay_txn_diff values ( ? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,? ,?  )";

	// 数据库配置
	private String vHiveServer;
	private String vHiveUser;
	private String vHivePwd;
	private String vHdfsServer;

	/** 发起方机构 */
	private String orgId;
	private String idProvince;
	private String date;
	private String diveTableName;
	String BOSS_HIS_FILE_PATTERN;

	private HiveJdbcClient hiveJdbcClient;
//	private DBConnectionManager connMngr = DBConnectionManager.getInstance();
//	private Connection conn;
	private ResultSet resultSet = null;
	private PreparedStatement preparedStatement = null;
	private Map<String, String> logMsgMap = new HashMap<String, String>();

	/** 天猫统一支付差异文件BCP入库脚本 */
	private String defualBcpImpTmallUpayDiff;

	/** hive导出文件路径, BCP从该路径获取文件并导入到Sybase数据库 */
	private String loadDBSrcPath;

	/** 天猫与统一支付比对差异表别名 */
	private String tmallUpayDiffTableAlias;

	/**
	 * BCP入库错误日志路径,如:/opt/upay/bcp/errorlog/${sttle}_${flowid}_${taskid}_${
	 * version}error.log
	 */
	private String bcpErrorFileReg;

	private String tmallUpayDiffTableHive;

	// 接口参数Value定义
	private String vSettleDate;
	private String vHivePath;
//	private String vDiffHiveTable;

	@Override
	public TaskResult execute(Map<String, String> map) {
		diveTableName = map.get("DIFF_BUSINESS_HIVE_TABLE");
		businessLogger.info(map, "开始任务引擎-天猫对账-统一支付差异生成插件程序......");
		logMsgMap = map;
		orgId = map.get("INIT_ORG");// 发起方机构
		idProvince = map.get("PARTNER_ORG");// 填每一个省代码
		date = map.get("sttlDate");
		systemRunLogger.info(map, "获取参数......");

		BOSS_HIS_FILE_PATTERN = map.get("BOSS_HIS_FILE_PATTERN");

		// 获取参数
		getParams(map);

		if (StringUtil.isEmptyString(orgId)) {
			alarmLogger.error(logMsgMap, "天猫缴费对账差异生成失败！INIT_ORG参数未传递.....");
			upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_ERROR, this.getClass()
					.toString(), "任务引擎-统一支付天猫对账-差异生成", "", "", "", "天猫缴费对账差异生成失败！INIT_ORG参数未传递.....", "");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED, "天猫缴费对账差异生成失败！INIT_ORG参数未传递.....");
		}
		if (StringUtil.isEmptyString(idProvince)) {
			alarmLogger.error(logMsgMap, "统一支付天猫对账-差异生成！PARTNER_ORG参数  未传递.....");
			upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_ERROR, this.getClass()
					.toString(), "任务引擎-天猫对账-统一支付差异生成", "", "", "",
					"天猫缴费对账差异生成失败！PARTNER_ORG参数未传递.....", "");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED, "天猫缴费对账差异生成失败！PARTNER_ORG参数  未传递.....");
		}
		if (StringUtil.isEmptyString(date)) {
			alarmLogger.error(logMsgMap, "统一支付天猫对账-差异生成失败！sttlDate参数  未传递.....");
			upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_ERROR, this.getClass()
					.toString(), "任务引擎-统一支付天猫对账-差异生成", "", "", "", "天猫缴费对账差异生成失败！sttlDate参数未传递.....",
					"");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED, "天猫缴费对账差异生成失败！sttlDate参数  未传递.....");
		}
		if (StringUtil.isEmptyString(diveTableName)) {
			alarmLogger.error(logMsgMap, "统一支付天猫对账-差异生成失败！DIFF_BUSINESS_HIVE_TABLE参数  未传递.....");
			upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_ERROR, this.getClass()
					.toString(), "任务引擎-统一支付天猫对账-差异生成", "", "", "", "天猫缴费对账差异生成失败！DIFF_BUSINESS_HIVE_TABLE参数未传递.....",
					"");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED, "天猫缴费对账差异生成失败！sttlDate参数  未传递.....");
		}
		if (StringUtil.isEmptyString(vHiveServer)) {
			alarmLogger.error(logMsgMap,"天猫缴费对账差异生成失败！HIVE_SERVER参数未传递.....");
			upayBatAlarmLogsService.insert(logMsgMap,
					CommonConstant.LEVEL_ERROR,this.getClass().toString(),
					"任务引擎-统一支付天猫对账-差异生成","","","",
					"天猫缴费对账差异生成失败！HIVE_SERVER参数未传递.....","");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED,
					"天猫缴费对账差异生成失败！HIVE_SERVER参数未传递.....");
		}
		if (StringUtil.isEmptyString(vHdfsServer)) {
			alarmLogger.error(logMsgMap,"天猫缴费对账差异生成失败！HDFS_SERVER参数  未传递.....");
			upayBatAlarmLogsService.insert(logMsgMap,
					CommonConstant.LEVEL_ERROR,this.getClass().toString(),
					"任务引擎-统一支付天猫对账-差异生成","","","",
					"天猫缴费对账差异生成失败！HIVE_SERVER参数未传递.....","");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED,
					"天猫缴费对账差异生成失败！HDFS_SERVER参数  未传递.....");
		}
		if (StringUtil.isEmptyString(vHiveUser)) {
			alarmLogger.error(logMsgMap,"天猫缴费对账差异生成失败！HIVE_USER参数  未传递.....");
			upayBatAlarmLogsService.insert(logMsgMap,
					CommonConstant.LEVEL_ERROR,this.getClass().toString(),
					"任务引擎-统一支付天猫对账-差异生成","","","",
					"天猫缴费对账差异生成失败！HIVE_USER参数未传递.....","");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED,
					"天猫缴费对账差异生成失败！HIVE_USER参数  未传递.....");
		}

		// BCP相关参数校验
		String bcpErrorMsg = this.checkBcpParam(map);
		if (StringUtils.isNotEmpty(bcpErrorMsg)) {
			alarmLogger.error(logMsgMap, bcpErrorMsg);
			upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_ERROR, this.getClass().toString(), "任务引擎-天猫对账-差异比对", "", "", "", bcpErrorMsg, "");
			return new TaskResult(TaskResult.TASK_EXEC_FAILED, bcpErrorMsg);
		}

		try {
			systemRunLogger.info(map, "Starting  delete upayBatTmallUpayTxnDiff......");
			upayBatTmallUpayTxnDiffMapper.deleteTmallUpayDiffInfo(map);
			systemRunLogger.info(map, "获取连接hiveJdbcClient......");
			hiveJdbcClient = HiveJdbcClient.getHiveJDBCClient(vHdfsServer, vHiveServer, vHiveUser, vHivePwd, map);

			systemRunLogger.info(map, "Starting  dealHive......");


			//TODO 执行插入
			String insertCommnd = map.get("TMALL_UPAY_DIFF_SQL");
			insertCommnd=insertCommnd.replace(varPO, idProvince).replace(varDate,vSettleDate);
			hiveJdbcClient.executeSql(insertCommnd);

			//删除当前账单日期当前省份的历史对账文件
			long begin  = System.currentTimeMillis();

			FileUtil.removeFiles(loadDBSrcPath , vSettleDate + "_" + idProvince + "_" + this.tmallUpayDiffTableAlias+"_"+BOSS_HIS_FILE_PATTERN);
			systemRunLogger.info(map,"删除当前账单日期当前省份的历史对账文件耗时："+(System.currentTimeMillis()-begin)/1000f +"秒");

			begin  = System.currentTimeMillis();
			boolean resultDiff = this.getDBFileFromHDFS(tmallUpayDiffTableHive, this.tmallUpayDiffTableAlias);
			systemRunLogger.info(map,"导出的天猫与省比对差异文件耗时："+(System.currentTimeMillis()-begin)/1000f +"秒");

			if (resultDiff && this.dealHiveByBCP(map)==null) {
				String errorInfo = "天猫缴费对账BCP错误";
				alarmLogger.error(logMsgMap, DateUtil.getFormateDate() + " -机构编号为：" + orgId + errorInfo);
				upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_ERROR, this.getClass().toString(), "任务引擎-天猫对账-差异比对", "", "", "", " -机构编号为：" + orgId + errorInfo, "");
				return new TaskResult(TaskResult.TASK_EXEC_FAILED, DateUtil.getFormateDate() + " -机构编号为：" + orgId + errorInfo);
			}


		} catch (Exception e) {
			supperLogger.error(e.getMessage(), e);
			e.printStackTrace();

			// 输出日志
			alarmLogger.error(logMsgMap, DateUtil.getFormateDate() + " -机构编号为：" + orgId
					+ "任务引擎-天猫对账-统一支付差异生成出错!" + e.getMessage());
			systemRunLogger.info(logMsgMap, DateUtil.getFormateDate() + " -机构编号为：" + orgId + "出错!"
					+ e.getMessage());
			upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_ERROR, this.getClass()
					.toString(), "任务引擎-统一支付天猫对账-差异生成", "", "", "", " -机构编号为：" + orgId
					+ "任务引擎-天猫对账-统一支付差异生成出错" + e.getMessage(), "");

			// 输出异常
			return new TaskResult(TaskResult.TASK_EXEC_FAILED, DateUtil.getFormateDate()
					+ " -机构编号为：" + orgId + "任务引擎-天猫对账-统一支付差异生成出错" + e.getMessage());
		} finally {
			// 释放资源
			if (hiveJdbcClient != null) {
				try {
					hiveJdbcClient.closeHiveJDBCClien();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		systemRunLogger.info(logMsgMap, "任务引擎-天猫对账-统一支付差异生成成功......");
		businessLogger.info(map, "结束任务引擎-天猫对账-统一支付差异生成插件程序......");
		return new TaskResult(TaskResult.TASK_EXEC_SUCCESS, "成功!");
	}

	/**
	 * 将dive中的统一支付数据迁移到Sybase中
	 *
	 * @param diveTableName
	 *            dive表名
	 * @return 处理的数据总行数
	 * @throws SQLException
	 */
	private long dealHive(String diveTableName) throws SQLException {
		systemRunLogger.info("天猫-统一支付差异生成：Starting dealHive......");
		// 行计数器
		long rowCount = 0;

		if (hiveJdbcClient != null) {
			long begin  = System.currentTimeMillis();
			String diveQuerySql = "SELECT ebiz_flag,"
						+"ebiz_buss_chl,"
						+"ebiz_activity_code,"
						+"ebiz_buss_type,"
						+"ebiz_int_txn_date,"
						+"ebiz_int_txn_seq,"
						+"ebiz_tmall_transh_dt,"
						+"ebiz_tmall_trans_id,"
						+"ebiz_order_id,"
						+"ebiz_pay_trans_id,"
						+"ebiz_settle_date,"
						+"ebiz_id_type,"
						+"ebiz_id_value,"
						+"ebiz_id_province,"
						+"ebiz_product_id,"
						+"ebiz_product_num,"
						+"ebiz_txn_amt,"
						+"ebiz_payed_type,"
						+"ebiz_mark_activity_num,"
						+"ebiz_goods_shelves_num,"
						+"ebiz_order_amt,"
						+"ebiz_settle_amt,"
						+"ebiz_commission_amt,"
						+"ebiz_integ_rebate_fee,"
						+"ebiz_credit_fee_amt,"
						+"ebiz_service_fee,"
						+"ebiz_other_fee1,"
						+"ebiz_other_fee2,"
						+"ebiz_other_fee3,"
						+"ebiz_other_fee4,"
						+"ebiz_other_fee5,"
						+"ebiz_status,"
						+"ebiz_diff_result,"
						+"ebiz_reserved1,"
						+"ebiz_reserved2,"
						+"ebiz_reserved3,"
						+"ebiz_file_nm,"
						+"upay_seq_id,"
						+"upay_upay_biz_code,"
						+"upay_req_channel,"
						+"upay_settle_date,"
						+"upay_req_date_time,"
						+"upay_tmall_trans_id,"
						+"upay_upay_biz_seq,"
						+"upay_crm_trans_id,"
						+"upay_session_id,"
						+"upay_buss_trans_id,"
						+"upay_transaction_id,"
						+"upay_order_id,"
						+"upay_pay_trans_id,"
						+"upay_id_type,"
						+"upay_id_value,"
						+"upay_home_prov,"
						+"upay_payment,"
						+"upay_charge_money,"
						+"upay_prod_cnt,"
						+"upay_prod_id,"
						+"upay_commission,"
						+"upay_rebate_fee,"
						+"upay_prod_discount,"
						+"upay_credit_card_fee,"
						+"upay_service_fee,"
						+"upay_payed_type,"
						+"upay_activity_no,"
						+"upay_prod_shelf_no,"
						+"upay_crm_rsp_code,"
						+"upay_crm_rsp_info,"
						+"upay_crm_sub_rsp_code,"
						+"upay_crm_sub_rsp_info,"
						+"upay_tmall_rsp_code,"
						+"upay_tmall_rsp_info,"
						+"upay_action_date,"
						+"upay_crm_rsp_type,"
						+"upay_last_upd_time,"
						+"upay_file_nm,"
						+"upay_crm_activity_code,"
						+"upay_crm_bip_code,"
						+"upay_crm_org_id,"
						+"upay_crm_start_tm,"
						+"upay_crm_transh_tm,"
						+"upay_pay_mode,"
						+"upay_reconciliation_flag,"
						+"upay_resend_count,"
						+"upay_reserved1,"
						+"upay_reserved2,"
						+"upay_reserved3,"
						+"upay_reserved4,"
						+"diff_type FROM " + diveTableName + " where prov='" + this.idProvince
					+ "' and date='" + this.date + "' and diff_type != 0";
			UpayBatTmallUpayTxnDiffBean tmallTxn = null;

				resultSet = hiveJdbcClient.executeQuery(diveQuerySql);
				businessLogger.info(logMsgMap,"执行hiveJdbcClient.executeQuery耗时："+(System.currentTimeMillis()-begin)/1000f +"秒");

				begin  = System.currentTimeMillis();
				List<UpayBatTmallUpayTxnDiffBean> tempList = new ArrayList<UpayBatTmallUpayTxnDiffBean>();
//				preparedStatement = conn.prepareStatement(DIFFSQL);
				if (resultSet != null) {
				while (resultSet.next()) {
					rowCount++;

					TmallEbizUpayDiffBean tmallEbizUpayDiff = transToUpayBatTmallUpayTxnBean(resultSet);
					String diffType = tmallEbizUpayDiff.getDiffType();

					if (CommonConstant.COMPARE_TAMLL_UPAY_LESS_TYPE.equals(diffType)) {
						// 设置差异各个字段值(天猫少数据)
						tmallTxn = setTmallTxnValue(tmallEbizUpayDiff, CommonConstant.COMPARE_TAMLL_UPAY_LESS_TYPE);

					} else if (CommonConstant.COMPARE_TAMLL_UPAY_MORE_TYPE.equals(diffType)) {
						// 设置差异各个字段值(天猫多数据)
						tmallTxn = setTmallTxnValue(tmallEbizUpayDiff, CommonConstant.COMPARE_TAMLL_UPAY_MORE_TYPE);
					} else if (CommonConstant.COMPARE_TAMLL_UPAY_DIFF_TYPE.equals(diffType)) {
						// 设置差异各个字段值(对账不平数据)
						tmallTxn = setTmallTxnValue(tmallEbizUpayDiff, CommonConstant.COMPARE_TAMLL_UPAY_DIFF_TYPE);
					} else {
						// 设置差异各个字段值(平账数据)
						tmallTxn = setTmallTxnValue(tmallEbizUpayDiff, CommonConstant.COMPARE_TAMLL_UPAY_EQUAL_TYPE);
					}
					tempList.add(tmallTxn);
					// 插入tmall端差异数据
					// initSqlByUpayBatTmallCrmTxnDiff(preparedStatement,
					// tmallTxn);
					if (tempList.size() % CommonConstant.BATCH_MAX_COMMIT_ROW == 0) {
						long _begin = System.currentTimeMillis();
						batchDiffBossInsert(tempList);
						businessLogger.info("执行batchDiffBossInsert耗时：" + (System.currentTimeMillis() - _begin) / 1000f + "秒");
						tempList.clear();
					}
				}
				//执行最后一个批处理
				if (tempList.size() % CommonConstant.BATCH_MAX_COMMIT_ROW != 0) {
					  long _begin  = System.currentTimeMillis();
					  batchDiffBossInsert(tempList);
					  businessLogger.info("执行batchDiffBossInsert耗时："+(System.currentTimeMillis()-_begin)/1000f +"秒");
					  tempList.clear();
				}
				businessLogger.info(logMsgMap,"执行JDBC insert耗时："+(System.currentTimeMillis()-begin)/1000f +"秒");

				} else {
					alarmLogger.warn(logMsgMap, "省代码：" + idProvince + "省代码：" + idProvince
							+ " -机构编号为：" + orgId + "未从HIVE获取到结果集");
					upayBatAlarmLogsService.insert(logMsgMap, CommonConstant.LEVEL_WARN, this
							.getClass().toString(), "任务引擎-天猫对账-统一支付差异生成", "", "", "", "省代码："
							+ idProvince + " -机构编号为：" + orgId + "未从HIVE获取到结果集", "");
				}

		}

		systemRunLogger.info("End dealHive......");
		return rowCount;
	}

	/**
	 * 使用Sybase BCP命令从文件导入数据到数据库
	 * @return
	 * @throws Exception
	 */
	private TaskResult dealHiveByBCP(Map<String, String> map) throws Exception {

		String flowId = map.get("flowId");
		String taskid = map.get("taskId");
		long errorCount=0;
		// BCP入库错误日志路径
		// 如:/opt/upay/bcp/errorlog/${sttle}_${flowid}_${taskid}_${version}error.log
		String errorAbsoluteFile = this.bcpErrorFileReg.replace("${sttle}", this.date).replace("${flowid}", flowId).replace("${taskid}", taskid);
		systemRunLogger.debug(this.logMsgMap, "errorAbsoluteFile:" + errorAbsoluteFile);

		// 获取数据库服务名称,用户名,密码
		Map<String, String> dbParam = DbConfigUtil.getDBConfig();
		String host = dbParam.get("host");
		String username = dbParam.get("username");
		String password = dbParam.get("password");
		String dbservername = dbParam.get("dbservername");
		String maxerror = dbParam.get("maxerror");
		String batchsize = dbParam.get("batchsize");

		// sybase从文件中导入全量表数据
		// TODO BCP命令
		String commnd = defualBcpImpTmallUpayDiff.replace("${opertion}", "in").replace("${dbservername}", dbservername).replace("${user}", username).replace("${password}", password)
				.replace("${maxerror}", maxerror).replace("${batchsize}", batchsize);

		// 从hive导出全量表数据到文件中
		//List<String> list=FileUtil.getFiles(loadDBSrcPath , vSettleDate + "_" + idProvince + "_" + tmallUpayDiffTableAlias+"_000000_"+"\\d+");
		List<String> list=FileUtil.getFiles(loadDBSrcPath , vSettleDate + "_" + idProvince + "_" + tmallUpayDiffTableAlias+"_"+BOSS_HIS_FILE_PATTERN);
		if (list != null) {
			int i = 0;
			String tempCommnd = null;
			for (String filePath : list) {
				tempCommnd=commnd;
				errorAbsoluteFile = errorAbsoluteFile.replace("${version}", String.valueOf(i));
				String diffErrorFolder = errorAbsoluteFile.substring(0,errorAbsoluteFile.lastIndexOf(File.separator));
				LinuxExecutor.linuxLocalExcute("mkdir -p "+diffErrorFolder);
				LinuxExecutor.linuxLocalExcute("rm -f  "+errorAbsoluteFile);
				LinuxExecutor.linuxLocalExcute("touch -f  "+errorAbsoluteFile);

				tempCommnd = tempCommnd.replace("${dbfile}", filePath).replace("${errorFile}", errorAbsoluteFile);
				tempCommnd="sh "+new File("").getAbsolutePath()+File.separator+"config"+File.separator+"tm_up_d_bcp.sh "+tempCommnd;
				systemRunLogger.info(logMsgMap, "BCP commnd:" + tempCommnd);

				List<BCPErrorLogInfoBean> bcpErrorBeanList = LinuxSybaseBCPExecutor.bcpLocalExcute(tempCommnd, filePath, errorAbsoluteFile);
				tempCommnd = null;

				if (bcpErrorBeanList != null && bcpErrorBeanList.size() > 0) {
					errorCount++;
					upayBatAlarmLogsService.insert(logMsgMap,
							CommonConstant.LEVEL_ERROR,this.getClass().toString(),
							"任务引擎-统一支付天猫对账-差异生成",filePath,"","",bcpErrorBeanList.toString(),"");
				}

				FileUtils.deleteQuietly(new File(filePath));

				if(errorCount>0){
					return null;
				}

				i++;
			}
		}

		return new TaskResult(TaskResult.TASK_EXEC_SUCCESS,"任务引擎-统一支付天猫对账-差异生成批处理入库成功!");
	}

	/**
	 *
	 * @param hiveTableAlias
	 *            所以导出的hive表别名
	 * @return
	 */
	public boolean getDBFileFromHDFS(String hiveTable, String hiveTableAlias) throws Exception {
		String srcHdfs = vHivePath + File.separator + hiveTable + File.separator + "date=" + vSettleDate + File.separator + "prov=" + idProvince;
        boolean result=true;
		// 文件名称: 账期+省+表别名+文件名称
		String dstFilePrefix = vSettleDate + "_" + idProvince + "_" + hiveTableAlias;
		systemRunLogger.info(this.logMsgMap, "srcHdfs:" + srcHdfs+" dstFilePrefix"+dstFilePrefix);
		String[] hdfsFileNames = hiveJdbcClient.getHdfsFileList(srcHdfs);
		if(hdfsFileNames != null){
			for (String hdfFileName : hdfsFileNames) {
				// 文件改名
				String dstFileNewPath = loadDBSrcPath + File.separator + dstFilePrefix + "_" + hdfFileName;
				result=hiveJdbcClient.getHdfsToLocal(srcHdfs + File.separator + hdfFileName, dstFileNewPath);
				if(!result){
					throw new Exception("hdfs导出文件异常.");
				}
			}
		}

		return result;
	}

	/**
	 * BCP相关参数校验
	 *
	 * @param map
	 * @return
	 */
	private String checkBcpParam(Map<String, String> map) {

		// 天猫统一支付差异文件BCP入库脚本
		if (StringUtil.isEmptyString(defualBcpImpTmallUpayDiff)) {
			return "任务引擎-统一支付天猫对账-差异生成！DEFUAL_BCP_IMP_TMALL_UPAY_DIFF参数未传递.....";
		}

		// 校验hive导出文件路径
		if (StringUtil.isEmptyString(loadDBSrcPath)) {
			return "任务引擎-统一支付天猫对账-差异生成！LOADDB_SRC_PATH参数未传递.....";
		}

		// 校验天猫与统一支付比对差异表别名
		if (StringUtil.isEmptyString(tmallUpayDiffTableAlias)) {
			return "任务引擎-统一支付天猫对账-差异生成！TMALL_UPAY_DIFF_TABLE_ALIAS参数未传递.....";
		}

		// 校验天猫与省比对全量表别名
		if (StringUtil.isEmptyString(bcpErrorFileReg)) {
			return "任务引擎-统一支付天猫对账-差异生成！BCP_ERROR_FILE_REG参数未传递.....";
		}

		if (StringUtil.isEmptyString(tmallUpayDiffTableHive)) {
			return "任务引擎-统一支付天猫对账-差异生成！TMALL_UPAY_DIFF_TABLE_HIVE参数未传递.....";
		}

		return "";
	}

	/**
	 * 从Map里获取参数
	 *
	 * @param map
	 */
	private void getParams(Map<String, String> map) {
		vSettleDate = map.get("sttlDate");
		vHivePath = map.get("HIVE_PATH");
//		vDiffHiveTable = map.get("DIFF_HIVE_TABLE");

		vHiveServer = map.get(kHiveServer);
		vHiveUser = map.get(kHiveUser);
		vHivePwd = map.get(kHivePwd);
		vHdfsServer = map.get(kHdfsServer);

		defualBcpImpTmallUpayDiff = map.get("BCP_SHELL_PARAMS");
		loadDBSrcPath = map.get("LOADDB_SRC_PATH");
		tmallUpayDiffTableAlias = map.get("TMALL_UPAY_DIFF_TABLE_ALIAS");
		bcpErrorFileReg =  map.get("BCP_ERROR_FILE_REG");
		tmallUpayDiffTableHive = map.get("TMALL_UPAY_DIFF_TABLE_HIVE");

		systemRunLogger.debug(map, "天猫统一支付差异文件BCP入库脚本" + defualBcpImpTmallUpayDiff);
		systemRunLogger.debug(map, "hive导出文件存放路径:" + loadDBSrcPath);
		systemRunLogger.debug(map, "天猫与省比对差异表别名:" + tmallUpayDiffTableAlias);
		systemRunLogger.debug(map, "BCP入库错误日志路径" + bcpErrorFileReg);
	}

	/**
	 * 将结果集转换为 UpayBatTmallUpayTxnDiffBean
	 *
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	private TmallEbizUpayDiffBean transToUpayBatTmallUpayTxnBean(ResultSet resultSet)
			throws SQLException {
		systemRunLogger.info(logMsgMap,
				"starting transTobean [TmallEbizUpayDiffBean]..........");

		TmallEbizUpayDiffBean tmallEbizUpayDiffBean = new TmallEbizUpayDiffBean();

		tmallEbizUpayDiffBean.setEbizFlag(StringUtil.parseNullString(
				resultSet.getString(1)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 发起方渠道
		tmallEbizUpayDiffBean.setEbizBussChl(StringUtil.parseNullString(
				resultSet.getString(2)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 发起方渠道
		tmallEbizUpayDiffBean.setEbizActivityCode(StringUtil.parseNullString(
				resultSet.getString(3)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 交易代码
		tmallEbizUpayDiffBean.setEbizBussType(StringUtil.parseNullString(
				resultSet.getString(4)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 交易类型
		tmallEbizUpayDiffBean.setEbizIntTxnDate(StringUtil.parseNullString(
				resultSet.getString(5)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 统一支付交易日期
		tmallEbizUpayDiffBean.setEbizIntTxnSeq(StringUtil.parseNullString(
				resultSet.getString(6)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 统一支付交易流水
		tmallEbizUpayDiffBean.setEbizTmallTranshDt(StringUtil.parseNullString(
				resultSet.getString(7)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 浙江运营中心交易日期
		tmallEbizUpayDiffBean.setEbizTmallTransId(StringUtil.parseNullString(
				resultSet.getString(8)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 浙江运营中心交易流水
		tmallEbizUpayDiffBean.setEbizOrderId(StringUtil.parseNullString(
				resultSet.getString(9)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 订单编号
		tmallEbizUpayDiffBean.setEbizPayTransId(StringUtil.parseNullString(
				resultSet.getString(10)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 支付流水号
		tmallEbizUpayDiffBean.setEbizSettleDate(StringUtil.parseNullString(
				resultSet.getString(11)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 帐期日
		tmallEbizUpayDiffBean.setEbizIdType(StringUtil.parseNullString(
				resultSet.getString(12)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 用户标识类型
		tmallEbizUpayDiffBean.setEbizIdValue(StringUtil.parseNullString(
				resultSet.getString(13)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 用户标识
		tmallEbizUpayDiffBean.setEbizIdProvince(StringUtil.parseNullString(
				resultSet.getString(14)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 归属省代码
		tmallEbizUpayDiffBean.setEbizProductId(StringUtil.parseNullString(
				resultSet.getString(15)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 产品编号
		tmallEbizUpayDiffBean.setEbizProductNum(StringUtil
				.parseStringToLong((resultSet.getInt(16) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 产品数量
		tmallEbizUpayDiffBean.setEbizTxnAmt(StringUtil.parseStringToLong((resultSet
				.getInt(17) + SpeSymbol.BLANK.getValue()).replace(
				SpeSymbol.S_NULL.getValue(),SpeSymbol.BLANK.getValue())));// 充值金额
		tmallEbizUpayDiffBean.setEbizPayedType(StringUtil.parseNullString(
				resultSet.getString(18)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 付费类型
		tmallEbizUpayDiffBean.setEbizMarkActivityNum(StringUtil.parseNullString(
				resultSet.getString(19)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 营销活动号
		tmallEbizUpayDiffBean.setEbizGoodsShelvesNum(StringUtil.parseNullString(
				resultSet.getString(20)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 商品上架编号
		tmallEbizUpayDiffBean.setEbizOrderAmt(StringUtil
				.parseStringToLong((resultSet.getInt(21) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 订单金额
		tmallEbizUpayDiffBean.setEbizSettleAmt(StringUtil
				.parseStringToLong((resultSet.getInt(22) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 结算金额
		tmallEbizUpayDiffBean.setEbizCommissionAmt(StringUtil
				.parseStringToLong((resultSet.getInt(23) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 佣金
		tmallEbizUpayDiffBean.setEbizIntegRebateFee(StringUtil
				.parseStringToLong((resultSet.getInt(24) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 积分返点金额
		tmallEbizUpayDiffBean.setEbizCreditFeeAmt(StringUtil
				.parseStringToLong((resultSet.getInt(25) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 信用卡手续费
		tmallEbizUpayDiffBean.setEbizServiceFee(StringUtil
				.parseStringToLong((resultSet.getInt(26) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 运营服务费用
		tmallEbizUpayDiffBean.setEbizOtherFee1(StringUtil
				.parseStringToLong((resultSet.getInt(27) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 其他费用1
		tmallEbizUpayDiffBean.setEbizOtherFee2(StringUtil
				.parseStringToLong((resultSet.getInt(28) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 其他费用2
		tmallEbizUpayDiffBean.setEbizOtherFee3(StringUtil
				.parseStringToLong((resultSet.getInt(29) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 其他费用3
		tmallEbizUpayDiffBean.setEbizOtherFee4(StringUtil
				.parseStringToLong((resultSet.getInt(30) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 其他费用4
		tmallEbizUpayDiffBean.setEbizOtherFee5(StringUtil
				.parseStringToLong((resultSet.getInt(31) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));// 其他费用5
		tmallEbizUpayDiffBean.setEbizStatus(StringUtil.parseNullString(
				resultSet.getString(32)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 交易状态
		tmallEbizUpayDiffBean.setEbizDiffResult(StringUtil.parseNullString(
				resultSet.getString(33)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 核对结果
		tmallEbizUpayDiffBean.setEbizReserved1(StringUtil.parseNullString(
				resultSet.getString(34)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 备注1
		tmallEbizUpayDiffBean.setEbizReserved2(StringUtil.parseNullString(
				resultSet.getString(35)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 备注2
		tmallEbizUpayDiffBean.setEbizReserved3(StringUtil.parseNullString(
				resultSet.getString(36)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 备注3
		tmallEbizUpayDiffBean.setEbizFileName(StringUtil.parseNullString(
				resultSet.getString(37)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));// 天猫文件名

		tmallEbizUpayDiffBean.setUpaySeqId(StringUtil.parseStringToLong((resultSet.getInt(38) + SpeSymbol.BLANK
				.getValue()).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue())));;
		tmallEbizUpayDiffBean.setUpayBizCode(StringUtil.parseNullString(
				resultSet.getString(39)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayReqChannel(StringUtil.parseNullString(
				resultSet.getString(40)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpaySettleDate(StringUtil.parseNullString(
				resultSet.getString(41)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayReqDateTime(StringUtil.parseNullString(
				resultSet.getString(42)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayTmallTransId(StringUtil.parseNullString(
				resultSet.getString(43)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayBizSeq(StringUtil.parseNullString(
				resultSet.getString(44)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmTransId(StringUtil.parseNullString(
				resultSet.getString(45)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpaySessionId(StringUtil.parseNullString(StringUtil
				.parseNullString(resultSet.getString(46) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayBussTransId(StringUtil.parseNullString(
				resultSet.getString(47)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayTransactionId(StringUtil.parseNullString(
				resultSet.getString(48)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayOrderId(StringUtil.parseNullString(
				resultSet.getString(49)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayPayTransId(StringUtil.parseNullString(
				resultSet.getString(50)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));

		tmallEbizUpayDiffBean.setUpayIdType(StringUtil.parseNullString(StringUtil.parseNullString(resultSet.getString(51)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayIdValue(StringUtil.parseNullString(StringUtil.parseNullString(resultSet.getString(52)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayHomeProv(StringUtil.parseNullString(resultSet.getString(53)).replace(SpeSymbol.S_NULL.getValue(), SpeSymbol.BLANK.getValue()));

		tmallEbizUpayDiffBean.setUpayPayment(StringUtil.parseStringToLong((resultSet.getInt(54) + SpeSymbol.BLANK
				.getValue()).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue())));;
		tmallEbizUpayDiffBean.setUpayChargeMoney(StringUtil.parseStringToLong((resultSet.getInt(55) + SpeSymbol.BLANK
				.getValue()).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayProdCnt(StringUtil
				.parseStringToLong((resultSet.getInt(56) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));

		tmallEbizUpayDiffBean.setUpayProdId(StringUtil.parseNullString(resultSet.getString(57)).replace(SpeSymbol.S_NULL.getValue(), SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCommision(StringUtil
				.parseStringToLong((resultSet.getInt(58) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayRebateFee(StringUtil
				.parseStringToLong((resultSet.getInt(59) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayProdDiscount(StringUtil.parseStringToLong((resultSet.getInt(60) + SpeSymbol.BLANK
				.getValue()).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayCreditCardFee(StringUtil
				.parseStringToLong((resultSet.getInt(61) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));
		tmallEbizUpayDiffBean.setUpayServiceFee(StringUtil
				.parseStringToLong((resultSet.getInt(62) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));

		tmallEbizUpayDiffBean.setUpayPayedType(StringUtil.parseNullString(
				resultSet.getString(63)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayActivityNo(StringUtil.parseNullString(
				resultSet.getString(64)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayProdShelfNo(StringUtil.parseNullString(
				resultSet.getString(65)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmRspCode(StringUtil.parseNullString(
				resultSet.getString(66)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmRspInfo(StringUtil.parseNullString(
				resultSet.getString(67)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmSubRspCode(StringUtil.parseNullString(
				resultSet.getString(68)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmSubRspInfo(StringUtil.parseNullString(
				resultSet.getString(69)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayTmallRspCode(StringUtil.parseNullString(
				resultSet.getString(70)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayTmallRspInfo(StringUtil.parseNullString(
				resultSet.getString(71)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayActionDate(StringUtil.parseNullString(
				resultSet.getString(72)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmRspType(StringUtil.parseNullString(
				resultSet.getString(73)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayLastUpdTime(StringUtil.parseNullString(
				resultSet.getString(74)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayFileName(StringUtil.parseNullString(
				resultSet.getString(75)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmBipCode(StringUtil.parseNullString(
				resultSet.getString(76)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmBipCode(StringUtil.parseNullString(
				resultSet.getString(77)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmOrgId(StringUtil.parseNullString(
				resultSet.getString(78)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmStartTm(StringUtil.parseNullString(
				resultSet.getString(79)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayCrmTranshTm(StringUtil.parseNullString(
				resultSet.getString(80)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayPayMode(StringUtil.parseNullString(
				resultSet.getString(81)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayReconciliationFlag(StringUtil.parseNullString(
				resultSet.getString(82)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));


		tmallEbizUpayDiffBean.setUpayResendCount(StringUtil
				.parseStringToLong((resultSet.getInt(83) + SpeSymbol.BLANK
						.getValue()).replace(SpeSymbol.S_NULL.getValue(),
						SpeSymbol.BLANK.getValue())));

		tmallEbizUpayDiffBean.setUpayReserved1(StringUtil.parseNullString(
				resultSet.getString(84)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayReserved2(StringUtil.parseNullString(
				resultSet.getString(85)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayReserved3(StringUtil.parseNullString(
				resultSet.getString(86)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));
		tmallEbizUpayDiffBean.setUpayReserved4(StringUtil.parseNullString(
				resultSet.getString(87)).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));


		tmallEbizUpayDiffBean.setDiffType((resultSet.getInt(88) + SpeSymbol.BLANK
				.getValue()).replace(SpeSymbol.S_NULL.getValue(),
				SpeSymbol.BLANK.getValue()));


		return tmallEbizUpayDiffBean;
	}

	private UpayBatTmallUpayTxnDiffBean setTmallTxnValue(TmallEbizUpayDiffBean tmallEbizUpayDiff,
			String diffType) {
		UpayBatTmallUpayTxnDiffBean upayBatTmallUpayTxn = new UpayBatTmallUpayTxnDiffBean();
		if (CommonConstant.COMPARE_TAMLL_UPAY_MORE_TYPE.equals(diffType)
				|| CommonConstant.COMPARE_TAMLL_UPAY_DIFF_TYPE.equals(diffType)
				|| CommonConstant.COMPARE_TAMLL_UPAY_EQUAL_TYPE.equals(diffType)) {
			upayBatTmallUpayTxn.setIntTxnSeq(tmallEbizUpayDiff.getEbizIntTxnSeq());
			upayBatTmallUpayTxn.setBusiTransId(tmallEbizUpayDiff.getEbizTmallTransId());
			upayBatTmallUpayTxn.setPayTransId(tmallEbizUpayDiff.getEbizPayTransId());
			upayBatTmallUpayTxn.setSettleDate(tmallEbizUpayDiff.getEbizSettleDate());
			upayBatTmallUpayTxn.setOrderType(tmallEbizUpayDiff.getEbizBussType());
			upayBatTmallUpayTxn.setBussType(tmallEbizUpayDiff.getEbizBussType());
			upayBatTmallUpayTxn.setIdType(tmallEbizUpayDiff.getEbizIdType());
			upayBatTmallUpayTxn.setIdValue(tmallEbizUpayDiff.getEbizIdValue());
			upayBatTmallUpayTxn.setTxnAmt(tmallEbizUpayDiff.getEbizTxnAmt());
			upayBatTmallUpayTxn.setBussChl(tmallEbizUpayDiff.getEbizBussChl());
			upayBatTmallUpayTxn.setPayedType(tmallEbizUpayDiff.getEbizPayedType());
			upayBatTmallUpayTxn.setOrderId(tmallEbizUpayDiff.getEbizOrderId());
			upayBatTmallUpayTxn.setProductId(tmallEbizUpayDiff.getEbizProductId());
			upayBatTmallUpayTxn.setOrderAmt(tmallEbizUpayDiff.getEbizOrderAmt());
			upayBatTmallUpayTxn.setSettleAmt(tmallEbizUpayDiff.getEbizSettleAmt());
			upayBatTmallUpayTxn.setProductNum(tmallEbizUpayDiff.getEbizProductNum());
			upayBatTmallUpayTxn.setMarkActivityNum(tmallEbizUpayDiff.getEbizMarkActivityNum());
			upayBatTmallUpayTxn.setGoodsShelvesNum(tmallEbizUpayDiff.getEbizGoodsShelvesNum());
			upayBatTmallUpayTxn.setCommissionAmt(tmallEbizUpayDiff.getEbizCommissionAmt());
			upayBatTmallUpayTxn.setServerFee(tmallEbizUpayDiff.getEbizServiceFee());
			upayBatTmallUpayTxn.setIntegRebateFee(tmallEbizUpayDiff.getEbizIntegRebateFee());
			upayBatTmallUpayTxn.setCreditFeeAmt(tmallEbizUpayDiff.getEbizCreditFeeAmt());
			upayBatTmallUpayTxn.setOtherFee1(tmallEbizUpayDiff.getEbizOtherFee1());
			upayBatTmallUpayTxn.setOtherFee2(tmallEbizUpayDiff.getEbizOtherFee2());
			upayBatTmallUpayTxn.setOtherFee3(tmallEbizUpayDiff.getEbizOtherFee3());
			upayBatTmallUpayTxn.setOtherFee4(tmallEbizUpayDiff.getEbizOtherFee4());
			upayBatTmallUpayTxn.setOtherFee5(tmallEbizUpayDiff.getEbizOtherFee5());
			upayBatTmallUpayTxn.setReserved1(tmallEbizUpayDiff.getEbizReserved1());
			upayBatTmallUpayTxn.setReserved2(tmallEbizUpayDiff.getEbizReserved2());
			upayBatTmallUpayTxn.setReserved3(tmallEbizUpayDiff.getEbizReserved3());

			upayBatTmallUpayTxn.setIdProvince(tmallEbizUpayDiff.getEbizIdProvince());
			upayBatTmallUpayTxn.setActivityCode(tmallEbizUpayDiff.getEbizActivityCode());
			upayBatTmallUpayTxn.setTmallTransId(tmallEbizUpayDiff.getEbizTmallTransId());
			upayBatTmallUpayTxn.setStatus(tmallEbizUpayDiff.getEbizStatus());
			upayBatTmallUpayTxn.setFileName(tmallEbizUpayDiff.getEbizFileName());
			// upayBatTmallUpayTxn.setDiffResult(tmallEbizUpayDiff.getDiffType());
			upayBatTmallUpayTxn.setDiffType(tmallEbizUpayDiff.getDiffType());

			upayBatTmallUpayTxn.setDiffResult(diffType);
			upayBatTmallUpayTxn.setDiffPower(CommonConstant.ORG_SIDE_TYPE_EBIZ);
			upayBatTmallUpayTxn.setReqOrgId(orgId);
			upayBatTmallUpayTxn.setLastUpdTime(DateUtil.getLongDate());
			upayBatTmallUpayTxn.setIntTxnDate(tmallEbizUpayDiff.getEbizIntTxnDate());
			upayBatTmallUpayTxn.setTmallTranshDt(tmallEbizUpayDiff.getEbizTmallTranshDt());
		} else if (CommonConstant.COMPARE_TAMLL_UPAY_LESS_TYPE.equals(diffType)) {
			// 操作流水号
			upayBatTmallUpayTxn.setIntTxnSeq(tmallEbizUpayDiff.getUpayTransactionId());
			upayBatTmallUpayTxn.setBusiTransId(tmallEbizUpayDiff.getUpayBussTransId());
			upayBatTmallUpayTxn.setPayTransId(tmallEbizUpayDiff.getUpayPayTransId());
			upayBatTmallUpayTxn.setSettleDate(tmallEbizUpayDiff.getUpaySettleDate());

			upayBatTmallUpayTxn.setReqOrgId(tmallEbizUpayDiff.getUpayCrmOrgId());
			// upayBatTmallUpayTxn.setReqOrgId(orgId);
			upayBatTmallUpayTxn.setIdType(tmallEbizUpayDiff.getUpayIdType());
			upayBatTmallUpayTxn.setIdValue(tmallEbizUpayDiff.getUpayIdValue());
			upayBatTmallUpayTxn.setTxnAmt(tmallEbizUpayDiff.getUpayChargeMoney());
			upayBatTmallUpayTxn.setBussChl(tmallEbizUpayDiff.getUpayReqChannel());
			upayBatTmallUpayTxn.setPayedType(tmallEbizUpayDiff.getUpayPayedType());
			upayBatTmallUpayTxn.setOrderId(tmallEbizUpayDiff.getUpayOrderId());
			upayBatTmallUpayTxn.setProductId(tmallEbizUpayDiff.getUpayProdId());
			upayBatTmallUpayTxn.setOrderAmt(tmallEbizUpayDiff.getUpayPayment());
			// upayBatTmallUpayTxn.setSettleAmt();
			upayBatTmallUpayTxn.setProductNum(tmallEbizUpayDiff.getUpayProdCnt());
			upayBatTmallUpayTxn.setMarkActivityNum(tmallEbizUpayDiff.getUpayActivityNo());
			upayBatTmallUpayTxn.setGoodsShelvesNum(tmallEbizUpayDiff.getUpayProdShelfNo());
			upayBatTmallUpayTxn.setCommissionAmt(tmallEbizUpayDiff.getUpayCommision());
			upayBatTmallUpayTxn.setServerFee(tmallEbizUpayDiff.getUpayServiceFee());
			upayBatTmallUpayTxn.setIntegRebateFee(tmallEbizUpayDiff.getUpayRebateFee());
			upayBatTmallUpayTxn.setCreditFeeAmt(tmallEbizUpayDiff.getUpayCreditCardFee());
			upayBatTmallUpayTxn.setStatus(tmallEbizUpayDiff.getUpayCrmSubRspCode());

			upayBatTmallUpayTxn.setDiffResult(diffType);
			upayBatTmallUpayTxn.setDiffType(tmallEbizUpayDiff.getDiffType());
			upayBatTmallUpayTxn.setFileName(tmallEbizUpayDiff.getUpayFileName());
			upayBatTmallUpayTxn.setLastUpdTime(DateUtil.getLongDate());
			// upayBatTmallUpayTxn.setTmallTranshDt();
			upayBatTmallUpayTxn.setTmallTransId(tmallEbizUpayDiff.getUpayTmallTransId());
			// upayBatTmallUpayTxn.setIntTxnDate();
			upayBatTmallUpayTxn.setIdProvince(idProvince);
			upayBatTmallUpayTxn.setActivityCode(tmallEbizUpayDiff.getUpayBizCode());
			upayBatTmallUpayTxn.setDiffPower(CommonConstant.ORG_SIDE_TYPE_EBIZ);
			upayBatTmallUpayTxn.setReserved1(tmallEbizUpayDiff.getUpayReserved1());
			upayBatTmallUpayTxn.setReserved2(tmallEbizUpayDiff.getUpayReserved2());
			upayBatTmallUpayTxn.setReserved3(tmallEbizUpayDiff.getUpayReserved3());

		}
		upayBatTmallUpayTxn.setDirectionType(CommonConstant.ORG_SIDE_TYPE_EBIZ);
		return upayBatTmallUpayTxn;
	}

	private void batchDiffBossInsert(final List<UpayBatTmallUpayTxnDiffBean> list) {
		jdbcTemplate.batchUpdate(DIFFSQL, new BatchPreparedStatementSetter() {
			private UpayBatTmallUpayTxnDiffBean upayBatTmallUpayTxnDiffBean = null;

			@Override
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
				upayBatTmallUpayTxnDiffBean = list.get(i);
				ps.setString(1, upayBatTmallUpayTxnDiffBean.getIntTxnSeq());
				ps.setString(2, upayBatTmallUpayTxnDiffBean.getBusiTransId());
				ps.setString(3, upayBatTmallUpayTxnDiffBean.getPayTransId());
				ps.setString(4, upayBatTmallUpayTxnDiffBean.getSettleDate());
				ps.setString(5, upayBatTmallUpayTxnDiffBean.getOrderType());
				ps.setString(6, upayBatTmallUpayTxnDiffBean.getBussType());
				ps.setString(7, upayBatTmallUpayTxnDiffBean.getReqOrgId());
				ps.setString(8, upayBatTmallUpayTxnDiffBean.getIdType());
				ps.setString(9, upayBatTmallUpayTxnDiffBean.getIdValue());

				Long txnAmt = upayBatTmallUpayTxnDiffBean.getTxnAmt();
				if (txnAmt == null) {
					txnAmt = 0L;
				}
				ps.setLong(10, txnAmt);

				ps.setString(11, upayBatTmallUpayTxnDiffBean.getBussChl());
				ps.setString(12, upayBatTmallUpayTxnDiffBean.getPayedType());
				ps.setString(13, upayBatTmallUpayTxnDiffBean.getOrderId());
				ps.setString(14, upayBatTmallUpayTxnDiffBean.getProductId());

				Long orderAmt = upayBatTmallUpayTxnDiffBean.getOrderAmt();
				if (orderAmt == null) {
					orderAmt = 0L;
				}
				ps.setLong(15, orderAmt);

				Long settleAmt = upayBatTmallUpayTxnDiffBean.getSettleAmt();
				if (settleAmt == null) {
					settleAmt = 0L;
				}
				ps.setLong(16, settleAmt);

				Long productNum = upayBatTmallUpayTxnDiffBean.getProductNum();
				if (productNum == null) {
					productNum = 0L;
				}
				ps.setLong(17, productNum);
				ps.setString(18, upayBatTmallUpayTxnDiffBean.getMarkActivityNum());
				ps.setString(19, upayBatTmallUpayTxnDiffBean.getGoodsShelvesNum());

				Long commissionAmt = upayBatTmallUpayTxnDiffBean.getCommissionAmt();
				if (commissionAmt == null) {
					commissionAmt = 0L;
				}
				ps.setLong(20, commissionAmt);

				Long serverFee = upayBatTmallUpayTxnDiffBean.getServerFee();
				if (serverFee == null) {
					serverFee = 0L;
				}
				ps.setLong(21, serverFee);

				Long integRebateFee = upayBatTmallUpayTxnDiffBean.getIntegRebateFee();
				if (integRebateFee == null) {
					integRebateFee = 0L;
				}
				ps.setLong(22, integRebateFee);

				Long creditFeeAmt = upayBatTmallUpayTxnDiffBean.getCreditFeeAmt();
				if (creditFeeAmt == null) {
					creditFeeAmt = 0L;
				}
				ps.setLong(23, creditFeeAmt);

				ps.setString(24, upayBatTmallUpayTxnDiffBean.getStatus());

				Long otherFee1 = upayBatTmallUpayTxnDiffBean.getOtherFee1();
				if (otherFee1 == null) {
					otherFee1 = 0L;
				}
				ps.setLong(25, otherFee1);

				Long otherFee2 = upayBatTmallUpayTxnDiffBean.getOtherFee2();
				if (otherFee2 == null) {
					otherFee2 = 0L;
				}
				ps.setLong(26, otherFee2);

				Long otherFee3 = upayBatTmallUpayTxnDiffBean.getOtherFee3();
				if (otherFee3 == null) {
					otherFee3 = 0L;
				}
				ps.setLong(27, otherFee3);

				Long otherFee4 = upayBatTmallUpayTxnDiffBean.getOtherFee4();
				if (otherFee4 == null) {
					otherFee4 = 0L;
				}
				ps.setLong(28, otherFee4);

				Long otherFee5 = upayBatTmallUpayTxnDiffBean.getOtherFee5();
				if (otherFee5 == null) {
					otherFee5 = 0L;
				}
				ps.setLong(29, otherFee5);
				ps.setString(30, upayBatTmallUpayTxnDiffBean.getDiffResult());
				ps.setString(31, upayBatTmallUpayTxnDiffBean.getDiffType());
				ps.setString(32, upayBatTmallUpayTxnDiffBean.getFileName());
				ps.setString(33, upayBatTmallUpayTxnDiffBean.getLastUpdTime());
				ps.setString(34, upayBatTmallUpayTxnDiffBean.getTmallTranshDt());
				ps.setString(35, upayBatTmallUpayTxnDiffBean.getTmallTransId());
				ps.setString(36, upayBatTmallUpayTxnDiffBean.getIntTxnDate());
				ps.setString(37, upayBatTmallUpayTxnDiffBean.getIdProvince());
				ps.setString(38, upayBatTmallUpayTxnDiffBean.getActivityCode());
				ps.setString(39, upayBatTmallUpayTxnDiffBean.getDiffPower());
				ps.setString(40, upayBatTmallUpayTxnDiffBean.getReserved1());
				ps.setString(41, upayBatTmallUpayTxnDiffBean.getReserved2());
				ps.setString(42, upayBatTmallUpayTxnDiffBean.getReserved3());
				ps.setString(43, upayBatTmallUpayTxnDiffBean.getDirectionType());

			}

					@Override
					public int getBatchSize() {
						return list.size();
					}
				});
	}
}
