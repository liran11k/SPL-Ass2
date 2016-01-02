package bgu.spl.app.passiveObjects;

import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.text.html.HTMLDocument.Iterator;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.impl.MessageBusImpl;

public class Store{
	private Map<String,ShoeStorageInfo> _myStorage = null;
	private LinkedBlockingQueue<Receipt> _myReceipts;

	private static class StoreHolder{
		private static Store instance = new Store();
	}
	
	private Store(){
		_myReceipts = new LinkedBlockingQueue<Receipt>();
		_myStorage = new HashMap<String, ShoeStorageInfo>();
	}
	
	public static Store getInstance(){
		return StoreHolder.instance;
	}
		
	/**
	 * Copying given storage to this store's storage.
	 * @param storage ShoeTypeInfo array which will be the new store storage
	 */
	public void load(ShoeStorageInfo[] storage){
		_myStorage = new HashMap<String, ShoeStorageInfo>();
		for(int i=0;i<storage.length;i++)
			_myStorage.put(storage[i].getType(), storage[i]);
	}
	
	/**
	 * 
	 * @param shoeType
	 * @param onlyDiscount
	 * @return
	 */
	public BuyResult take(String shoeType, boolean onlyDiscount){
		ShoeStorageInfo shoe = _myStorage.get(shoeType);
		BuyResult result = BuyResult.getStatus(shoe,onlyDiscount);
		return result;
	}
	
	public void add(String shoeType, int amount){
		if(_myStorage.containsKey(shoeType)){
			ShoeStorageInfo tmp = _myStorage.get(shoeType);
			tmp.setAmount(tmp.getAmountOnStorage()+amount);
		}
		else{
			ShoeStorageInfo toAdd = new ShoeStorageInfo(shoeType,amount);
			_myStorage.put(shoeType, toAdd);
		}
	}
	
	public void remove(String shoeType){
		ShoeStorageInfo tmp = _myStorage.get(shoeType);
		tmp.setAmount(tmp.getAmountOnStorage()-1);
		if(tmp.getDiscountedAmount()>0)
			tmp.setDiscountAmount(tmp.getDiscountedAmount()-1);			
	}
	
	public void addDiscount(String shoeType, int amount){
		if(_myStorage.containsKey(shoeType)){
			ShoeStorageInfo tmp = _myStorage.get(shoeType);
			if(tmp.getDiscountedAmount() == 0)
				tmp.setDiscountAmount(amount);
			else
				tmp.setDiscountAmount(tmp.getDiscountedAmount()+amount);
		}
	}
	
	public void file(Receipt receipt){
		_myReceipts.add(receipt);
		MessageBusImpl.LOGGER.info("Receipt created for ShoeType: " + receipt.getShoeType() + " (" + receipt.getAmount() + ") " + "Buyer: " + receipt.getCustomer());
	}
	
	public void print(){
		for (Map.Entry<String, ShoeStorageInfo> shoe : _myStorage.entrySet())
			shoe.getValue().print();
		System.err.println();
		int i = 1;
		for (Receipt receipt : _myReceipts){
			receipt.print(i);
			i++;
		}
	}
	
	public synchronized ShoeStorageInfo getShoe(String shoeType){
		return _myStorage.get(shoeType);
	}
	
	// -----Getters to test Class via JUnit

	public Set<String> getStorageNames(){
		return _myStorage.keySet();
	}
	
	public ShoeStorageInfo[] getStorage(){
		ShoeStorageInfo[] storage = new ShoeStorageInfo[_myStorage.size()];
		int i=0;
		for(Map.Entry<String, ShoeStorageInfo> entry : _myStorage.entrySet()){
			storage[i] = entry.getValue();
			i++;
		}
		return storage;
	}
	
	public Receipt[] getReceipts(){
		Receipt[] receipts = new Receipt[_myReceipts.size()];
		int i=0;
		while(!_myReceipts.isEmpty()){
			receipts[i]=_myReceipts.remove();
			i++;
		}
		return receipts;
	}

	public int getStorageSize() {
		return _myStorage.size();
	}

	public void initialize() {
		_myStorage = new HashMap<String, ShoeStorageInfo>();
		_myReceipts = new LinkedBlockingQueue<Receipt>();
	}
	
}