package com.hiperf.common.ui.server.listener;

public abstract class LicenseProvider {
	
	private static LicenseProvider instance;

	public static LicenseProvider getInstance() {
		return instance;
	}
	
	



	public static void setInstance(LicenseProvider instance) {
		LicenseProvider.instance = instance;
	}





	public abstract void startLicenseCheck();





	public abstract void endLicenseCheck();
	
	

}
