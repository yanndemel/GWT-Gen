package com.hiperf.common.rebind;


public class MessagesReference {

	private static String messageProviderStaticReference = null;



	public static void setMessageProviderStaticReference(
			String messageProviderStaticReference) {
		MessagesReference.messageProviderStaticReference = messageProviderStaticReference;
	}


	public static String getMessageProviderStaticReference() {
		return MessagesReference.messageProviderStaticReference;
	}



}
