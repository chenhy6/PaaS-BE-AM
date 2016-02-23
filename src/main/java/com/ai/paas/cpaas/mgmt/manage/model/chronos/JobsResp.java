package com.ai.paas.cpaas.mgmt.manage.model.chronos;

import java.util.List;

import com.ai.paas.cpaas.mgmt.manage.model.GeneralHttpResp;

public class JobsResp extends GeneralHttpResp {
	private List<ChronosJob> jobs;

	public List<ChronosJob> getJobs() {
		return jobs;
	}

	public void setJobs(List<ChronosJob> jobs) {
		this.jobs = jobs;
	}

}
