#ifndef __NGX_STRING_H__
#define __NGX_STRING_H__

// 去掉字符串前后两边空格
void trim(char *str);

// 移除字符串左边空格
void ltrim(char *str);

// 移除字符串右边空格
void rtrim(char *str);

// 初始化标题设置
void ngx_init_setproctitle();

// 设置进程标题
void ngx_setproctitle(const char *title);

#endif // __NGX_STRING_H__
