def str1="hello,World";
println(str1)

String str2 = "hello, Groovy"
println(str2)

def age1 = 30;
println("you age:"+age1);

age2 = 21;
println("you age:"+age2);

def s = """
this is a
multiple line
"""
println(s);

def func(a,b){
    println("Your name is:"+a+"and you age is:"+ b);
}

func("Diego",30)

def func1(a,b=25){
    if (b==null){
        println("Your name is:"+1+"and your age is a secret");
    }else{
        println("Your name is:"+a+"and you age is:"+b);
    }
}

func1("Deigo")
func1("Deigo",30)
func1("Deigo",null)

awk -F' ' '{for(i=1;i<=NF;i=i+1){print $i}}' lower2upper.sh | sort|uniq -c|sort -nr| awk -F  ' ' '{print $2,$1}'
