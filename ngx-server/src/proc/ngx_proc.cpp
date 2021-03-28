#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>

#include "../include/ngx_config.h"
#include "../include/ngx_string.h"
#include "../include/ngx_proc.h"
#include "../include/ngx_log.h"



// 子进程创建时调用本函数进行一些初始化工作
void ngx_worker_proc_init(int pindex)
{
    sigset_t set;
    sigemptyset(&set);
    if(sigprocmask(SIG_SETMASK, &set, NULL) == -1){
        ngx_log_error("%s\n", strerror(errno));
    }
}

// worker子进程的功能函数，每个woker子进程，就在这里循环着了（无限循环处理网络事件和定时器事件以对外提供web服务）
// 子进程分叉才会走到之类
// pindex：进程编号从0开始
void ngx_worker_proc_cycle(int pindex, const char *title)
{
    // 设置进程的类型，是worker进程
    ngx_process = NGX_WORKER_PROCESS; 
    // 进行子进程的初始化工作
    ngx_worker_proc_init(pindex);
    // 设置子进程标题
    ngx_setproctitle(title);

    for(;;){
        sleep(1);
    }
}


// 描述：  产生一个子进程
// @pindex：进程编号0开始
// @title： 子进程名字"worker process" + pindex
int ngx_spawn_proc(int pindex, const char *title)
{
    pid_t pid = fork();
    switch(pid) { 
    case -1:    // 产生子进程失败
        ngx_log_error("%s\n", strerror(errno));
        return -1;
    case 0:     // 产生子进程失败
        ngx_parent = ngx_pid;   // 因为是子进程了，所有原来的pid变成了父pid
        ngx_pid = getpid();     //重新获取pid 即本子进程的pid
        ngx_worker_proc_cycle(pindex, title);
        break;
    default:    //这个应该是父进程分支，直接break;，流程往switch之后走  
        break;
    }
    
    // 父进程分支会走到这里，子进程流程不往下走
    // 若有需要，以后再扩展增加其他代码
    return pid;
}


// 开始创建子进程
// workers创建子进程数量
void ngx_start_worker_proc(int workers)
{
    char worker_title[100];
    
    for(int i=0; i<workers; ++i){
        memset(worker_title, 0, sizeof(worker_title));
        sprintf(worker_title, "%s%d", "nginx: worker process", (i + 1));

        ngx_spawn_proc(i, worker_title);
    }

}


void ngx_master_proc_cycle()
{
    sigset_t set;        //信号集

    sigemptyset(&set);   //清空信号集

    // 下列这些信号在执行本函数期间不希望收到（保护不希望由信号中断的代码临界区）
    // 建议fork()子进程时学习这种写法，防止信号的干扰
    // 可以根据开发的实际需要往其中添加其他要屏蔽的信号
    sigaddset(&set, SIGCHLD);     //子进程状态改变
    sigaddset(&set, SIGALRM);     //定时器超时
    sigaddset(&set, SIGIO);       //异步I/O
    sigaddset(&set, SIGINT);      //终端中断符
    sigaddset(&set, SIGHUP);      //连接断开
    sigaddset(&set, SIGUSR1);     //用户定义信号
    sigaddset(&set, SIGUSR2);     //用户定义信号
    sigaddset(&set, SIGWINCH);    //终端窗口大小改变
    sigaddset(&set, SIGTERM);     //终止
    sigaddset(&set, SIGQUIT);     //终端退出符

    // 设置此时无法接受的信号，阻塞期间，你发过来的上述信号，多个会被合并为一个，暂存着，等你放开信号屏蔽后才能收到这些信号
    if(sigprocmask(SIG_BLOCK, &set, NULL) == -1){
        printf("%s\n", strerror(errno));
    }

    // 设置主进程标题
    const char *master_title = "nginx: master process";

    ngx_setproctitle(master_title);

    // 从配置文件中读取要创建的worker进程数量，默认4个worker进程
    NgxConfig *config = NgxConfig::getInstance();
    int workers = config->getInt("worker_proc", 4);
    // 这里要创建worker子进程
    ngx_start_worker_proc(workers);

    // 创建子进程后，父进程的执行流程会返回到这里，子进程不会走进来
    // 信号屏蔽字为空，表示不屏蔽任何信号
    sigemptyset(&set);

    for(;;) {
        // a)根据给定的参数设置新的mask，并阻塞当前进程，因为是个空集，所以不阻塞任何信号
        // b)此时，一旦收到信号，便恢复原先的信号屏蔽，我们原来的mask在上边设置的，阻塞了多达10个信号，从而保证我下边的执行流程不会再次被其他信号截断
        // c)调用该信号对应的信号处理函数
        // d)信号处理函数返回后，sigsuspend返回，使程序流程继续往下走
        // 阻塞在这里，等待一个信号，此时进程是挂起的，不占用cpu时间，只有收到信号才会被唤醒返回
        // 此时master进程完全靠信号驱动干活
        sigsuspend(&set);

        sleep(1);
    }
}


