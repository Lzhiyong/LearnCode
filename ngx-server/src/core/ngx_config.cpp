#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <errno.h>

#include "../include/ngx_config.h"
#include "../include/ngx_log.h"

// 静态变量mInstance初始化
NgxConfig *NgxConfig::mInstance = NULL;


NgxConfig::NgxConfig()
{
    // Nothing TODO
}


// 析构函数
NgxConfig::~NgxConfig()
{
    // 释放ConfigItem所占用的内存
    for(auto iter=mConfigList.begin(); iter!=mConfigList.end(); ++iter){
        delete *iter;
        *iter = NULL;
    }

    mConfigList.clear();
}


// 加载nginx.conf配置文件
bool NgxConfig::load(const char *conf)
{
    FILE *fp = fopen(conf, "r");

    if(fp == NULL){
        ngx_log_error("%s\n", strerror(errno));
        return false;
    }

    int size = 1024;
    char buf[size+1];

    
    while(!feof(fp)){
        if(fgets(buf, size, fp) == NULL)
            continue;
        // 去掉左边的空格
        ltrim(buf);
        // #开头的表示注释不进行处理
        if(*buf == '#' || *buf == '\0')
            continue;
        
        size_t len = strlen(buf);

        // 去掉字符串尾部的回车空格
        while(len > 0 && (buf[len-1] == ' ' || buf[len-1]   == '\n' || buf[len-1] == '\r')){
            buf[len-1] = '\0';
            --len;
        }
        
        // 全是空格
        if(*buf == '\0')
            continue;
        
        // 找到第一个字符'='
        char *ptr = strchr(buf, '=');
        if(ptr != NULL){
            conf_item_t *item = new conf_item_t();
            memset(item, 0, sizeof(conf_item_t));
            strncpy(item->name, buf, ptr - buf);
            strcpy(item->content, ptr + 1);

            trim(item->name);
            trim(item->content);

            mConfigList.emplace_back(item);
        }
    }
    fclose(fp);
    return true;
}


// 获取配置选项item的内容，String类型
const char* NgxConfig::getString(const char *item_name)
{
    for(auto iter=mConfigList.begin(); iter!=mConfigList.end(); ++iter){
        if(strcasecmp((*iter)->name, item_name) == 0){
            return (*iter)->content;
        }
    }
    return NULL;
}


// // 获取配置选项item的内容，Int类型
int NgxConfig::getInt(const char *item_name, const int defvalue)
{
    for(auto iter=mConfigList.begin(); iter!=mConfigList.end(); ++iter){
        if(strcasecmp((*iter)->name, item_name) == 0){
            return atoi((*iter)->content);
        }
    }
    return defvalue;
}
