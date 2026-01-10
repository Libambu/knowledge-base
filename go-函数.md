# 一、函数引入

【1】为什么要使用函数：
提高代码的复用型，减少代码的冗余，代码的维护性也提高了

【2】函数的定义：
为完成某一功能的程序指令(语句)的集合,称为函数。

【3】基本语法
func 函数名（形参列表)（返回值类型列表）{
执行语句..
return + 返回值列表
}

【4】函数的定义和函数的调用案例：

```go
package main
import "fmt"
// func   函数名（形参列表)（返回值类型列表）{
// 	执行语句..
// 	return + 返回值列表
// }
//自定义函数：功能：两个数相加：
func cal (num1 int,num2 int) (int) { //如果返回值类型就一个的话，那么()是可以省略不写的
        var sum int = 0
        sum += num1
        sum += num2
        return sum
}
func main(){
        //功能：10 + 20
        //调用函数：
        sum := cal(10,20)
        fmt.Println(sum)
        // var num1 int = 10
        // var num2 int = 20
        //求和：
        // var sum int = 0
        // sum += num1
        // sum += num2
        // fmt.Println(sum)
        //功能：30 + 50
        var num3 int = 30
        var num4 int = 50
        //调用函数：
        sum1 := cal(num3,num4)
        fmt.Println(sum1)
        //求和：
        // var sum1 int = 0
        // sum1 += num3
        // sum1 += num4
        // fmt.Println(sum1)
}
```

# 二、函数细节详讲

【1】函数：
对特定的功能进行提取，形成一个代码片段，这个代码片段就是我们所说的函数
【2】函数的作用：提高代码的复用性
【3】函数和函数是并列的关系，所以我们定义的函数不能写到main函数中
【4】基本语法
func 函数名（形参列表)（返回值类型列表）{
执行语句..
return + 返回值列表
}
（1）函数名：
遵循标识符命名规范:见名知意 addNum,驼峰命名addNum
首字母不能是数字
首字母大写该函数可以被本包文件和其它包文件使用(类似public)
首学母小写只能被本包文件使用，其它包文件不能使用(类似private)

（2）形参列表：
形参列表：个数：可以是一个参数，可以是n个参数，可以是0个参数
形式参数列表：作用：接收外来的数据
实际参数：实际传入的数据

![image-20260110214628660](E:\program\workspace\knowledge-base\assets\image-20260110214628660.png)

（3）返回值类型列表：函数的返回值对应的类型应该写在这个列表中
返回0个：

![image-20260110214644182](E:\program\workspace\knowledge-base\assets\image-20260110214644182.png)

返回1个：

![image-20260110214655953](E:\program\workspace\knowledge-base\assets\image-20260110214655953.png)

返回多个：

![image-20260110214713650](E:\program\workspace\knowledge-base\assets\image-20260110214713650.png)

![image-20260110214723769](E:\program\workspace\knowledge-base\assets\image-20260110214723769.png)

【5】通过例题感受内存分析：

```
package main
import "fmt"
//自定义函数：功能：交换两个数
func exchangeNum (num1 int,num2 int){ 
        var t int
        t = num1
        num1 = num2
        num2 = t
}
func main(){
        //调用函数：交换10和20
        var num1 int = 10
        var num2 int = 20
        fmt.Printf("交换前的两个数： num1 = %v,num2 = %v \n",num1,num2)
        exchangeNum(num1,num2)
        fmt.Printf("交换后的两个数： num1 = %v,num2 = %v \n",num1,num2)
}
```

![image-20260110214746773](E:\program\workspace\knowledge-base\assets\image-20260110214746773.png)

**【6】Golang中函数不支持重载**

![image-20260110214755958](E:\program\workspace\knowledge-base\assets\image-20260110214755958.png)

**【7】Golang中支持可变参数 (如果你希望函数带有可变数量的参数)**

```
package main
import "fmt"
//定义一个函数，函数的参数为：可变参数 ...  参数的数量可变
//args...int 可以传入任意多个数量的int类型的数据  传入0个，1个，，，，n个
func test (args...int){
        //函数内部处理可变参数的时候，将可变参数当做切片来处理
        //遍历可变参数：
        for i := 0; i < len(args); i++ {
                fmt.Println(args[i])
        }
}
func main(){
        test()
        fmt.Println("--------------------")
        test(3)
        fmt.Println("--------------------")
        test(37,58,39,59,47)
}
```

【8】基本数据类型和数组默认都是值传递的，即进行值拷贝。在函数内修改，不会影响到原来的值。

![image-20260110214844114](E:\program\workspace\knowledge-base\assets\image-20260110214844114.png)

【9】以值传递方式的数据类型，如果希望在函数内的变量能修改函数外的变量，可以传入变量的地址&，函数内以指针的方式操作变量。从效果来看类似引用传递。

![image-20260110214912748](E:\program\workspace\knowledge-base\assets\image-20260110214912748.png)

【10】在Go中，函数也是一种数据类型，可以赋值给一个变量，则该变量就是一个函数类型的变量了。通过该变量可以对函数调用。

```
package main
import "fmt"
//定义一个函数：
func test(num int){
        fmt.Println(num)
}
func main(){
        //函数也是一种数据类型，可以赋值给一个变量
        a := test//变量就是一个函数类型的变量
        fmt.Printf("a的类型是：%T,test函数的类型是：%T \n",a,test)//a的类型是：func(int),test函数的类型是：func(int)
        //通过该变量可以对函数调用
        a(10) //等价于  test(10)
}
```

**【11】函数既然是一种数据类型，因此在Go中，函数可以作为形参，并且调用**
（把函数本身当做一种数据类型）

```
package main
import "fmt"
//定义一个函数：
func test(num int){
        fmt.Println(num)
}
//定义一个函数，把另一个函数作为形参：
func test02 (num1 int ,num2 float32, testFunc func(int)){
        fmt.Println("-----test02")
}
func main(){
        //函数也是一种数据类型，可以赋值给一个变量
        a := test//变量就是一个函数类型的变量
        fmt.Printf("a的类型是：%T,test函数的类型是：%T \n",a,test)//a的类型是：func(int),test函数的类型是：func(int)
        //通过该变量可以对函数调用
        a(10) //等价于  test(10)
        //调用test02函数：
        test02(10,3.19,test)
        test02(10,3.19,a)
}
```

【12】为了简化数据类型定义,Go支持自定义数据类型
基本语法: type 自定义数据类型名 数据类型
可以理解为 : 相当于起了一个别名
例如:type mylnt int -----》这时mylnt就等价int来使用了.

![image-20260110215444766](E:\program\workspace\knowledge-base\assets\image-20260110215444766.png)

例如:type mySum func (int , int) int -----》这时mySum就等价一个函数类型func(int, int) int

![image-20260110215610377](E:\program\workspace\knowledge-base\assets\image-20260110215610377.png)

【13】支持对函数返回值命名
传统写法要求：返回值和返回值的类型对应，顺序不能差

![image-20260110215623774](E:\program\workspace\knowledge-base\assets\image-20260110215623774.png)

升级写法：对函数返回值命名，里面顺序就无所谓了，顺序不用对应

![image-20260110215632162](E:\program\workspace\knowledge-base\assets\image-20260110215632162.png)

# 三、包的引入

【1】使用包的原因：
（1）我们不可能把所有的函数放在同一个源文件中，可以分门别类的把函数放在不同的原文件中

![image-20260110215722343](E:\program\workspace\knowledge-base\assets\image-20260110215722343.png)

2）解决同名问题：两个人都想定义一个同名的函数，在同一个文件中是不可以定义相同名字的函数的。此时可以用包来区分

【2】案例展示包：
项目的结构：

![image-20260110215740853](E:\program\workspace\knowledge-base\assets\image-20260110215740853.png)

代码展示：

![image-20260110215811751](E:\program\workspace\knowledge-base\assets\image-20260110215811751.png)

![image-20260110215819233](E:\program\workspace\knowledge-base\assets\image-20260110215819233.png)

# 四、包的细节详讲

1.package进行包的声明，建议：包的声明这个包和所在的文件夹同名

![image-20260110215839492](E:\program\workspace\knowledge-base\assets\image-20260110215839492.png)

2.main包是程序的入口包，一般main函数会放在这个包下
main函数一定要放在main包下，否则不能编译执行

![image-20260110215900453](E:\program\workspace\knowledge-base\assets\image-20260110215900453.png)

3.打包语法：
package 包名
4.引入包的语法：import "包的路径"
包名是从$GOPATH/src/后开始计算的，使用/进行路径分隔。

![image-20260110215932195](E:\program\workspace\knowledge-base\assets\image-20260110215932195.png)

![image-20260110215939031](E:\program\workspace\knowledge-base\assets\image-20260110215939031.png)

5.如果有多个包，建议一次性导入,格式如下：
import(
"fmt"
"gocode/testproject01/unit5/demo09/crm/dbutils"
)
6.在函数调用的时候前面要定位到所在的包

![image-20260110215949385](E:\program\workspace\knowledge-base\assets\image-20260110215949385.png)

7.函数名，变量名首字母大写，函数，变量可以被其它包访问

![image-20260110215955754](E:\program\workspace\knowledge-base\assets\image-20260110215955754.png)

8.一个目录/包下不能有重复的函数

![image-20260110220008120](E:\program\workspace\knowledge-base\assets\image-20260110220008120.png)

9.包名和文件夹的名字，可以不一样

![image-20260110220023023](E:\program\workspace\knowledge-base\assets\image-20260110220023023.png)

![image-20260110220037504](E:\program\workspace\knowledge-base\assets\image-20260110220037504.png)

10.一个目录下的同级文件归属一个包
同级别的源文件的包的声明必须一致

![image-20260110220103865](E:\program\workspace\knowledge-base\assets\image-20260110220103865.png)

11.包到底是什么：
（1）在程序层面，所有使用相同 package 包名 的源文件组成的代码模块
（2）在源文件层面就是一个文件夹

12.可以给包取别名，取别名后，原来的包名就不能使用了

![image-20260110220121113](E:\program\workspace\knowledge-base\assets\image-20260110220121113.png)

# 五、init函数

【1】init函数：初始化函数，可以用来进行一些初始化的操作
每一个源文件都可以包含一个init函数，该函数会在main函数执行前，被Go运行框架调用。

![image-20260110220654503](E:\program\workspace\knowledge-base\assets\image-20260110220654503.png)

【2】全局变量定义，init函数，main函数的执行流程？

![image-20260110220805179](E:\program\workspace\knowledge-base\assets\image-20260110220805179.png)

【3】多个源文件都有init函数的时候，如何执行：

执行过程：

![image-20260110220831733](E:\program\workspace\knowledge-base\assets\image-20260110220831733.png)

# 六、匿名函数

【1】Go支持匿名函数，如果我们某个函数只是希望使用一次，可以考虑使用匿名函数
【2】匿名函数使用方式：
（1）在定义匿名函数时就直接调用，这种方式匿名函数只能调用一次（用的多）

![image-20260110220954248](E:\program\workspace\knowledge-base\assets\image-20260110220954248.png)

（2）将匿名函数赋给一个变量(该变量就是函数变量了)，再通过该变量来调用匿名函数（用的少）

![image-20260110221021446](E:\program\workspace\knowledge-base\assets\image-20260110221021446.png)

【3】如何让一个匿名函数，可以在整个程序中有效呢?将匿名函数给一个全局变量就可以了

```
package main
import "fmt"
var Func01 = func (num1 int,num2 int) int{
        return num1 * num2
}
func main(){
        //定义匿名函数：定义的同时调用
        result := func (num1 int,num2 int) int{
                return num1 + num2
        }(10,20)
        fmt.Println(result)
        //将匿名函数赋给一个变量，这个变量实际就是函数类型的变量
        //sub等价于匿名函数
        sub := func (num1 int,num2 int) int{
                return num1 - num2
        }
        //直接调用sub就是调用这个匿名函数了
        result01 := sub(30,70)
        fmt.Println(result01)
        result02 := sub(30,70)
        fmt.Println(result02)
        result03 := Func01(3,4)
        fmt.Println(result03)
}
```

# 七、闭包

【1】什么是闭包：
闭包就是一个函数和与其相关的引用环境组合的一个整体

【2】案例展示：

```
package main
import "fmt"
//函数功能：求和
//函数的名字：getSum 参数为空
//getSum函数返回值为一个函数，这个函数的参数是一个int类型的参数，返回值也是int类型
func getSum() func (int) int {
        var sum int = 0
        return func (num int) int{
                sum = sum + num 
                return sum
        }
}
//闭包：返回的匿名函数+匿名函数以外的变量num
func main(){
        f := getSum()
        fmt.Println(f(1))//1 
        fmt.Println(f(2))//3
        fmt.Println(f(3))//6
        fmt.Println(f(4))//10
}
```

感受：匿名函数中引用的那个变量会一直保存在内存中，可以一直使用

【3】闭包的本质：
闭包本质依旧是一个匿名函数，只是这个函数引入外界的变量/参数
匿名函数+引用的变量/参数 = 闭包

【4】特点：
（1）返回的是一个匿名函数，但是这个匿名函数引用到函数外的变量/参数 ,因此这个匿名函数就和变量/参数形成一个整体，构成闭包。
（2）闭包中使用的变量/参数会一直保存在内存中，所以会一直使用---》意味着闭包不可滥用（对内存消耗大）

【5】不使用闭包可以吗？

```
package main
import "fmt"
//函数功能：求和
//函数的名字：getSum 参数为空
//getSum函数返回值为一个函数，这个函数的参数是一个int类型的参数，返回值也是int类型
func getSum() func (int) int {
        var sum int = 0
        return func (num int) int{
                sum = sum + num 
                return sum
        }
}
//闭包：返回的匿名函数+匿名函数以外的变量num
func main(){
        f := getSum()
        fmt.Println(f(1))//1 
        fmt.Println(f(2))//3
        fmt.Println(f(3))//6
        fmt.Println(f(4))//10
        fmt.Println("----------------------")
        fmt.Println(getSum01(0,1))//1
        fmt.Println(getSum01(1,2))//3
        fmt.Println(getSum01(3,3))//6
        fmt.Println(getSum01(6,4))//10
}
func getSum01(sum int,num int) int{
        sum = sum + num
        return sum
}
//不使用闭包的时候：我想保留的值，不可以反复使用
//闭包应用场景：闭包可以保留上次引用的某个值，我们传入一次就可以反复使用了
```

# 八、defer关键字

【1】defer关键字的作用：
在函数中，程序员经常需要创建资源，为了在函数执行完毕后，及时的释放资源，Go的设计者提供defer关键字

【2】案例展示：

![image-20260110221520039](E:\program\workspace\knowledge-base\assets\image-20260110221520039.png)

【3】代码变动一下，再次看结果：

![image-20260110221638447](E:\program\workspace\knowledge-base\assets\image-20260110221638447.png)

发现：遇到defer关键字，会将后面的代码语句压入栈中，也会将相关的值同时拷贝入栈中，不会随着函数后面的变化而变化。

【4】defer应用场景：
比如你想关闭某个使用的资源，在使用的时候直接随手defer，因为defer有延迟执行机制（函数执行完毕再执行defer压入栈的语句），
所以你用完随手写了关闭，比较省心，省事

# 九、系统函数

## 9.1 字符串相关函数

【1】统计字符串的长度,按字节进行统计：
len(str)
使用内置函数也不用导包的，直接用就行

![image-20260110221659268](E:\program\workspace\knowledge-base\assets\image-20260110221659268.png)

【2】字符串遍历：
(1)利用方式1：for-range键值循环：

![image-20260110222310750](E:\program\workspace\knowledge-base\assets\image-20260110222310750.png)

（2）r:=[]rune(str)

![image-20260110222351878](E:\program\workspace\knowledge-base\assets\image-20260110222351878.png)

【3】字符串转整数：
n, err := strconv.Atoi("66")

【4】整数转字符串：
str = strconv.Itoa(6887)
【5】查找子串是否在指定的字符串中:
strings.Contains("javaandgolang", "go")

![image-20260110222430558](E:\program\workspace\knowledge-base\assets\image-20260110222430558.png)

【6】统计一个字符串有几个指定的子串:
strings.Count("javaandgolang","a")

![image-20260110222445443](E:\program\workspace\knowledge-base\assets\image-20260110222445443.png)

【7】不区分大小写的字符串比较:
strings.EqualFold("go" , "Go")

![image-20260110222505051](E:\program\workspace\knowledge-base\assets\image-20260110222505051.png)

【8】返回子串在字符串第一次出现的索引值，如果没有返回-1 :
strings.lndex("javaandgolang" , "a")

![image-20260110222516538](E:\program\workspace\knowledge-base\assets\image-20260110222516538.png)

【9】字符串的替换：
strings.Replace("goandjavagogo", "go", "golang", n)
n可以指定你希望替换几个,如果n=-1表示全部替换，替换两个n就是2

![image-20260110222536973](E:\program\workspace\knowledge-base\assets\image-20260110222536973.png)

【10】按照指定的某个字符，为分割标识，将一个学符串拆分成字符串数组:
strings.Split("go-python-java", "-")

![image-20260110222544508](E:\program\workspace\knowledge-base\assets\image-20260110222544508.png)

【11】将字符串的字母进行大小写的转换:
strings.ToLower("Go")// go
strings.ToUpper"go")//Go

![image-20260110222600838](E:\program\workspace\knowledge-base\assets\image-20260110222600838.png)

【12】将字符串左右两边的空格去掉:
strings.TrimSpace(" go and java ")

![image-20260110222609364](E:\program\workspace\knowledge-base\assets\image-20260110222609364.png)

【13】将字符串左右两边指定的字符去掉:
strings.Trim("~~golang~~ ", " ~")

![image-20260110222616150](E:\program\workspace\knowledge-base\assets\image-20260110222616150.png)

【14】将字符串左边指定的字符去掉:
strings.TrimLeft("~~golang~~", "~")

【15】将字符串右边指定的字符去掉:
strings.TrimRight("~~golang~~", "~")

【16】判断字符串是否以指定的字符串开头:
strings.HasPrefix("http://java.sun.com/jsp/jstl/fmt", "http")

【17】判断字符串是否以指定的字符串结束:
strings.HasSuffix("demo.png", ".png")

![image-20260110222638292](E:\program\workspace\knowledge-base\assets\image-20260110222638292.png)

## 9.2 日期和时间相关函数

【1】时间和日期的函数，需要到入time包，所以你获取当前时间，就要调用函数Now函数：

```
package main
import (
        "fmt"
        "time"
)
func main(){
        //时间和日期的函数，需要到入time包，所以你获取当前时间，就要调用函数Now函数：
        now := time.Now()
        //Now()返回值是一个结构体，类型是：time.Time
        fmt.Printf("%v ~~~ 对应的类型为：%T\n",now,now)
        //2021-02-08 17:47:21.7600788 +0800 CST m=+0.005983901 ~~~ 对应的类型为：time.Time
        //调用结构体中的方法：
        fmt.Printf("年：%v \n",now.Year())
        fmt.Printf("月：%v \n",now.Month())//月：February
        fmt.Printf("月：%v \n",int(now.Month()))//月：2
        fmt.Printf("日：%v \n",now.Day())
        fmt.Printf("时：%v \n",now.Hour())
        fmt.Printf("分：%v \n",now.Minute())
        fmt.Printf("秒：%v \n",now.Second())
}
```

【2】日期的格式化：
（1）将日期以年月日时分秒按照格式输出为字符串：

```
//Printf将字符串直接输出：
        fmt.Printf("当前年月日： %d-%d-%d 时分秒：%d:%d:%d  \n",now.Year(),now.Month(),
        now.Day(),now.Hour(),now.Minute(),now.Second())
        //Sprintf可以得到这个字符串，以便后续使用：
        datestr := fmt.Sprintf("当前年月日： %d-%d-%d 时分秒：%d:%d:%d  \n",now.Year(),now.Month(),
        now.Day(),now.Hour(),now.Minute(),now.Second())
        fmt.Println(datestr)
```

（2）按照指定格式：

```
//这个参数字符串的各个数字必须是固定的，必须这样写 
        datestr2 := now.Format("2006/01/02 15/04/05")
        fmt.Println(datestr2)
        //选择任意的组合都是可以的，根据需求自己选择就可以（自己任意组合）。
        datestr3 := now.Format("2006 15:04")
        fmt.Println(datestr3)
```

## 9.3 内置函数

【1】什么是内置函数/内建函数：
Golang设计者为了编程方便，提供了一些函数，这些函数不用导包可以直接使用，我们称为Go的内置函数/内建函数。

【2】内置函数存放位置：
在builtin包下，使用内置函数也的，直接用就行

【3】常用函数：
（1）len函数：
统计字符串的长度,按字节进行统计

![image-20260110222657252](E:\program\workspace\knowledge-base\assets\image-20260110222657252.png)

（2）new函数：
分配内存，主要用来分配值类型（int系列, float系列, bool, string、数组和结构体struct）

![image-20260110222804294](E:\program\workspace\knowledge-base\assets\image-20260110222804294.png)



（3）make函数：
分配内存，主要用来分配引用类型（指针、slice切片、map、管道chan、interface 等）





简单直接的回答：**没有任何关系。**

它们虽然名字看起来很像，都是用来“创建”东西的，但它们在 Go 语言中负责的领域完全不同，**互不干扰，不能混用**。

为了让你这个 Java/C++ 老手彻底看清它们的本质，我用最通俗的方式给你拆解一下。

---

### 1. `make`：专款专用的“初始化工厂”

**只服务于三位 VIP：**
1.  **Slice** (切片)
2.  **Map** (字典)
3.  **Channel** (通道)

**作用：**
这三个家伙内部结构很复杂（比如 Map 里面要有哈希桶，Slice 里面要有底层数组指针、长度、容量）。
光分配内存是不够的，必须**初始化内部结构**，机器才能转起来。`make` 就是负责这个“内部装修”的。

**返回值：**
返回的是 **类型本身 `T`**（比如 `map[string]int`），**不是指针**。
*因为这三个家伙本身就是“引用类型”，不需要再套一层指针了。*

**Java 对比：**
```go
m := make(map[string]int)
```
等同于 Java 的：
```java
// 不仅分配了内存，还初始化了内部的哈希表结构
Map<String, Integer> m = new HashMap<>(); 
```

---

### 2. `new`：单纯的“内存分配器”

**服务于所有类型：**
`int`, `string`, `struct`, `array` 等等。

**作用：**
它非常“懒”。它只做两件事：
1.  在堆上划一块内存。
2.  把这块内存清零（置为零值）。
**它不管内部初始化**。

**返回值：**
返回的是 **指针 `*T`**（内存地址）。

**Java/C++ 对比：**
```go
p := new(int) // p 是 *int
```
等同于 C++ 的：
```cpp
int* p = new int(); // 分配内存，置为0，返回指针
```

---

### 3. 为什么不能混用？（核心区别）

这是最容易踩坑的地方。我们来做个实验：用 `new` 去创建 `map` 会发生什么？

```go
func main() {
    // ❌ 错误示范：用 new 创建 map
    // m 的类型是 *map[string]int (指针)
    // new 分配了内存，并把内存置为 0 (也就是 nil)
    m := new(map[string]int) 

    // *m 解引用后，就是一个 nil map
    // 往 nil map 里写数据，直接 panic！
    (*m)["key"] = 100 // 💥 报错：assignment to entry in nil map
}
```

**结论：**
*   `new` 给了你一个指向 `nil` map 的指针，没法用。
*   `make` 给了你一个初始化好的 map，可以直接用。

---

### 4. 总结对比表

| 特性         | `make`                         | `new`                               |
| :----------- | :----------------------------- | :---------------------------------- |
| **适用类型** | 仅限 `slice`, `map`, `channel` | 任意类型 (`int`, `struct` 等)       |
| **做了什么** | 分配内存 + **初始化内部结构**  | 分配内存 + **置零**                 |
| **返回类型** | `T` (值/引用)                  | `*T` (指针)                         |
| **能否混用** | 不能 (编译器报错)              | 能写但不推荐 (会得到 nil map/slice) |
| **常用程度** | 🔥 **非常常用**                 | 🧊 **很少用** (通常用 `&T{}` 代替)   |

---

### 5. 既然 `new` 这么“废”，什么时候用它？

你会发现写 Go 代码时，`make` 满天飞，但 `new` 很少见。
因为对于结构体，我们更喜欢用 **取地址符 `&`**，它比 `new` 更灵活。

**对比一下：**

```go
type Student struct {
    Name string
    Age  int
}

// 写法 A: 使用 new (很少见)
s1 := new(Student) // s1 是 *Student，Name="", Age=0
s1.Name = "Tom"

// 写法 B: 使用字面量 (最推荐，Go 的主流写法)
// 相当于 new + 赋值，一步到位
s2 := &Student{Name: "Tom", Age: 18} 
```

**只有一种情况你会想用 `new`：**
当你只需要一个指向“零值”的指针，且不想立马赋值时。（比如 `v := new(int)`）。

### 一句话总结给 Java 程序员：

*   **`make`** = `new ArrayList<>()` / `new HashMap<>()` （专门搞集合的）。
*   **`new`** = C++ 的 `new` （只给内存地址，很少直接用）。
*   **`&Struct{}`** = Java 的 `new Object(...)` （创建对象并赋值，最常用）。