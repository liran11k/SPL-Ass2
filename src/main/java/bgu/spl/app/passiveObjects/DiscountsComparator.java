package bgu.spl.app.passiveObjects;

import java.util.Comparator;

public class DiscountsComparator<T> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {
		return ((DiscountSchedule) o1).getTick() - ((DiscountSchedule) o2).getTick();
	}

}
