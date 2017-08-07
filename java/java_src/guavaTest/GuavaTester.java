import com.google.common.base.Optional;

public class GuavaTester{

    
    public static void main(String[] args){
	Guavatester guavaTester = new GuavaTester();

	Integer value1 = null;
	Integer value2 = new Integer(10);
	// Optional.fromNullable - allows passed parameter to be null
	Optional<Integer> a = Optional.fromNullable(value1);
	Optional<Integer> b = Optional.fromNullable(value2);

	System.out.println(guavaTest.sum(a,b));
    }

    public Integer sum(Optional<Integer> a, Optional<Integer> b){

	// Optional.isPresent - checks the value os present or not
	System.out.println("First Parameter is present:"+a.isPresent());
	System.out.println("Second Parameter is present:"+b.isPresent());

	//Optional.or - returns the value if present otherwise returns
	//the default value passed
	Integer value1=a.or(new Integer(0));

	//Optional.get - get the value, value should be present
	Integer value2 = b.get();

	return value1+value2;
    }
}
