package com.raveer.disir.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.raveer.disir.demo.AnnotationDemo;
import com.raveer.disir.demo.BeanDemo;

public class TestDisir {
	@Test
	public void testBeanPropertyManager() {
		BeanDemo bd = new BeanDemo();
		assertThat("SHA-1").isEqualTo(bd.getKeyRingHash());
		assertThat("32").isEqualTo(bd.getKeyRingSalt());
	}
	@Test
	public void testFieldAnnotation() {
		AnnotationDemo ad = new AnnotationDemo();
		assertThat("SHA-256").isEqualTo(ad.getKeyRingHash());
		assertThat("128").isEqualTo(ad.getKeyRingSalt());
	}
}