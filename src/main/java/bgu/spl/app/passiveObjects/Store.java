package bgu.spl.app.passiveObjects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import bgu.spl.mics.impl.MessageBusImpl;

public class Store{
	private MessageBusImpl instance;
	private Map<String,ShoeStorageInfo> myStorage;
	private LinkedList<Receipt> myReceipts;
	
	public Store(){
		instance=MessageBusImpl.getInstance();
		myReceipts = new LinkedList<Receipt>();
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
	
	/*
	public BuyResult take(String shoeType, boolean onlyDiscount){
		
	}
	*/
	
	public void add(String shoeType, int amount){
		if(myStorage.containsKey(shoeType)){
			ShoeStorageInfo tmp = myStorage.get(shoeType); 
			tmp.setAmount(tmp.getAmountOnStorage()+amount);
		}
		else{
			ShoeStorageInfo toAdd = new ShoeStorageInfo(shoeType,amount,0);
			myStorage.put(shoeType, toAdd);
		}
	}
	
	public void addDiscount(String shoeType, int amount){
		if(myStorage.containsKey(shoeType)){
			ShoeStorageInfo tmp = myStorage.get(shoeType); 
			tmp.setDiscountAmount(tmp.getDiscountedAmount()+amount);
		}
	}
	
	public void file(Receipt receipt){
		myReceipts.addLast(receipt);
	}
	
	public void print(){
		for (Map.Entry<String, ShoeStorageInfo> shoe : myStorage.entrySet())
			shoe.getValue().print();
		for (Receipt receipt : myReceipts)
			receipt.print();
	}
}
