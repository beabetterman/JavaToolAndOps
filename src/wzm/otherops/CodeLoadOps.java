package wzm.otherops;

import wzm.otherops.CodeLoadOps.ChildClass;
import wzm.otherops.CodeLoadOps.ParentClass;

/**
 * 1. JVM采用双亲委派的加载机制。决定类由哪个ClassLoader进行加载。
 * 		https://blog.csdn.net/briblue/article/details/54973413
 * 2. 真正加载的过程中的加载顺序。静态部分加载是最早的。而那些类需要加载，则是JVM规范中说明。具体参考笔记内容。
 * 
 * 
 * @author SimpleOne
 *
 */
public class CodeLoadOps {

	public static final String static_variable = "This is static variable";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 以下各种情况下，父类 子类的加载顺序，以及初始化的顺序。
		ParentClass test = new ChildClass();
		classLoaderCheck(test);
		// ChildClass a = new ChildClass();
		// ChildClass b = null;
		// 对于仅引用常量时，只有常量定义的类才会被load。也就是此时实际上只有父类Load了。
		// 如果前面都注释掉，这里只会加载定义常量STATIC_VARIABLE_SUPER的ParentClass
		System.out.println(ChildClass.STATIC_VARIABLE_SUPER);
		// System.out.println(b.getClass());
	}
	
	public static void classLoaderCheck(Object object) {
		
		// BootstrapClassLoader <- ExtClassLoader <- AppClassLoader <- CustomerClassLoader
		
		ClassLoader classLoader = object.getClass().getClassLoader();
		while(classLoader != null) {
			System.out.println("ClassLoader info: " + classLoader.toString());
			classLoader = classLoader.getParent();
		}
	}

	static class ParentClass {
		public static String STATIC_VARIABLE_SUPER = "In SuperClassA, this is static variable";

		static {
			System.out.println("In SupserClassA, this is static block.");
		}

	}

	static class ChildClass extends ParentClass {
		public static String STATIC_VARIABLE_CHILD = "In ChildClassB, this is static variable";

		static {
			System.out.println("In ChildClassB, this is static block.");
		}

		public static void main(String[] args) {
			// TODO Auto-generated method stub

		}

	}

}
