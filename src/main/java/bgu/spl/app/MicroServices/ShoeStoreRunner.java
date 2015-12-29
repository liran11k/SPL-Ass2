package bgu.spl.app.MicroServices;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import bgu.spl.app.passiveObjects.DiscountSchedule;
import bgu.spl.app.passiveObjects.PurchaseSchedule;
import bgu.spl.app.passiveObjects.ShoeStorageInfo;
import bgu.spl.app.passiveObjects.Store;

public class ShoeStoreRunner {
	
	//MicroServices:
	private static TimeService _timer;
	private static ShoeFactoryService[] _factories;
	private static ManagementService _manager;
	private static SellingService[] _sellers;
	private static WebsiteClientService[] _clients;
	
	//General data:
	private static int _numOfSellers;
	private static int _numOfFactories;
	private static int _numOfClients;
	private static int _numOfServices;
	private static int _speed;
	private static int _duration;
	private static CountDownLatch _startLatch;
	private static CountDownLatch _finishLatch;
	public static void main(String[] args) {
		
		/**
		 * 1. Read Json file and load data
		 * 2. Create all micro services with countDownLatch so no one will miss first tick
		 * 3. Run store according to data
		 * 4. Terminate gracefully all micro services
		 * 5. Print all store's receipts 
		 */
		
		Json();
		
		_timer = new TimeService(_speed, _duration, _startLatch);
		ExecutorService executor = Executors.newFixedThreadPool(_numOfServices);
		//TODO: check with variety of threads
		
		executor.execute(_timer);
		executor.execute(_manager);
		System.out.println(_startLatch.getCount());
		for(int i=0; i<_numOfFactories; i++)
			executor.execute(_factories[i]);
		for(int i=0; i<_numOfSellers; i++)
			executor.execute(_sellers[i]);
		for(int i=0; i<_numOfClients; i++)
			executor.execute(_clients[i]);
		
		executor.shutdown();
		
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		//Test Json load:
		/*
		System.out.println("Timer: "+ _timer.getName() +" "+ _timer.getSpeed() +" "+ _timer.getDuration());
		System.out.println("Manager:" +_manager.getName());
		for(int i=0; i<_manager.getDiscounts().size(); i++){
			System.out.print("Type & tick: ");
			System.out.print(((DiscountSchedule) _manager.getDiscounts().get(i)).getType());
			System.out.println(((DiscountSchedule) _manager.getDiscounts().get(i)).getTick());
		}
		for(int i=0; i<_factories.length; i++)
			System.out.println("Factory: " +_factories[i].getName());
		for(int i=0; i<_sellers.length; i++)
			System.out.println("Seller:" +_sellers[i].getName());
		for(int i=0; i<_clients.length; i++){
			System.out.println("Client: " +_clients[i].getName());
			for(int j=0; j<_clients[i].getPurchaseList().size(); j++){
				System.out.print("Purchase: ");
				System.out.print(((PurchaseSchedule)_clients[i].getPurchaseList().get(j)).getType());
				System.out.println(((PurchaseSchedule)_clients[i].getPurchaseList().get(j)).getTick());
			}
			for(String wish : _clients[i].getWishList()){
				System.out.print("wish: ");
				System.out.println(wish);
			}
			
			
		}
		*/
	}
	
	private static void Json(){
		
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
			Store.getInstance().load(shoes);
			
			
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
			_speed = (int)(long) time.get("speed");
			_duration = (int)(long) time.get("duration");
			
			
			//manager:
			JSONObject manager = (JSONObject) services.get("manager");
			JSONArray discountSchedule = (JSONArray) manager.get("discountSchedule");
			List<DiscountSchedule> discounts = new LinkedList<DiscountSchedule>(); 
			for(int i=0; i<discountSchedule.size(); i++){
				JSONObject discount = (JSONObject) discountSchedule.get(i);
				String shoeType = (String) discount.get("shoeType");
				int amount = (int)(long) discount.get("amount");
				int tick = (int)(long) discount.get("tick");
				DiscountSchedule tmp = new DiscountSchedule(shoeType, tick, amount);
				discounts.add(tmp);
			}

			
			_numOfFactories = (int)(long)services.get("factories");
			_numOfSellers = (int)(long)services.get("sellers");

			//customers: 
			JSONArray customers = (JSONArray) services.get("customers");
			_numOfClients = customers.size();
			
			_numOfServices = _numOfFactories + _numOfSellers + _numOfClients + 1 /*for manager*/;
			_startLatch = new CountDownLatch(_numOfServices);
			
			_clients = new WebsiteClientService[customers.size()];
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
					JSONObject purchase = (JSONObject) purchases.get(j);
					String type = (String) purchase.get("shoeType");
					int tick = (int)(long) purchase.get("tick");
					PurchaseSchedule purchaseSchedule = new PurchaseSchedule(type,tick);
					purchasesSchedule.addLast(purchaseSchedule);
				}
				// create WebsiteClientService for each customer
				// and create a database of clients
				WebsiteClientService client = new WebsiteClientService(name, purchasesSchedule, wishList, _startLatch);
				_clients[i]= client;
			}
			
			
			// Manager:
			_manager = new ManagementService("manager", discounts, _startLatch);
			
			
			// Factories:
			_factories = new ShoeFactoryService[_numOfFactories];
			for(int i=0; i<_numOfFactories; i++){
				_factories[i] = new ShoeFactoryService("factory "+(i+1), _startLatch);
			}
			
			// Sellers:
			_sellers = new SellingService[_numOfSellers];
			for(int i=0; i<_numOfSellers; i++){
				_sellers[i] = new SellingService("seller "+(i+1), _startLatch);
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
