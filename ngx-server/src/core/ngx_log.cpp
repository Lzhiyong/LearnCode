#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <malloc.h>
#include <time.h>
#include <pthread.h>

#include "../include/ngx_log.h"

#define printf(fmt, ...) \
    do{ \
        fprintf(stderr, "[%s:%s:%d]  " fmt, __FILE__, __func__, __LINE__, ##__VA_ARGS__); \
    } while(0)

static pthread_rwlock_t rwlock = PTHREAD_RWLOCK_INITIALIZER;

static char pathname[PATH_MAX] = {0};

// 获取当前的时间
const char *get_curr_time()
{
    char buf[64];
    time_t tm = time(NULL);
    strftime(buf, sizeof(buf), "%Y-%m-%d %X",localtime(&tm) );
    return strdup(buf);
}


// 初始化log函数
void ngx_log_init(const char *logfile)
{

    pthread_rwlock_rdlock(&rwlock);
    
    memset(pathname, 0, sizeof(pathname));

    if(logfile != NULL) {
        strcpy(pathname, logfile);
    }
    
    pthread_rwlock_unlock(&rwlock);
}


// 格式化输出log
static void ngx_log_format(FILE *fp, const char *file, const char *func, int line,
        int level, const char *format, const char *args)
{
    char buf[1024 * 2] = {0};
    memset(buf, 0, sizeof(buf));

    char priority[10];   
    switch(level){
    case NGX_LEVEL_DEBUG:
        strcpy(priority, "DEBUG");
        break;
    case NGX_LEVEL_INFO:
        strcpy(priority, "INFO");
        break;
    case NGX_LEVEL_NOTICE:
        strcpy(priority, "NOTICE");
        break;
    case NGX_LEVEL_WARN:
        strcpy(priority, "WARN");
        break;
    case NGX_LEVEL_ERROR:
        strcpy(priority, "ERROR");
        break;
    case NGX_LEVEL_FATAL:
        strcpy(priority, "FATAL");
        break;
    default:
        strcpy(priority, "UNKNOWN");
        break;
    }

    const char *time = get_curr_time();
    
    const char *filename = strrchr(file, '/');
    pid_t pid = getpid();
    sprintf(buf, "%s  %d   %s    [%s:(%s):%d]  %s", time, pid, priority, filename+1, func, line, args);

    fprintf(fp, "%s", buf);
    free((void*)time);
    time = NULL;
}

// 打印log函数
void ngx_log_print(const char *file, const char *func, int line,
        int level, const char *format, ...)
{
    pthread_rwlock_rdlock(&rwlock);
    
    FILE *fp = NULL;
    if(*pathname == '\0') {
        fp = stderr;
    } else {
        fp = fopen(pathname, "a+");
    }

    if(fp == NULL) {
        goto error;
    }

    char args[1024];
    memset(args, 0, sizeof(args));
    
    va_list ap;
    va_start(ap, format);
    vsnprintf(args, sizeof(args), format, ap);
    va_end(ap);
    
    ngx_log_format(fp, file, func, line, level, format, args);

    fclose(fp);
    pthread_rwlock_unlock(&rwlock);
    return;
error:
    pthread_rwlock_unlock(&rwlock);
    printf("%s\n", strerror(errno));
}
