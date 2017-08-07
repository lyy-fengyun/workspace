public class NullCustomer extends AbstractCustomer{
    @Override
    public String getName(){
        return "Not available in Customer Database";
    }

    @Override
    public boolean isNil(){
        return true;
    }
}
