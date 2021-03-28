#ifndef __NGX_GLOBAL_H__
#define __NGX_GLOBAL_H__

#include <signal.h>

typedef enum {
    NGX_MASTER_PROCESS, NGX_WORKER_PROCESS
}ngx_proc_t;

// main函数的argv参数
extern char **arg;

// 保存环境变量字符数组
extern char *env;

// 环境变量字符数组大小
extern int envsize;

extern pid_t ngx_pid;

extern pid_t ngx_parent;

// 进程类型是master process还是worker process
extern int ngx_process;

extern sig_atomic_t  ngx_reap;   

#endif // __NGX_GLOBAL_H__
