package dna;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static List<Double> asList(double[] array) {
		List<Double> list = new ArrayList<Double> ();
		for ( double d : array ) {
			list.add(d);
		}
		
		return list;
	}
	
}
