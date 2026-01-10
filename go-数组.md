# 一、数组的引入

【1】练习引入：

```
package main
import "fmt"
func main(){
        //实现的功能：给出五个学生的成绩，求出成绩的总和，平均数：
        //给出五个学生的成绩：
        score1 := 95
        score2 := 91
        score3 := 39
        score4 := 60
        score5 := 21
        //求和：
        sum := score1 + score2 + score3 + score4 + score5 
        //平均数：
        avg := sum / 5
        //输出
        fmt.Printf("成绩的总和为：%v,成绩的平均数为：%v",sum,avg)
}
```

缺点：
成绩变量定义个数太多，成绩管理费劲，维护费劲 ---》 将成绩找个地方存起来 ---》 数组
---》存储相同类型的数据

【2】数组解决练习：

```
package main
import "fmt"
func main(){
        //实现的功能：给出五个学生的成绩，求出成绩的总和，平均数：
        //给出五个学生的成绩：--->数组存储：
        //定义一个数组：
        var scores [5]int
        //将成绩存入数组：
        scores[0] = 95
        scores[1] = 91
        scores[2] = 39
        scores[3] = 60
        scores[4] = 21
        //求和：
        //定义一个变量专门接收成绩的和：
        sum := 0
        for i := 0;i < len(scores);i++ {//i: 0,1,2,3,4 
                sum += scores[i]
        }
        //平均数：
        avg := sum / len(scores)
        //输出
        fmt.Printf("成绩的总和为：%v,成绩的平均数为：%v",sum,avg)
}
```

总结：
数组定义格式：

var 数组名 [数组大小]数据类型

```
例如：
var scores [5]int
赋值：
                                scores[0] = 95
        scores[1] = 91
        scores[2] = 39
        scores[3] = 60
        scores[4] = 21
```

# 二、内存分析

【1】代码：

```
package main
import "fmt"
func main(){
        //声明数组：
        var arr [3]int16
        //获取数组的长度：
        fmt.Println(len(arr))
        //打印数组：
        fmt.Println(arr)//[0 0 0]
        //证明arr中存储的是地址值：
        fmt.Printf("arr的地址为：%p",&arr)
        //第一个空间的地址：
        fmt.Printf("arr的地址为：%p",&arr[0])
        //第二个空间的地址：
        fmt.Printf("arr的地址为：%p",&arr[1])
        //第三个空间的地址：
        fmt.Printf("arr的地址为：%p",&arr[2])
}
```

运行结果：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/89d0b27b666943eaae11fc526e2b0f5e.png)

【2】内存分析：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/429da5fd51c744588a7fd0f386fa1c93.png)

PS : 数组每个空间占用的字节数取决于数组类型

【3】赋值内存：（数组是值类型，在栈中开辟内存）

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/4d6acac7940b48859a7b9c167b632aec.png)

数组优点：访问/查询/读取 速度快

# 三、数组的遍历

【1】普通for循环
【2】键值循环
（键值循环） for range结构是Go语言特有的一种的迭代结构，在许多情况下都非常有用，for range 可以遍历数组、切片、字符串、map 及通道，for range 语法上类似于其它语言中的 foreach 语句，一般形式为：
for key, val := range coll {
...
}

注意：
（1）coll就是你要的数组
（2）每次遍历得到的索引用key接收，每次遍历得到的索引位置上的值用val
（3）key、value的名字随便起名 k、v key、value
（4）key、value属于在这个循环中的局部变量
（5）你想忽略某个值：用_就可以了：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/e95d65cea2854a23bd346994b1dbe955.png)

代码：

```
package main
import "fmt"
func main(){
        //实现的功能：给出五个学生的成绩，求出成绩的总和，平均数：
        //给出五个学生的成绩：--->数组存储：
        //定义一个数组：
        var scores [5]int
        //将成绩存入数组：(循环 + 终端输入)
        for i := 0; i < len(scores);i++ {//i：数组的下标
                fmt.Printf("请录入第个%d学生的成绩",i + 1)
                fmt.Scanln(&scores[i])
        }
        //展示一下班级的每个学生的成绩：（数组进行遍历）
        //方式1：普通for循环：
        for i := 0; i < len(scores);i++ {
                fmt.Printf("第%d个学生的成绩为：%d\n",i+1,scores[i])
        }
        fmt.Println("-------------------------------")
        //方式2：for-range循环
        for key,value := range scores {
                fmt.Printf("第%d个学生的成绩为：%d\n",key + 1,value)
        }
}
```

运行结果：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/8fab07c1293f45a7a9d58a02faa24d5a.png)

# 四、数据的初始化方式

```
package main
import "fmt"
func main(){
        //第一种：
        var arr1 [3]int = [3]int{3,6,9}
        fmt.Println(arr1)
        //第二种：
        var arr2 = [3]int{1,4,7}
        fmt.Println(arr2)
        //第三种：
        var arr3 = [...]int{4,5,6,7}
        fmt.Println(arr3)
        //第四种：
        var arr4 = [...]int{2:66,0:33,1:99,3:88}
        fmt.Println(arr4)
}
```

# 五、注意事项

【1】长度属于类型的一部分 ：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/0302c88698714fba87f13d4df2d146c8.png)

【2】Go中数组属值类型，在默认情况下是值传递，因此会进行值拷贝。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/1d31413e1a6949949272bce89f405319.png)

【3】如想在其它函数中，去修改原来的数组，可以使用引用传递(指针方式)。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/d3210cdef58c44a8b926b3a60be2bdf3.png)

# 六、二维数组

【1】二维数组的定义，并且有默认初始值：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/249bbdfdfa3f4b01bd7a91763377af61.png)

【2】二维数组内存：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/e56b2640d7be43718318865570763f28.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/7ad0e18f84e141b8974347b8b83d485b.png)

【3】赋值操作：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/ac526ce2ec6a459898d56121b0b4cbae.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/509b83c008c14137b54ece4c0219ac01.png)

【4】二维数组的初始化：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2817/1752413561037/a40c41027de8440897b3e077f7b90c29.png)

# 七、二维数据的遍历

【1】普通for循环
【2】键值循环（for range）

代码：

```
package main
import "fmt"
func main(){
        //定义二维数组：
        var arr [3][3]int = [3][3]int{{1,4,7},{2,5,8},{3,6,9}}
        fmt.Println(arr)
        fmt.Println("------------------------")
        //方式1：普通for循环：
        for i := 0;i < len(arr);i++{
                for j := 0;j < len(arr[i]);j++ {
                        fmt.Print(arr[i][j],"\t")
                }
                fmt.Println()
        }
        fmt.Println("------------------------")
        //方式2：for range循环：
        for key,value := range arr {
                for k,v := range value {
                        fmt.Printf("arr[%v][%v]=%v\t",key,k,v)
                }
                fmt.Println()
        }
}
```