package org.nlpcn.jcoder.domain;

/**
 * Created by Ansj on 28/12/2017.
 */
public class TaskStatistics {
	private String hostPort;

	private long success;

	private long error;

	private long sumWeight;

	private long weight;

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}

	public long getSuccess() {
		return success;
	}

	public void setSuccess(long success) {
		this.success = success;
	}

	public long getError() {
		return error;
	}

	public void setError(long error) {
		this.error = error;
	}

	public String getSuccessAlias() {
		return num2Str(success);
	}


	public String getErrorAlias() {
		return num2Str(error);
	}

	private String num2Str(long error) {
		String result = String.valueOf(error);
		if (error > 999999) {
			result = (error / 1000) + "k";
		} else if (error > 999999999) {
			result = (error / 1000000) + "m";
		}
		return result;
	}

	public long getSumWeight() {
		if (sumWeight == 0) {
			sumWeight = 1;
		}
		return sumWeight;
	}

	public void setSumWeight(long sumWeight) {
		this.sumWeight = sumWeight;
	}

	public long getWeight() {
		return weight;
	}

	public void setWeight(long weight) {
		this.weight = weight;
	}
}
