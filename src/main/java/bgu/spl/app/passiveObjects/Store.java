package bgu.spl.app.passiveObjects;

import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.impl.MessageBusImpl;

public class Store{
	private MessageBusImpl instance;
	private Map<String,ShoeStorageInfo> myStorage = null;
	private ArrayList<Receipt> myReceipts;

	private static class StoreHolder{
		private static Store instance = new Store();
	}
	
	private Store(){
		myReceipts = new ArrayList<Receipt>();
		myStorage = new HashMap<String, ShoeStorageInfo>();
	}
	
	public static Store getInstance(){
		return StoreHolder.instance;
	}
		
	/**
	 * Copying given storage to this store's storage.
	 * @param storage ShoeTypeInfo array which will be the new store storage
	 */
	public void load(ShoeStorageInfo[] storage){
		myStorage = new HashMap<String, ShoeStorageInfo>();
		for(int i=0;i<storage.length;i++)
			myStorage.put(storage[i].getType(), storage[i]);
	}
	
	/**
	 * 
	 * @param shoeType
	 * @param onlyDiscount
	 * @return
	 */
	public BuyResult take(String shoeType, boolean onlyDiscount){
		ShoeStorageInfo shoe = myStorage.get(shoeType);
		BuyResult result = BuyResult.getStatus(shoe,onlyDiscount);
		return result;
	}
	
	public void add(String shoeType, int amount){
		if(myStorage.containsKey(shoeType)){
			ShoeStorageInfo tmp = myStorage.get(shoeType);
			tmp.setAmount(tmp.getAmountOnStorage()+amount);
		}
		else{
			ShoeStorageInfo toAdd = new ShoeStorageInfo(shoeType,amount);
			myStorage.put(shoeType, toAdd);
		}
	}
	
	public void remove(String shoeType){
		ShoeStorageInfo tmp = myStorage.get(shoeType);
		tmp.setAmount(tmp.getAmountOnStorage()-1);
		if(tmp.getDiscountedAmount()>0)
			tmp.setDiscountAmount(tmp.getDiscountedAmount()-1);			
	}
	
	public void addDiscount(String shoeType, int amount){
		if(myStorage.containsKey(shoeType)){
			ShoeStorageInfo tmp = myStorage.get(shoeType);
			if(tmp.getDiscountedAmount() == 0)
				tmp.setDiscountAmount(amount);
			else
				tmp.setDiscountAmount(tmp.getDiscountedAmount()+amount);
		}
	}
	
	public synchronized void file(Receipt receipt){
		myReceipts.add(receipt);
		MessageBusImpl.LOGGER.info("Receipt created for ShoeType: " + receipt.getShoeType() + " (" + receipt.getAmount() + ") " + "Buyer: " + receipt.getCustomer());
	}
	
	public void print(){
		for (Map.Entry<String, ShoeStorageInfo> shoe : myStorage.entrySet())
			shoe.getValue().print();
		System.err.println();
		int i = 1;
		for (Receipt receipt : myReceipts){
			receipt.print(i);
			i++;
		}
	}
	
	public synchronized ShoeStorageInfo getShoe(String shoeType){
		return myStorage.get(shoeType);
	}
	
	// -----Getters to test Class via JUnit

	public Set<String> getStorageNames(){
		return myStorage.keySet();
	}
	
	public ShoeStorageInfo[] getStorage(){
		ShoeStorageInfo[] storage = new ShoeStorageInfo[myStorage.size()];
		int i=0;
		for(Map.Entry<String, ShoeStorageInfo> entry : myStorage.entrySet()){
			storage[i] = entry.getValue();
			i++;
		}
		return storage;
	}
	
	public ArrayList<Receipt> getReceipts(){
		return myReceipts;
	}

	public int getStorageSize() {
		return myStorage.size();
	}

	public void initialize() {
		myStorage = new HashMap<String, ShoeStorageInfo>();
		myReceipts = new ArrayList<Receipt>();
	}
	
}