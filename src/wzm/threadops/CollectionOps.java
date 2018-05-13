package wzm.threadops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/*
 * 1. 集合在遍历过程中的更新方法(新增，删除)：使用iterator处理。
 * 2. 遍历Key,Value集合的方法：entrySet().iterator()  和  JDK8的Map.foreach((K,V) -> {...})
 * 3. ArrayList的subList是ArrayList的内部类，不能转换为ArrayList的类型。subList操作会反应到原ArrayList上。
 * 		注意:使用subList的场景中，对原ArrayList的元素个数的修改会导致subList遍历、增加、删除均抛出ConcurrentModificationException
 * 4. 集合转数组 ： 明确初始化Array（大小和类型），然后通过toArray(arrayName)进行转化。
 * 5. 数组转集合 : 不能使用其修改集合相关的方法，它的add/remove/clear方法会抛出UnsupportedOperationException异常。
 * 		原因：asList的返回对象是一个Arrays内部类，并没有实现集合的修改方法。Arrays.asList体现的是适配器模式，只是转换接口，后台的数据仍是数组。
 * 6. 泛型的通配符: extends 接收数据，次集合不能使用add, super 不能使用get方法。
 * 		泛型通配符<? extends T>来接收返回的数据，此写法的泛型集合不能使用add方法，而<? super T>不能使用get方法，做为接口调用赋值时易出错。 
 * 			PECS(Producer Extends Consumer Super)原则：第一、频繁往外读取内容的，适合用<? extends T>。第二、经常往里插入的，适合用<? super T>。
 * 7. hashCode 和 equals: 
 *    关于hashCode和equals的处理，遵循如下规则：
 * 		1） 只要重写equals，就必须重写hashCode。
 * 		2） 因为Set存储的是不重复的对象，依据hashCode和equals进行判断，所以Set存储的对象必须重写这两个方法。 
 * 		3） 如果自定义对象做为Map的键，那么必须重写hashCode和equals。 说明：String重写了hashCode和equals方法，所以我们可以非常愉快地使用String对象作为key来使用。
 * 8. Comparator的操作: 在JDK7版本及以上，Comparator要满足如下三个条件，不然Arrays.sort，Collections.sort会报IllegalArgumentException异常。
 * 		1） x，y的比较结果和y，x的比较结果相反。
 * 		2） x>y，y>z，则x>z。
 * 		3） x=y，则x，z比较结果和y，z比较结果相同。
 * 9. Hashtable（线程安全）， ConcurrentHashMap（锁分段, JDK8:CAS），
 * 		线程不安全：TreeMap (Key不能null)  HashMap(Key 和 Value 都可以为null)
 * 10. 利用Set元素唯一的特性，可以快速对一个集合进行去重操作，避免使用List的contains方法进行遍历、对比、去重操作。
 * 11. 合理利用好集合的有序性(sort)和稳定性(order)，避免集合的无序性(unsort)和不稳定性(unorder)带来的负面影响。 
 * 		说明：有序性是指遍历的结果是按某种比较规则依次排列的。稳定性指集合每次遍历的元素次序是一定的。
 * 		如：ArrayList是order/unsort；HashMap是unorder/unsort；TreeSet是order/sort。
 * 
 * 
 */


public class CollectionOps {
	
	
	public static void main(String[] args) {
		
		// 遍历中的新增，删除
		List<String> list = initList();
		//iteratorChangeList(list);
		list = initList();
		//foreachChangeList(list);
		
		// Map的遍历操作
		Map<String, String> map = initHashMap();
		//traverseHashMap(map);
		
		// ArrayList的subList
		list = initList();
		//subListOps(list);
		
		// Collection to 数组
		//collectionToArray(list);
		
		// 数组转集合
		String[] strArray = initArray();
		//arrayToCollection(strArray);
		
		// <? extends T> 和 <? super E>
		
		// hashCode() 和 equals()
		
		
		// Set 去重
		list = initList();
		listRemoveDuplicated(list);
		
		
		
	}
	
	/**
	 * 去重操作
	 * @param list
	 */
	public static void listRemoveDuplicated(List<String> list) {
		System.out.println("Before duplicate the items, list size:"+list.size());
		list.addAll(list);
		list.addAll(list.subList(0, 2));
		System.out.println("After duplicate the items, list size:"+list.size());
		
		Set<String> set = new TreeSet<String>(list);
		System.out.println("Set size:"+set.size());
		
		
		System.out.println("Set Values:");
		for(String str: set) {
			System.out.print(str+",");
		}
		System.out.println();
		System.out.println("List Values:");
		for(String str : list) {
			System.out.print(str+",");
		}

	}
	
	/*
	 * 数组转Collection. 适配器模式，后台数据还是数组。不能使用add/remove/clear方法。
	 */
	public static void arrayToCollection(String[] array) {
		List<String> list = Arrays.asList(array);
		// 不能使用其修改集合相关的方法，它的add/remove/clear方法会抛出UnsupportedOperationException异常。
		try {
			// 原因：asList的返回对象是一个Arrays内部类，并没有实现集合的修改方法。Arrays.asList体现的是适配器模式，只是转换接口，后台的数据仍是数组。
			list.add("newStrElementByAddMethod");
		} catch (UnsupportedOperationException unsupportedOperationException){
			unsupportedOperationException.printStackTrace();
		}
		// 修改对应的值，会直接体现在数组中。反之亦然，因为后台数据仍是数组。。。
		System.out.println("Before change, 0:"+array[0]+", 1:"+list.get(1));
		list.set(0, "0_ChangedByList");
		array[1] = "1_ChangeByArray";
		System.out.println("After change, 0:"+array[0]+", 1:"+list.get(1));
	}
	
	/*
	 * Collection转Array的方式，明确初始化Array（大小和类型），然后通过toArray(arrayName)进行转化。
	 */
	public static void collectionToArray(List<String> list) {
		String[] strArray = new String[list.size()];
		list.toArray(strArray);
		System.out.println(strArray.length);
		// 下面的方法可能会导致CastException错误。
		Object[] strArrayBadWay = list.toArray();
		System.out.println(strArrayBadWay[0].getClass()); 
		System.out.println(strArrayBadWay.length);
		
	}
	
	
	/*
	 * ArrayList的 subList的操作和注意事项
	 */
	public static void subListOps(List<String> list) {
		List<String> subList = list.subList(0, list.size()/2);
		System.out.println("subList class info:" + list.subList(0, list.size()/2).getClass());
		// Important. subList ops will apply to original ArrayList
		System.out.println("list size:" + list.size());
		subList.add("subList0");
		System.out.println("After subList add item, list size:" + list.size());
		
		// 一旦原ArrayList增加或者删除元素后，导致subList遍历、增加、删除时均会抛出ConcurrentModificationException 异常
		list.add("list_new");
		try {
			System.out.println("Once changed the original ArrayList, subList will raise Exception, when get/set/delete/traverse ");
			String testStr = subList.get(0);
		} catch (ConcurrentModificationException concurrentModificationException) {
			
			concurrentModificationException.printStackTrace();
		}
		
		
		
	}
	
	public static String[] initArray(){
		
		return new String[]{"0", "1", "2", "3"};
	}
	
	public static List<String> initList(){
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		return list;
	}
	
	public static HashMap<String, String> initHashMap(){
		
		// 初始size大小要设置成合适值，resize会重建hash表，影响性能。
		// size = 需要存储的元素数 / 0.75(负载因子也可以自定义) + 1
		HashMap<String, String> hashMap = new HashMap<String, String>(10);
		
		for (int i=0; i<5; i++) {
			hashMap.put(String.valueOf(i), String.valueOf(i));
		}
		
		return hashMap;
	}
	
	/*
	 * Traverse Map.
	 */
	public static void traverseHashMap(Map<String, String> hashMap) {
		
		// Use entrySet() and iterator to traverse Map.
		//	 This will only traverse Map once. If use Map.keySet() will traverse one more time.
		Iterator<Entry<String, String>> iterator = hashMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>)iterator.next();
			System.out.println("Key:"+entry.getKey() + ", Value:"+entry.getValue());
		}
		
		// From JDK1.8, use forEach() in Map.
		System.out.println("Map.forEach traverse.");
		hashMap.forEach((K,V) -> {
			// Also could do extra work.
			System.out.println("Key:" + K + ", Value:" + V);
		});
	}
	
	
	/*
	 * Right practice.
	 */
	public static void iteratorChangeList(List<String> list) {
		
		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext()) {
			String itemValue = iterator.next();
			if ("1".equals(itemValue)) {
				iterator.remove();
				System.out.println("Deleting item:" + itemValue);
			}
			System.out.println("Traverse item:" + itemValue);
		}
		System.out.println(list.size());
		System.out.println(list.get(0));
	}
	
	
	/*
	 * Wrong practice.
	 */
	public static void foreachChangeList(List<String> list) {
		for (String item : list) {
			if ("1".equals(item)) {
				list.remove(item);
				System.out.println("Deleting item:" + item);
			}
			System.out.println("Traverse item:" + item);
		}
		System.out.println(list.size());
		System.out.println(list.get(0));
	}
	
}
