#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string>

#include "../include/ngx_global.h"
#include "../include/ngx_string.h"
#include "../include/ngx_log.h"

// 初始化标题设置
void ngx_init_setproctitle()
{
    
    // 获取总共的环境变量字符串长度
    for(int i=0; environ[i]; ++i){
        // +1每一项字符串末尾都有'\0'
        envsize += strlen(environ[i]) + 1;
    }

    // new一个env用于保存environ环境变量
    env = new char[envsize];
    // 清空new出来的内存
    memset(env, 0, envsize);

    // 把environ中的每一项都复制到env所指向的内存
    for(int j=0; environ[j]; ++j){
        size_t len = strlen(environ[j]) + 1;
        strcpy(env, environ[j]);
        // 让environ中每一项指向新的env
        environ[j] = env;
        // env地址偏移到当前当前地址+len的位置
        env += len;
    }
}


// 设置进程标题
void ngx_setproctitle(const char *title)
{
    if(title == NULL) return;

    size_t argsize = 0;
    size_t len = strlen(title);
    
    // 获取argv所有参数字符串长度
    for(int i=0; arg[i]; ++i){
        argsize += strlen(arg[i]) +  1;
    }

    int total_size = argsize + envsize;
    if(total_size <= len){
        ngx_log_error("%s\n", "title is too long!");
        return;
    }

    arg[1] = NULL;
    // 指向arg首地址
    char *ptr = arg[0];
    strcpy(ptr, title);

    // 地址偏移len
    ptr += len;
    
    // 将argv + environ剩余的内存清空，剩余内存 = 总共占用内存 - 标题占用内存
    size_t size = total_size - len;
    memset(ptr, 0, size);
}


// 去掉字符串左右两边空格
void trim(char *str)
{
    ltrim(str);
    rtrim(str);
}


// 去掉字符串左边空格
void ltrim(char *str)
{
    if(*str != ' ' || str == NULL) return;

    size_t len = strlen(str);

    char *ptr = str;

    // 找到第一个不是空格的字符
    while(*ptr != '\0'){
        if(*ptr != ' ')
            break;
        ++ptr;
    }


    if(*ptr == '\0'){
        // 程序走到这里说明字符串全是空格
        *str = '\0';
        return;
    }

    while(*ptr != '\0'){
        *str++ = *ptr++;
    }

    *str = '\0';

}


// 去掉字符串右边的空格
void rtrim(char *str)
{
    if(str == NULL) return;
    
    size_t len = strlen(str);

    while(len > 0 && str[len-1] == ' '){
        str[--len] = '\0';
    }
}
