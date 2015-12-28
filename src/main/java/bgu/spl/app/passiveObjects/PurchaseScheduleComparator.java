package bgu.spl.app.passiveObjects;

import java.util.Comparator;

public class PurchaseScheduleComparator<T> implements Comparator<T>{

	@Override
	public int compare(T o1, T o2) {
		return ((PurchaseSchedule) o1).getTick() - ((PurchaseSchedule) o2).getTick();
	}

}
