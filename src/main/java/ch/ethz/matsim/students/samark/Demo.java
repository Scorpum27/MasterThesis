/* package ch.ethz.matsim.students.samark;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.matsim.api.core.v01.network.Link;

public class Demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Map map = new HashMap<String, Long>();
        map.put("1$", new Long(10));
        map.put("2$", new Long(20));
		
        public static Link randomMapElementKey(Map map) {
    		int nElements = map.size();
    		Random r = new Random();
    		int randomElementNr = r.nextInt(nElements);
    		int counter = 0;
    		Set<?> set = map.entrySet();
            Iterator<?> iterator = set.iterator();
            iterator.next();
            if(iterator.hasNext()) {;
            	Map.Entry entry = (Entry) iterator.next();
                String valueClassType = entry.getValue().getClass().getSimpleName();
                String keyClassType = entry.getKey().getClass().getSimpleName();
                Class valueClass = entry.getValue().getClass();
                Class keyClass = entry.getKey().getClass();
                System.out.println("key type : "+keyClassType);
                System.out.println("value type : "+valueClassType);
        		for (keyClassType. elementKey : map.keySet()) {
        			if(counter == randomElementNr) {
        				 randomElement = elementKey;
        				return randomLink;
        			}
        			counter++;
        		}
        		System.out.println("Error: No random link has been selected.");
        		return null;
            }
            /* Field testMap = Test.class.getDeclaredField("map");
    	     testMap.setAccessible(true);
    	     ParameterizedType type = (ParameterizedType) testMap.getGenericType();
    	     Type key = type.getActualTypeArguments()[0];
    	     System.out.println("Key: " + key);
    	     Type value = type.getActualTypeArguments()[1];
    	     System.out.println("Value: " + value);
    	  	*/