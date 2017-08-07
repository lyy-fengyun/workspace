

public class Java8Test{

    final static String salutation = "Hello! ";

    public static void main(String[] args){
	GreetingService greetservice1 = message -> System.out.println(salutation+ message);
	greetService1.sayMessage("Mahesh");
    }


    interface GreetingService{
	void sayMesssage(String message);

    }
    
}
