#ifndef __NGX_PROC_H__
#define __NGX_PROC_H__

// 创建守护进程
int ngx_daemon(void);

// 子进程初始化工作
void ngx_worker_proc_init(int pindex);

// 创建子线程数量
void ngx_start_worker_proc(int workers);

// 主进程循环
void ngx_master_proc_cycle(void);

// 子进程循环
void ngx_worker_proc_cycle(int pindex, const char *title);

// 创建子进程
int ngx_spawn_proc(int pindex, const char *title);

int ngx_signal_init(void);

#endif // __NGX_PROC_H__
