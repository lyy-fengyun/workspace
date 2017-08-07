public class JvmTester{

    public static void main(String[] args){
	ClassLoader classLoader = JvmTester.class.getClassLoader();
	while(classLoader !=  null){
	    System.out.println(classLoader.getClass().getName());
	    classLoader = classLoader.getParent();
	}
    }


}
