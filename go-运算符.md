# 一、运算符介绍

运算符是—种特殊的符号，用以表示数据的运算、赋值和比较等

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752406709050/b0c110a085e246108abf7907e5f006af.png)

# 二、算术运算符

【1】算术运算符：+ ，-，*，/，%，++，--
【2】介绍：算术运算符是对数值类型的变量进行运算的，比如，加减乘除。
【3】代码展示：

```
package main
import "fmt"
func main(){
        //+加号：
        //1.正数 2.相加操作  3.字符串拼接
        var n1 int = +10
        fmt.Println(n1)
        var n2 int = 4 + 7
        fmt.Println(n2)
        var s1 string = "abc" + "def"
        fmt.Println(s1)
        // /除号：
        fmt.Println(10/3) //两个int类型数据运算，结果一定为整数类型
        fmt.Println(10.0/3)//浮点类型参与运算，结果为浮点类型
        // % 取模  等价公式： a%b=a-a/b*b
        fmt.Println(10%3) // 10%3= 10-10/3*3 = 1
        fmt.Println(-10%3)
        fmt.Println(10%-3)
        fmt.Println(-10%-3)
        //++自增操作：
        var a int = 10
        a++
        fmt.Println(a)
        a--
        fmt.Println(a)
        //++ 自增 加1操作，--自减，减1操作
        //go语言里，++，--操作非常简单，只能单独使用，不能参与到运算中去
        //go语言里，++，--只能在变量的后面，不能写在变量的前面 --a  ++a  错误写法
}
```

# 三、赋值运算符

【1】赋值运算符:=,+=，-=，*=，/=,%=
【2】赋值运算符就是将某个运算后的值，赋给指定的变量。
【3】代码展示：

```
package main
import "fmt"
func main(){
        var num1 int = 10
        fmt.Println(num1)
        var num2 int = (10 + 20) % 3 + 3 - 7   //=右侧的值运算清楚后，再赋值给=的左侧
        fmt.Println(num2)
        var num3 int = 10
        num3 += 20 //等价num3 = num3 + 20;
        fmt.Println(num3)
}
```

【4】练习：

```
//练习：交换两个数的值并输出结果：
        var a int = 8
        var b int = 4
        fmt.Printf("a = %v,b = %v",a,b)
        //交换：
        //引入一个中间变量：
        var t int
        t = a
        a = b
        b = t
        fmt.Printf("a = %v,b = %v",a,b)
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752406709050/100017619e2347438850facfb8932d45.png)

# 四、关系运算符

【1】关系运算符:==,!=,>,<,> =，<=
关系运算符的结果都是bool型，也就是要么是true，要么是false
【2】关系表达式经常用在流程控制中
【3】代码展示：

```
package main
import "fmt"
func main(){
        fmt.Println(5==9)//判断左右两侧的值是否相等，相等返回true，不相等返回的是false， ==不是=
        fmt.Println(5!=9)//判断不等于
        fmt.Println(5>9)
        fmt.Println(5<9)
        fmt.Println(5>=9)
        fmt.Println(5<=9)
}
```

# 五、逻辑运算符

【1】逻辑运算符:&&(逻辑与/短路与)，||（逻辑或/短路或），!（逻辑非）
【2】用来进行逻辑运算的
【3】代码展示

```
package main
import "fmt"
func main(){
        //与逻辑：&& :两个数值/表达式只要有一侧是false，结果一定为false
        //也叫短路与：只要第一个数值/表达式的结果是false，那么后面的表达式等就不用运算了，直接结果就是false  -->提高运算效率
        fmt.Println(true&&true)
        fmt.Println(true&&false)
        fmt.Println(false&&true)
        fmt.Println(false&&false)
        //或逻辑：||:两个数值/表达式只要有一侧是true，结果一定为true
        //也叫短路或：只要第一个数值/表达式的结果是true，那么后面的表达式等就不用运算了，直接结果就是true -->提高运算效率
        fmt.Println(true||true)
        fmt.Println(true||false)
        fmt.Println(false||true)
        fmt.Println(false||false)
        //非逻辑：取相反的结果：
        fmt.Println(!true)
        fmt.Println(!false)
}
```

# 六、位运算符

pass

# 七、其它运算符

【1】其它运算符：
& ：返回变量的存储地址
*：取指针变量对应的数值

【2】代码练习：

```
package main
import "fmt"
func main(){
        //定义一个变量：
        var age int = 18
        fmt.Println("age对应的存储空间的地址为：",&age)//age对应的存储空间的地址为： 0xc0000100b0
        var ptr *int = &age
        fmt.Println(ptr)
        fmt.Println("ptr这个指针指向的具体数值为：",*ptr)
}
```

# 八、运算符的优选级别

Go语言有几十种运算符，被分成十几个级别，有的运算符优先级不同，有的运算符优先级相同，请看下表。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752406709050/83604e38c3a34bf0a94728a8cae9420a.png)

一句话：为了提高优先级，可以加（）

# 九、获取用户终端输入

【1】介绍：在编程中，需要接收用户输入的数据，就可以使用键盘输入语句来获取。
【2】API：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752406709050/3ce92969e895427db1160fa4f795f5e1.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752406709050/d373147211bf4f159e661c8abf9d958c.png)

【3】代码练习：

```
package main
import "fmt"
func main(){
        //实现功能：键盘录入学生的年龄，姓名，成绩，是否是VIP
        //方式1：Scanln
        var age int
        // fmt.Println("请录入学生的年龄：")
        //传入age的地址的目的：在Scanln函数中，对地址中的值进行改变的时候，实际外面的age被影响了
        //fmt.Scanln(&age)//录入数据的时候，类型一定要匹配，因为底层会自动判定类型的
        var name string
        // fmt.Println("请录入学生的姓名：")
        // fmt.Scanln(&name)
        var score float32
        // fmt.Println("请录入学生的成绩：")
        // fmt.Scanln(&score)
        var isVIP bool
        // fmt.Println("请录入学生是否为VIP：")
        // fmt.Scanln(&isVIP)
        //将上述数据在控制台打印输出：
        //fmt.Printf("学生的年龄为：%v,姓名为：%v,成绩为：%v,是否为VIP:%v",age,name,score,isVIP)
        //方式2：Scanf
        fmt.Println("请录入学生的年龄，姓名，成绩，是否是VIP，使用空格进行分隔")
        fmt.Scanf("%d %s %f %t",&age,&name,&score,&isVIP)
        //将上述数据在控制台打印输出：
        fmt.Printf("学生的年龄为：%v,姓名为：%v,成绩为：%v,是否为VIP:%v",age,name,score,isVIP)
}
```