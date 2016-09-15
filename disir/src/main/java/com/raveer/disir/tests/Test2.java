package com.raveer.disir.tests;

import com.raveer.disir.PropertyManager;

public class Test2 {

	public static void main(String[] args) {
		PropertyManager pm = new PropertyManager();
		pm.DEFAULT_NAMESPACE="WILD WEST";
		
		System.out.println(pm.getProperty("foo", "bar"));
	}

}
