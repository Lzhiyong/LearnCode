### Linux内核红黑树
这个是从Linux内核中提取的rbtree，内核中有很多地方用到了红黑树，比如高精度计时器使用红黑树树组织定时请求，EXT3文件系统也使用红黑树树来管理目录，虚拟存储管理系统也有用红黑树树进行VMAs（Virtual Memory Areas）的管理,，进程调度…

#### 结构定义

Linux内核红黑树的实现与传统的实现方式有些不同，它对针对内核对速度的需要做了优化。每一个rb_node节点是嵌入在用RB树进行组织的数据结构中，而不是用rb_node指针进行数据结构的组织。

Linux内核中红黑树节点的定义如下，其中rb_node是节点类型，而rb_root是仅包含一个节点指针的类，用来表示根节点
```c
struct rb_node { 	
unsigned long rb_parent_color; 
#define	RB_RED	 0 
#define	RB_BLACK 1 	
struct rb_node *rb_right; 	
struct rb_node *rb_left; 
} __attribute__((aligned(sizeof(long)))); 

struct rb_root { 	
struct rb_node *rb_node; 
};
```

这里似乎没有定义颜色的域，但这就是这里红黑树实现的一个巧妙的地方。rb_parent_color这个域其实同时包含了颜色信息以及父节点的指针，因为该域是一个long的类型，需要大小为sizeof(long)的对齐，那么在一般的32位机器上，其后两位的数值永远是0，于是可以拿其中的一位来表示颜色。事实上，这里就是使用了最低位来表示颜色信息。明白了这点，那么以下关于父亲指针和颜色信息的操作都比较容易理解了，其本质上都是对rb_parent_color的位进行操作
```c
#define rb_parent(r) ((struct rb_node *)((r)->rb_parent_color & ~3)) //低两位清0

#define rb_color(r) ((r)->rb_parent_color & 1) //取最后一位 

#define rb_is_red(r) (!rb_color(r)) //最后一位为0？ 

#define rb_is_black(r) rb_color(r) //最后一位为1？ #define rb_set_red(r) do { (r)->rb_parent_color &= ~1; } while (0) //最后一位置0 

#define rb_set_black(r) do { (r)->rb_parent_color |= 1; } while (0) //最后一位置1 

static inline void rb_set_parent(struct rb_node *rb, struct rb_node *p) //设置父节点 {
 	rb->rb_parent_color = (rb->rb_parent_color & 3) | (unsigned long)p; 
} 

static inline void rb_set_color(struct rb_node *rb, int color) //设置颜色 {
 	rb->rb_parent_color = (rb->rb_parent_color & ~1) | color; 
}
```
宏定义
```c
#define RB_ROOT	(struct rb_root) { NULL, } //初始根节点指针 
#define rb_entry(ptr, type, member) container_of(ptr, type, member)//包含ptr的结构体指针 
#define RB_EMPTY_ROOT(root) ((root)->rb_node == NULL) //判断树是否空 
#define RB_EMPTY_NODE(node) (rb_parent(node) == node) //判断节点是否空，父节点是否等于自身 
#define RB_CLEAR_NODE(node) (rb_set_parent(node, node)) //设置节点为空，父节点等于自身
```
还有container_of和offsetof本身也是个宏，其定义在kernel.h中：
```c
#define container_of(ptr, type, member) ({ \
 const typeof( ((type *)0)->member ) *__mptr = (ptr); \ 
(type *)( (char *)__mptr - offsetof(type,member) );})
```
而offsetof则定义在stddef.h中：
```c
//offsetof宏取得member成员在type对象中相对于对象首地址的偏移量，具体是通过把0强制转化成为type类型指针，
//然后引用成员member，此时得到的指针大小即为偏移量（因为对象首地址为0）container_of宏取得包含ptr的数据结构的指针
//具体是把ptr转化为type对象中member类型的指针，然后减去member类型在type对象的偏移量得到type对象的首地址

#define offsetof(TYPE, MEMBER) ((size_t) &((TYPE *)0)->MEMBER)
```

#### 红黑树操作

接下来介绍红黑树的一些函数使用，__rb_rotate_left和__rb_rotate_right就是对红黑树进行的左旋和右旋操作。注意，代码中的第一个if语句中是=而不是==，意思是先赋值，然后再对该值判断是否为空，如果不为空的情况下才设置该节点的父亲。这样代码显得非常简洁，但如果以为是==的比较，则可能会感到困惑，不够他这里也使用了两个小括号进行提示，因为一般情况只需一个括号即可
```c
void __rb_rotate_left(struct rb_node *node, struct rb_root *root); 
void __rb_rotate_right(struct rb_node *node, struct rb_root *root);
```
rb_insert_color则是把新插入的节点进行着色，并且修正红黑树使其达到平衡
```c
void rb_insert_color(struct rb_node *, struct rb_root *);
```
插入节点时需要把新节点指向其父节点，这可以通过rb_link_node函数完成：
```c
void rb_link_node(struct rb_node * node, struct rb_node * parent, struct rb_node ** rb_link);
```

删除节点则通过rb_erase进行，然后通过__rb_erase_color进行红黑树的修正
```c
void rb_erase(struct rb_node *, struct rb_root *);
void __rb_erase_color(struct rb_node *node, struct rb_node *parent, struct rb_root *root);

```
rb_replace_node函数用来替换一个节点，但是替换完成后并不会对红黑树做任何调整，所以如果新节点的值与被替换的值有所不同时，可能会出现问题
```c
void rb_replace_node(struct rb_node *old, struct rb_node *new, struct rb_root *tree);

```
红黑树遍历，其原理均非常简单，本质上就是这里的求前驱，后继、最大值、最小值的函数实现
```c
extern struct rb_node *rb_first(const struct rb_root *);//最小值 
extern struct rb_node *rb_last(const struct rb_root *); //最大值
extern struct rb_node *rb_prev(const struct rb_node *); //前驱 
extern struct rb_node *rb_next(const struct rb_node *); //后继 

```
##### 内核中的红黑树是一种通用的数据结构，所以再使用内核中rbtree的源码时，需要自己去实现插入，搜索，删除等操作，具体使用请看源码main.c

编译: 

mkdir build && cd build && cmake ..

gcc -o main rbtree.c main.c
