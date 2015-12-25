package bgu.spl.app.MicroServices;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.ShoeStorageInfo;
import bgu.spl.app.passiveObjects.Store;

public class JsonLoad {
	public static void main(String[] args) {
			
		JSONParser parser = new JSONParser();
		
		try {
			/**
			 * mainObj: main object to iterate through the json file
			 */
			Object obj = parser.parse(new FileReader("example.txt"));
			JSONObject mainObject= (JSONObject) obj;
			
			/**
			 * Initial Storage>>
			 * 1. initialStorage: json array to hold storage
			 * 2. creating each shoe from json data
			 * 3. add this shoe to an array
			 * 4. creating the store with this array as argument
			 */
			JSONArray initialStorage= (JSONArray) mainObject.get("initialStorage");
			ShoeStorageInfo[] shoes = new ShoeStorageInfo[initialStorage.size()];
			for(int i=0; i<initialStorage.size(); i++){
				JSONObject tmpObject = (JSONObject)initialStorage.get(i);
				String shoeName = (String) tmpObject.get("shoeType");
				int amount = (int)(long) tmpObject.get("amount");
				shoes[i] = new ShoeStorageInfo(shoeName,amount,0);
			}
			// TODO: Initialize the store with this storage
			Store store = Store.getInstance();
			store.load(shoes);
			
			/**
			 * Services>>
			 * time:		(speed, duration)
			 * -> TimeService
			 * manager:		(array of discountSchedule: shoeType,amount,tick)
			 * factories:	(# of factories)
			 * sellers:		(# of sellers)
			 * Customers:	(array of customers: name,wishList[<string>],purchaseSchedule[shoeType,tick])
			 */
			JSONObject services = (JSONObject) mainObject.get("services");
			
			//time:
			JSONObject time = (JSONObject) services.get("time");
			int speed = (int)(long) time.get("speed");
			int duration = (int)(long) time.get("duration");
			TimeService timer = new TimeService(speed, duration);
			
			//manager:
			JSONObject manager = (JSONObject) services.get("manager");
			JSONArray discountSchedule = (JSONArray) manager.get("discountSchedule");
			
			//factories:
			int numOfFactories = (int)(long)services.get("factories");
			
			//sellers:
			int sellers = (int)(long)services.get("sellers");
			
			//customers: 
			JSONArray customers = (JSONArray) services.get("customers");
			WebsiteClientService[] clients = new WebsiteClientService[customers.size()];
			for(int i=0; i<customers.size(); i++){
				JSONObject customer = (JSONObject) customers.get(i);
				String name = (String) customer.get("name");
				JSONArray wishes = (JSONArray) customer.get("wishList");
				JSONArray purchases = (JSONArray) customer.get("purchaseSchedule");
				HashSet<String> wishList = new HashSet<String>(wishes.size());
				for(int j=0; j<wishes.size(); j++)
					wishList.add((String) wishes.get(j));
				LinkedList<PurchaseSchedule> purchasesSchedule = new LinkedList<PurchaseSchedule>();
				for(int j=0; j<purchases.size(); j++){
					JSONObject shoe = (JSONObject) purchases.get(j);
					String type = (String) shoe.get("shoeType");
					int tick = (int)(long) shoe.get("tick");
					PurchaseSchedule purchaseSchedule = new PurchaseSchedule(type,tick);
					purchasesSchedule.addLast(purchaseSchedule);
				}
				// create WebsiteClientService for each customer
				// and create a database of clients
				WebsiteClientService client = new WebsiteClientService(name, purchasesSchedule, wishList);
				clients[i]= client;
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
