package com.ai.paas.cpaas.mgmt.manage.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ai.paas.cpaas.mgmt.dao.mapper.bo.AppReqInfo;
import com.ai.paas.cpaas.mgmt.dao.mapper.bo.AppTaskDetail;
import com.ai.paas.cpaas.mgmt.dao.mapper.bo.AppTaskLog;
import com.ai.paas.cpaas.mgmt.dao.mapper.bo.ResClusterInfo;
import com.ai.paas.cpaas.mgmt.interfaces.IDeployServiceManager;
import com.ai.paas.cpaas.mgmt.manage.model.ActionType;
import com.ai.paas.cpaas.mgmt.manage.model.GeneralReq;
import com.ai.paas.cpaas.mgmt.manage.model.GeneralReq.Container;
import com.ai.paas.cpaas.mgmt.manage.model.GeneralDeployResp;
import com.ai.paas.cpaas.mgmt.manage.model.LogReq;
import com.ai.paas.cpaas.mgmt.manage.model.LogResp;
import com.ai.paas.cpaas.mgmt.manage.model.LogResp.Log;
import com.ai.paas.cpaas.mgmt.manage.model.LogResp.Task;
import com.ai.paas.cpaas.mgmt.manage.model.StatusResp;
import com.ai.paas.cpaas.mgmt.manage.model.TaskStateType;
import com.ai.paas.cpaas.mgmt.manage.model.marathon.GetAppResp;
import com.ai.paas.cpaas.mgmt.manage.thread.CreateLongRun;
import com.ai.paas.cpaas.mgmt.manage.thread.DestroyApp;
import com.ai.paas.cpaas.mgmt.manage.thread.ScaleApp;
import com.ai.paas.cpaas.mgmt.manage.thread.UpgradeApp;
import com.ai.paas.cpaas.mgmt.service.IAppReqInfoService;
import com.ai.paas.cpaas.mgmt.service.IAppTaskDetailService;
import com.ai.paas.cpaas.mgmt.service.IAppTaskLogService;
import com.ai.paas.cpaas.mgmt.service.IResClusterInfoService;
import com.ai.paas.cpaas.mgmt.service.remote.DirectRemoteService;
import com.ai.paas.ipaas.PaaSBeanFactory;
import com.ai.paas.ipaas.PaaSMgmtConstant;
import com.alibaba.dubbo.config.annotation.Service;
import com.google.gson.Gson;

@Service
public class DeployServiceManage implements IDeployServiceManager {
	private static Logger logger = Logger.getLogger(DeployServiceManage.class);
	@Autowired
	private IAppReqInfoService appReqInfoService;
	@Autowired
	private IAppTaskDetailService appTaskDetailService;
	@Autowired
	private IAppTaskLogService appTaskLogService;
	@Autowired
	private IResClusterInfoService resClusterInfoService;

	@Override
	public String createLongRun(String param) {
		Gson gson = new Gson();
		GeneralDeployResp generalResp = new GeneralDeployResp();
		int reqId = -1;

		try {
			GeneralReq createReq = gson.fromJson(param, GeneralReq.class);
			generalResp = gson.fromJson(param, GeneralDeployResp.class);
			reqId = appReqInfoService.saveReqInfo(createReq, param, ActionType.deploy);
			CreateLongRun createLongRun = PaaSBeanFactory.getBean("createLongRun", CreateLongRun.class);
			createLongRun.setReqId(reqId);
			createLongRun.setAppId(createReq.getAppId());
			createLongRun.setGeneralReq(createReq);
			createLongRun.setActionType(ActionType.deploy);
			new Thread(createLongRun).start();

			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_SUCCESS);
			generalResp.setResultMsg("deploy started");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_FAIL);
			generalResp.setResultMsg("deploy failed because exception");
		}
		generalResp.setReqId(reqId);
		appReqInfoService.updateReqInfo(reqId, generalResp);
		return gson.toJson(generalResp);
	}
	
	@Override
	public String destroyLongRun(String param) {
		Gson gson = new Gson();
		GeneralDeployResp generalResp = new GeneralDeployResp();
		int reqId = -1;
		try {
			GeneralReq destroyReq = gson.fromJson(param, GeneralReq.class);
			reqId = appReqInfoService.saveReqInfo(destroyReq, param, ActionType.destroy);
			generalResp = gson.fromJson(param, GeneralDeployResp.class);
			DestroyApp destroyApp = PaaSBeanFactory.getBean("destroyApp", DestroyApp.class);
			destroyApp.setReqId(reqId);
			destroyApp.setAppId(destroyReq.getAppId());
			destroyApp.setGeneralReq(destroyReq);
			destroyApp.setActionType(ActionType.destroy);
			new Thread(destroyApp).start();

			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_SUCCESS);
			generalResp.setResultMsg("deploy started");
		} catch (Exception e) {
			logger.error(e);
			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_FAIL);
			generalResp.setResultMsg("deploy failed because exception");
		}
		generalResp.setReqId(reqId);
		appReqInfoService.updateReqInfo(reqId, generalResp);
		return gson.toJson(generalResp);
	}

	@Override
	public String createTimer(String param) {
		return null;
	}

	@Override
	public String start(String param) {
		Gson gson = new Gson();
		GeneralDeployResp generalResp = new GeneralDeployResp();
		int reqId = -1;

		try {
			GeneralReq scaleReq = gson.fromJson(param, GeneralReq.class);
			generalResp = gson.fromJson(param, GeneralDeployResp.class);
			reqId = appReqInfoService.saveReqInfo(scaleReq, param, ActionType.start);
			ScaleApp scaleApp = PaaSBeanFactory.getBean("scaleApp", ScaleApp.class);
			scaleApp.setReqId(reqId);
			scaleApp.setAppId(scaleReq.getAppId());
			scaleApp.setGeneralReq(scaleReq);
			scaleApp.setActionType(ActionType.start);
			new Thread(scaleApp).start();

			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_SUCCESS);
			generalResp.setResultMsg("deploy started");
		} catch (Exception e) {
			logger.error(e);
			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_FAIL);
			generalResp.setResultMsg("deploy failed because exception");
		}
		generalResp.setReqId(reqId);
		appReqInfoService.updateReqInfo(reqId, generalResp);
		return gson.toJson(generalResp);
	}

	@Override
	public String stop(String param) {
		Gson gson = new Gson();
		GeneralDeployResp generalResp = new GeneralDeployResp();
		int reqId = -1;

		try {
			GeneralReq scaleReq = gson.fromJson(param, GeneralReq.class);
			reqId = appReqInfoService.saveReqInfo(scaleReq, param, ActionType.stop);
			generalResp = gson.fromJson(param, GeneralDeployResp.class);
			if (CollectionUtils.isNotEmpty(scaleReq.getContainers())) {
				for (Container container : scaleReq.getContainers())
					container.setInstances(0);
			}
			ScaleApp scaleApp = PaaSBeanFactory.getBean("scaleApp", ScaleApp.class);
			scaleApp.setReqId(reqId);
			scaleApp.setAppId(scaleReq.getAppId());
			scaleApp.setGeneralReq(scaleReq);
			scaleApp.setActionType(ActionType.stop);
			new Thread(scaleApp).start();

			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_SUCCESS);
			generalResp.setResultMsg("deploy started");
		} catch (Exception e) {
			logger.error(e);
			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_FAIL);
			generalResp.setResultMsg("deploy failed because exception");
		}
		generalResp.setReqId(reqId);
		appReqInfoService.updateReqInfo(reqId, generalResp);
		return gson.toJson(generalResp);
	}

	@Override
	public String scale(String param) {
		Gson gson = new Gson();
		GeneralDeployResp generalResp = new GeneralDeployResp();
		int reqId = -1;

		try {
			GeneralReq scaleReq = gson.fromJson(param, GeneralReq.class);
			reqId = appReqInfoService.saveReqInfo(scaleReq, param, ActionType.scale);
			generalResp = gson.fromJson(param, GeneralDeployResp.class);
			ScaleApp scaleApp = PaaSBeanFactory.getBean("scaleApp", ScaleApp.class);
			scaleApp.setReqId(reqId);
			scaleApp.setAppId(scaleReq.getAppId());
			scaleApp.setGeneralReq(scaleReq);
			scaleApp.setActionType(ActionType.scale);
			new Thread(scaleApp).start();

			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_SUCCESS);
			generalResp.setResultMsg("deploy started");
		} catch (Exception e) {
			logger.error(e);
			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_FAIL);
			generalResp.setResultMsg("deploy failed because exception");
		}
		generalResp.setReqId(reqId);
		appReqInfoService.updateReqInfo(reqId, generalResp);
		return gson.toJson(generalResp);
	}

	@Override
	public String upgrade(String param) {
		Gson gson = new Gson();
		GeneralDeployResp generalResp = new GeneralDeployResp();
		int reqId = -1;
		try {
			GeneralReq upgradeReq = gson.fromJson(param, GeneralReq.class);
			reqId = appReqInfoService.saveReqInfo(upgradeReq, param, ActionType.upgrade);
			generalResp = gson.fromJson(param, GeneralDeployResp.class);
			UpgradeApp upgradeApp = PaaSBeanFactory.getBean("upgradeApp", UpgradeApp.class);
			upgradeApp.setReqId(reqId);
			upgradeApp.setAppId(upgradeReq.getAppId());
			upgradeApp.setGeneralReq(upgradeReq);
			upgradeApp.setActionType(ActionType.upgrade);
			new Thread(upgradeApp).start();

			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_SUCCESS);
			generalResp.setResultMsg("deploy started");
		} catch (Exception e) {
			logger.error(e);
			generalResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_FAIL);
			generalResp.setResultMsg("deploy failed because exception");
		}
		generalResp.setReqId(reqId);
		appReqInfoService.updateReqInfo(reqId, generalResp);
		return gson.toJson(generalResp);
	}

	@Override
	public String log(String param) {
		Gson gson = new Gson();
		LogResp logResp = new LogResp();
		LogReq logReq = gson.fromJson(param, LogReq.class);
		logResp = gson.fromJson(param, LogResp.class);
		AppReqInfo appReqInfo = appReqInfoService.getReqInfo(logReq.getReqId());
		logResp.setActionType(ActionType.valueOf(appReqInfo.getActionType()));
		List<AppTaskDetail> taskDetails = appTaskDetailService.getTasksByReq(logReq.getReqId());
		if (CollectionUtils.isNotEmpty(taskDetails)) {
			List<Task> tasks = new ArrayList<>();
			for (AppTaskDetail appTaskDetail : taskDetails) {
				Task task = new Task();
				task.setStartTime(appTaskDetail.getTaskStartTime().toString());
				task.setEndTime(appTaskDetail.getTaskEndTime().toString());
				task.setTaskName(appTaskDetail.getTaskName());
				task.setTaskState(TaskStateType.valueOf(appTaskDetail.getTaskState()));
				List<AppTaskLog> appTaskLogs = appTaskLogService.getTaskLogs(appTaskDetail.getTaskId());
				if (CollectionUtils.isNotEmpty(appTaskLogs)) {
					List<Log> logs = new ArrayList<>();
					for (AppTaskLog appTaskLog : appTaskLogs) {
						Log log = new Log();
						log.setLogTime(appTaskLog.getLogTime().toString());
						log.setLogCnt(appTaskLog.getLogCnt());
						logs.add(log);
					}
					task.setLogs(logs);
				}
				tasks.add(task);
			}
			logResp.setTasks(tasks);
		}
		return gson.toJson(logResp);
	}

	@Override
	public String status(String param) {
		Gson gson = new Gson();
		StatusResp statusResp = new StatusResp();

		try {
			GeneralReq statusReq = gson.fromJson(param, GeneralReq.class);
			ResClusterInfo resClusterInfo = resClusterInfoService.getClusterInfo(statusReq.getClusterId());
			DirectRemoteService clusterProxy = new DirectRemoteService(resClusterInfo);
			List<GetAppResp> status = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(statusReq.getContainers()))
				for (Container container : statusReq.getContainers()) {
					GetAppResp getAppResp = clusterProxy.getContainerInfo(container.getContainerName(), GetAppResp.class);
					status.add(getAppResp);
				}
			statusResp.setStatus(status);
			statusResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_SUCCESS);
			statusResp.setResultMsg("deploy started");
		} catch (Exception e) {
			logger.error(e);
			statusResp.setResultCode(PaaSMgmtConstant.REST_SERVICE_RESULT_FAIL);
			statusResp.setResultMsg("deploy failed because exception");
		}
		return gson.toJson(statusResp);
	}

	public String testCallback(String param) {
		Gson gson = new Gson();
		GeneralDeployResp generalResp = new GeneralDeployResp();
		generalResp = gson.fromJson(param, GeneralDeployResp.class);
		logger.info("test callback param : " + param);
		System.out.println(param);
		return gson.toJson(generalResp);
	}

	@Override
	public String destroyTimer(String param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String startTimer(String param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String upgradeTimer(String param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String statusTimer(String param) {
		// TODO Auto-generated method stub
		return null;
	}
}