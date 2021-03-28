#ifndef __NGX_CONFIG_H__
#define __NGX_CONFIG_H__

#include <vector>

#include "../include/ngx_string.h"
#include "../include/ngx_global.h"

typedef struct {
    char name[200];
    char content[200];
}conf_item_t;


// Nginx配置
class NgxConfig
{
    public:
        // 获取NgxConfig实例
        static NgxConfig* getInstance()
        {
            if(mInstance == NULL){
                
                // 相当于加锁
                if(mInstance == NULL){
                    mInstance = new NgxConfig();
                    // 进程结束时，静态对象的生命周期随之结束，SingletonRelease析构函数会被调用
                    static SingletonRelease singleton;
                }
            }
            return mInstance;
        }
        
        
        // 内部类用于释放new出来的mInstance
        // 此处为何使用内部类进行内存释放，而不是直接delete NgxConfig::mInstance
        // 
        class SingletonRelease
        {
            public:
                ~SingletonRelease()
                {
                    if(NgxConfig::mInstance){
                        delete NgxConfig::mInstance;
                        NgxConfig::mInstance = NULL;
                    }
                }
        };
        
    public:
        // 用于保存所有的配置选项
        std::vector<conf_item_t*> mConfigList;
        
        // 加载配置文件
        bool load(const char *conf);
        
        // 获取配置选项的内容，字符串类型
        const char* getString(const char *item);
        
        // 获取配置选项的内容，整型数值类型
        int getInt(const char *item, const int defvalue);
        
        // 析构函数
        ~NgxConfig();

    private:
        // 静态NgxConfig实例
        static NgxConfig *mInstance;
        
        // 私有构造函数，单例模式
        NgxConfig();

};

#endif // __NGX_CONFIG_H__
