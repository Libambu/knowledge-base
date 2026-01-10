一、defer+recover机制处理错误

【1】展示错误：

![image-20260110214118528](E:\program\workspace\knowledge-base\assets\image-20260110214118528.png)

发现：程序中出现错误/恐慌以后，程序被中断，无法继续执行。

【2】错误处理/捕获机制：
go中追求代码优雅，引入机制：defer+recover机制处理错误

内置函数recover:

![image-20260110214132594](E:\program\workspace\knowledge-base\assets\image-20260110214132594.png)

![image-20260110214141924](E:\program\workspace\knowledge-base\assets\image-20260110214141924.png)

优点：提高程序健壮性

# 二、自定义错误

自定义错误：需要调用errors包下的New函数：函数返回error类型

![image-20260110214154771](E:\program\workspace\knowledge-base\assets\image-20260110214154771.png)

代码：

![image-20260110214205489](E:\program\workspace\knowledge-base\assets\image-20260110214205489.png)

有一种情况：程序出现错误以后，后续代码就没有必要执行，想让程序中断，退出程序：
借助：builtin包下内置函数：panic

![image-20260110214218741](E:\program\workspace\knowledge-base\assets\image-20260110214218741.png)

代码：

![image-20260110214228999](E:\program\workspace\knowledge-base\assets\image-20260110214228999.png)