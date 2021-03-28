#ifndef __NGX_LOG_H__
#define __NGX_LOG_H__

#include <stdio.h>
#include <stdarg.h>

//日志级别
typedef enum {
	NGX_LEVEL_DEBUG,
	NGX_LEVEL_INFO,
	NGX_LEVEL_NOTICE,
	NGX_LEVEL_WARN,
    NGX_LEVEL_ERROR,
	NGX_LEVEL_FATAL
} ngx_log_level; 


void ngx_log_init(const char *logfile);

void ngx_log_print(const char *file, const char *func, int line,
    int level, const char *format, ...);


#if !defined(__STDC_VERSION__) || __STDC_VERSION__ < 199901L
#if defined __GNUC__ && __GNUC__ >= 2
#  define __func__ __FUNCTION__
# else
# define __func__ "<unknown>"
#endif
#endif


#if defined(__STDC_VERSION__) && __STDC_VERSION__ >199901L
    #define ngx_log_debug(...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_DEBUG, __VA_ARGS__)

    #define ngx_log_info(...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_INFO, __VA_ARGS__)
    
    #define ngx_log_notice(...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_NOTICE, __VA_ARGS__)
    
    #define ngx_log_warn(...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_WARN, __VA_ARGS__)
    
    #define ngx_log_error(...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_ERROR, __VA_ARGS__)
    
    #define ngx_log_fatal(...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_FATAL, __VA_ARGS__)
    
#elif defined(__GNUC__)
    #define ngx_log_debug(format, args...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_DEBUG, format, ##args)

    #define ngx_log_info(format, args...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_INFO, format, ##args)
    
    #define ngx_log_notice(format, args...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_NOTICE, format, ##args)
    
    #define ngx_log_warn(format, args...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_WARN, format, ##args)
    
    #define ngx_log_error(format, args...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_ERROR, format, ##args)
    
    #define ngx_log_fatal(format, args...) \
    ngx_log_print(__FILE__, __func__, __LINE__, NGX_LEVEL_FATAL, format, ##args)
#endif


#endif // __NGX_LOG_H__
