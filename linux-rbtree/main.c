#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "rbtree.h"

/* 定义rbtree节点 */
typedef struct {
    struct rb_node rb_node; // 内核中提供的节点
    int key;
    char value[10];
} node_t;

/* 键值比较函数 */
int compare(int key1, int key2) { return key1 - key2; }

/* 插入节点函数 */
bool insert(struct rb_root *root, node_t *node) {
    assert(root && node);
    struct rb_node **new = &(root->rb_node);
    struct rb_node *parent = NULL;
    int ret = 0;

    node_t *curr = NULL;
    while (*new) {
        curr = rb_entry(*new, node_t, rb_node);
        parent = *new;

        ret = compare(node->key, curr->key);

        if (ret < 0)
            new = &((*new)->rb_left);
        else if (ret > 0)
            new = &((*new)->rb_right);
        else
            return false;
    }

    // 插入节点
    rb_link_node(&(node->rb_node), parent, new);
    // 使树继续保持平衡
    rb_insert_color(&(node->rb_node), root);
    return true;
}

/* 节点搜索，返回找到节点，节点不存在返回NULL */
node_t *find(struct rb_root *root, int key) {
    assert(root);
    struct rb_node *_node = root->rb_node;
    node_t *curr = NULL;
    int ret = 0;
    while (_node) {
        curr = rb_entry(_node, node_t, rb_node);
        ret = compare(key, curr->key);
        if (ret < 0)
            _node = _node->rb_left;
        else if (ret > 0)
            _node = _node->rb_right;
        else
        	return curr;
    }
    return NULL;
}


/* 替换节点 */
void replace(struct rb_root *root ,node_t *node,int key) {
    node_t *pnode = find(root, key);
    if (pnode)
        rb_replace_node(&(pnode->rb_node), &(node->rb_node), root);
}


/* 删除一个节点 */
void delete (struct rb_root *root ,int key) {

    node_t *node = find(root, key);
    if(node)
        rb_erase(&(node->rb_node), root);
}


/* 红黑树遍历 */
void traverse(struct rb_root *root) {
    assert(root);
    struct rb_node *_node = NULL;

    node_t *curr = NULL;

    for (_node = rb_first(root); _node != NULL; _node = rb_next(_node)) {
        curr = rb_entry(_node, node_t, rb_node);
        printf("key = %d value = %s\n", curr->key, curr->value);
    }
}

// 删除所有节点
void clear(struct rb_root *root) {
    assert(root);
    struct rb_node *_node = NULL;

    node_t *curr = NULL;

    for (_node = rb_first(root); _node != NULL; _node = rb_next(_node)) {
        curr = rb_entry(_node, node_t, rb_node);
        rb_erase(&(curr->rb_node), root);
    }
}

/* the main function */
int main() {

    //定义节点的key和value
    int karr[] = {1, 3, 2, 5, 6, 8, 7, 4, 9, 10};
    char *varr[] = {"AA", "BB", "CC", "DD", "EE", "FF", "GG", "HH", "II", "JJ"};
    int size = sizeof(karr) / sizeof(karr[0]);

    //定义10个节点
    node_t *node[size];
    //定义树的根节点
    struct rb_root root = RB_ROOT;

    printf("******************* insert ********************\n\n");
    for (int i = 0; i < size; ++i) {
        node[i] = (node_t *)malloc(sizeof(node_t));
        node[i]->key = karr[i];
        strncpy(node[i]->value, varr[i], strlen(varr[i]));
        insert(&root, node[i]);
    }
    traverse(&root);


    printf("******************* find ********************\n\n");
    int key = 6;
    node_t *pnode = find(&root, key);
    if (pnode)
        printf("key is find, the value = %s\n", pnode->value);
    else
        printf("key is not find！\n");

    
    printf("******************* delete ********************\n\n");
    delete (&root, key);
    traverse(&root);
    
    
    printf("******************* replace ********************\n\n");

    pnode = (node_t*)malloc(sizeof(node_t));
    pnode->key = 100;
    char *str = "Hello";
    strncpy(pnode->value, str, strlen(str));
	
    //替换键值为5的节点
    replace(&root, pnode, 5);
    traverse(&root);

    // 释放节点内存
    clear(&root);
    for (int i = 0; i < size; ++i) {
        free(node[i]);
        node[i] = NULL;
    }
    
    free(pnode);
    pnode = NULL;
    traverse(&root);

    //printf("key = %d value = %s\n",pnode->key,pnode->value);
    
    return 0;
}
