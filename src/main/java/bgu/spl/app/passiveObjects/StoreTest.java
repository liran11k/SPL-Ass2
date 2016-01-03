package bgu.spl.app.passiveObjects;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {

	@Before
	public void setUp() throws Exception {
		Store.getInstance().initialize();
	}

	@After
	public void tearDown() throws Exception {
		Store.getInstance().initialize();
	}
	
	@Test
	public void testEmptyStorage() {
		assertEquals(0, (double)Store.getInstance().getStorage().length, 0);
	}
	
	@Test
	public void testEmptyReceipts() {
		assertEquals(0, (double)Store.getInstance().getReceipts().length,0);
	}
	
	@Test
	public void testLoad() {
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 0), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		assertArrayEquals(storage, Store.getInstance().getStorage());
	}
	
	@Test
	public void testStorageLoadSize() {
		Store.getInstance();
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 0), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		assertEquals(3, (double)Store.getInstance().getStorageSize() ,3);
	}
	
	@Test
	public void testReceiptFile() {
		Receipt receipt = new Receipt("seller", "customer", "shoe", true, 0, 1, 1);
		Store.getInstance().file(receipt);
		assertEquals(receipt,Store.getInstance().getReceipts()[0]);
	}
	
	@Test
	public void testAdd() {
		ShoeStorageInfo shoe = new ShoeStorageInfo("ShoeA", 0);
		Store.getInstance().add("ShoeA", 0);
		assertTrue(Store.getInstance().getStorageNames().contains("ShoeA"));
	}
	
	@Test
	public void testRemove() {
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 0), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		Store.getInstance().remove("ShoeB");
		assertEquals(0, (double)Store.getInstance().getShoe("ShoeB").getAmountOnStorage(), 0);
	}
	
	@Test
	public void testTake() {
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 0), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		BuyResult result = Store.getInstance().take("ShoeB", false);
		assertTrue(result.compareTo(BuyResult.DISCOUNTED_PRICE) == 0);
	}
	
	@Test
	public void testTakeB() {
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 1), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		BuyResult result = Store.getInstance().take("ShoeA", true);
		assertTrue(result.compareTo(BuyResult.NOT_ON_DISCOUNT) == 0);
	}
	
	@Test
	public void testTakeC() {
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 0), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		BuyResult result = Store.getInstance().take("Shoe!", false);
		assertTrue(result.compareTo(BuyResult.NOT_IN_STOCK) == 0);
	}
	
	@Test
	public void testTakeD() {
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 1), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		BuyResult result = Store.getInstance().take("ShoeA", false);
		assertTrue(result.compareTo(BuyResult.REGULAR_PRICE) == 0);
	}
	
	@Test
	public void testAddDiscount() {
		ShoeStorageInfo[] storage = {new ShoeStorageInfo("ShoeA", 2), new ShoeStorageInfo("ShoeB", 1), new ShoeStorageInfo("ShoeC", 2)};
		storage[1].setDiscountAmount(1);
		storage[2].setDiscountAmount(2);
		Store.getInstance().load(storage);
		assertEquals(0, (double)Store.getInstance().getShoe("ShoeA").getDiscountedAmount(), 0);
		Store.getInstance().addDiscount("ShoeA", 1);
		assertEquals(1, (double)Store.getInstance().getShoe("ShoeA").getDiscountedAmount(), 1);
	}
	
	
}
