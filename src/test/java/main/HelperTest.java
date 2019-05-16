package main;

import static org.hamcrest.CoreMatchers.is; 
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
@RunWith(JUnitPlatform.class)

class HelperTest {

	@Test
	void testMake2() {
		assertThat(Helper.make2(1), is("01"));
		assertThat(Helper.make2(21), is("21"));
	}
	
	@Test
	void testMake12() {
		assertThat(Helper.make12("1"), is("           1 "));
		assertThat(Helper.make12("121"), is("         121 "));
	}
	
	@Test
	void testMake12NPE() { 
		Assertions.assertThrows(NullPointerException.class, () -> {
			Helper.make12(null); 
		});
	}
	

}
