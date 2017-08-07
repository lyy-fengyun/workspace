class FreshJulice {
    enum FreshJulice{SMALL, MEDIUM, LARGE;}
    FreshJuliceSize size;
}


public class FreshJuliceTest{
    public static void main(String [] args){
        FreshJulice juice = new FreshJulice();
        juice.size = FreshJulice.FreshJuliceSize.MEDIUM;
    } 
}



